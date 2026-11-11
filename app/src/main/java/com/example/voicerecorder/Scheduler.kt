package com.example.voicerecorder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object Scheduler {
    const val ACTION_START = "com.example.voicerecorder.SCHED_START"
    const val ACTION_STOP = "com.example.voicerecorder.SCHED_STOP"
    const val EXTRA_MINUTES = "minutes"
    private const val REQ_START = 1001
    private const val REQ_STOP = 1002

    fun schedule(ctx: Context, startAt: Long, minutes: Int) {
        val pi = pending(ctx, REQ_START, Intent(ACTION_START).putExtra(EXTRA_MINUTES, minutes))
        setAlarm(ctx, startAt, pi)
    }

    fun scheduleStop(ctx: Context, stopAt: Long) {
        setAlarm(ctx, stopAt, pending(ctx, REQ_STOP, Intent(ACTION_STOP)))
    }

    private fun pending(ctx: Context, req: Int, intent: Intent): PendingIntent {
        intent.setClass(ctx, ScheduleReceiver::class.java)
        return PendingIntent.getBroadcast(
            ctx, req, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setAlarm(ctx: Context, at: Long, pi: PendingIntent) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            // لو المستخدم ما فعلش إذن "المنبهات والتذكيرات" التوقيت ممكن يتأخر دقايق
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }
}
