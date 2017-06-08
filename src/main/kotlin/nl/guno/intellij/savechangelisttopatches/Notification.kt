package nl.guno.intellij.savechangelisttopatches

import javax.swing.event.HyperlinkListener

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager
import com.intellij.openapi.wm.ToolWindowManager

class Notification(private val project: Project, private val message: String, private val type: MessageType) {

    fun showBalloon(): Notification {
        ToolWindowManager.getInstance(project).notifyByBalloon(ChangesViewContentManager.TOOLWINDOW_ID, type, message)
        return this
    }

    fun showBalloon(hyperlinkListener: HyperlinkListener): Notification {
        ToolWindowManager.getInstance(project).notifyByBalloon(ChangesViewContentManager.TOOLWINDOW_ID, type, message,
                null, hyperlinkListener)
        return this
    }

    fun addToEventLog(): Notification {
        NOTIFICATION_GROUP.createNotification(message, type.toNotificationType()).notify(project)
        return this
    }

    fun addToEventLog(notificationListener: NotificationListener): Notification {
        NOTIFICATION_GROUP.createNotification("", message, type.toNotificationType(), notificationListener).notify(project)
        return this
    }

    companion object {
        private val NOTIFICATION_GROUP = NotificationGroup.logOnlyGroup("SaveChangelistPatches")
    }
}
