package nl.guno.intellij.savechangelisttopatches.settings

import com.intellij.ide.ui.search.SearchableOptionProcessor
import nl.guno.intellij.savechangelisttopatches.MessageResources

class SearchableOptionContributor : com.intellij.ide.ui.search.SearchableOptionContributor() {
    override fun processOptions(processor: SearchableOptionProcessor) {
        val configurableId = SettingsManager::class.java.name
        val displayName = MessageResources.message("configuration.key")
        processor.addOptions(MessageResources.message("configuration.saveLocation.label"),
                null, "SaveChangeListPatches options", configurableId, displayName, true)
        processor.addOptions(MessageResources.message("configuration.includeShelved.label"),
                null, "SaveChangeListPatches options", configurableId, displayName, true)
        processor.addOptions(MessageResources.message("configuration.saveOnClose.label"),
                null, "SaveChangeListPatches options", configurableId, displayName, true)
    }
}
