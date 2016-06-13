package com.benrose.plugins.saveChangeListsToPatches;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.*;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;

public class SaveChangeListsToPatchesApplicationComponent
        implements ProjectComponent, Configurable, JDOMExternalizable {

    private SaveChangeListsToPatchesConfiguration form;
    private String saveLocationField;
    private boolean saveOnClose;
    private Project project;

    public SaveChangeListsToPatchesApplicationComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {}

    @Override
    public void projectClosed() {
        if (saveOnClose) {
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

    String getSaveLocationField() {
        return saveLocationField;
    }

    void setSaveLocationField(String saveLocationField) {
        this.saveLocationField = saveLocationField;
    }

    boolean getSaveOnClose() {
        return saveOnClose;
    }

    void setSaveOnClose(boolean saveOnClose) {
        this.saveOnClose = saveOnClose;
    }

    @Override
    public String getDisplayName() {
        return "Save Changelists to Patches - Plugin";
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public JComponent createComponent() {
        if (form == null) {
            form = new SaveChangeListsToPatchesConfiguration();
        }
        return form.getRootComponent();
    }

    @Override
    public boolean isModified() {
        return form != null && form.isModified(this);
    }

    @Override
    public void apply() throws ConfigurationException {
        if (form != null) {
            if (new File(form.getSaveLocation()).canWrite()) {
                form.getData(this);
            } else {
                Messages.showMessageDialog("Invalid directory.  Changes will not be saved.", "Message", null);
            }
        }
    }

    @Override
    public void reset() {
        if (form == null) {
            return;
        }

        form.setData(this);
    }

    @Override
    public void disposeUIResources() {
        form.closeDownForm();
        form = null;
    }

    @Override
    public void readExternal(Element Element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, Element);
    }

    @Override
    public void writeExternal(Element Element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, Element);
    }

    void savePatches() {
        String FilePath = getSaveLocationField();
        if ((FilePath == null) || (FilePath.length() < 1)) {
            Messages.showMessageDialog("Save path has not been set.\nPlease set this in the plugin configuration under project settings.", "Save Location Not Set", null);
            return;
        }
        if (FilePath.charAt(FilePath.length() - 1) != '/') {
            FilePath = FilePath + "/";
        }
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        List<LocalChangeList> localChangeLists = changeListManager.getChangeLists();

        for (LocalChangeList localChangeList : localChangeLists) {

            Collection<FilePatch> patches;
            try {
                patches = IdeaTextPatchBuilder.buildPatch(project, localChangeList.getChanges(), project.getBaseDir().getPath(), false);
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
                patches = null;
            }

            if (patches != null) {
                File patchFile = new File(FilePath + localChangeList.getName() + ".patch");
                try (FileWriter writer = new FileWriter(patchFile.getPath())) {
                    UnifiedDiffWriter.write(project, patches, writer, "\n", null);
                    writer.flush();
                } catch (FileNotFoundException ex) {
                    Messages.showMessageDialog("Invalid File name: " + localChangeList.getName() + ".patch\nThis File was not saved.", "Invalid File Name", null);
                } catch (IOException ex) {
                    Messages.showMessageDialog(ex.toString(), "Exception Message", null);
                }
            }
        }
    }
}
