package nl.guno.intellij.savechangelisttopatches;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

class SaveChangeListsToPatchesTask extends Task.Backgroundable {


    SaveChangeListsToPatchesTask(@Nullable Project project) {
        super(project, MessageResources.message("task.saveChangeListsToPatches.title"), false);
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        new ChangeListsSaver(getProject()).savePatches(false);
    }



}
