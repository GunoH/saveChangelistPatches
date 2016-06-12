package com.benrose.plugins.saveChangeListsToPatches;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;

public class SaveChangeListsToPatchesAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        ((SaveChangeListsToPatchesApplicationComponent)project
                .getComponent("SaveChangeListsToPatchesApplicationComponent")).savePatches();
    }
}
