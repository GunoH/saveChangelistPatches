package nl.guno.intellij.savechangelisttopatches.settings;

import java.io.File;

import javax.swing.*;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import nl.guno.intellij.savechangelisttopatches.MessageResources;

public class SettingsPanel {
    private Settings mySettings;

    private JPanel rootComponent;
    private JTextField saveLocationField;
    private JCheckBox saveOnCloseField;
    private JButton directoryButton;

    SettingsPanel() {
        mySettings = Settings.getInstance();
        reset();
        directoryButton.addActionListener(e -> {
            String currentFileLocation = saveLocationField.getText();
            JFileChooser folderChooser = new JFileChooser(currentFileLocation);
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = folderChooser.showDialog(rootComponent, MessageResources.message("configuration.folderChooser.approveButton.text"));
            if ((returnVal == 0) && (new File(folderChooser.getSelectedFile().getPath()).canWrite()))
                saveLocationField.setText(folderChooser.getSelectedFile().getPath());
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
        if (new File(saveLocation).canWrite()) {
            mySettings.setSaveLocation(saveLocation);
            mySettings.setSaveOnClose(saveOnCloseField.isSelected());
        } else {
            Messages.showMessageDialog(MessageResources.message("configuration.folderChooser.error.invalidDirectory.text"),
                    MessageResources.message("configuration.folderChooser.error.invalidDirectory.title"), null);
        }

    }

    JComponent getPanel() {
        return rootComponent;
    }
}
