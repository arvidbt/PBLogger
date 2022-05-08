package se.umu.cs.dv20arn.pblogger.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import se.umu.cs.dv20arn.pblogger.R
import se.umu.cs.dv20arn.pblogger.databinding.ActivityViewEntryBinding
import se.umu.cs.dv20arn.pblogger.objects.Keys

class ViewEntryActivity : AppCompatActivity() {

    private lateinit var keys: Keys
    private lateinit var sharedPref: SharedPreferences
    private lateinit var binding: ActivityViewEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        keys = Keys()
        sharedPref = applicationContext.getSharedPreferences(keys.PREF_KEY, Context.MODE_PRIVATE)
        onReplay()
        populateSpinner()
    }

    /**
     * Fills spinners with choices and adds itemlisteners to them to update UI.
     */
    private fun populateSpinner() {
        ArrayAdapter.createFromResource(this, R.array.LIFTS, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.LIFTSPINNERPR.adapter = adapter
                binding.LIFTSPINNERPR.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        onLoadPB(binding.LIFTSPINNERPR.selectedItem as String)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
            }
    }

    /**
     * Replays video.
     */
    private fun onReplay() {
        binding.REPLAYVIDEO.setOnClickListener {
            replayVideo()
        }
    }

    /**
     * Loads a lifts corresponding stats and video.
     */
    private fun onLoadPB(lift:String) {
        if(entryExists(lift)) {
            loadVideo(lift)
            loadWeight(lift)
            binding.LOGGER.text = lift
        } else {
            Toast.makeText(this, "No PB logged!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Checks if there exists a entry for selected weight.
     */
    private fun entryExists(lift: String): Boolean {
        when(lift) {
            "SQUAT" -> return sharedPref.getInt(keys.SQUAT_WEIGHT_KEY, 0) != 0
            "DEADLIFT" -> return sharedPref.getInt(keys.DEADLIFT_WEIGHT_KEY, 0) != 0
            "BENCHPRESS" -> return sharedPref.getInt(keys.BENCHPRESS_WEIGHT_KEY, 0) != 0
        }
        return false
    }


    /**
     * Loads the weight of selected lift.
     */
    private fun loadWeight(lift: String) {
        when(lift) {
            "SQUAT" -> binding.PBWEIGHT.text = sharedPref.getInt(keys.SQUAT_WEIGHT_KEY, 0).toString()
            "DEADLIFT" -> binding.PBWEIGHT.text = sharedPref.getInt(keys.DEADLIFT_WEIGHT_KEY, 0).toString()
            "BENCHPRESS" ->binding.PBWEIGHT.text = sharedPref.getInt(keys.BENCHPRESS_WEIGHT_KEY, 0).toString()
        }
    }


    /**
     * Loads the video of selected lift.
     */
    private fun loadVideo(lift: String) {
        when(lift) {
            "SQUAT" -> sharedPref.getString(keys.SQUAT_VIDEO_ID_KEY, keys.DEFAULT_MSG)
                ?.let { load(it) }
            "DEADLIFT" -> sharedPref.getString(keys.DEADLIFT_VIDEO_ID_KEY, keys.DEFAULT_MSG)
                ?.let { load(it) }
            "BENCHPRESS" -> sharedPref.getString(keys.BENCHPRESS_VIDEO_ID_KEY, keys.DEFAULT_MSG)
                ?.let { load(it) }
        }
    }

    /**
     * Loads video to VideoView.
     */
    private fun load(videoID: String) {
        binding.PREVIEWVIDEO.setVideoPath(videoID)
        binding.PREVIEWVIDEO.start()
    }


    /**
     * Restarts VideoView.
     */
    private fun replayVideo() {
        binding.PREVIEWVIDEO.start()
    }
}