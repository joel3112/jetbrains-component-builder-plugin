package org.joel3112.componentbuilder.utils

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project


class NotificationUtils {
    companion object {
        private val notificationGroup = NotificationGroupManager.getInstance()
            .getNotificationGroup("ComponentBuilder")

        fun notifyError(message: String, project: Project) {
            notificationGroup
                .createNotification(message, NotificationType.ERROR)
                .notify(project)
        }

        fun notifyInfo(message: String, project: Project) {
            notificationGroup
                .createNotification(message, NotificationType.INFORMATION)
                .notify(project)
        }
    }
}
