package se.umu.cs.dv20arn.pblogger.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import se.umu.cs.dv20arn.pblogger.databinding.ActivityCalculatorBinding

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)
        onCalculate1RM()
    }


    /**
     * On button pressed, calculate users probable one rep maximum.
     */
    fun onCalculate1RM() {
        binding.CALC1RMBTN.setOnClickListener {
            binding.RM.text ="${epleysFormula()} KG"
        }
    }



    private fun epleysFormula(): Int {
        return (weight() * ((1 + (reps()).toDouble() / 30))).toInt()
    }

    /**
     * Get user reps.
     */
    fun reps(): Int {
        return binding.REPS1RM.text.toString().toInt()
    }

    fun weight(): Int {
        return binding.WEIGHT1RM.text.toString().toInt()
    }
}