package at.favre.app.blurbenchmark

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import androidx.renderscript.RenderScript
import android.util.Log

import java.io.IOException

import at.favre.app.blurbenchmark.blur.EBlurAlgorithm
import at.favre.app.blurbenchmark.models.BenchmarkImage
import at.favre.app.blurbenchmark.models.BenchmarkWrapper
import at.favre.app.blurbenchmark.models.StatInfo
import at.favre.app.blurbenchmark.util.BenchmarkUtil
import at.favre.app.blurbenchmark.util.BitmapUtil
import at.favre.app.blurbenchmark.util.BlurUtil

/**
 * This is the the task for completing a single Benchmark with
 * the given image, blur radius, algorithm and rounds.
 *
 *
 * It uses warmup rounds to warmup the VM. After the benchmark
 * the statistics and downscaled versions of the blurred images
 * are store to disk.
 *
 * @author pfavre
 * @since 2014/04/14
 */
open class BlurBenchmarkTask(image: BenchmarkImage,
                             private val benchmarkRounds: Int,
                             private val radius: Int,
                             private val algorithm: EBlurAlgorithm,
                             private val rs: RenderScript,
                             private val ctx: Context) : AsyncTask<Void, Void, BenchmarkWrapper>() {

    private lateinit var statInfo: StatInfo

    private var startWholeProcess: Long = 0

    private var bitmapDrawableResId: Int = 0
    private var absolutePath: String? = null
    private var master: Bitmap? = null
    private var run = false
    private var isCustomPic = false

    init {
        if (image.isResId) {
            bitmapDrawableResId = image.resId
        } else {
            absolutePath = image.absolutePath
            isCustomPic = true
        }
    }

    override fun onPreExecute() {
        Log.d(TAG, "Start test with " + radius + "px radius, " + benchmarkRounds + "rounds in " + algorithm)
        startWholeProcess = BenchmarkUtil.elapsedRealTimeNanos()
    }

    override fun doInBackground(vararg voids: Void): BenchmarkWrapper? {
        try {
            run = true
            val startReadBitmap = BenchmarkUtil.elapsedRealTimeNanos()
            master = loadBitmap()

            if (master == null) {
                throw IOException("Could not load bitmap")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                master!!.setHasMipMap(false)
            val readBitmapDuration = (BenchmarkUtil.elapsedRealTimeNanos() - startReadBitmap) / 1000000L

            statInfo = StatInfo(master!!.height, master!!.width, radius, algorithm, benchmarkRounds, BitmapUtil.sizeOf(master!!))
            statInfo.loadBitmap = readBitmapDuration

            var blurredBitmap: Bitmap? = null

            //if just quick round, skip warmup
            if (benchmarkRounds > WARMUP_ROUNDS) {
                Log.d(TAG, "Warmup")
                for (i in 0 until WARMUP_ROUNDS) {
                    if (!run) {
                        break
                    }

                    BenchmarkUtil.elapsedRealTimeNanos()
                    blurredBitmap = master!!.copy(master!!.config, true)
                    blurredBitmap = BlurUtil.blur(rs, ctx, blurredBitmap!!, radius, algorithm)
                }
            } else {
                Log.d(TAG, "Skip warmup")
            }

            Log.d(TAG, "Start benchmark")
            for (i in 0 until benchmarkRounds) {
                if (!run) {
                    break
                }

                val startBlur = BenchmarkUtil.elapsedRealTimeNanos()
                blurredBitmap = master!!.copy(master!!.config, true)
                blurredBitmap = BlurUtil.blur(rs, ctx, blurredBitmap!!, radius, algorithm)
                statInfo.benchmarkData.add((BenchmarkUtil.elapsedRealTimeNanos() - startBlur) / 1000000.0)
            }

            if (!run) {
                return null
            }

            statInfo.benchmarkDuration = (BenchmarkUtil.elapsedRealTimeNanos() - startWholeProcess) / 1000000L

            val fileName = master!!.width.toString() + "x" + master!!.height + "_" + radius + "px_" + algorithm + ".png"
            return BenchmarkWrapper(BitmapUtil.saveBitmapDownscaled(blurredBitmap!!, fileName, BitmapUtil.getCacheDir(ctx), false, 800, 800),
                    BitmapUtil.saveBitmapDownscaled(BitmapUtil.flip(blurredBitmap), "mirror_$fileName", BitmapUtil.getCacheDir(ctx), true, 300, 300),
                    statInfo, isCustomPic)
        } catch (e: Throwable) {
            Log.e(TAG, "Could not complete benchmark", e)
            statInfo.setException(e)
            return BenchmarkWrapper(null, null, statInfo, isCustomPic)
        }

    }

    private fun loadBitmap(): Bitmap {
        return if (isCustomPic && absolutePath != null) {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            options.inMutable = true
            BitmapFactory.decodeFile(absolutePath, options)
        } else {
            val options = BitmapFactory.Options()
            BitmapFactory.decodeResource(ctx.resources, bitmapDrawableResId, options)
        }
    }

    fun cancelBenchmark() {
        run = false
        Log.d(TAG, "canceled")
    }

    override fun onPostExecute(bitmap: BenchmarkWrapper) {
        master!!.recycle()
        master = null
        Log.d(TAG, "test done")
    }

    companion object {
        private val TAG = BlurBenchmarkTask::class.java.simpleName
        private const val WARMUP_ROUNDS = 5
    }

}
