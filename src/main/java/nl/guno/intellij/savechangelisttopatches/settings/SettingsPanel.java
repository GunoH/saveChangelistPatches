package nl.guno.intellij.savechangelisttopatches.settings;

import java.io.File;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import nl.guno.intellij.savechangelisttopatches.MessageResources;

public class SettingsPanel {
    private Settings mySettings;

    private JPanel rootComponent;
    private JTextField saveLocationField;
    private JCheckBox saveOnCloseField;
    private JButton directoryButton;

    SettingsPanel(Project project) {
        mySettings = Settings.getInstance(project);
        reset();
        directoryButton.addActionListener(e -> {
            String currentFileLocation = saveLocationField.getText();

            final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
            final VirtualFile toSelect = currentFileLocation == null ? null :
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(currentFileLocation));

            FileChooser.chooseFile(descriptor, project, toSelect, file -> {
                if (isValid(file)) {
                    saveLocationField.setText(FileUtil.toSystemDependentName(file.getPath()));
                }
            });
        });
    }

    void reset() {
        saveLocationField.setText(mySettings.getSaveLocation());
        saveOnCloseField.setSelected(mySettings.getSaveOnClose());
    }

    boolean isModified() {
        return !Comparing.equal(mySettings.getSaveLocation(), saveLocationField.getText().trim())
                || mySettings.getSaveOnClose() != saveOnCloseField.isSelected();
    }

    void apply() {
        final String saveLocation = saveLocationField.getText().trim();

        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(saveLocation));
        if (!isValid(file)) {
            Messages.showMessageDialog(MessageResources.message("configuration.folderChooser.error.invalidDirectory.text"),
                    MessageResources.message("configuration.folderChooser.error.invalidDirectory.title"), null);
            return;
        }

        mySettings.setSaveLocation(saveLocation);
        mySettings.setSaveOnClose(saveOnCloseField.isSelected());
    }

    private boolean isValid(@Nullable VirtualFile file) {
        return file != null && file.isDirectory() && file.isWritable();
    }

    JComponent getPanel() {
        return rootComponent;
    }
}
