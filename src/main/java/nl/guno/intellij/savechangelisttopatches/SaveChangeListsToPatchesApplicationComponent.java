package nl.guno.intellij.savechangelisttopatches;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import nl.guno.intellij.savechangelisttopatches.settings.Settings;

public class SaveChangeListsToPatchesApplicationComponent implements ProjectComponent {

    private Project project;

    public SaveChangeListsToPatchesApplicationComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {}

    @Override
    public void projectClosed() {
        if (Settings.getInstance(project).getSaveOnClose()) {
            new ChangeListsSaver(project).savePatches();
        }
    }

    @Override
    public void initComponent() {}

    @Override
    public void disposeComponent() {}

    @Override
    @NotNull
    public String getComponentName() {
        return "SaveChangeListsToPatchesApplicationComponent";
    }


}
