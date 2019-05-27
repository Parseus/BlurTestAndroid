package at.favre.app.blurbenchmark.blur.algorithms

import android.graphics.Bitmap

import at.favre.app.blurbenchmark.blur.IBlur

/**
 * http://stackoverflow.com/a/13436737/774398
 * by gordi
 */
class GaussianFastBlur : IBlur {
    override fun blur(radius: Int, original: Bitmap): Bitmap {
        val w = original.width
        val h = original.height
        val pix = IntArray(w * h)
        original.getPixels(pix, 0, w, 0, 0, w, h)

        var r = radius
        while (r >= 1) {
            for (i in r until h - r) {
                for (j in r until w - r) {
                    val tl = pix[(i - r) * w + j - r]
                    val tr = pix[(i - r) * w + j + r]
                    val tc = pix[(i - r) * w + j]
                    val bl = pix[(i + r) * w + j - r]
                    val br = pix[(i + r) * w + j + r]
                    val bc = pix[(i + r) * w + j]
                    val cl = pix[i * w + j - r]
                    val cr = pix[i * w + j + r]

                    pix[i * w + j] = -0x1000000 or (
                            (tl and 0xFF) + (tr and 0xFF) + (tc and 0xFF) + (bl and 0xFF) + (br and 0xFF) + (bc and 0xFF) + (cl and 0xFF) + (cr and 0xFF) shr 3 and 0xFF) or (
                            (tl and 0xFF00) + (tr and 0xFF00) + (tc and 0xFF00) + (bl and 0xFF00) + (br and 0xFF00) + (bc and 0xFF00) + (cl and 0xFF00) + (cr and 0xFF00) shr 3 and 0xFF00) or (
                            (tl and 0xFF0000) + (tr and 0xFF0000) + (tc and 0xFF0000) + (bl and 0xFF0000) + (br and 0xFF0000) + (bc and 0xFF0000) + (cl and 0xFF0000) + (cr and 0xFF0000) shr 3 and 0xFF0000)
                }
            }
            r /= 2
        }
        original.setPixels(pix, 0, w, 0, 0, w, h)
        return original
    }
}
