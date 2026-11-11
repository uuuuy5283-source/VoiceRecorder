package com.example.voicerecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        when (intent.action) {
            Scheduler.ACTION_START -> {
                val minutes = intent.getIntExtra(Scheduler.EXTRA_MINUTES, 60)
                try {
                    RecordingService.start(ctx)
                    Scheduler.scheduleStop(ctx, System.currentTimeMillis() + minutes * 60_000L)
                } catch (e: Exception) {
                    // أندرويد ممكن يمنع بدء الخدمة من الخلفية في حالات معينة
                }
            }
            Scheduler.ACTION_STOP -> {
                try { RecordingService.stop(ctx) } catch (_: Exception) {}
            }
        }
    }
}
