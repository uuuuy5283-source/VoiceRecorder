package com.example.voicerecorder

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.voicerecorder.databinding.ActivityMainBinding
import java.io.File
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val adapter = RecAdapter(::play, ::share, ::confirmDelete)
    private var player: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val ticker = object : Runnable {
        override fun run() {
            updateButtons()
            handler.postDelayed(this, 500)
        }
    }

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        if (res[Manifest.permission.RECORD_AUDIO] != true) {
            toast("لازم تدي إذن الميكروفون عشان التسجيل يشتغل")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.rvRecordings.layoutManager = LinearLayoutManager(this)
        b.rvRecordings.adapter = adapter
        b.btnRecord.setOnClickListener { toggleRecording() }
        b.btnSchedule.setOnClickListener { pickSchedule() }

        askPermissions()
    }

    override fun onResume() {
        super.onResume()
        loadList()
        handler.post(ticker)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    private fun askPermissions() {
        val needed = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) needed += Manifest.permission.POST_NOTIFICATIONS
        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) permLauncher.launch(missing.toTypedArray())
    }

    private fun hasMic(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private fun toggleRecording() {
        if (RecordingService.isRunning) {
            RecordingService.stop(this)
            return
        }
        if (!hasMic()) {
            askPermissions()
            return
        }
        try {
            RecordingService.start(this)
        } catch (e: Exception) {
            toast("ما قدرتش أبدأ التسجيل، جرب تاني")
        }
    }

    private fun updateButtons() {
        val rec = RecordingService.isRunning
        b.btnRecord.text = if (rec) "إيقاف" else "تسجيل"
        b.tvStatus.text = if (rec) "جاري التسجيل..." else "جاهز"
    }

    private fun loadList() {
        val files = Storage.dir(this).listFiles()
            ?.filter { it.extension == "m4a" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        adapter.submit(files)
    }

    private fun pickSchedule() {
        val now = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            val start = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
            }
            val options = arrayOf("15 دقيقة", "30 دقيقة", "60 دقيقة", "120 دقيقة")
            val minutes = intArrayOf(15, 30, 60, 120)
            AlertDialog.Builder(this)
                .setTitle("مدة التسجيل")
                .setItems(options) { _, i ->
                    Scheduler.schedule(this, start.timeInMillis, minutes[i])
                    toast("هيسجل الساعة %02d:%02d لمدة %d دقيقة".format(h, m, minutes[i]))
                }
                .show()
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
    }

    private fun play(f: File) {
        try {
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(f.absolutePath)
                prepare()
                start()
                setOnCompletionListener { mp ->
                    mp.release()
                    if (player === mp) player = null
                }
            }
        } catch (e: Exception) {
            toast("ما قدرتش أشغل الملف")
        }
    }

    private fun share(f: File) {
        try {
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
            val i = Intent(Intent.ACTION_SEND).apply {
                type = "audio/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(i, "مشاركة التسجيل"))
        } catch (e: Exception) {
            toast("ما قدرتش أشارك الملف")
        }
    }

    private fun confirmDelete(f: File) {
        AlertDialog.Builder(this)
            .setMessage("تحذف ${f.name}؟")
            .setPositiveButton("حذف") { _, _ -> if (f.delete()) loadList() }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}
