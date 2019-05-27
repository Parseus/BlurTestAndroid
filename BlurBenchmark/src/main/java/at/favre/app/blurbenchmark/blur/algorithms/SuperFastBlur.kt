package at.favre.app.blurbenchmark.blur.algorithms

import android.graphics.Bitmap

import at.favre.app.blurbenchmark.blur.IBlur

/**
 * Super Fast Blur by Mario Klingemann<br></br>
 * Resource: http://incubator.quasimondo.com/processing/superfast_blur.php<br></br>
 * StackOverflow - Understanding super fast blur algorithm: http://stackoverflow.com/questions/21418892/understanding-super-fast-blur-algorithm<br></br>
 */
class SuperFastBlur : IBlur {
    override fun blur(radius: Int, original: Bitmap): Bitmap? {
        val w = original.width
        val h = original.height
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var p1: Int
        var p2: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))
        val vmax = IntArray(Math.max(w, h))
        val pix = IntArray(w * h)

        original.getPixels(pix, 0, w, 0, 0, w, h)

        val dv = IntArray(256 * div)
        i = 0
        while (i < 256 * div) {
            dv[i] = i / div
            i++
        }

        yi = 0
        yw = yi

        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                rsum += p and 0xff0000 shr 16
                gsum += p and 0x00ff00 shr 8
                bsum += p and 0x0000ff
                i++
            }
            x = 0
            while (x < w) {

                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                    vmax[x] = Math.max(x - radius, 0)
                }
                p1 = pix[yw + vmin[x]]
                p2 = pix[yw + vmax[x]]

                rsum += (p1 and 0xff0000) - (p2 and 0xff0000) shr 16
                gsum += (p1 and 0x00ff00) - (p2 and 0x00ff00) shr 8
                bsum += (p1 and 0x0000ff) - (p2 and 0x0000ff)
                yi++
                x++
            }
            yw += w
            y++
        }

        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                rsum += r[yi]
                gsum += g[yi]
                bsum += b[yi]
                yp += w
                i++
            }
            yi = x
            y = 0
            while (y < h) {
                pix[yi] = -0x1000000 or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                if (x == 0) {
                    vmin[y] = Math.min(y + radius + 1, hm) * w
                    vmax[y] = Math.max(y - radius, 0) * w
                }
                p1 = x + vmin[y]
                p2 = x + vmax[y]

                rsum += r[p1] - r[p2]
                gsum += g[p1] - g[p2]
                bsum += b[p1] - b[p2]

                yi += w
                y++
            }
            x++
        }

        original.setPixels(pix, 0, w, 0, 0, w, h)

        return original
    }
}
