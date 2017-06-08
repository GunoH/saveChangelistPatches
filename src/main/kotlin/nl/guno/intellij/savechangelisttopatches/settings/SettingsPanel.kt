package nl.guno.intellij.savechangelisttopatches.settings

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File

import javax.swing.*

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import nl.guno.intellij.savechangelisttopatches.MessageResources

class SettingsPanel internal constructor(project: Project) {
    private val mySettings: Settings = Settings.getInstance(project)

    private var rootComponent: JPanel? = null
    private var saveLocationField: TextFieldWithBrowseButton? = null
    private var saveOnCloseField: JCheckBox? = null
    private var saveOnCloseLabel: JLabel? = null
    private var includeShelvedField: JCheckBox? = null
    private var includeShelvedLabel: JLabel? = null
    private var useSubDirsField: JCheckBox? = null
    private var useSubDirsLabel: JLabel? = null

    init {
        reset()
        saveLocationField!!.addActionListener { _ ->
            val currentFileLocation = saveLocationField!!.text

            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            val toSelect = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(File(currentFileLocation))

            FileChooser.chooseFile(descriptor, project, toSelect) { file ->
                if (file != null && file.isDirectory && file.isWritable) {
                    saveLocationField!!.text = FileUtil.toSystemDependentName(file.path)
                }
            }
        }

        saveOnCloseLabel!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                saveOnCloseField!!.doClick()
            }
        })
        includeShelvedLabel!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                includeShelvedField!!.doClick()
            }
        })
        useSubDirsLabel!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                useSubDirsField!!.doClick()
            }
        })
    }

    internal fun reset() {
        saveLocationField!!.text = mySettings.saveLocation
        saveOnCloseField!!.isSelected = mySettings.saveOnClose
        includeShelvedField!!.isSelected = mySettings.includeShelved
        useSubDirsField!!.isSelected = mySettings.useSubDirs
    }

    internal val isModified: Boolean
        get() = !Comparing.equal(mySettings.saveLocation, saveLocationField!!.text.trim { it <= ' ' })
                || mySettings.saveOnClose != saveOnCloseField!!.isSelected
                || mySettings.includeShelved != includeShelvedField!!.isSelected
                || mySettings.useSubDirs != useSubDirsField!!.isSelected

    @Throws(ConfigurationException::class)
    internal fun apply() {
        val saveLocation = saveLocationField!!.text.trim { it <= ' ' }

        val file = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(File(saveLocation))
        if (file == null || !file.isDirectory) {
            throw ConfigurationException(
                    MessageResources.message("configuration.folderChooser.error.invalidDirectory.text"))
        }
        if (saveOnCloseField!!.isSelected && StringUtil.isEmpty(saveLocation)) {
            throw ConfigurationException(
                    MessageResources.message("configuration.folderChooser.error.directoryMandatory.text"))
        }
        if (!file.isWritable) {
            throw ConfigurationException(
                    MessageResources.message("configuration.folderChooser.error.directoryNotWritable.text"))
        }

        mySettings.saveLocation = saveLocation
        mySettings.saveOnClose = saveOnCloseField!!.isSelected
        mySettings.includeShelved = includeShelvedField!!.isSelected
        mySettings.useSubDirs = useSubDirsField!!.isSelected
    }

    internal val panel: JComponent?
        get() = rootComponent
}
