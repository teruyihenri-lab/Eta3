package io.github.mangi.eta.agent.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import io.github.mangi.eta.R

/** Overlay 不等同于前台 Activity；只在可见浮窗采集或播放音频期间提升服务类型。 */
internal class AssistantSpeechForeground(private val service: Service) {
    fun recording() = start(ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE, service.getString(R.string.speech_foreground_listening))
    fun playback() = start(ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK, service.getString(R.string.speech_foreground_reading))

    private fun start(type: Int, title: String) {
        service.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, service.getString(R.string.speech_foreground_channel), NotificationManager.IMPORTANCE_LOW),
        )
        val stop = PendingIntent.getService(service, 0,
            Intent(service, EtaAssistantOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = Notification.Builder(service, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(service.getString(R.string.speech_foreground_hint))
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .addAction(Notification.Action.Builder(null, service.getString(R.string.action_stop), stop).build())
            .build()
        try {
            service.startForeground(NOTIFICATION_ID, notification, type)
        } catch (_: RuntimeException) {
            throw SpeechFailure(SpeechErrorCode.PERMISSION, service.getString(R.string.speech_foreground_permission))
        }
    }

    fun stop() { service.stopForeground(Service.STOP_FOREGROUND_REMOVE) }

    companion object {
        const val ACTION_STOP = "io.github.mangi.eta.STOP_ASSISTANT_SPEECH"
        private const val CHANNEL = "eta_assistant_speech"
        private const val NOTIFICATION_ID = 4203
    }
}
