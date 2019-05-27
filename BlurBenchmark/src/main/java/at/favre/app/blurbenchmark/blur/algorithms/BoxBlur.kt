package at.favre.app.blurbenchmark.blur.algorithms

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color

import at.favre.app.blurbenchmark.blur.IBlur

/**
 * http://stackoverflow.com/questions/8218438
 * by saarraz1
 */
class BoxBlur : IBlur {
    override fun blur(radius: Int, original: Bitmap): Bitmap {
        assert(radius and 1 == 0) { "Range must be odd." }

        val blurred = Bitmap.createBitmap(original.width, original.height,
                Bitmap.Config.ARGB_8888)
        val c = Canvas(blurred)

        val w = original.width
        val h = original.height

        val pixels = IntArray(original.width * original.height)
        original.getPixels(pixels, 0, w, 0, 0, w, h)

        boxBlurHorizontal(pixels, w, h, radius / 2)
        boxBlurVertical(pixels, w, h, radius / 2)

        c.drawBitmap(pixels, 0, w, 0.0f, 0.0f, w, h, true, null)

        return blurred
    }

    private fun boxBlurHorizontal(pixels: IntArray, w: Int, h: Int,
                                  halfRange: Int) {
        var index = 0
        val newColors = IntArray(w)

        for (y in 0 until h) {
            var hits = 0
            var r: Long = 0
            var g: Long = 0
            var b: Long = 0
            for (x in -halfRange until w) {
                val oldPixel = x - halfRange - 1
                if (oldPixel >= 0) {
                    val color = pixels[index + oldPixel]
                    if (color != 0) {
                        r -= Color.red(color).toLong()
                        g -= Color.green(color).toLong()
                        b -= Color.blue(color).toLong()
                    }
                    hits--
                }

                val newPixel = x + halfRange
                if (newPixel < w) {
                    val color = pixels[index + newPixel]
                    if (color != 0) {
                        r += Color.red(color).toLong()
                        g += Color.green(color).toLong()
                        b += Color.blue(color).toLong()
                    }
                    hits++
                }

                if (x >= 0) {
                    newColors[x] = Color.argb(0xFF, (r / hits).toInt(), (g / hits).toInt(), (b / hits).toInt())
                }
            }

            System.arraycopy(newColors, 0, pixels, index + 0, w)

            index += w
        }
    }

    private fun boxBlurVertical(pixels: IntArray, w: Int, h: Int,
                                halfRange: Int) {

        val newColors = IntArray(h)
        val oldPixelOffset = -(halfRange + 1) * w
        val newPixelOffset = halfRange * w

        for (x in 0 until w) {
            var hits = 0
            var r: Long = 0
            var g: Long = 0
            var b: Long = 0
            var index = -halfRange * w + x
            for (y in -halfRange until h) {
                val oldPixel = y - halfRange - 1
                if (oldPixel >= 0) {
                    val color = pixels[index + oldPixelOffset]
                    if (color != 0) {
                        r -= Color.red(color).toLong()
                        g -= Color.green(color).toLong()
                        b -= Color.blue(color).toLong()
                    }
                    hits--
                }

                val newPixel = y + halfRange
                if (newPixel < h) {
                    val color = pixels[index + newPixelOffset]
                    if (color != 0) {
                        r += Color.red(color).toLong()
                        g += Color.green(color).toLong()
                        b += Color.blue(color).toLong()
                    }
                    hits++
                }

                if (y >= 0) {
                    newColors[y] = Color.argb(0xFF, (r / hits).toInt(), (g / hits).toInt(), (b / hits).toInt())
                }

                index += w
            }

            for (y in 0 until h) {
                pixels[y * w + x] = newColors[y]
            }
        }
    }
}
