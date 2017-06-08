package nl.guno.intellij.savechangelisttopatches

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class SaveChangeListsToPatchesAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)

        val task = SaveChangeListsToPatchesTask(project)
        task.queue()
    }
}
