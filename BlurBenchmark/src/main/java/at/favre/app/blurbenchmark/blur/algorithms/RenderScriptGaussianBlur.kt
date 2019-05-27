package at.favre.app.blurbenchmark.blur.algorithms

import android.graphics.Bitmap
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur

import at.favre.app.blurbenchmark.blur.IBlur

/**
 * Simple example of ScriptIntrinsicBlur Renderscript gaussion blur.
 * In production always use this algorithm as it is the fastest on Android.
 */
class RenderScriptGaussianBlur(private val rs: RenderScript) : IBlur {

    override fun blur(radius: Int, original: Bitmap): Bitmap {
        val input = Allocation.createFromBitmap(rs, original)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setRadius(radius.toFloat())
        script.setInput(input)
        script.forEach(output)
        output.copyTo(original)
        return original
    }
}
