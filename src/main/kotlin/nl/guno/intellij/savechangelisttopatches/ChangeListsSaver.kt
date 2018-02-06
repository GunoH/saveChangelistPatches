package nl.guno.intellij.savechangelisttopatches

import com.intellij.notification.NotificationListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diff.impl.patch.FilePatch
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vcs.changes.shelf.ShelveChangesManager
import com.intellij.openapi.vcs.changes.shelf.ShelvedChangeList
import com.intellij.util.WaitForProgressToShow
import nl.guno.intellij.savechangelisttopatches.settings.Settings
import nl.guno.intellij.savechangelisttopatches.settings.SettingsManager
import org.apache.commons.lang.StringUtils
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener

internal class ChangeListsSaver(private val project: Project) {

    /**
     * @param showModalErrors Indicates if any errors should be shown in a modal dialogue.
     */
    fun savePatches(showModalErrors: Boolean) {

        if (StringUtils.isEmpty(Settings.getInstance(project).saveLocation)) {
            logError(MessageResources.message("dialog.saveLocation.notSet.text"),
                    MessageResources.message("dialog.couldNotSavePatches.title"), showModalErrors)
            return
        }

        var savePath = Paths.get(Settings.getInstance(project).saveLocation)
        if (!savePath.toFile().exists() || !savePath.toFile().canWrite()) {
            logError(MessageResources.message("dialog.saveLocation.notValid.text", savePath.toString()),
                    MessageResources.message("dialog.couldNotSavePatches.title"), showModalErrors)
            return
        }

        if (Settings.getInstance(project).useSubDirs) {
            savePath = savePath.resolve(Paths.get(currentDateAsString()))
            if (!savePath.toFile().exists() && !savePath.toFile().mkdir()) {
                logError(MessageResources.message("dialog.saveLocation.notValid.text", savePath.toString()),
                        MessageResources.message("dialog.couldNotSavePatches.title"), showModalErrors)
                return
            }
        }

        val changeListManager = ChangeListManager.getInstance(project)
        val localChangeLists = changeListManager.changeLists

        val failed = HashSet<String>()

        var count = 0
        for (localChangeList in localChangeLists) {
            try {
                if (savePatchForChangelist(localChangeList, savePath)) {
                    count++
                }
            } catch (e: SaveFailedException) {
                failed.add(e.name)
            }

        }

        var countShelved = 0
        if (Settings.getInstance(project).includeShelved) {

            val shelveChangesManager = ShelveChangesManager.getInstance(project)
            val shelvedChangeLists = shelveChangesManager.shelvedChangeLists
            for (shelvedChangeList in shelvedChangeLists) {
                try {
                    if (savePatchForShelvedChangelist(shelvedChangeList, savePath)) {
                        countShelved++
                    }
                } catch (e: SaveFailedException) {
                    failed.add(e.name)
                }

            }
        }

        if (count > 0 || countShelved > 0) {
            logSaveSuccessful(count, countShelved, savePath, showModalErrors)
        }

        if (!failed.isEmpty()) {
            logFailure(savePath, failed, showModalErrors)
        }
    }

    /** @return `true` if a patch was created, `false` otherwise. */
    @Throws(SaveFailedException::class)
    private fun savePatchForChangelist(changeList: LocalChangeList, saveLocation: Path) : Boolean {

        if (changeList.changes.isEmpty()) {
            // Don't create patches for empty change lists.
            return false
        }

        return savePatchForChange(changeList.changes, saveLocation, changeList.name)
    }

    /** @return `true` if a patch was created, `false` otherwise. */
    @Throws(SaveFailedException::class)
    private fun savePatchForChange(changes: Collection<Change>, saveLocation: Path, name: String) : Boolean {

        val patches: Collection<FilePatch>?
        try {
            patches = IdeaTextPatchBuilder.buildPatch(project, changes, project.baseDir.path, false)
        } catch (ex: Exception) {
            ex.printStackTrace(System.out)
            return false
        }

        var filename = name
        if (!Settings.getInstance(project).useSubDirs) {
            filename = filename + "_" + currentDateAsString()
        }
        val patchFile = ShelveChangesManager.suggestPatchName(project, filename,
                saveLocation.toFile(), null)
        try {
            FileWriter(patchFile.path).use { writer ->
                UnifiedDiffWriter.write(project, patches, writer, "\n", null)
                writer.flush()
            }
        } catch (ex: FileNotFoundException) {
            throw SaveFailedException(name)
        } catch (ex: IOException) {
            Notification(project, ex.toString(), MessageType.ERROR).showBalloon().addToEventLog()
        }

        return true
    }

    private fun currentDateAsString(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        return dateFormat.format(Date())
    }

    /** @return `true` if a patch was created, `false` otherwise. */
    @Throws(SaveFailedException::class)
    private fun savePatchForShelvedChangelist(changeList: ShelvedChangeList, saveLocation: Path) : Boolean {

        if (changeList.getChanges(project).isEmpty()) {
            // Don't create patches for empty change lists.
            return false
        }

        val changes =
                changeList.getChanges(project).stream().map {
                    it.getChange(project)
                }.collect(Collectors.toCollection( Supplier<HashSet<Change>> { HashSet() }))

        return savePatchForChange(changes, saveLocation, "_shelf_" + changeList.name)
    }

    private fun openSettings(project: Project) {
        ShowSettingsUtil.getInstance().editConfigurable(project, SettingsManager(project))
    }

    private fun logFailure(saveLocation: Path, failed: Collection<String>, showModalErrors: Boolean) {
        val failedChangeLists = StringBuilder()
        for (filename in failed) {
            failedChangeLists.append("  - ").append(filename).append("\n")
        }

        logError(
                MessageResources.message("dialog.couldNotSavePatches.text", saveLocation, failedChangeLists.toString()),
                MessageResources.message("dialog.couldNotSavePatches.title"),
                showModalErrors)
    }

    private fun logError(message: String, title: String, showModalErrors: Boolean) {
        if (showModalErrors) {
            runInDispatchThread(Runnable { Messages.showErrorDialog(message, title) })
        } else {
            runInDispatchThread(
                    Runnable {
                        Notification(project, message, MessageType.ERROR)
                                .showBalloon(HyperlinkListener { event ->
                                    if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                                        openSettings(project)
                                    }
                                }).addToEventLog(NotificationListener { _, _ -> openSettings(project) })
                    })
        }
    }

    private fun logSaveSuccessful(count: Int, countShelved: Int, saveLocation: Path, showModalErrors: Boolean) {

        // Never show this message as modal dialog, since that would be lame.
        if (!showModalErrors) {

            val message: String = if (Settings.getInstance(project).includeShelved) {
                MessageResources.message(
                        "dialog.patchesSaved.text.includingShelved", Integer.valueOf(count), Integer.valueOf(countShelved), saveLocation)
            } else {
                MessageResources.message(
                        "dialog.patchesSaved.text", Integer.valueOf(count), saveLocation)
            }
            runInDispatchThread(
                    Runnable { Notification(project, message, MessageType.INFO).showBalloon().addToEventLog() })
        }
    }

    private fun runInDispatchThread(action: Runnable) {
        val application = ApplicationManager.getApplication()
        if (application.isUnitTestMode || application.isDispatchThread) {
            action.run()
        } else {
            WaitForProgressToShow.runOrInvokeAndWaitAboveProgress(action)
        }
    }

    private inner class SaveFailedException internal constructor(internal val name: String) : Throwable()
}
