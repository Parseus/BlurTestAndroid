package at.favre.app.blurbenchmark.models

import at.favre.app.blurbenchmark.blur.EBlurAlgorithm
import com.fasterxml.jackson.annotation.JsonIgnore

import java.io.File
import java.util.Date

/**
 * Info assembled after one benchmark
 *
 * @author pfavre
 */
class BenchmarkWrapper(val bitmapFile: File?,
                            val flippedBitmapFile: File?,
                            val statInfo: StatInfo,
                            val customPic: Boolean) : Comparable<BenchmarkWrapper> {
    constructor() : this(null,
            null,
            StatInfo(0, 0, 0, EBlurAlgorithm.NONE, 0,
                    null), false)

    var bitmapPath: String? = null
    var flippedBitmapPath: String? = null
    var isAdditionalInfoVisibility = false

    val bitmapAsFile: File
        @JsonIgnore
        get() = File(bitmapPath!!)

    val flippedBitmapAsFile: File
        @JsonIgnore
        get() = File(flippedBitmapPath!!)

    init {
        if (bitmapFile != null && flippedBitmapFile != null) {
            this.bitmapPath = bitmapFile.absolutePath
            this.flippedBitmapPath = flippedBitmapFile.absolutePath
        }
        if (bitmapPath == null) {
            statInfo.isError = true
        }
    }

    override fun compareTo(other: BenchmarkWrapper): Int {
        return Date(statInfo.date).compareTo(Date(other.statInfo.date)) * -1
    }
}
