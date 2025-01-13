package com.rian.jambonnotes.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rian.jambonnotes.R
import com.rian.jambonnotes.entity.Note


import com.rian.jambonnotes.entity.NoteDB
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var newFileButton: ImageButton
    private lateinit var noteAdapter: NoteAdapter
    private val db by lazy { NoteDB(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newFileButton = findViewById(R.id.newFileButton)
        setupRecyclerView()

        newFileButton.setOnClickListener {
            val intent = Intent(this, HomeScreenActivity::class.java)
            startActivity(intent)
        }

        loadData()
    }

    private fun loadData() {
        // Menjalankan proses async untuk memuat data
        CoroutineScope(Dispatchers.IO).launch {
            val notes = db.noteDao().getNotes() // Ambil semua catatan dari database
            withContext(Dispatchers.Main) {
                noteAdapter.setData(notes) // Update adapter dengan data baru
                noteAdapter.notifyDataSetChanged() // Notifikasi ke adapter untuk update UI
            }
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(arrayListOf(), object : NoteAdapter.OnAdapterListener {
            override fun onClick(note: Note) {
                // Navigasi ke detail atau tampilan catatan berdasarkan klik
                val intent = Intent(this@MainActivity, HomeScreenActivity::class.java)
                intent.putExtra("intent_type", 1) // 1 untuk view/read mode
                intent.putExtra("note_id", note.id)
                startActivity(intent)
            }

            override fun onUpdate(note: Note) {
                // Navigasi ke edit activity untuk mengupdate catatan
                val intent = Intent(this@MainActivity, HomeScreenActivity::class.java)
                intent.putExtra("intent_type", 2) // 2 untuk update mode
                intent.putExtra("note_id", note.id)
                startActivity(intent)
            }

            override fun onDelete(note: Note) {
                // Hapus catatan setelah konfirmasi
                deleteAlert(note)
            }
        })

        // Set RecyclerView dan adapter
        val recyclerView = findViewById<RecyclerView>(R.id.list_note)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = noteAdapter
    }

    private fun deleteAlert(note: Note) {
        val dialog = AlertDialog.Builder(this)
        dialog.apply {
            setTitle("Konfirmasi Hapus")
            setMessage("Yakin hapus ${note.title}?")
            setNegativeButton("Batal") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            setPositiveButton("Hapus") { dialogInterface, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.noteDao().deleteNote(note) // Hapus catatan dari database
                    withContext(Dispatchers.Main) {
                        loadData() // Memuat data ulang setelah penghapusan
                    }
                    dialogInterface.dismiss()
                }
            }
        }
        dialog.show()
    }

    // Fungsi untuk load data dan refresh setelah aksi di database
    override fun onResume() {
        super.onResume()
        loadData() // Pastikan data selalu diperbarui saat kembali ke activity ini
    }
}
