package nl.guno.intellij.savechangelisttopatches.settings;

import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import nl.guno.intellij.savechangelisttopatches.MessageResources;

public class SettingsManager implements SearchableConfigurable {
    private final Project myProject;
    private SettingsPanel mySettingsPane;

    public SettingsManager(Project project) {
        myProject = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return MessageResources.message("settingsManager.displayName");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @NotNull
    @Override
    public String getId() {
        return "saveChangeListPatches";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (mySettingsPane == null) {
            mySettingsPane = new SettingsPanel(myProject);
        }
        return mySettingsPane.getPanel();
    }

    @Override
    public boolean isModified() {
        return mySettingsPane != null && mySettingsPane.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        if (mySettingsPane != null) {
            mySettingsPane.apply();
        }
    }

    @Override
    public void reset() {
        if (mySettingsPane != null) {
            mySettingsPane.reset();
        }
    }

    @Override
    public void disposeUIResources() {
    }
}
