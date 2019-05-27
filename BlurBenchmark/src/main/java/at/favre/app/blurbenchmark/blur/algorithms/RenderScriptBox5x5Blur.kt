package at.favre.app.blurbenchmark.blur.algorithms

import android.graphics.Bitmap
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicConvolve5x5

import at.favre.app.blurbenchmark.blur.BlurKernels
import at.favre.app.blurbenchmark.blur.IBlur

/**
 * This is a convolve matrix based blur algorithms powered by Renderscript's ScriptIntrinsicConvolve class. This uses a box kernel.
 * Instead of radius it uses passes, so a radius parameter of 16 makes the convolve algorithm applied 16 times onto the image.
 */
class RenderScriptBox5x5Blur(private val rs: RenderScript) : IBlur {

    override fun blur(radius: Int, original: Bitmap): Bitmap {
        var input = Allocation.createFromBitmap(rs, original)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicConvolve5x5.create(rs, Element.U8_4(rs))
        script.setCoefficients(BlurKernels.BOX_5x5)
        for (i in 0 until radius) {
            script.setInput(input)
            script.forEach(output)
            input = output
        }
        output.copyTo(original)
        return original
    }
}
