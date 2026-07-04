package com.yuandao.music.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.yuandao.music.R
import com.yuandao.music.YuandaoApp

class YuandaoPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundBootstrapIfNeeded()
        val playbackController = (application as YuandaoApp).container.playbackController
        mediaSession = MediaSession.Builder(this, playbackController.sessionPlayer)
            .setId(SESSION_ID)
            .setCallback(sessionCallback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession.takeIf { isControllerAllowed(controllerInfo) }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || shouldStopWhenTaskIsRemoved(player)) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun startForegroundBootstrapIfNeeded() {
        if (!PlaybackServiceStartupPolicy.requiresImmediateForegroundBootstrap(Build.VERSION.SDK_INT)) return

        ensureNotificationChannel()
        val notification = NotificationCompat.Builder(this, PlaybackServiceStartupPolicy.BOOTSTRAP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("本地播放服务已准备")
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        ServiceCompat.startForeground(
            this,
            PlaybackServiceStartupPolicy.BOOTSTRAP_NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
        )
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val existing = manager.getNotificationChannel(PlaybackServiceStartupPolicy.BOOTSTRAP_CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            PlaybackServiceStartupPolicy.BOOTSTRAP_CHANNEL_ID,
            "播放",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "本地音乐播放状态"
        }
        manager.createNotificationChannel(channel)
    }

    private fun shouldStopWhenTaskIsRemoved(player: Player): Boolean =
        PlaybackServiceStopPolicy.shouldStopWhenTaskRemoved(
            playWhenReady = player.playWhenReady,
            mediaItemCount = player.mediaItemCount,
            playbackState = player.playbackState,
        )

    private val sessionCallback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult =
            if (isControllerAllowed(controller)) {
                super.onConnect(session, controller)
            } else {
                MediaSession.ConnectionResult.reject()
            }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun isControllerAllowed(controller: MediaSession.ControllerInfo): Boolean =
        MediaSessionAccessPolicy.allowsController(
            controllerPackageName = controller.packageName,
            appPackageName = packageName,
            trusted = controller.isTrusted,
        )

    companion object {
        private const val SESSION_ID = "yuandao_music_playback"

        fun start(context: Context) {
            val intent = Intent(context, YuandaoPlaybackService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, YuandaoPlaybackService::class.java)
            context.stopService(intent)
        }
    }
}
