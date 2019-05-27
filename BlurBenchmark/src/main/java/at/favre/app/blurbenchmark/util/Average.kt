package at.favre.app.blurbenchmark.util

import java.util.ArrayList
import java.util.HashMap
import java.util.TreeSet

/**
 * Helper class for calculating some simple statistics
 * data. It uses a cache for better performance, but sacrificeing
 * a little memory efficiency.
 *
 * @author pfavre
 */
class Average<T : Number>() {

    var avg: Double? = null
        get() {
            if (field == null) {
                var sum = 0.0
                for (t in data!!) {
                    sum += t.toDouble()
                }
                field = sum / data!!.size.toDouble()
            }
            return field!!
        }
    private var variance: Double? = null
        get() {
            if (field == null) {
                var xxbar = 0.0
                for (t in data!!) {
                    xxbar += Math.pow(t.toDouble() - avg!!, 2.0)
                }

                field = xxbar / (data!!.size - 1)
            }
            return field!!
        }
    private var mean: Double? = null
    private var cache: MutableMap<Double, ConfidenceIntervall>? = null
    private var data: TreeSet<T>? = null

    val median: Double
        get() {
            if (mean == null) {
                val array = ArrayList(data!!)
                val middle = array.size / 2
                mean = if (array.size % 2 == 0) {
                    val left = array[middle - 1]
                    val right = array[middle]
                    (left.toDouble() + right.toDouble()) / 2.0
                } else {
                    array[middle].toDouble()
                }
            }
            return mean!!
        }

    val eightyPercentConfidenceIntervall:ConfidenceIntervall?
    get()
    {
        return getConfidenceIntervall(1.28)
    }

    val ninetyPercentConfidenceIntervall:ConfidenceIntervall?
    get()
    {
        return getConfidenceIntervall(1.645)
    }

    val ninetyFivePercentConfidenceIntervall:ConfidenceIntervall?
    get()
    {
        return getConfidenceIntervall(1.96)
    }

    val ninetyNinePercentConfidenceIntervall:ConfidenceIntervall?
    get()
    {
        return getConfidenceIntervall(2.58)
    }

    val max: T
        get() = data!!.last()

    val min: T
        get() = data!!.first()

    constructor(data: Collection<T>) : this() {
        this.data = TreeSet(data)
    }

    init {
        data = TreeSet()
        reset()
    }

    fun add(elem: T) {
        data!!.add(elem)
        reset()
    }

    fun addAll(data: Collection<T>) {
        this.data!!.addAll(data)
        reset()
    }

    private fun reset() {
        cache = HashMap()
        mean = null
        variance = null
        avg = null
    }

    private fun getConfidenceIntervall(confidenceLevel: Double): ConfidenceIntervall? {
        if (!cache!!.containsKey(confidenceLevel)) {
            val stddev = Math.sqrt(variance!!)
            val stdErr = confidenceLevel * stddev
            cache!![confidenceLevel] = ConfidenceIntervall(avg!!, stdErr)
        }
        return cache!![confidenceLevel]
    }

    private fun getValuesGreaterThanGiven(lowerLimit: Double): List<T> {
        val overList = ArrayList<T>()
        for (t in data!!) {
            if (lowerLimit < t.toDouble()) {
                overList.add(t)
            }
        }
        return overList
    }

    fun getPercentageOverGivenValue(lowerLimit: Double): Double {
        val overCount = getValuesGreaterThanGiven(lowerLimit).size.toDouble()
        val wholeCount = data!!.size.toDouble()

        return overCount * 100 / wholeCount
    }

    data class ConfidenceIntervall(val avg: Double, val stdError: Double)
}
