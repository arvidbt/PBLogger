package se.umu.cs.dv20arn.pblogger.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        onLoadPB()
        onReplay()
    }

    private fun onReplay() {
        binding.REPLAYVIDEO.setOnClickListener {
            replayVideo()
        }
    }

    /**
     * Loads a lifts corresponding stats and video.
     */
    private fun onLoadPB() {
        loadVideo()
        loadWeight()
        loadType()
    }

    private fun loadWeight() {
        when(intent.getStringExtra("TYPE_OF_LIFT")) {
            "SQUAT" -> binding.PBWEIGHT.text = sharedPref.getInt(keys.SQUAT_WEIGHT_KEY, 0).toString()
            "DEADLIFT" -> binding.PBWEIGHT.text = sharedPref.getInt(keys.DEADLIFT_WEIGHT_KEY, 0).toString()
            "BENCHPRESS" ->binding.PBWEIGHT.text = sharedPref.getInt(keys.BENCHPRESS_WEIGHT_KEY, 0).toString()
        }
    }


    private fun loadType() {
        when(intent.getStringExtra("TYPE_OF_LIFT")) {
            "SQUAT" -> "SQUAT".also { binding.LIFTTYPE.text = it }
            "DEADLIFT" -> "DEADLIFT".also { binding.LIFTTYPE.text = it }
            "BENCHPRESS" -> "BENCHPRESS".also { binding.LIFTTYPE.text = it }
        }
    }

    private fun loadVideo() {
        when(intent.getStringExtra("TYPE_OF_LIFT")) {
            "SQUAT" -> sharedPref.getString(keys.SQUAT_VIDEO_ID_KEY, keys.DEFAULT_MSG)
                ?.let { load(it) }
            "DEADLIFT" -> sharedPref.getString(keys.DEADLIFT_VIDEO_ID_KEY, keys.DEFAULT_MSG)
                ?.let { load(it) }
            "BENCHPRESS" -> sharedPref.getString(keys.BENCHPRESS_VIDEO_ID_KEY, keys.DEFAULT_MSG)
                ?.let { load(it) }
        }
    }

    private fun load(videoID: String) {
        binding.PREVIEWPRVIDEO.setVideoPath(videoID)
        binding.PREVIEWPRVIDEO.start()
    }


    private fun replayVideo() {
        binding.PREVIEWPRVIDEO.start()
    }
}