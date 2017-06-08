package nl.guno.intellij.savechangelisttopatches

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import nl.guno.intellij.savechangelisttopatches.settings.Settings

class SaveChangeListsToPatchesApplicationComponent(private val project: Project) : ProjectComponent {

    override fun projectOpened() {}

    override fun projectClosed() {
        if (Settings.getInstance(project).saveOnClose) {
            ChangeListsSaver(project).savePatches(true)
        }
    }

    override fun initComponent() {}

    override fun disposeComponent() {}

    override fun getComponentName(): String {
        return "SaveChangeListsToPatchesApplicationComponent"
    }


}
