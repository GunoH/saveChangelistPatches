package nl.guno.intellij.savechangelisttopatches.settings

import javax.swing.*

import org.jetbrains.annotations.Nls

import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import nl.guno.intellij.savechangelisttopatches.MessageResources

class SettingsManager(private val myProject: Project) : SearchableConfigurable {
    private var mySettingsPane: SettingsPanel? = null

    @Nls
    override fun getDisplayName(): String {
        return MessageResources.message("settingsManager.displayName")
    }

    override fun getHelpTopic(): String? {
        return null
    }

    override fun getId(): String {
        return "saveChangeListPatches"
    }

    override fun enableSearch(option: String): Runnable? {
        return null
    }

    override fun createComponent(): JComponent? {
        if (mySettingsPane == null) {
            mySettingsPane = SettingsPanel(myProject)
        }
        return mySettingsPane!!.panel
    }

    override fun isModified(): Boolean {
        return mySettingsPane != null && mySettingsPane!!.isModified
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        if (mySettingsPane != null) {
            mySettingsPane!!.apply()
        }
    }

    override fun reset() {
        if (mySettingsPane != null) {
            mySettingsPane!!.reset()
        }
    }

    override fun disposeUIResources() {}
}
