/*    */ package com.benrose.plugins.saveChangeListsToPatches;
/*    */ 
/*    */ import com.intellij.openapi.actionSystem.AnAction;
/*    */ import com.intellij.openapi.actionSystem.AnActionEvent;
/*    */ import com.intellij.openapi.actionSystem.DataContext;
/*    */ import com.intellij.openapi.project.Project;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SaveChangeListsToPatchesAction
/*    */   extends AnAction
/*    */ {
/*    */   public void actionPerformed(AnActionEvent e)
/*    */   {
/* 19 */     Project project = (Project)e.getDataContext().getData("project");
/*    */     
/* 21 */     ((SaveChangeListsToPatchesApplicationComponent)project.getComponent("SaveChangeListsToPatchesApplicationComponent")).savePatches();
/*    */   }
/*    */ }


/* Location:              C:\Users\brose_wndz\Downloads\plugin\plugin\lib\plugin.jar!\com\benrose\plugins\saveChangeListsToPatches\SaveChangeListsToPatchesAction.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */