package se.umu.cs.dv20arn.pblogger.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
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
    private var videoKey: String = "NO_VIDEO"
    private var pbCounts = false
    private lateinit var bundle: Bundle

    // Launcher for camera
    private var resultLauncher = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                videoKey = data.data.toString()
                saveVideoKey()
            }
        }
    }




    constructor(parcel: Parcel) : this() {
        pbCounts = parcel.readByte() != 0.toByte()
        videoKey = parcel.readString().toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        if(savedInstanceState != null) {
            videoKey = savedInstanceState.getString("VIDEO").toString()
            println("RESTORING $videoKey")
            Toast.makeText(this, "Restore state", Toast.LENGTH_SHORT).show()

        }
        keys = Keys()
        sharedPref = applicationContext.getSharedPreferences(keys.PREF_KEY, Context.MODE_PRIVATE)
        populateSpinner()
        recordVideo()
        onSavePR()
        onCalculate1RM()
        loadLiftInfo(binding.LIFTSPINNER.selectedItem as String)
    }

    /**
     * Starts activity for calculating 1RM.
     */
    private fun onCalculate1RM() {
        binding.CALC1RM.setOnClickListener {
            startActivity(Intent(this, CalculatorActivity::class.java))

        }
    }

    /**
     * Saves the video path as a key.
     */
    private fun saveVideoKey() {
        when(getLiftType()) {
            "SQUAT" -> keys.SQUAT_VIDEO_KEY = videoKey
            "DEADLIFT" -> keys.DEADLIFT_VIDEO_KEY = videoKey
            "BENCHPRESS" -> keys.BENCHPRESS_VIDEO_KEY = videoKey
        }
        keys.RESTORE_VIDEO_KEY = videoKey
    }

    /**
     * Fills spinner with different lift types.
     */
    private fun populateSpinner() {
        ArrayAdapter.createFromResource(this, R.array.LIFTS, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.LIFTSPINNER.adapter = adapter
                binding.LIFTSPINNER.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        loadLiftInfo(binding.LIFTSPINNER.selectedItem as String)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        loadLiftInfo(binding.LIFTSPINNER.selectedItem as String)
                    }


                }
            }
    }


    /**
     * Sends launch request to resultLauncher.
     */
    private fun recordVideo() {
        binding.RECORDVIDEO.setOnClickListener {
            resultLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
        }
    }

    /**
     * Retrieves what lift is currently selected
     */
    private fun getLiftType(): String {
        return binding.LIFTSPINNER.selectedItem as String
    }

    private fun loadLiftInfo(s: String) {
        when(s) {
            "SQUAT" -> loadLift(s, sharedPref.getInt(keys.SQUAT_WEIGHT_KEY, 0))
            "DEADLIFT" -> loadLift(s, sharedPref.getInt(keys.DEADLIFT_WEIGHT_KEY, 0))
            "BENCHPRESS" -> loadLift(s, sharedPref.getInt(keys.BENCHPRESS_WEIGHT_KEY, 0))
        }
    }

    private fun loadLift(s: String, oldPB: Int) {
        binding.newWeight.text = (oldPB * 1.05).toString()
    }


    /**
     * Get weight from EditText input.
     */
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

    /**
     * Saves the the log entry.
     */
    private fun onSavePR() {
        binding.SAVEPRBTN.setOnClickListener {
            incrementTotalGained(getLiftType())
            when (getLiftType()) {
                "SQUAT"      -> logLift(keys.SQUAT_KEY, keys.SQUAT_WEIGHT_KEY, keys.SQUAT_VIDEO_ID_KEY)
                "BENCHPRESS" -> logLift(keys.BENCHPRESS_KEY, keys.BENCHPRESS_WEIGHT_KEY, keys.BENCHPRESS_VIDEO_ID_KEY)
                "DEADLIFT"   -> logLift(keys.DEADLIFT_KEY, keys.DEADLIFT_WEIGHT_KEY, keys.DEADLIFT_VIDEO_ID_KEY)
            }
            incrementPBlog()
            startActivity(Intent(this, Main::class.java))
            finish()
        }
    }

    /**
     * Registers a lift.
     */
    private fun logLift(liftKey: String, weightKey: String, videoIDKey: String) {
        with (sharedPref.edit()) {
            putString(liftKey, getLiftType())
            putInt(weightKey, getWeight(weightKey))
            putString(videoIDKey, videoKey)
            apply()
        }
    }

    /**
     * Set weight gained in the lifts.
     */
    private fun incrementTotalGained(lift:String) {
        when(lift) {
            "SQUAT" -> getGains(keys.SQUAT_WEIGHT_KEY, keys.TOTAL_SQUAT_GAINED_KEY)
            "DEADLIFT" -> getGains(keys.DEADLIFT_WEIGHT_KEY, keys.TOTAL_DEADLIFT_GAINED_KEY)
            "BENCHPRESS" -> getGains(keys.BENCHPRESS_WEIGHT_KEY, keys.TOTAL_BENCHPRESS_GAINED_KEY)
        }
    }

    /**
     * Retrieve current weight for lift and calculate how much it has increased.
     */
    private fun getGains(weightKey: String, gainedKey: String) {
        if(sharedPref.getInt(weightKey, 0) == 0) {
            // If key is 0, then it's the first lift and should
            // not get logged.
            return
        } else {
            var gained = sharedPref.getInt(gainedKey, 0)
            gained += calculateGains(weightKey)
            sharedPref.edit().putInt(gainedKey, gained).apply()
        }
    }

    /**
     * Calculate increased weight.
     */
    private fun calculateGains(weightKey: String): Int {
        val total = sharedPref.getInt(weightKey,0)
        val gained = binding.WEIGHTINPUT.text.toString().toInt()
        return (gained-total)
    }

    /**
     * Increment the counter for total amount of PB's accomplished.
     */
    private fun incrementPBlog() {
        if(pbCounts) {
            Toast.makeText(this, "PB logged!", Toast.LENGTH_SHORT).show()
            var int = sharedPref.getInt(keys.PB_LOGGED_KEY, 0)
            int++
            sharedPref.edit().putInt(keys.PB_LOGGED_KEY, int).apply()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (pbCounts) 1 else 0)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        println("ADDING ${keys.RESTORE_VIDEO_KEY}")
        outState.putString("VIDEO", keys.RESTORE_VIDEO_KEY)
    }

}