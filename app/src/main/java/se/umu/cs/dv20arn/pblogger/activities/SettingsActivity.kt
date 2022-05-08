package se.umu.cs.dv20arn.pblogger.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import se.umu.cs.dv20arn.pblogger.Main
import se.umu.cs.dv20arn.pblogger.R
import se.umu.cs.dv20arn.pblogger.databinding.ActivitySettingsBinding
import se.umu.cs.dv20arn.pblogger.objects.Keys

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var keys: Keys

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)
        keys = Keys()
        sharedPref = applicationContext.getSharedPreferences(keys.PREF_KEY, Context.MODE_PRIVATE)
        populateSpinner()
        onUpdateSettings()
    }

    /**
     * Updates settings on click and returns user to Main activity.
     */
    private fun onUpdateSettings() {
        binding.SAVESETTINGS.setOnClickListener {
            onSelectUserGender()
            onSelectUserBodyweight()
            startActivity(Intent(this, Main::class.java))
        }
    }
    /**
     * Populates spinner with options.
     */
    private fun populateSpinner() {
        ArrayAdapter.createFromResource(this, R.array.GENDERS, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.GENDERSPINNER.adapter = adapter
            }
    }

    /**
     * Adds currently selected gender to sharedPreferences.
     */
    private fun onSelectUserGender() {
        sharedPref.edit().putString(keys.USER_GENDER_KEY, binding.GENDERSPINNER.selectedItem as String).apply()
    }

    /**
     * Adds bodyweight to sharedPreferences.
     */
    private fun onSelectUserBodyweight() {
        val bw = binding.BODYWEIGHTINPUT.text.toString().trim()
        if(bw == "") {
            if(sharedPref.getInt(keys.USER_BW_KEY, 0) > 0 ) {
                Toast.makeText(this, "Weight not changed, ${sharedPref.getInt(keys.USER_BW_KEY, 0)}", Toast.LENGTH_SHORT).show()
            } else {
                sharedPref.edit().putInt(keys.USER_BW_KEY, 80).apply()
                Toast.makeText(this, "No weight input, default to 80kg.", Toast.LENGTH_SHORT).show()
            }
        } else {
            sharedPref.edit().putInt(keys.USER_BW_KEY, bw.toInt()).apply()
        }
    }
}