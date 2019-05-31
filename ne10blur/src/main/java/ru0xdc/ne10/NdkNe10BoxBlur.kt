package ru0xdc.ne10

import android.graphics.Bitmap

/**
 * NdkNe10BoxBlur using the box filter of the NE10 image processing module
 * https://github.com/projectNe10/Ne10
 */
class NdkNe10BoxBlur {

    fun blur(radius: Int, bitmap: Bitmap): Bitmap {
        functionToBlur(bitmap, radius)
        return bitmap
    }

    private external fun functionToBlur(bitmapOut: Bitmap, radius: Int)

    companion object {
        init {
            System.loadLibrary("ne10blur")
        }
    }
}
