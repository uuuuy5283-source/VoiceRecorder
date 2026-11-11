package com.example.voicerecorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class RecAdapter(
    private val onPlay: (File) -> Unit,
    private val onShare: (File) -> Unit,
    private val onDelete: (File) -> Unit
) : RecyclerView.Adapter<RecAdapter.VH>() {

    private val items = mutableListOf<File>()

    fun submit(list: List<File>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvName)
        val play: Button = v.findViewById(R.id.btnPlay)
        val share: Button = v.findViewById(R.id.btnShare)
        val del: Button = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_recording, parent, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val f = items[position]
        h.name.text = "${f.nameWithoutExtension}  (${f.length() / 1024} KB)"
        h.play.setOnClickListener { onPlay(f) }
        h.share.setOnClickListener { onShare(f) }
        h.del.setOnClickListener { onDelete(f) }
    }

    override fun getItemCount(): Int = items.size
}
