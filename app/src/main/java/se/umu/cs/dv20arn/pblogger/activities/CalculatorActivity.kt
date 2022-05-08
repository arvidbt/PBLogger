package se.umu.cs.dv20arn.pblogger.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import se.umu.cs.dv20arn.pblogger.databinding.ActivityCalculatorBinding
import se.umu.cs.dv20arn.pblogger.objects.Keys

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var keys: Keys

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        keys = Keys()
        sharedPref = applicationContext.getSharedPreferences(keys.PREF_KEY, Context.MODE_PRIVATE)
        setContentView(binding.root)
        onCalculate1RM()
    }


    /**
     * On button pressed, calculate users probable one rep maximum.
     */
    private fun onCalculate1RM() {
        binding.CALC1RMBTN.setOnClickListener {
            "${epleysFormula()} KG".also { binding.RM.text = it }
        }
    }


    /**
     * Return calculated sum of epleys formula.
     */
    private fun epleysFormula(): Int {
        return (weight() * ((1 + (reps()).toDouble() / 30))).toInt()
    }

    private fun getAverage() : Int {
        return (sharedPref.getInt(keys.DEADLIFT_WEIGHT_KEY,0)
                + sharedPref.getInt(keys.BENCHPRESS_WEIGHT_KEY, 0)
                + sharedPref.getInt(keys.SQUAT_WEIGHT_KEY, 0)) / 3
    }

    /**
     * Get user reps.
     */
    private fun reps(): Int {
        val reps = binding.REPS1RM.text.toString().trim()
        return if(reps == "") {
            1
        } else {
            reps.toInt()
        }
    }

    /**
     * Get weight input.
     */
    fun weight(): Int {
        val weight = binding.WEIGHT1RM.text.toString().trim()
        return if(weight == "") {
            getAverage()
        } else {
            weight.toInt()
        }
    }
}