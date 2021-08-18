package edu.bupt.jetcapture.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import edu.bupt.jetcapture.MainActivity
import edu.bupt.jetcapture.R

class CPVpnService: VpnService() {
    companion object{
        const val ACTION_START = "edu.bupt.jetcapture.action.Start"
        const val ACTION_STOP = "edu.bupt.jetcapture.action.Stop"
        const val NOTIFICATION_ID = 12138
        const val NOTIFICATION_CHANNEL_ID = "edu.bupt.wiregato.NOTIFICATION_CHANNEL_ID"
    }

    var isRunning = false
    lateinit var capturePackageThread: CPThread

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_NOT_STICKY
        }
        when (intent.action) {
            ACTION_START -> {
                if (!isRunning) {
                    prepareCapture()
                    startForeground(NOTIFICATION_ID, createNotification())
                    isRunning = true
                }
            }
            ACTION_STOP -> {
                stopSelf()
                isRunning = false
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun prepareCapture() {
        capturePackageThread = CPThread(this, CPVpnManager.getConfig())
        capturePackageThread.start()
    }


    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.action = Intent.ACTION_MAIN
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        var builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        return builder
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_name))
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}