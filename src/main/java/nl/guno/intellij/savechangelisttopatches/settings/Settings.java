package nl.guno.intellij.savechangelisttopatches.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

@State(name = "SaveChangeListPatchesSettings", storages = @Storage("saveChangeListPatchesSettings.xml"))
public class Settings implements PersistentStateComponent<Settings.State> {
    
    private State myState = new State();
    
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(State state) {
        myState = state;
    }

    @SuppressWarnings("WeakerAccess")
    public static class State {
        State() {
            saveLocation = "";
            saveOnClose = false;
        }

        public String saveLocation;
        public boolean saveOnClose;
    }

    public static Settings getInstance() {
        return ServiceManager.getService(Settings.class);
    }

    public String getSaveLocation() {
        return myState.saveLocation;
    }

    void setSaveLocation(String saveLocation) {
        myState.saveLocation = saveLocation;
    }

    public boolean getSaveOnClose() {
        return myState.saveOnClose;
    }

    void setSaveOnClose(boolean saveOnClose) {
        myState.saveOnClose = saveOnClose;
    }
}
