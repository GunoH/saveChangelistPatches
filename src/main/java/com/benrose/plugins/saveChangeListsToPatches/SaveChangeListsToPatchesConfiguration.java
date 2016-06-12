/*     */ package com.benrose.plugins.saveChangeListsToPatches;
/*     */ 
/*     */ import com.jgoodies.forms.layout.CellConstraints;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class SaveChangeListsToPatchesConfiguration implements java.awt.event.ActionListener
/*     */ {
/*     */   private JPanel rootComponent;
/*     */   private JLabel saveLocationLabel;
/*     */   private JTextField saveLocationField;
/*     */   private JCheckBox saveOnClose;
/*     */   private JLabel SaveOnCloseLabel;
/*     */   private JButton directoryButton;
/*     */   
/*     */   public SaveChangeListsToPatchesConfiguration()
/*     */   {
/*  21 */     setupUI();
/*  22 */     this.directoryButton.addActionListener(this);
/*     */   }
/*     */   
/*     */   public void closeDownForm()
/*     */   {
/*  27 */     this.directoryButton.removeActionListener(this);
/*     */   }
/*     */   
/*     */   public void actionPerformed(java.awt.event.ActionEvent actionEvent)
/*     */   {
/*  32 */     String currentFileLocation = this.saveLocationField.getText();
/*  33 */     javax.swing.JFileChooser folderChooser = new javax.swing.JFileChooser(currentFileLocation);
/*  34 */     folderChooser.setFileSelectionMode(1);
/*  35 */     int returnVal = folderChooser.showDialog(this.rootComponent, "Select Directory");
/*  36 */     if ((returnVal == 0) && (new java.io.File(folderChooser.getSelectedFile().getPath()).canWrite()))
/*  37 */       this.saveLocationField.setText(folderChooser.getSelectedFile().getPath());
/*  38 */     folderChooser = null;
/*     */   }
/*     */   
/*     */   public javax.swing.JComponent getRootComponent()
/*     */   {
/*  43 */     return this.rootComponent;
/*     */   }
/*     */   
/*     */   public void setData(SaveChangeListsToPatchesApplicationComponent data)
/*     */   {
/*  48 */     this.saveLocationField.setText(data.getSaveLocationField());
/*  49 */     this.saveOnClose.setSelected(data.getSaveOnClose());
/*     */   }
/*     */   
/*     */   public void getData(SaveChangeListsToPatchesApplicationComponent data)
/*     */   {
/*  54 */     data.setSaveLocationField(this.saveLocationField.getText());
/*  55 */     data.setSaveOnClose(this.saveOnClose.isSelected());
/*     */   }
/*     */   
/*     */   public String getSaveLocation()
/*     */   {
/*  60 */     return this.saveLocationField.getText();
/*     */   }
/*     */   
/*     */   public boolean isSaveOnClose()
/*     */   {
/*  65 */     return this.saveOnClose.isSelected();
/*     */   }
/*     */   
/*     */   public boolean isModified(SaveChangeListsToPatchesApplicationComponent data)
/*     */   {
/*  70 */     return (!this.saveLocationField.getText().equals(data.getSaveLocationField())) || (this.saveOnClose.isSelected() != data.getSaveOnClose());
/*     */   }
/*     */   
/*     */ 
/*     */   private void createUIComponents() {}
/*     */   
/*     */ 
/*     */   private void setupUI()
/*     */   {
/*  79 */     JPanel JPanel = new JPanel();
/*  80 */     this.rootComponent = JPanel;
/*  81 */     JPanel.setLayout(new com.jgoodies.forms.layout.FormLayout("fill:d:grow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
/*  82 */     JTextField JTextField = new JTextField();
/*  83 */     this.saveLocationField = JTextField;
/*  84 */     JTextField.setToolTipText("This is where the patches will be saved.");
/*     */     
/*  86 */     JPanel.add(JTextField, new CellConstraints(3, 1, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT, new java.awt.Insets(0, 0, 0, 0)));
/*  87 */     JLabel JLabel = new JLabel();
/*  88 */     this.saveLocationLabel = JLabel;
/*  89 */     JLabel.setText("Save Location");
/*  90 */     JPanel.add(JLabel, new CellConstraints(7, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new java.awt.Insets(0, 0, 0, 0)));
/*  91 */     JLabel JLabel1 = new JLabel();
/*  92 */     this.SaveOnCloseLabel = JLabel1;
/*  93 */     JLabel1.setText("Save on close");
/*  94 */     JPanel.add(JLabel1, new CellConstraints(7, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new java.awt.Insets(0, 0, 0, 0)));
/*  95 */     JButton JButton = new JButton();
/*  96 */     this.directoryButton = JButton;
/*  97 */     JButton.setHorizontalAlignment(2);
/*  98 */     JButton.setEnabled(true);
/*  99 */     JButton.setHideActionText(false);
/* 100 */     JButton.setText("...");
/* 101 */     JPanel.add(JButton, new CellConstraints(5, 1, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT, new java.awt.Insets(0, 0, 0, 0)));
/* 102 */     JCheckBox JCheckBox = new JCheckBox();
/* 103 */     this.saveOnClose = JCheckBox;
/* 104 */     JCheckBox.setEnabled(true);
/* 105 */     JCheckBox.setToolTipText("Select this option to save patches when closing IntelliJ.");
/* 106 */     JCheckBox.setText("");
/* 107 */     JCheckBox.setHorizontalAlignment(2);
/* 108 */     JPanel.add(JCheckBox, new CellConstraints(5, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new java.awt.Insets(0, 0, 0, 0)));
/*     */   }
/*     */   
/*     */   public javax.swing.JComponent $$$getRootComponent$$$()
/*     */   {
/* 113 */     return this.rootComponent;
/*     */   }
/*     */ }


/* Location:              C:\Users\brose_wndz\Downloads\plugin\plugin\lib\plugin.jar!\com\benrose\plugins\saveChangeListsToPatches\SaveChangeListsToPatchesConfiguration.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */