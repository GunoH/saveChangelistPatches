package com.benrose.plugins.savechangeliststopatches;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import nl.guno.intellij.savechangelisttopatches.MessageResources;

import javax.swing.*;

class SaveChangeListsToPatchesConfiguration implements ActionListener {
    private JPanel rootComponent;
    private JTextField saveLocationField;
    private JCheckBox saveOnClose;
    private JButton directoryButton;

    SaveChangeListsToPatchesConfiguration() {
        setupUI();
        directoryButton.addActionListener(this);
    }

    void closeDownForm() {
        directoryButton.removeActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String currentFileLocation = saveLocationField.getText();
        JFileChooser folderChooser = new JFileChooser(currentFileLocation);
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = folderChooser.showDialog(rootComponent, MessageResources.message("configuration.folderChooser.approveButton.text"));
        if ((returnVal == 0) && (new java.io.File(folderChooser.getSelectedFile().getPath()).canWrite()))
            saveLocationField.setText(folderChooser.getSelectedFile().getPath());
    }

    JComponent getRootComponent() {
        return rootComponent;
    }

    void setData(SaveChangeListsToPatchesApplicationComponent data) {
        saveLocationField.setText(data.getSaveLocationField());
        saveOnClose.setSelected(data.getSaveOnClose());
    }

    void getData(SaveChangeListsToPatchesApplicationComponent data) {
        data.setSaveLocationField(saveLocationField.getText());
        data.setSaveOnClose(saveOnClose.isSelected());
    }

    String getSaveLocation() {
        return saveLocationField.getText();
    }

    boolean isModified(SaveChangeListsToPatchesApplicationComponent data) {
        return (!saveLocationField.getText().equals(data.getSaveLocationField())) || (saveOnClose.isSelected() != data.getSaveOnClose());
    }

    private void setupUI() {
        JPanel JPanel = new JPanel();
        rootComponent = JPanel;
        JPanel.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        JTextField JTextField = new JTextField();
        saveLocationField = JTextField;
        JTextField.setToolTipText(MessageResources.message("configuration.saveLocation.tooltipText"));

        JPanel.add(JTextField, new CellConstraints(3, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
        JLabel JLabel = new JLabel();
        JLabel.setText(MessageResources.message("configuration.saveLocation.label"));
        JPanel.add(JLabel, new CellConstraints(7, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
        JLabel JLabel1 = new JLabel();
        JLabel1.setText(MessageResources.message("configuration.saveOnClose.label"));
        JPanel.add(JLabel1, new CellConstraints(7, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
        JButton JButton = new JButton();
        directoryButton = JButton;
        JButton.setHorizontalAlignment(SwingConstants.LEFT);
        JButton.setEnabled(true);
        JButton.setHideActionText(false);
        JButton.setText(MessageResources.message("configuration.folderChooser.label"));
        JPanel.add(JButton, new CellConstraints(5, 1, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
        JCheckBox JCheckBox = new JCheckBox();
        saveOnClose = JCheckBox;
        JCheckBox.setEnabled(true);
        JCheckBox.setToolTipText(MessageResources.message("configuration.saveOnClose.tooltipText"));
        JCheckBox.setText("");
        JCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
        JPanel.add(JCheckBox, new CellConstraints(5, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
    }
}
