package com.benrose.plugins.saveChangeListsToPatches;

import java.awt.*;
import java.awt.event.ActionEvent;

import com.jgoodies.forms.layout.CellConstraints;

import javax.swing.*;

class SaveChangeListsToPatchesConfiguration implements java.awt.event.ActionListener {
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

    public void actionPerformed(ActionEvent actionEvent) {
        String currentFileLocation = saveLocationField.getText();
        javax.swing.JFileChooser folderChooser = new javax.swing.JFileChooser(currentFileLocation);
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = folderChooser.showDialog(rootComponent, "Select Directory");
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
        JPanel.setLayout(new com.jgoodies.forms.layout.FormLayout("fill:d:grow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        JTextField JTextField = new JTextField();
        saveLocationField = JTextField;
        JTextField.setToolTipText("This is where the patches will be saved.");

        JPanel.add(JTextField, new CellConstraints(3, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
        JLabel JLabel = new JLabel();
        JLabel.setText("Save Location");
        JPanel.add(JLabel, new CellConstraints(7, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
        JLabel JLabel1 = new JLabel();
        JLabel1.setText("Save on close");
        JPanel.add(JLabel1, new CellConstraints(7, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
        JButton JButton = new JButton();
        directoryButton = JButton;
        JButton.setHorizontalAlignment(SwingConstants.LEFT);
        JButton.setEnabled(true);
        JButton.setHideActionText(false);
        JButton.setText("...");
        JPanel.add(JButton, new CellConstraints(5, 1, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
        JCheckBox JCheckBox = new JCheckBox();
        saveOnClose = JCheckBox;
        JCheckBox.setEnabled(true);
        JCheckBox.setToolTipText("Select this option to save patches when closing IntelliJ.");
        JCheckBox.setText("");
        JCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
        JPanel.add(JCheckBox, new CellConstraints(5, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 0)));
    }
}
