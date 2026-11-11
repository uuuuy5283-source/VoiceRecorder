package com.example.voicerecorder

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Storage {
    // مجلد Recordings داخل مساحة التطبيق (لا يحتاج أي إذن تخزين)
    fun dir(ctx: Context): File =
        File(ctx.getExternalFilesDir(null) ?: ctx.filesDir, "Recordings").apply { mkdirs() }

    fun newFile(ctx: Context): File {
        val name = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        return File(dir(ctx), "$name.m4a")
    }
}
