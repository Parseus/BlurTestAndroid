package at.favre.app.blurbenchmark.blur

import java.util.ArrayList

import at.favre.app.blurbenchmark.R

/**
 * Enum of all supported algorithms
 *
 * @author pfavre
 */
enum class EBlurAlgorithm(
        /**
         * Color used in graphs
         */
        val colorResId: Int) {
    RS_GAUSS_FAST(R.color.graphBgGreen), RS_BOX_5x5(R.color.graphBlue),
    RS_GAUSS_5x5(R.color.graphBgWhite), RS_STACKBLUR(R.color.graphBgViolet),
    //    NDK_STACKBLUR(R.color.graphBgOrange),
    //    NDK_NE10_BOX_BLUR(R.color.graphBgSkyBlue),
    STACKBLUR(R.color.graphBgYellow),
    GAUSS_FAST(R.color.graphBgRed), BOX_BLUR(R.color.graphBgTurquoise),
    SUPER_FAST_BLUR(R.color.graphBgSandyBrown), NONE(R.color.graphBgBlack);


    companion object {

        val allAlgorithms: List<EBlurAlgorithm>
            get() {
                val algorithms = ArrayList<EBlurAlgorithm>()
                for (algorithm in values()) {
                    if (algorithm != NONE) {
                        algorithms.add(algorithm)
                    }
                }
                return algorithms
            }
    }
}
