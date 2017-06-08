package nl.guno.intellij.savechangelisttopatches

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

internal class SaveChangeListsToPatchesTask(project: Project?)
    : Task.Backgroundable(project, MessageResources.message("task.saveChangeListsToPatches.title"), false) {

    override fun run(progressIndicator: ProgressIndicator) {
        ChangeListsSaver(project).savePatches(false)
    }
}
