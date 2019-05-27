package at.favre.app.blurbenchmark.util

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.util.Log

import java.io.File
import java.io.FileOutputStream

/**
 * Created by PatrickF on 15.04.2014.
 */
object BitmapUtil {
    private val TAG = BitmapUtil::class.java.simpleName

    fun clearCacheDir(cacheDir: File) {
        val files = cacheDir.listFiles()

        if (files != null) {
            for (file in files)
                file.delete()
        }
    }

    fun saveBitmapDownscaled(bitmap: Bitmap, filename: String, path: String, recycle: Boolean, maxWidth: Int, maxHeight: Int): File? {
        var bitmap = bitmap
        var heightScaleFactor = 1f
        var widthScaleFactor = 1f
        var scaleFactor = 1f

        if (bitmap.height > maxHeight) {
            heightScaleFactor = maxHeight.toFloat() / bitmap.height.toFloat()
        }

        if (bitmap.width > maxWidth) {
            widthScaleFactor = maxWidth.toFloat() / bitmap.width.toFloat()
        }
        if (heightScaleFactor < 1 || widthScaleFactor < 1) {
            scaleFactor = Math.min(heightScaleFactor, widthScaleFactor)
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scaleFactor).toInt(), (bitmap.height * scaleFactor).toInt(), true)
        return saveBitmap(bitmap, filename, path, recycle)
    }

    private fun saveBitmap(bitmap: Bitmap, filename: String, path: String, recycle: Boolean): File? {
        var out: FileOutputStream? = null
        try {
            val f = File(path, filename)
            if (!f.exists()) {
                f.createNewFile()
            }
            out = FileOutputStream(f)
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                return f
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not save bitmap", e)
        } finally {
            try {
                out!!.close()
            } catch (ignore: Throwable) {
            }

            if (recycle) {
                bitmap.recycle()
            }
        }
        return null
    }

    fun getCacheDir(ctx: Context): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable())
            ctx.externalCacheDir!!.path
        else
            ctx.cacheDir.path
    }

    fun flip(src: Bitmap): Bitmap {
        val m = Matrix()
        m.preScale(-1f, 1f)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, false)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun sizeOf(data: Bitmap): Int {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT -> data.byteCount
            else -> data.allocationByteCount
        }
    }

    /**
     * @param bmp        input bitmap
     * @param contrast   0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    fun changeBitmapContrastBrightness(bmp: Bitmap, contrast: Float, brightness: Float): Bitmap {
        val cm = ColorMatrix(floatArrayOf(contrast, 0f, 0f, 0f, brightness, 0f, contrast, 0f, 0f, brightness, 0f, 0f, contrast, 0f, brightness, 0f, 0f, 0f, 1f, 0f))

        val ret = Bitmap.createBitmap(bmp.width, bmp.height, bmp.config)

        val canvas = Canvas(ret)

        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bmp, 0f, 0f, paint)

        return ret
    }
}
