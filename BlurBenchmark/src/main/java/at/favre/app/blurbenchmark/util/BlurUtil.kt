package at.favre.app.blurbenchmark.util

import android.graphics.Bitmap
import android.os.Build
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlend

import at.favre.app.blurbenchmark.blur.EBlurAlgorithm
import at.favre.app.blurbenchmark.blur.algorithms.BoxBlur
import at.favre.app.blurbenchmark.blur.algorithms.GaussianFastBlur
import at.favre.app.blurbenchmark.blur.algorithms.RenderScriptBox5x5Blur
import at.favre.app.blurbenchmark.blur.algorithms.RenderScriptGaussian5x5Blur
import at.favre.app.blurbenchmark.blur.algorithms.RenderScriptGaussianBlur
import at.favre.app.blurbenchmark.blur.algorithms.RenderScriptStackBlur
import at.favre.app.blurbenchmark.blur.algorithms.StackBlur
import at.favre.app.blurbenchmark.blur.algorithms.SuperFastBlur

/**
 * Created by PatrickF on 07.04.2014.
 */
object BlurUtil {

    fun blur(rs: RenderScript, bitmap: Bitmap, radius: Int, algorithm: EBlurAlgorithm): Bitmap? {
        return when (algorithm) {
            EBlurAlgorithm.RS_GAUSS_FAST -> RenderScriptGaussianBlur(rs).blur(radius, bitmap)
            EBlurAlgorithm.RS_BOX_5x5 -> RenderScriptBox5x5Blur(rs).blur(radius, bitmap)
            EBlurAlgorithm.RS_GAUSS_5x5 -> RenderScriptGaussian5x5Blur(rs).blur(radius, bitmap)
            EBlurAlgorithm.RS_STACKBLUR -> RenderScriptStackBlur(rs).blur(radius, bitmap)
            EBlurAlgorithm.STACKBLUR -> StackBlur().blur(radius, bitmap)
            EBlurAlgorithm.GAUSS_FAST -> GaussianFastBlur().blur(radius, bitmap)
            EBlurAlgorithm.BOX_BLUR -> BoxBlur().blur(radius, bitmap)
//            EBlurAlgorithm.NDK_STACKBLUR -> NdkStackBlur.create().blur(radius, bitmap)
            EBlurAlgorithm.NDK_NE10_BOX_BLUR -> ru0xdc.ne10.NdkNe10BoxBlur().blur(radius, bitmap)
            EBlurAlgorithm.SUPER_FAST_BLUR -> SuperFastBlur().blur(radius, bitmap)
            else -> bitmap
        }
    }

    fun blendRenderScript(rs: RenderScript, bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
        if (Build.VERSION.SDK_INT >= 17) {
            val input1 = Allocation.createFromBitmap(rs, bitmap1, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)
            val input2 = Allocation.createFromBitmap(rs, bitmap2, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)
            val blendScript = ScriptIntrinsicBlend.create(rs, Element.U8_4(rs))
            blendScript.forEachAdd(input1, input2)
            input2.copyTo(bitmap1)
            return bitmap1
        } else {
            throw IllegalStateException("Renderscript needs sdk >= 17")
        }
    }
}
