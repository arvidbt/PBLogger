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


    private fun getPermissions() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission()
        }
    }

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

    private fun onOpenSettings() {
        binding.SETTINGSBTN.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun onUpdate() {
        binding.SQUATWEIGHTTEXT.text = sharedPref.getInt(keys.SQUAT_WEIGHT_KEY, 0).toString()
        binding.BENCHPRESSWEIGHTTEXT.text = sharedPref.getInt(keys.BENCHPRESS_WEIGHT_KEY, 0).toString()
        binding.DEADLIFTWEIGHTTEXT.text = sharedPref.getInt(keys.DEADLIFT_WEIGHT_KEY, 0).toString()
        binding.PB.text = sharedPref.getInt(keys.PB_LOGGED_KEY, 0).toString()
        //binding.WILKS.text = sharedPref.getInt(keys.WILKS_SCORE, 0)
        if(totalWeightLifted() > 0) {
            println("heh")
            binding.WILKS.text = calculateNewWilks()
        }
    }

    private fun totalWeightLifted(): Int {
        return sharedPref.getInt(keys.SQUAT_WEIGHT_KEY, 0) +
                sharedPref.getInt(keys.BENCHPRESS_WEIGHT_KEY, 0) +
                sharedPref.getInt(keys.DEADLIFT_WEIGHT_KEY, 0)
    }

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

    private fun viewEntry(lift:String) {
        val intent = Intent(this, ViewEntryActivity::class.java)
        if(loggedPBExists(lift)) {
            intent.putExtra("TYPE_OF_LIFT", lift)
            startActivity(intent)
        } else {
            makeToast("NO PB LOGGED FOR $lift!")
        }
    }

    /***
     * Updates the squat entry.
     */
    private fun onViewEntry() {
        binding.SQUATBTN.setOnClickListener {
            viewEntry("SQUAT")
        }
        binding.DEADLIFTBTN.setOnClickListener {
            viewEntry("DEADLIFT")
        }
        binding.BENCHPRESSBTN.setOnClickListener {
            viewEntry("BENCHPRESS")
        }
    }

    private fun loggedPBExists(lift: String): Boolean{
        when(lift) {
            "SQUAT" -> return sharedPref.getInt(keys.SQUAT_WEIGHT_KEY, 0) != 0
            "DEADLIFT" -> return sharedPref.getInt(keys.DEADLIFT_WEIGHT_KEY, 0) != 0
            "BENCHPRESS" -> return sharedPref.getInt(keys.BENCHPRESS_WEIGHT_KEY, 0) != 0
        }
        return false
    }

    private fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}