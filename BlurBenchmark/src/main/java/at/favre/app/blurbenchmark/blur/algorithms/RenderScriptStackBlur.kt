package at.favre.app.blurbenchmark.blur.algorithms

import android.content.Context
import android.graphics.Bitmap
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript

import at.favre.app.blurbenchmark.ScriptC_stackblur
import at.favre.app.blurbenchmark.blur.IBlur

/**
 * by kikoso
 * from https://github.com/kikoso/android-stackblur/blob/master/StackBlur/src/blur.rs
 */
class RenderScriptStackBlur(private val _rs: RenderScript, private val ctx: Context) : IBlur {

    override fun blur(radius: Int, original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height

        val blurScript = ScriptC_stackblur(_rs)
        val inAllocation = Allocation.createFromBitmap(_rs, original)

        blurScript._gIn = inAllocation
        blurScript._width = width.toLong()
        blurScript._height = height.toLong()
        blurScript._radius = radius.toLong()

        var row_indices = IntArray(height)
        for (i in 0 until height) {
            row_indices[i] = i
        }

        val rows = Allocation.createSized(_rs, Element.U32(_rs), height, Allocation.USAGE_SCRIPT)
        rows.copyFrom(row_indices)

        row_indices = IntArray(width)
        for (i in 0 until width) {
            row_indices[i] = i
        }

        val columns = Allocation.createSized(_rs, Element.U32(_rs), width, Allocation.USAGE_SCRIPT)
        columns.copyFrom(row_indices)

        blurScript.forEach_blur_h(rows)
        blurScript.forEach_blur_v(columns)
        inAllocation.copyTo(original)

        return original
    }
}
