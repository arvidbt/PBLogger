package se.umu.cs.dv20arn.pblogger.objects

import kotlin.math.pow


class Calculator() {
    // Male constants for WILKS
    private val ma = -216.0475144
    private val mb = 16.2606339
    private val mc = -0.002388645
    private val md = -0.00113732
    private val me = 7.01863E-06
    private val mf = -1.291E-08

    // Female constants for WILKS
    private val fa = 594.31747775582
    private val fb = -27.23842536447
    private val fc = 0.82112226871
    private val fd = -0.00930733913
    private val fe = 4.731582E-05
    private val ff = -9.054E-08


    /**
     * Calculates Wilks and returns score, based on gender.
     */
    fun calculateWilks(gender:String, bodyweight: Int, weightLifted: Int): Int {
        val bw = bodyweight.toDouble()
        if(gender == "MALE" && bodyweight > 0 && weightLifted > 0) {
            return wilksFormula(ma, mb, mc, md, me, mf, bw, weightLifted).toInt()
        } else if (gender == "FEMALE" && bodyweight > 0 && weightLifted > 0) {
            return wilksFormula(fa, fb, fc, fd, fe, ff, bw, weightLifted).toInt()
        }
        return 0
    }

    /**
     *  Calculates Wilks Formula.
     */
    private fun wilksFormula(a:Double,
                             b:Double,
                             c:Double,
                             d:Double,
                             e:Double,
                             f:Double,
                             bw: Double,
                             weightLifted: Int
                             ) : Double{
        return ((weightLifted*500) /
                (a + b* powerOf(bw, 1) + c* powerOf(bw,2) + d* powerOf(bw,3)
                        + e* powerOf(bw,4) + f* powerOf(bw,5)))
    }

    // bwf = Body Weight Factor
    private fun powerOf(bw: Double, powerOf:Int) : Double{
        return bw.pow(powerOf.toDouble())
    }
}