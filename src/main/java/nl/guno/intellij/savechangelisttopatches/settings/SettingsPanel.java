package nl.guno.intellij.savechangelisttopatches.settings;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.*;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import nl.guno.intellij.savechangelisttopatches.MessageResources;

public class SettingsPanel {
    private Settings mySettings;

    private JPanel rootComponent;
    private TextFieldWithBrowseButton saveLocationField;
    private JCheckBox saveOnCloseField;
    private JLabel saveOnCloseLabel;

    SettingsPanel(Project project) {
        mySettings = Settings.getInstance(project);
        reset();
        saveLocationField.addActionListener(e -> {
            String currentFileLocation = saveLocationField.getText();

            final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            final VirtualFile toSelect = currentFileLocation == null ? null :
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(currentFileLocation));

            FileChooser.chooseFile(descriptor, project, toSelect, file -> {
                if (file != null && file.isDirectory() && file.isWritable()) {
                    saveLocationField.setText(FileUtil.toSystemDependentName(file.getPath()));
                }
            });
        });
        
        saveOnCloseLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                saveOnCloseField.doClick();
            }
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

    void apply() throws ConfigurationException {
        final String saveLocation = saveLocationField.getText().trim();

        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(saveLocation));
        if (file == null || !file.isDirectory()) {
            throw new ConfigurationException(
                    MessageResources.message("configuration.folderChooser.error.invalidDirectory.text"));
        }
        if (saveOnCloseField.isSelected() && StringUtil.isEmpty(saveLocation)) {
            throw new ConfigurationException(
                    MessageResources.message("configuration.folderChooser.error.directoryMandatory.text"));
        }
        if (!file.isWritable()) {
            throw new ConfigurationException(
                    MessageResources.message("configuration.folderChooser.error.directoryNotWritable.text"));
        }

        mySettings.setSaveLocation(saveLocation);
        mySettings.setSaveOnClose(saveOnCloseField.isSelected());
    }

    JComponent getPanel() {
        return rootComponent;
    }
}
