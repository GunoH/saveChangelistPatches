package nl.guno.intellij.savechangelisttopatches;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.event.HyperlinkEvent;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import nl.guno.intellij.savechangelisttopatches.settings.Settings;
import nl.guno.intellij.savechangelisttopatches.settings.SettingsManager;

public class SaveChangeListsToPatchesApplicationComponent implements ProjectComponent {

    private Project project;

    public SaveChangeListsToPatchesApplicationComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {}

    @Override
    public void projectClosed() {
        if (Settings.getInstance(project).getSaveOnClose()) {
            savePatches();
        }
    }

    @Override
    public void initComponent() {}

    @Override
    public void disposeComponent() {}

    @Override
    @NotNull
    public String getComponentName() {
        return "SaveChangeListsToPatchesApplicationComponent";
    }

    void savePatches() {
        String saveLocation = Settings.getInstance(project).getSaveLocation();
        if ((saveLocation == null) || (saveLocation.length() < 1)) {
            new Notification(project, MessageResources.message("dialog.saveLocation.notSet.text"), MessageType.ERROR)
                    .showBalloon(event -> {
                        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            openSettings(project);
                        }
                    }).addToEventLog((notification, event) -> openSettings(project));
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

            if (patches != null) {
                File patchFile = new File(saveLocation + localChangeList.getName() + ".patch");
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
            StringBuilder failedChangeLists = new StringBuilder();
            for (String filename : failed) {
                failedChangeLists.append("  - " + filename).append("\n");
            }
            
            Messages.showMessageDialog(
                    MessageResources.message("dialog.couldNotSavePatches.text", saveLocation, failedChangeLists.toString()),
                    MessageResources.message("dialog.couldNotSavePatches.title"),
                    null);

        }
    }

    private static void openSettings(Project project) {
        ShowSettingsUtil.getInstance().editConfigurable(project, new SettingsManager(project));
    }
}
