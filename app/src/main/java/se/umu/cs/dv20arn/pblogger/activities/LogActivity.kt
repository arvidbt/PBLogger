package se.umu.cs.dv20arn.pblogger.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import se.umu.cs.dv20arn.pblogger.Main
import se.umu.cs.dv20arn.pblogger.R
import se.umu.cs.dv20arn.pblogger.databinding.ActivityLogBinding
import se.umu.cs.dv20arn.pblogger.objects.Keys

//TODO Save state

class LogActivity() : AppCompatActivity(), Parcelable{

    private lateinit var keys: Keys
    private lateinit var sharedPref: SharedPreferences
    private lateinit var binding: ActivityLogBinding
    private lateinit var videoKey: String
    private var pbCounts = false

    private var resultLauncher = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                videoKey = data.data.toString()
                previewVideo(data.data)
                saveVideoKey(data.data)
            }
        }
    }

    constructor(parcel: Parcel) : this() {
        pbCounts = parcel.readByte() != 0.toByte()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        if(savedInstanceState != null) {
            TODO()
        }
        keys = Keys()
        sharedPref = applicationContext.getSharedPreferences(keys.PREF_KEY, Context.MODE_PRIVATE)

        populateSpinner()
        recordVideo()
        onSavePR()
        onCalculate1RM()
    }

    private fun onCalculate1RM() {
        binding.CALC1RM.setOnClickListener {
            startActivity(Intent(this, CalculatorActivity::class.java))

        }
    }

    private fun previewVideo(data: Uri?) {
        binding.PREVIEWVIDEO.setVideoPath(data.toString())
        binding.PREVIEWVIDEO.start()
    }

    private fun saveVideoKey(data: Uri?) {
        when(getLiftType()) {
            "SQUAT" -> keys.SQUAT_VIDEO_KEY = data.toString()
            "DEADLIFT" -> keys.DEADLIFT_VIDEO_KEY = data.toString()
            "BENCHPRESS" -> keys.BENCHPRESS_VIDEO_KEY = data.toString()
        }
    }

    private fun populateSpinner() {
        ArrayAdapter.createFromResource(this, R.array.LIFTS, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.LIFTSPINNER.adapter = adapter
            }
    }

    private fun recordVideo() {
        binding.RECORDVIDEO.setOnClickListener {
            resultLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
        }
    }

    private fun getLiftType(): String {
        return binding.LIFTSPINNER.selectedItem as String
    }


    private fun getWeight(weightKey: String): Int {
        val weight = binding.WEIGHTINPUT.text.toString().trim()
        pbCounts = false
        return if(weight == "") {
            Toast.makeText(this, "No weight input, default to last PB weight.", Toast.LENGTH_SHORT).show()
            sharedPref.getInt(weightKey, 0)
        } else {
            pbCounts = true
            weight.toInt()
        }
    }

    private fun onSavePR() {
        binding.SAVEPRBTN.setOnClickListener {
            when (getLiftType()) {
                "SQUAT"      -> logLift(keys.SQUAT_KEY, keys.SQUAT_WEIGHT_KEY, keys.SQUAT_VIDEO_ID_KEY, keys.SQUAT_VIDEO_KEY)
                "BENCHPRESS" -> logLift(keys.BENCHPRESS_KEY, keys.BENCHPRESS_WEIGHT_KEY, keys.BENCHPRESS_VIDEO_ID_KEY, keys.BENCHPRESS_VIDEO_KEY)
                "DEADLIFT"   -> logLift(keys.DEADLIFT_KEY, keys.DEADLIFT_WEIGHT_KEY, keys.DEADLIFT_VIDEO_ID_KEY, keys.BENCHPRESS_VIDEO_KEY)
            }
            Toast.makeText(this, "PB logged!", Toast.LENGTH_SHORT).show()
            incrementPBlog()
            startActivity(Intent(this, Main::class.java))
        }
    }

    private fun logLift(liftKey: String, weightKey: String, videoIDKey: String, videoKey: String) {
        with (sharedPref.edit()) {
            putString(liftKey, getLiftType())
            putInt(weightKey, getWeight(weightKey))
            putString(videoIDKey, videoKey)
            apply()
        }
    }

    private fun incrementPBlog() {
        if(pbCounts) {
            var int = sharedPref.getInt(keys.PB_LOGGED_KEY, 0)
            int++
            sharedPref.edit().putInt(keys.PB_LOGGED_KEY, int).apply()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (pbCounts) 1 else 0)
        parcel.writeInt(binding.WEIGHTINPUT.text.toString().toInt())
        parcel.writeString(binding.LIFTSPINNER.selectedItem.toString())
        parcel.writeString(videoKey)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LogActivity> {
        override fun createFromParcel(parcel: Parcel): LogActivity {
            return LogActivity(parcel)
        }

        override fun newArray(size: Int): Array<LogActivity?> {
            return arrayOfNulls(size)
        }
    }

}