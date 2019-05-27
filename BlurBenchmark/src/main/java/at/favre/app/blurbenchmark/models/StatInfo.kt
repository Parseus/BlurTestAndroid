package at.favre.app.blurbenchmark.models

import com.fasterxml.jackson.annotation.JsonIgnore

import java.util.ArrayList
import java.util.Date

import at.favre.app.blurbenchmark.blur.EBlurAlgorithm
import at.favre.app.blurbenchmark.util.Average
import at.favre.app.blurbenchmark.util.BenchmarkUtil

/**
 * Wrapper for all statistic info on a benchmark
 *
 * @author pfavre
 */
class StatInfo(var bitmapHeight: Int,
                    var bitmapWidth: Int,
                    var blurRadius: Int,
                    val algorithm: EBlurAlgorithm,
                    var rounds: Int,
                    private var byteAllocation: Int?) {
    @Suppress("unused")
    constructor() : this(-1, -1, -1, EBlurAlgorithm.NONE, -1, null)

    var benchmarkData: MutableList<Double> = ArrayList()
        get() {
            avg = null
            return field
        }
        set(value) {
            avg = null
            field = value
        }
    var benchmarkDuration: Long = 0
    var loadBitmap: Long = 0
    var isError = false
    var errorDescription: String? = null
        private set
    var date: Long = Date().time

    @get:JsonIgnore
    @set:JsonIgnore
    var avg: Average<Double>? = null

    val throughputMPixelsPerSec: Double
        @JsonIgnore
        get() = bitmapWidth.toDouble() * bitmapHeight.toDouble() / asAvg.avg!! * 1000.0 / 1000000.0

    val keyString: String
        @JsonIgnore
        get() = bitmapHeight.toString() + "x" + bitmapWidth + "_" + algorithm + "_" + String.format("%02d", blurRadius) + "px"

    val categoryString: String
        @JsonIgnore
        get() = imageSizeCategoryString + " / " + BenchmarkUtil.formatNum(blurRadius.toDouble(), "00") + "px"

    private val imageSizeCategoryString: String
        @JsonIgnore
        get() = bitmapHeight.toString() + "x" + bitmapWidth

    val bitmapByteSize: String
        @JsonIgnore
        get() = if (byteAllocation == null) {
            BenchmarkUtil.getScalingUnitByteSize(bitmapHeight * bitmapWidth)
        } else {
            BenchmarkUtil.getScalingUnitByteSize(byteAllocation!!)
        }

    val megaPixels: String
        @JsonIgnore
        get() = (Math.round((bitmapHeight * bitmapWidth).toDouble() / 1000000.0 * 100.0).toDouble() / 100.0).toString() + "MP"

    val asAvg: Average<Double>
        @JsonIgnore
        get() {
            if (avg == null) {
                avg = Average(benchmarkData)
            }
            return avg!!
        }

    fun setException(throwable: Throwable) {
        isError = true
        errorDescription = throwable.toString()
    }

    fun getByteAllocation(): Long {
        return if (byteAllocation == null) {
            byteAllocation = bitmapWidth * bitmapHeight
            byteAllocation!!.toLong()
        } else byteAllocation!!.toLong()
    }

    fun setByteAllocation(byteAllocation: Int) {
        this.byteAllocation = byteAllocation
    }
}
