package se.umu.cs.dv20arn.pblogger

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import se.umu.cs.dv20arn.pblogger.activities.LogActivity
import se.umu.cs.dv20arn.pblogger.activities.SettingsActivity
import se.umu.cs.dv20arn.pblogger.activities.ViewEntryActivity
import se.umu.cs.dv20arn.pblogger.databinding.ActivityMainBinding
import se.umu.cs.dv20arn.pblogger.objects.Calculator
import se.umu.cs.dv20arn.pblogger.objects.Keys

/*
TODO
-Fix calculators
-Save LogActivity state.
-Fix settings UI
-Fix ViewEntry UI
-Fix logActivity UI
-
 */

class Main : AppCompatActivity() {
    private lateinit var keys: Keys
    private lateinit var sharedPref : SharedPreferences
    private lateinit var binding: ActivityMainBinding
    private var STORAGE_PERMISSION_CODE: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        getPermissions()
        keys = Keys()
        sharedPref = getSharedPreferences(keys.PREF_KEY, Context.MODE_PRIVATE)
        onNewLogEntry()
        onViewEntry()
        onUpdate()
        onOpenSettings()
    }


    /**
     * Get permissions on first start to access memory for video.
     */
    private fun getPermissions() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission()
        }
    }

    /**
     * Handles the alert for requesting permission.
     */
    private fun requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("To record and view your PBs, PBlogger needs permission to your gallery. PLS")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        STORAGE_PERMISSION_CODE)
                }
                .setNegativeButton("DISMISS") { dialogInterface, _ -> dialogInterface.dismiss() }
                .create()
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        }
    }

    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeToast("Permission granted")
            }
        }
    }

    /**
     * Launches settingsActivity.
     */
    private fun onOpenSettings() {
        binding.SETTINGSBTN.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    /**
     * Updates UI.
     */
    private fun onUpdate() {
        binding.SQUATWEIGHT.text = sharedPref.getInt(keys.SQUAT_WEIGHT_KEY, 0).toString()
        binding.BENCHPRESSWEIGHT.text = sharedPref.getInt(keys.BENCHPRESS_WEIGHT_KEY, 0).toString()
        binding.DEADLIFTWEIGHT.text = sharedPref.getInt(keys.DEADLIFT_WEIGHT_KEY, 0).toString()
        binding.PB.text = sharedPref.getInt(keys.PB_LOGGED_KEY, 0).toString()
        if(totalWeightLifted() > 0) {
            binding.WILKS.text = calculateNewWilks()
        }
        updateGains()
    }

    /**
     * Updates users gained weight in lifts.
     */
    private fun updateGains() {
        val squat = sharedPref.getInt(keys.TOTAL_SQUAT_GAINED_KEY, 0).toString()
        val deadlift = sharedPref.getInt(keys.TOTAL_DEADLIFT_GAINED_KEY, 0).toString()
        val benchpress = sharedPref.getInt(keys.TOTAL_BENCHPRESS_GAINED_KEY, 0).toString()
        "+ $squat KG".also { binding.SQUATGAINED.text = it }
        "+ $deadlift KG".also { binding.DEADLIFTGAINED.text = it }
        "+ $benchpress KG".also { binding.BENCHGAINED.text = it }
    }

    /**
     * Get total amount of kilos user has lifted.
     */
    private fun totalWeightLifted(): Int {
        return sharedPref.getInt(keys.SQUAT_WEIGHT_KEY, 0) +
                sharedPref.getInt(keys.BENCHPRESS_WEIGHT_KEY, 0) +
                sharedPref.getInt(keys.DEADLIFT_WEIGHT_KEY, 0)
    }

    /**
     * Calculates Wilks and returns it as string.
     */
    private fun calculateNewWilks(): String {
        return Calculator().calculateWilks(
            sharedPref.getString(keys.USER_GENDER_KEY, "MALE").toString(),
            sharedPref.getInt(keys.USER_BW_KEY, 80), // default to 80kg
            totalWeightLifted()
        ).toString()
    }

    /**
     * Starts activity for logging a new PB.
     */
    private fun onNewLogEntry() {
        binding.LOGNEWPR.setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }
    }



    /***
     * Updates the squat entry.
     */
    private fun onViewEntry() {
        binding.PREVIEWPRVIDEO.setOnClickListener {
            startActivity(Intent(this, ViewEntryActivity::class.java))
        }

    }

    /**
     * Generic method for making toasts.
     */
    private fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}