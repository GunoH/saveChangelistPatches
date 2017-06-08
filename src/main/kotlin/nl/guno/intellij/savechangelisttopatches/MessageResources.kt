package nl.guno.intellij.savechangelisttopatches

import java.util.ResourceBundle

import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

import com.intellij.CommonBundle

object MessageResources {
    @NonNls
    private const val BUNDLE_NAME = "nl.guno.intellij.savechangelisttopatches.MessageResources"
    private val BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME)

    fun message(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg params: Any): String {
        return CommonBundle.message(BUNDLE, key, *params)

    }
}
