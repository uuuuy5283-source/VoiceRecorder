package com.example.voicerecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import java.io.File

class RecordingService : Service() {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    companion object {
        const val ACTION_START = "com.example.voicerecorder.START"
        const val ACTION_STOP = "com.example.voicerecorder.STOP"
        private const val CHANNEL_ID = "recorder_channel"
        private const val NOTIF_ID = 1

        @Volatile
        var isRunning = false

        fun start(ctx: Context) {
            ContextCompat.startForegroundService(
                ctx, Intent(ctx, RecordingService::class.java).setAction(ACTION_START)
            )
        }

        fun stop(ctx: Context) {
            ctx.startService(Intent(ctx, RecordingService::class.java).setAction(ACTION_STOP))
        }
    }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "حالة المسجل", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopRecording()
            stopSelf()
            return START_NOT_STICKY
        }
        if (isRunning) return START_NOT_STICKY

        // لازم نستدعي startForeground فوراً بعد startForegroundService
        try {
            ServiceCompat.startForeground(
                this, NOTIF_ID, buildNotification(),
                if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0
            )
        } catch (e: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!startRecording()) stopSelf()
        return START_NOT_STICKY
    }

    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= 31) MediaRecorder(this) else legacyRecorder()

    @Suppress("DEPRECATION")
    private fun legacyRecorder(): MediaRecorder = MediaRecorder()

    private fun startRecording(): Boolean {
        val file = Storage.newFile(this)
        var r: MediaRecorder? = null
        return try {
            r = createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
                setOutputFile(file.absolutePath)
                setOnErrorListener { _, _, _ ->
                    stopRecording()
                    stopSelf()
                }
                prepare()
                start()
            }
            recorder = r
            currentFile = file
            isRunning = true
            true
        } catch (e: Exception) {
            try { r?.release() } catch (_: Exception) {}
            file.delete()
            isRunning = false
            false
        }
    }

    private fun stopRecording() {
        val r = recorder ?: return
        recorder = null
        isRunning = false
        try {
            r.stop()
        } catch (e: Exception) {
            // التسجيل كان قصير جداً ومفيش بيانات تتحفظ
            currentFile?.delete()
        }
        try { r.release() } catch (_: Exception) {}
        currentFile = null
    }

    private fun buildNotification(): Notification {
        val openPi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopPi = PendingIntent.getService(
            this, 1, Intent(this, RecordingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("المسجل يعمل")
            .setContentText("اضغط إيقاف لإنهاء التسجيل")
            .setContentIntent(openPi)
            .addAction(android.R.drawable.ic_media_pause, "إيقاف", stopPi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
