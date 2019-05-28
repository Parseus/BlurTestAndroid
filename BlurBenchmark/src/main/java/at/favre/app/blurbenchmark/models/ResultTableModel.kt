package at.favre.app.blurbenchmark.models

import android.util.Log
import at.favre.app.blurbenchmark.blur.EBlurAlgorithm
import at.favre.app.blurbenchmark.blur.IBlur
import at.favre.app.blurbenchmark.util.BenchmarkUtil
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ResultTableModel(db: BenchmarkResultDatabase?) {

    enum class DataType(val minIsBest: Boolean, val unit: String) {
        AVG(true, "ms"), MIN(true, "ms"),
        MAX(true, "ms"), MEDIAN(true, "ms"),
        CONF_95(true, "ms"), OVER_16_MS(true, "%"),
        MPIXEL_PER_S(false, "MPix/s");
    }

    enum class RelativeType { BEST, WORST, AVG }

    private var tableModel: MutableMap<String, MutableMap<String, BenchmarkResultDatabase.BenchmarkEntry?>> = HashMap()
    val rows = ArrayList<String>()
    val columns = ArrayList<String>()

    init {
        for (algorithm in EBlurAlgorithm.allAlgorithms) {
            columns.add(algorithm.toString())
        }
        columns.sort()

        val rowHeaders = TreeSet<BenchmarkResultDatabase.Category>()
        db?.let {
            for (benchmarkEntry in it.entryList) {
                rowHeaders.add(benchmarkEntry.categoryObj)
            }
        }

        for (rowHeader in rowHeaders) {
            rows.add(rowHeader.category)
        }

        for (column in columns) {
            tableModel
            tableModel[column] = HashMap()
            for (row in rows) {
                tableModel[column]?.put(row, db!!.getByCategoryAndAlgorithm(row, EBlurAlgorithm.valueOf(column)))
            }
        }
    }

    fun getCell(row: Int, column: Int): BenchmarkResultDatabase.BenchmarkEntry? {
        return tableModel[columns[column]]?.get(rows[row])
    }

    private fun getRecentWrapper(row: Int, column: Int): BenchmarkWrapper? {
        val entry = getCell(row, column)
        return BenchmarkResultDatabase.getRecentWrapper(entry)
    }

    fun getValue(row: Int, column: Int, type: DataType): String {
        try {
            val wrapper = getRecentWrapper(row, column)
            if (wrapper != null) {
                return getValueForType(wrapper, type).representation
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error while getting data", e)
        }

        return MISSING
    }

    fun getRelativeType(row: Int, column: Int, type: DataType, minIsBest: Boolean): RelativeType {
        if (row < 0 || column < 0) {
            return RelativeType.AVG
        }
        val columns = java.util.ArrayList<Double>()
        var entry: BenchmarkResultDatabase.BenchmarkEntry?
        var wrapper: BenchmarkWrapper? = null
        for (i in this.columns.indices) {
            entry = getCell(row, i)

            if (entry != null && entry.wrapper.isNotEmpty()) {
                entry.wrapper.sort()
                wrapper = entry.wrapper[0]
            }

            if (wrapper != null) {
                columns.add(getValueForType(wrapper, type).value)
            } else {
                columns.add(java.lang.Double.NEGATIVE_INFINITY)
            }
        }
        val sortedColumns = ArrayList(columns)
        sortedColumns.sort()

        val columnVal = columns[column]

        if (columnVal == java.lang.Double.NEGATIVE_INFINITY) {
            return RelativeType.AVG
        }

        val minThreshold = sortedColumns[0] + sortedColumns[0] * BEST_WORST_THRESHOLD_PERCENTAGE / 100
        val maxThreshold = sortedColumns[columns.size - 1] - sortedColumns[columns.size - 1] * BEST_WORST_THRESHOLD_PERCENTAGE / 100
        return if (columnVal >= maxThreshold && columnVal <= sortedColumns[columns.size - 1]) {
            if (minIsBest) {
                RelativeType.WORST
            } else {
                RelativeType.BEST
            }
        } else if (columnVal <= minThreshold && columnVal >= sortedColumns[0]) {
            if (minIsBest) {
                RelativeType.BEST
            } else {
                RelativeType.WORST
            }
        } else {
            RelativeType.AVG
        }
    }

    data class StatValue(val value: Double = Double.NEGATIVE_INFINITY, val representation: String = MISSING) {
        val noValue: Boolean
            get() = value == Double.NEGATIVE_INFINITY && representation == MISSING
    }

    companion object {
        val TAG: String = ResultTableModel::class.java.simpleName
        const val BEST_WORST_THRESHOLD_PERCENTAGE = 5.0
        const val MISSING = "?"

        private const val NUMBER_FORMAT = "0.00"

        fun getValueForType(wrapper: BenchmarkWrapper?, type: DataType): StatValue {
            return if (wrapper != null) {
                when (type) {
                    DataType.AVG -> StatValue(wrapper.statInfo.asAvg.avg!!,
                            BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.avg!!, NUMBER_FORMAT) + "ms")
                    DataType.MIN -> StatValue(wrapper.statInfo.asAvg.min,
                            BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.min, "0.#") + "ms")
                    DataType.MAX -> StatValue(wrapper.statInfo.asAvg.max,
                            BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.max, "0.#") + "ms")
                    DataType.MEDIAN -> StatValue(wrapper.statInfo.asAvg.median,
                            BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.median, "0.#") + "ms")
                    DataType.CONF_95 -> StatValue(wrapper.statInfo.asAvg.ninetyFivePercentConfidenceIntervall!!.stdError + wrapper.statInfo.asAvg.ninetyFivePercentConfidenceIntervall!!.avg,
                            BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.ninetyFivePercentConfidenceIntervall!!.avg, "0.#") + "ms +/-" + BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.ninetyFivePercentConfidenceIntervall!!.stdError, "0.#"))
                    DataType.OVER_16_MS -> StatValue(wrapper.statInfo.asAvg.getPercentageOverGivenValue(IBlur.MS_THRESHOLD_FOR_SMOOTH.toDouble()),
                            BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.getPercentageOverGivenValue(IBlur.MS_THRESHOLD_FOR_SMOOTH.toDouble()), NUMBER_FORMAT) + "%")
                    DataType.MPIXEL_PER_S -> StatValue(wrapper.statInfo.throughputMPixelsPerSec, BenchmarkUtil.formatNum(wrapper.statInfo.throughputMPixelsPerSec, NUMBER_FORMAT) + "MP/s")
                }
            } else {
                StatValue()
            }
        }
    }

}