package com.benrose.plugins.savechangeliststopatches;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import nl.guno.intellij.savechangelisttopatches.MessageResources;
import nl.guno.intellij.savechangelisttopatches.settings.Settings;

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
        String FilePath = Settings.getInstance(project).getSaveLocation();
        if ((FilePath == null) || (FilePath.length() < 1)) {
            Messages.showMessageDialog(
                    MessageResources.message("dialog.saveLocation.notSet.text"),
                    MessageResources.message("dialog.saveLocation.notSet.title"), null);
            return;
        }
        if (FilePath.charAt(FilePath.length() - 1) != '/') {
            FilePath = FilePath + "/";
        }
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        List<LocalChangeList> localChangeLists = changeListManager.getChangeLists();

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
                File patchFile = new File(FilePath + localChangeList.getName() + ".patch");
                try (FileWriter writer = new FileWriter(patchFile.getPath())) {
                    UnifiedDiffWriter.write(project, patches, writer, "\n", null);
                    writer.flush();
                } catch (FileNotFoundException ex) {
                    Messages.showMessageDialog(
                            MessageResources.message("dialog.invalidFileName.text", localChangeList.getName()),
                            MessageResources.message("dialog.invalidFileName.title"),
                            null);
                } catch (IOException ex) {
                    Messages.showMessageDialog(ex.toString(), MessageResources.message("dialog.exception.title"), null);
                }
            }
        }
    }
}
