package com.rian.jambonnotes.Activity


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.rian.jambonnotes.R
import com.rian.jambonnotes.databinding.AdapterNoteBinding
import com.rian.jambonnotes.entity.Note


class NoteAdapter(
    private val notes: ArrayList<Note>,
    private var listener: OnAdapterListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        // Menggunakan ViewBinding untuk mengakses view
        val binding = AdapterNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding) // Mengirim binding ke ViewHolder
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]

        // Mengakses view melalui binding
        holder.binding.textTitle.text = note.title // Set judul catatan
        holder.binding.textTitle.setOnClickListener {
            listener.onClick(note) // Menangani klik pada judul
        }
        holder.binding.iconEdit.setOnClickListener {
            listener.onUpdate(note) // Menangani klik untuk edit
        }
        holder.binding.iconTong.setOnClickListener {
            listener.onDelete(note) // Menangani klik untuk delete
        }
    }

    class NoteViewHolder(val binding: AdapterNoteBinding) : RecyclerView.ViewHolder(binding.root)

    fun setData(newList: List<Note>) {
        notes.clear()
        notes.addAll(newList)
        notifyDataSetChanged()
    }

    interface OnAdapterListener {
        fun onClick(note: Note)
        fun onUpdate(note: Note)
        fun onDelete(note: Note)
    }
}
