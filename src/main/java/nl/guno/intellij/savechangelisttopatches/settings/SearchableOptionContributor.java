package nl.guno.intellij.savechangelisttopatches.settings;

import org.jetbrains.annotations.NotNull;

import com.intellij.ide.ui.search.SearchableOptionProcessor;
import nl.guno.intellij.savechangelisttopatches.MessageResources;

public class SearchableOptionContributor extends com.intellij.ide.ui.search.SearchableOptionContributor {
    @Override
    public void processOptions(@NotNull SearchableOptionProcessor processor) {
        final String configurableId = SettingsManager.class.getName();
        final String displayName = MessageResources.message("configuration.key");
        processor.addOptions(MessageResources.message("configuration.saveLocation.label"), null, "SaveChangeListPatches options",
                configurableId, displayName, true);
        processor.addOptions(MessageResources.message("configuration.includeShelved.label"), null, "SaveChangeListPatches options",
                configurableId, displayName, true);
        processor.addOptions(MessageResources.message("configuration.saveOnClose.label"), null, "SaveChangeListPatches options",
                configurableId, displayName, true);
    }
}
