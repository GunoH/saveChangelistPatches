/*     */ package com.benrose.plugins.saveChangeListsToPatches;
/*     */ 
/*     */ import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
/*     */ import com.intellij.openapi.options.Configurable;
/*     */ import com.intellij.openapi.project.Project;
/*     */ import com.intellij.openapi.ui.Messages;
/*     */ import com.intellij.openapi.util.DefaultJDOMExternalizer;
/*     */ import com.intellij.openapi.util.InvalidDataException;
/*     */ import com.intellij.openapi.util.JDOMExternalizable;
/*     */ import com.intellij.openapi.util.WriteExternalException;
/*     */ import com.intellij.openapi.vcs.changes.ChangeListManager;
/*     */ import com.intellij.openapi.vcs.changes.LocalChangeList;
/*     */ import com.intellij.openapi.vfs.VirtualFile;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import javax.swing.Icon;
/*     */ import javax.swing.JComponent;
/*     */ import org.jdom.Element;
/*     */ 
/*     */ public class SaveChangeListsToPatchesApplicationComponent implements com.intellij.openapi.components.ProjectComponent, Configurable, JDOMExternalizable
/*     */ {
/*     */   private SaveChangeListsToPatchesConfiguration form;
/*     */   public String saveLocationField;
/*     */   public boolean saveOnClose;
/*     */   private Project project;
/*     */   
/*     */   public SaveChangeListsToPatchesApplicationComponent(Project project)
/*     */   {
/*  35 */     this.project = project;
/*     */   }
/*     */   
/*     */ 
/*     */   public void projectOpened() {}
/*     */   
/*     */ 
/*     */   public void projectClosed()
/*     */   {
/*  44 */     if (getSaveOnClose()) {
/*  45 */       savePatches();
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */   public void initComponent() {}
/*     */   
/*     */ 
/*     */   public void disposeComponent() {}
/*     */   
/*     */ 
/*     */   public String getComponentName()
/*     */   {
/*  58 */     return "SaveChangeListsToPatchesApplicationComponent";
/*     */   }
/*     */   
/*     */   public String getSaveLocationField()
/*     */   {
/*  63 */     return this.saveLocationField;
/*     */   }
/*     */   
/*     */   public void setSaveLocationField(String str)
/*     */   {
/*  68 */     this.saveLocationField = str;
/*     */   }
/*     */   
/*     */   public boolean getSaveOnClose()
/*     */   {
/*  73 */     return this.saveOnClose;
/*     */   }
/*     */   
/*     */   public void setSaveOnClose(boolean b)
/*     */   {
/*  78 */     this.saveOnClose = b;
/*     */   }
/*     */   
/*     */   public String getDisplayName()
/*     */   {
/*  83 */     return "Save changelists to patches - Plugin";
/*     */   }
/*     */   
/*     */   public Icon getIcon()
/*     */   {
/*  88 */     return null;
/*     */   }
/*     */   
/*     */   public String getHelpTopic()
/*     */   {
/*  93 */     return null;
/*     */   }
/*     */   
/*     */   public JComponent createComponent()
/*     */   {
/*  98 */     if (this.form == null)
/*  99 */       this.form = new SaveChangeListsToPatchesConfiguration();
/* 100 */     return this.form.getRootComponent();
/*     */   }
/*     */   
/*     */   public boolean isModified()
/*     */   {
/* 105 */     return (this.form != null) && (this.form.isModified(this));
/*     */   }
/*     */   
/*     */   public void apply()
/*     */     throws com.intellij.openapi.options.ConfigurationException
/*     */   {
/* 111 */     if (this.form != null) {
/* 112 */       if (new File(this.form.getSaveLocation()).canWrite()) {
/* 113 */         this.form.getData(this);
/*     */       } else
/* 115 */         Messages.showMessageDialog("Invalid directory.  Changes will not be saved.", "Message", null);
/*     */     }
/*     */   }
/*     */   
/*     */   public void reset() {
/* 120 */     if (this.form == null)
/*     */     {
/* 122 */       return;
/*     */     }
/*     */     
/* 125 */     this.form.setData(this);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public void disposeUIResources()
/*     */   {
/* 132 */     this.form.closeDownForm();
/* 133 */     this.form = null;
/*     */   }
/*     */   
/*     */   public void readExternal(Element Element)
/*     */     throws InvalidDataException
/*     */   {
/* 139 */     DefaultJDOMExternalizer.readExternal(this, Element);
/*     */   }
/*     */   
/*     */   public void writeExternal(Element Element)
/*     */     throws WriteExternalException
/*     */   {
/* 145 */     DefaultJDOMExternalizer.writeExternal(this, Element);
/*     */   }
/*     */   
/*     */   public void savePatches()
/*     */   {
/* 150 */     String FilePath = getSaveLocationField();
/* 151 */     if ((FilePath == null) || (FilePath.length() < 1))
/*     */     {
/* 153 */       Messages.showMessageDialog("Save path has not been set.\nPlease set this in the plugin configuration under project settings.", "Save Location Not Set", null);
/* 154 */       return;
/*     */     }
/* 156 */     if (FilePath.charAt(FilePath.length() - 1) != '/')
/* 157 */       FilePath = FilePath + "/";
/* 158 */     ChangeListManager changeListManager = ChangeListManager.getInstance(this.project);
/* 159 */     List localChangeLists = changeListManager.getChangeLists();
/* 160 */     Collection patches = null;
/* 161 */     Iterator i$ = localChangeLists.iterator();
/*     */     
/*     */ 
/* 164 */     while (i$.hasNext())
/*     */     {
/* 166 */       LocalChangeList localChangeList = (LocalChangeList)i$.next();
/*     */       
/*     */       try
/*     */       {
/* 170 */         patches = com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder.buildPatch(this.project, localChangeList.getChanges(), this.project.getBaseDir().getPath(), false);
/*     */       }
/*     */       catch (Exception ex)
/*     */       {
/* 174 */         System.out.println("Error: " + ex.getMessage());
/* 175 */         patches = null;
/*     */       }
/* 177 */       if (patches != null) {
/*     */         try
/*     */         {
/* 180 */           File patchFile = new File(FilePath + localChangeList.getName() + ".patch");
/* 181 */           FileWriter writer = new FileWriter(patchFile.getPath());
/* 182 */           UnifiedDiffWriter.write(this.project, patches, writer, "\n", null);
/* 183 */           writer.flush();
/* 184 */           writer.close();
/*     */         }
/*     */         catch (FileNotFoundException ex)
/*     */         {
/* 188 */           Messages.showMessageDialog("Invalid File name: " + localChangeList.getName() + ".patch\nThis File was not saved.", "Invalid File Name", null);
/*     */         }
/*     */         catch (IOException ex)
/*     */         {
/* 192 */           Messages.showMessageDialog(ex.toString(), "Exception Message", null);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ }


/* Location:              C:\Users\brose_wndz\Downloads\plugin\plugin\lib\plugin.jar!\com\benrose\plugins\saveChangeListsToPatches\SaveChangeListsToPatchesApplicationComponent.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */