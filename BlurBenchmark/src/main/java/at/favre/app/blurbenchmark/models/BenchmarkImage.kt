package at.favre.app.blurbenchmark.models

import androidx.annotation.DrawableRes

/**
 * Wrapper for an image, that either holds a file path or resource id
 *
 * @author pfavre
 */
data class BenchmarkImage(@DrawableRes val resId: Int = 0, val absolutePath: String? = null) {
    val isResId: Boolean
        get() = absolutePath == null
}