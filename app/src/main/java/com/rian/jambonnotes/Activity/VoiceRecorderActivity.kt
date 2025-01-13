package com.rian.jambonnotes.Activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.rian.jambonnotes.R
import com.rian.jambonnotes.databinding.ActivityVoicerecorderBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

const val  REQUEST_CODE = 200
class VoiceRecorderActivity : AppCompatActivity(), Timer.OnTimerTickListener {
    private lateinit var amplitude: ArrayList<Float>
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false
    private lateinit var binding: ActivityVoicerecorderBinding
    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var filename = ""
    private var isRecording = false
    private var isPaused = false
    private lateinit var vibrator: Vibrator
    private lateinit var timer: Timer
    private lateinit var  filenameinput : com.google.android.material.textfield.TextInputEditText
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var buttonCancel:MaterialButton
    private lateinit var buttonOK:MaterialButton





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoicerecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buttonCancel    =findViewById(R.id.btnCancel)
        buttonOK        =findViewById(R.id.btnOk)
        filenameinput   =findViewById(R.id.filenameinput)



        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        val bottomSheet     =findViewById<LinearLayout>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        timer = Timer(this)
        vibrator= getSystemService(Context.VIBRATOR_SERVICE) as Vibrator



        binding.btnRecord.setOnClickListener {
            when {
                isPaused -> resumeRecorder()
                isRecording -> pauseRecorder()
                else -> startRecording()

            }
            vibrator.vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE))
        }
        binding.btnList.setOnClickListener{
            //TODO
            Toast.makeText(this,"List Button",Toast.LENGTH_SHORT).show()
        }
        binding.btnDone.setOnClickListener{
            stopRecorder()
            Toast.makeText(this,"Record Saved",Toast.LENGTH_SHORT).show()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetBG.visibility = View.VISIBLE
            filenameinput.setText(filename)

            //Kirim path ke homescreen




        }
        buttonCancel.setOnClickListener{
            File("$dirPath$filename.mp3").delete()
            dissmiss()

        }
        buttonOK.setOnClickListener{
            dissmiss()
            save()
            val intent = Intent(this, HomeScreenActivity::class.java)
            intent.putExtra("AUDIO_PATH", "$dirPath$filename.mp3")
            startActivity(intent)
        }
        binding.bottomSheetBG.setOnClickListener{
            File("$dirPath$filename.mp3").delete()
            dissmiss()
        }


         binding.btnDelete.setOnClickListener{
            stopRecorder()
            File("$dirPath$filename.mp3")
            Toast.makeText(this,"Record Deleted",Toast.LENGTH_SHORT).show()

        }
        binding.btnDelete.isClickable=false
    }
    private fun save(){
        val newFilename=filenameinput.text.toString()
        if(newFilename!=filename){
            var newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)


        }
    }
    private fun dissmiss(){
        binding.bottomSheetBG.visibility=View.GONE
        hideKeyboard(filenameinput)

        android.os.Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        },100)
    }
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    )

    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun pauseRecorder() {
        recorder.pause()
        isPaused = true
        binding.btnRecord.setImageResource(R.drawable.ic_record)

        timer.pause()
    }

    private fun resumeRecorder() {
        recorder.resume()
        isPaused = false
        binding.btnRecord.setImageResource(R.drawable.ic_pause)

        timer.start()
    }

    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        //start recording

        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"
        var simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        var date: String = simpleDateFormat.format(Date())
        filename = "audio_record_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")
            try {
                prepare()
            } catch (e: IOException) {
            }

            start()
        }
        // Perbarui UI saat rekaman dimulai
        binding.btnRecord.setImageResource(R.drawable.ic_pause)
        isRecording = true
        isPaused = false

        timer.start()
        binding.btnDelete.isClickable=true
        binding.btnDelete.setImageResource(R.drawable.ic_delete)

        binding.btnList.visibility=View.GONE
        binding.btnDone.visibility=View.VISIBLE



    }
    private fun stopRecorder(){
        timer.stop()
        recorder.apply {
            stop()
            release()
        }
        isPaused=false
        isRecording=false

        binding.btnList.visibility =View.VISIBLE
        binding.btnDone.visibility=View.GONE
        binding.btnDelete.isClickable=false
        binding.btnDelete.setImageResource(R.drawable.ic_delete_disable)
        binding.btnRecord.setImageResource(R.drawable.ic_record)
        binding.tvTimer.text="00:00:00"
        amplitude=binding.waveformview.clear()
    }
    override fun onTimerTick(duration: String) {
        binding.tvTimer.text = duration
        binding.waveformview.addAmplitude(recorder.maxAmplitude.toFloat())
    }
}