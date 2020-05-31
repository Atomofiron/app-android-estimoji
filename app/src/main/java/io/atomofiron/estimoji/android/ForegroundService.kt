package io.atomofiron.estimoji.android

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.atomofiron.common.util.ChannelUtil
import io.atomofiron.estimoji.R
import io.atomofiron.estimoji.logI
import io.atomofiron.estimoji.screen.root.RootActivity
import io.atomofiron.estimoji.util.Const

class ForegroundService : IntentService("NotificationService") {

    override fun onCreate() {
        super.onCreate()
        logI("onCreate")
        startForeground()
    }

    override fun onDestroy() {
        super.onDestroy()
        logI("onDestroy")
        stopForeground(true)
    }

    private fun startForeground() {
        ChannelUtil.id(Const.FOREGROUND_NOTIFICATION_CHANNEL_ID)
                .name(getString(R.string.foreground_notification_name))
                .importance(IMPORTANCE_LOW)
                .fix(this)

        val intent = Intent(this, RootActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, Const.FOREGROUND_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val color = ContextCompat.getColor(this, R.color.colorPrimary)
        val notification = NotificationCompat.Builder(this, Const.FOREGROUND_NOTIFICATION_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(getString(R.string.session_is_active))
                .setSmallIcon(R.drawable.ic_cool)
                .setColor(color)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(Const.FOREGROUND_NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        logI("onBind")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logI("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onHandleIntent(intent: Intent?) = Unit
}