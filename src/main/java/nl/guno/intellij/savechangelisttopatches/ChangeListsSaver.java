package nl.guno.intellij.savechangelisttopatches;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.event.HyperlinkEvent;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.changes.shelf.ShelveChangesManager;
import com.intellij.openapi.vcs.changes.shelf.ShelvedChangeList;
import com.intellij.util.WaitForProgressToShow;
import nl.guno.intellij.savechangelisttopatches.settings.Settings;
import nl.guno.intellij.savechangelisttopatches.settings.SettingsManager;

class ChangeListsSaver {

    private Project project;

    ChangeListsSaver(Project project) {
        this.project = project;
    }

    /**
     * @param showModalErrors Indicates if any errors should be shown in a modal dialogue.
     */
    void savePatches(boolean showModalErrors) {

        if (StringUtils.isEmpty(Settings.getInstance(project).getSaveLocation())) {
            logError(MessageResources.message("dialog.saveLocation.notSet.text"), 
                    MessageResources.message("dialog.couldNotSavePatches.title"), showModalErrors);
            return;
        }

        Path savePath = Paths.get(Settings.getInstance(project).getSaveLocation());
        if (!savePath.toFile().exists() || !savePath.toFile().canWrite()) {
            logError(MessageResources.message("dialog.saveLocation.notValid.text", savePath.toString()),
                    MessageResources.message("dialog.couldNotSavePatches.title"), showModalErrors);
            return;
        }

        if (Settings.getInstance(project).getUseSubDirs()) {
            savePath = savePath.resolve(Paths.get(currentDateAsString()));
            if (!savePath.toFile().exists() && !savePath.toFile().mkdir()) {
                logError(MessageResources.message("dialog.saveLocation.notValid.text", savePath.toString()),
                        MessageResources.message("dialog.couldNotSavePatches.title"), showModalErrors);
                return;
            }
        }
        
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        List<LocalChangeList> localChangeLists = changeListManager.getChangeLists();

        Collection<String> failed = new HashSet<>();

        int count = 0;
        for (LocalChangeList localChangeList : localChangeLists) {
            try {
                if (savePatchForChangelist(localChangeList, savePath)) {
                    count++;
                }
            } catch (SaveFailedException e) {
                failed.add(e.getName());
            }
        }

        int countShelved = 0;
        if (Settings.getInstance(project).getIncludeShelved()) {

            ShelveChangesManager shelveChangesManager = ShelveChangesManager.getInstance(project);
            List<ShelvedChangeList> shelvedChangeLists = shelveChangesManager.getShelvedChangeLists();
            for (ShelvedChangeList shelvedChangeList : shelvedChangeLists) {
                try {
                    if (savePatchForShelvedChangelist(shelvedChangeList, savePath)) {
                        countShelved++;
                    }
                } catch (SaveFailedException e) {
                    failed.add(e.getName());
                }
            }
        }

        if (count > 0 || countShelved > 0) {
            logSaveSuccessful(count, countShelved, savePath, showModalErrors);
        }

        if (!failed.isEmpty()) {
            logFailure(savePath, failed, showModalErrors);
        }
    }

    /** @return {@code true} if a patch was created, {@code false} otherwise. */
    private boolean savePatchForChangelist(LocalChangeList changeList, Path saveLocation) throws SaveFailedException {
        
        if (changeList.getChanges().isEmpty()) {
            // Don't create patches for empty change lists.
            return false;
        }

        return savePatchForChange(changeList.getChanges(), saveLocation, changeList.getName());
    }

    /** @return {@code true} if a patch was created, {@code false} otherwise. */
    private boolean savePatchForChange(Collection<Change> changes, Path saveLocation, String name)
            throws SaveFailedException {
        
        Collection<FilePatch> patches;
        try {
            patches = IdeaTextPatchBuilder.buildPatch(project, changes, project.getBaseDir().getPath(), false);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            return false;
        }

        String filename = name;
        if (!Settings.getInstance(project).getUseSubDirs()) {
            filename = filename + "_" + currentDateAsString();
        }
        File patchFile = ShelveChangesManager.suggestPatchName(project, filename,
                saveLocation.toFile(), null);
        try (FileWriter writer = new FileWriter(patchFile.getPath())) {
            UnifiedDiffWriter.write(project, patches, writer, "\n", null);
            writer.flush();
        } catch (FileNotFoundException ex) {
            throw new SaveFailedException(name);
        } catch (IOException ex) {
            new Notification(project, ex.toString(), MessageType.ERROR).showBalloon().addToEventLog();
        }

        return true;
    }

    @NotNull
    private String currentDateAsString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return dateFormat.format(new Date());
    }

    /** @return {@code true} if a patch was created, {@code false} otherwise. */
    private boolean savePatchForShelvedChangelist(ShelvedChangeList changeList, Path saveLocation) throws SaveFailedException {
        
        if (changeList.getChanges(project).isEmpty()) {
            // Don't create patches for empty change lists.
            return false;
        }

        Collection<Change> changes = changeList.getChanges(project).stream().map(
                change -> change.getChange(project)).collect(Collectors.toCollection(HashSet::new));

        return savePatchForChange(changes, saveLocation, "_shelf_" + changeList.getName());
    }

    private static void openSettings(Project project) {
        ShowSettingsUtil.getInstance().editConfigurable(project, new SettingsManager(project));
    }

    private void logFailure(final Path saveLocation, final Collection<String> failed, boolean showModalErrors) {
        StringBuilder failedChangeLists = new StringBuilder();
        for (String filename : failed) {
            failedChangeLists.append("  - ").append(filename).append("\n");
        }

        logError(
                MessageResources.message("dialog.couldNotSavePatches.text", saveLocation, failedChangeLists.toString()),
                MessageResources.message("dialog.couldNotSavePatches.title"),
                showModalErrors);
    }

    private void logError(@NotNull String message, @NotNull String title, boolean showModalErrors) {
        if (showModalErrors) {
            runInDispatchThread(() -> Messages.showErrorDialog(message, title));
        } else {
            runInDispatchThread(
                    () -> new Notification(project, message, MessageType.ERROR)
                            .showBalloon(event -> {
                                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                    openSettings(project);
                                }
                            }).addToEventLog((notification, event) -> openSettings(project)));
        }
    }

    private void logSaveSuccessful(int count, int countShelved, Path saveLocation, boolean showModalErrors) {

        // Never show this message as modal dialog, since that would be lame.
        if (!showModalErrors) {

            String message;
            if (Settings.getInstance(project).getIncludeShelved()) {
                message = MessageResources.message(
                        "dialog.patchesSaved.text.includingShelved", Integer.valueOf(count), Integer.valueOf(countShelved), saveLocation);
            } else {
                message = MessageResources.message(
                        "dialog.patchesSaved.text", Integer.valueOf(count), saveLocation);
            }
        runInDispatchThread(
                () -> new Notification(project, message, MessageType.INFO).showBalloon().addToEventLog());
        }
    }

    private void runInDispatchThread(@NotNull Runnable action) {
        Application application = ApplicationManager.getApplication();
        if (application.isUnitTestMode() || application.isDispatchThread()) {
            action.run();
        }
        else {
            WaitForProgressToShow.runOrInvokeAndWaitAboveProgress(action);
        }
    }

    private class SaveFailedException extends Throwable {
        private String name;
        SaveFailedException(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }
}
