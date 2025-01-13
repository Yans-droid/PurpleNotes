package com.rian.jambonnotes.Activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rian.jambonnotes.databinding.ActivityHomescreenBinding
import com.rian.jambonnotes.R
import com.rian.jambonnotes.entity.Constant
import com.rian.jambonnotes.entity.Note
import com.rian.jambonnotes.entity.NoteDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class HomeScreenActivity : AppCompatActivity() {

    private lateinit var cam: ImageButton
    private lateinit var imageView: ImageView
    private lateinit var btnAudio: ImageButton
    private lateinit var playButton: Button
    private lateinit var btnSaveNote: ImageButton
    private var imagePath: String? = null
    private var audioPath: String? = null
    private val db by lazy { NoteDB(this) }
    private var noteId = 0
    private lateinit var textJudul: EditText
    private lateinit var editText: EditText
    lateinit var noteAdapter: NoteAdapter

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding: ActivityHomescreenBinding

    companion object {
        const val REQUEST_CODE_IMAGE = 1
        const val REQUEST_CODE_AUDIO = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomescreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi komponen UI
        cam = findViewById(R.id.button_image)
        imageView = findViewById(R.id.imgView)
        btnAudio = findViewById(R.id.buttonVoiceNote)
        playButton = findViewById(R.id.btnplay)
        mediaPlayer = MediaPlayer()
        textJudul = findViewById(R.id.textJudul)
        editText = findViewById(R.id.editText)
        btnSaveNote = findViewById(R.id.button_save)

        setupListener()
        setupView()

        // Meminta izin untuk kamera dan penyimpanan
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 0
            )
        }

        // Menangani klik untuk mengambil gambar
        cam.setOnClickListener {
            takePict()
        }

        // Menangani klik untuk merekam suara
        btnAudio.setOnClickListener {
            val intent = Intent(this, VoiceRecorderActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_AUDIO)
        }

        // Memainkan audio jika ada path audio yang diberikan
        val audioPath = intent.getStringExtra("AUDIO_PATH")
        if (audioPath != null) {
            playButton.visibility = View.VISIBLE
            playButton.setOnClickListener {
                try {
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(audioPath)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    playButton.text = "Stop"
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupView() {
        // Memastikan supportActionBar tidak null
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)  // Menambahkan tombol back di action bar
            when (intentType()) {
                Constant.TYPE_CREATE -> {
                    it.title = "BUAT BARU" // Menetapkan judul untuk mode Create
                    binding.buttonSave.visibility = View.VISIBLE  // Menampilkan tombol save
                    binding.buttonUpdate.visibility = View.GONE   // Menyembunyikan tombol update
                }

                Constant.TYPE_READ -> {
                    it.title = "BACA" // Menetapkan judul untuk mode Read
                    binding.buttonSave.visibility = View.GONE  // Menyembunyikan tombol save
                    binding.buttonUpdate.visibility = View.GONE  // Menyembunyikan tombol update
                    getNote()  // Mengambil catatan dari database
                }

                Constant.TYPE_UPDATE -> {
                    it.title = "EDIT"  // Menetapkan judul untuk mode Edit
                    binding.buttonSave.visibility = View.GONE  // Menyembunyikan tombol save
                    binding.buttonUpdate.visibility = View.VISIBLE  // Menampilkan tombol update
                    getNote()  // Mengambil catatan untuk diubah
                }
            }
        }
    }

    private fun setupListener() {
        // Listener untuk tombol save (untuk menambah catatan baru)
        binding.buttonSave.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                // Menyimpan catatan baru
                val note = Note(
                    0,  // ID ditetapkan ke 0, database yang akan menambah ID otomatis
                    binding.textJudul.text.toString(),
                    binding.editText.text.toString(),
                    imagePath, // Menyimpan path gambar
                    audioPath  // Menyimpan path audio
                )
                db.noteDao().addNote(note)  // Menambahkan catatan ke database
                withContext(Dispatchers.Main) {
                    finish()  // Kembali ke activity sebelumnya setelah menyimpan
                }
            }
        }

        // Listener untuk tombol update (untuk mengubah catatan yang ada)
        binding.buttonUpdate.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                // Memperbarui catatan yang sudah ada
                val note = Note(
                    noteId,  // Menggunakan ID catatan yang ada
                    binding.textJudul.text.toString(),
                    binding.editText.text.toString(),
                    imagePath, // Menyimpan path gambar
                    audioPath  // Menyimpan path audio
                )
                db.noteDao().updateNote(note)  // Memperbarui catatan di database
                withContext(Dispatchers.Main) {
                    finish()  // Kembali ke activity sebelumnya setelah memperbarui
                }
            }
        }
    }

    private fun getNote() {
        // Mengambil noteId dari intent
        noteId = intent.getIntExtra("note_id", 0)  // Mendapatkan note_id dari intent
        CoroutineScope(Dispatchers.IO).launch {
            // Mengambil data catatan dari database berdasarkan noteId
            val notes = db.noteDao().getNote(noteId)
            if (notes.isNotEmpty()) {
                val note = notes[0]
                withContext(Dispatchers.Main) {
                    // Mengisi UI dengan data catatan yang ditemukan
                    binding.textJudul.setText(note.title)
                    binding.editText.setText(note.note)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()  // Kembali ke activity sebelumnya ketika tombol back ditekan
        return super.onSupportNavigateUp()
    }

    private fun intentType(): Int {
        // Mendapatkan tipe intent dari intent yang dikirim
        return intent.getIntExtra("intent_type", 0)
    }

    // Menangani pengambilan gambar
    private fun takePict() {
        val picIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(picIntent, REQUEST_CODE_IMAGE)
    }

    // Menangani hasil pengambilan gambar dan audio
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE -> {
                    val photo = data?.extras?.get("data") as? Bitmap
                    imageView.setImageBitmap(photo)

                    // Menyimpan path gambar sementara atau menyimpannya ke file
                    val file = File(cacheDir, "selected_image.jpg")
                    try {
                        val outStream = FileOutputStream(file)
                        photo?.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                        outStream.close()
                        imagePath = file.absolutePath
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                REQUEST_CODE_AUDIO -> {
                    audioPath = data?.getStringExtra("audioPath")
                }
            }
        }
    }

    // Menangani penghapusan resource MediaPlayer saat activity dihancurkan
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
