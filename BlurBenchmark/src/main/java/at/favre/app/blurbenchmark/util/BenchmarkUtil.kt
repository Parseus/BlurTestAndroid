package at.favre.app.blurbenchmark.util

import android.annotation.TargetApi
import android.os.Build
import android.os.SystemClock

import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.ArrayList
import java.util.Locale

/**
 * Created by PatrickF on 16.04.2014.
 */
object BenchmarkUtil {
    private val format = DecimalFormat("#.0")
    private const val fileSeperator = ";"

    init {
        format.decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
        format.roundingMode = RoundingMode.HALF_UP
    }

    @TargetApi(17)
    fun elapsedRealTimeNanos(): Long {
        return if (Build.VERSION.SDK_INT >= 17) {
            SystemClock.elapsedRealtimeNanos()
        } else SystemClock.elapsedRealtime() * 1000000L
    }

    fun formatNum(number: Double): String {
        return format.format(number)
    }

    fun formatNum(number: Double, formatString: String): String {
        val format = DecimalFormat(formatString)
        format.decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
        format.roundingMode = RoundingMode.HALF_UP
        return format.format(number)
    }

    fun saveFiles(files: List<File>): String {
        val joiner = StringJoiner(fileSeperator)
        for (file in files) {
            joiner.add(file.absolutePath)
        }
        return joiner.toString()
    }

    fun getAsFiles(filestring: String): MutableList<File> {
        val files = filestring.split(fileSeperator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val fileArrayList = ArrayList<File>()
        for (absPath in files) {
            val f = File(absPath)
            if (f.isFile && f.absolutePath.isNotEmpty()) {
                fileArrayList.add(f)
            }
        }
        return fileArrayList
    }

    fun getScalingUnitByteSize(byteSize: Int): String {
        var scaledByteSize = byteSize.toDouble()
        var unit = "byte"

        return if (scaledByteSize < 1024) {
            formatNum(scaledByteSize, "0.##") + unit
        } else {
            unit = "KiB"
            scaledByteSize /= 1024.0

            if (scaledByteSize < 1024) {
                formatNum(scaledByteSize, "0.##") + unit
            } else {
                unit = "MiB"
                scaledByteSize /= 1024.0
                if (scaledByteSize < 1024) {
                    formatNum(scaledByteSize, "0.##") + unit
                } else {
                    unit = "GiB"
                    scaledByteSize /= 1024.0
                    formatNum(scaledByteSize, "0.##") + unit
                }
            }
        }

    }
}
