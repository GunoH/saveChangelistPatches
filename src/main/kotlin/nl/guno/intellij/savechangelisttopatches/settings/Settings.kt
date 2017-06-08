package nl.guno.intellij.savechangelisttopatches.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(name = "SaveChangeListPatchesSettings", defaultStateAsResource = true)
class Settings : PersistentStateComponent<Settings.State> {

    private var myState = State()

    override fun getState(): State? {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    class State internal constructor() {
        var saveLocation: String = ""
        var saveOnClose: Boolean = false
        var includeShelved: Boolean = false
        var useSubDirs: Boolean = false
    }

    var saveLocation: String
        get() = myState.saveLocation
        internal set(saveLocation) {
            myState.saveLocation = saveLocation
        }

    var saveOnClose: Boolean
        get() = myState.saveOnClose
        internal set(saveOnClose) {
            myState.saveOnClose = saveOnClose
        }

    var includeShelved: Boolean
        get() = myState.includeShelved
        internal set(includeShelved) {
            myState.includeShelved = includeShelved
        }

    var useSubDirs: Boolean
        get() = myState.useSubDirs
        internal set(useSubDirs) {
            myState.useSubDirs = useSubDirs
        }

    companion object {

        fun getInstance(project: Project): Settings {
            return ServiceManager.getService(project, Settings::class.java)
        }
    }
}
