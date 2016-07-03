package nl.guno.intellij.savechangelisttopatches;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;

public class SaveChangeListsToPatchesAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        ((SaveChangeListsToPatchesApplicationComponent)project
                .getComponent("SaveChangeListsToPatchesApplicationComponent")).savePatches();
    }
}
