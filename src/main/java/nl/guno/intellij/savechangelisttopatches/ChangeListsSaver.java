package nl.guno.intellij.savechangelisttopatches;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.swing.event.HyperlinkEvent;

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
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.changes.shelf.ShelveChangesManager;
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

        String saveLocation = Settings.getInstance(project).getSaveLocation();
        if ((saveLocation == null) || (saveLocation.length() < 1)) {
            logError(MessageResources.message("dialog.saveLocation.notSet.text"), "", showModalErrors);
            return;
        }
        if (saveLocation.charAt(saveLocation.length() - 1) != '/') {
            saveLocation = saveLocation + "/";
        }
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        List<LocalChangeList> localChangeLists = changeListManager.getChangeLists();

        Collection<String> failed = new HashSet<>();

        for (LocalChangeList localChangeList : localChangeLists) {

            if (localChangeList.getChanges().isEmpty()) {
                // Don't create patches for empty change lists.
                continue;
            }

            Collection<FilePatch> patches;
            try {
                patches = IdeaTextPatchBuilder.buildPatch(project, localChangeList.getChanges(), project.getBaseDir().getPath(), false);
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
                patches = null;
            }

            DateFormat dateFormat = new SimpleDateFormat("_yyyyMMdd_HHmmss");
            String dateString = dateFormat.format(new Date());

            if (patches != null) {
                File patchFile = ShelveChangesManager.suggestPatchName(project, localChangeList.getName() + dateString,
                        new File(saveLocation), null);
                try (FileWriter writer = new FileWriter(patchFile.getPath())) {
                    UnifiedDiffWriter.write(project, patches, writer, "\n", null);
                    writer.flush();
                } catch (FileNotFoundException ex) {
                    failed.add(localChangeList.getName());
                } catch (IOException ex) {
                    new Notification(project, ex.toString(), MessageType.ERROR).showBalloon().addToEventLog();
                }
            }
        }

        if (!failed.isEmpty()) {
            logFailure(saveLocation, failed, showModalErrors);
        }
    }

    private static void openSettings(Project project) {
        ShowSettingsUtil.getInstance().editConfigurable(project, new SettingsManager(project));
    }

    private void logFailure(final String saveLocation, final Collection<String> failed, boolean showModalErrors) {
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

    private void runInDispatchThread(@NotNull Runnable action) {
        Application application = ApplicationManager.getApplication();
        if (application.isUnitTestMode() || application.isDispatchThread()) {
            action.run();
        }
        else {
            WaitForProgressToShow.runOrInvokeAndWaitAboveProgress(action);
        }
    }

}
