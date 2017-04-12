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
import java.util.stream.Collectors;

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

        String saveLocation = Settings.getInstance(project).getSaveLocation();
        if ((saveLocation == null) || (saveLocation.length() < 1)) {
            logError(MessageResources.message("dialog.saveLocation.notSet.text"), 
                    MessageResources.message("dialog.couldNotSavePatches.title"), showModalErrors);
            return;
        }
        if (saveLocation.charAt(saveLocation.length() - 1) != '/') {
            saveLocation = saveLocation + "/";
        }

        File saveDir = new File(saveLocation);
        if (!saveDir.exists() || !saveDir.canWrite()) {
            logError(MessageResources.message("dialog.saveLocation.notValid.text", saveLocation),
                    MessageResources.message("dialog.couldNotSavePatches.title"), showModalErrors);
            return;
        }
        
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        List<LocalChangeList> localChangeLists = changeListManager.getChangeLists();

        Collection<String> failed = new HashSet<>();

        int count = 0;
        for (LocalChangeList localChangeList : localChangeLists) {
            try {
                savePatchForChangelist(localChangeList, saveLocation);
                count++;
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
                    savePatchForShelvedChangelist(shelvedChangeList, saveLocation);
                    countShelved++;
                } catch (SaveFailedException e) {
                    failed.add(e.getName());
                }
            }
        }

        if (count > 0 || countShelved > 0) {
            logSaveSuccessful(count, countShelved, saveLocation, showModalErrors);
        }

        if (!failed.isEmpty()) {
            logFailure(saveLocation, failed, showModalErrors);
        }
    }

    private void savePatchForChangelist(LocalChangeList changeList, String saveLocation) throws SaveFailedException {
        
        if (changeList.getChanges().isEmpty()) {
            // Don't create patches for empty change lists.
            return;
        }

        savePatchForChange(changeList.getChanges(), saveLocation, changeList.getName());
    }

    private void savePatchForChange(Collection<Change> changes, String saveLocation, String name) 
            throws SaveFailedException {
        
        Collection<FilePatch> patches;
        try {
            patches = IdeaTextPatchBuilder.buildPatch(project, changes, project.getBaseDir().getPath(), false);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            patches = null;
        }

        DateFormat dateFormat = new SimpleDateFormat("_yyyyMMdd_HHmmss");
        String dateString = dateFormat.format(new Date());

        if (patches != null) {
            File patchFile = ShelveChangesManager.suggestPatchName(project, name + dateString,
                    new File(saveLocation), null);
            try (FileWriter writer = new FileWriter(patchFile.getPath())) {
                UnifiedDiffWriter.write(project, patches, writer, "\n", null);
                writer.flush();
            } catch (FileNotFoundException ex) {
                throw new SaveFailedException(name);
            } catch (IOException ex) {
                new Notification(project, ex.toString(), MessageType.ERROR).showBalloon().addToEventLog();
            }
        }
    }

    private void savePatchForShelvedChangelist(ShelvedChangeList changeList, String saveLocation) throws SaveFailedException {
        
        if (changeList.getChanges(project).isEmpty()) {
            // Don't create patches for empty change lists.
            return;
        }

        Collection<Change> changes = changeList.getChanges(project).stream().map(
                change -> change.getChange(project)).collect(Collectors.toCollection(HashSet::new));

        savePatchForChange(changes, saveLocation, "_shelf_" + changeList.getName());
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

    private void logSaveSuccessful(int count, int countShelved, String saveLocation, boolean showModalErrors) {

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
