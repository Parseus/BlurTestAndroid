package at.favre.app.blurbenchmark.blur.algorithms

import android.graphics.Bitmap

import java.util.ArrayList
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import at.favre.app.blurbenchmark.blur.IBlur

/**
 * Blur using the NDK and native code.
 * from https://github.com/kikoso/android-stackblur/
 */
class NdkStackBlur(numThreads: Int) : IBlur {

    private val mExecutorThreads: Int
    private val mExecutor: ExecutorService?

    init {
        if (numThreads <= 1) {
            mExecutor = null
            mExecutorThreads = 1
        } else {
            mExecutorThreads = numThreads
            mExecutor = Executors.newFixedThreadPool(mExecutorThreads)

        }
    }

    override fun blur(radius: Int, original: Bitmap): Bitmap {
        if (mExecutorThreads == 1) {
            functionToBlur(original, radius, 1, 0, 1)
            functionToBlur(original, radius, 1, 0, 2)
        } else {
            val cores = mExecutorThreads

            val horizontal = ArrayList<NativeTask>(cores)
            val vertical = ArrayList<NativeTask>(cores)
            for (i in 0 until cores) {
                horizontal.add(NativeTask(original, radius, cores, i, 1))
                vertical.add(NativeTask(original, radius, cores, i, 2))
            }

            try {
                mExecutor!!.invokeAll(horizontal)
            } catch (e: InterruptedException) {
                return original
            }

            try {
                mExecutor.invokeAll(vertical)
            } catch (e: InterruptedException) {
                return original
            }

        }
        return original
    }

    private class NativeTask(private val _bitmapOut: Bitmap, private val _radius: Int, private val _totalCores: Int, private val _coreIndex: Int, private val _round: Int) : Callable<Void> {

        @Throws(Exception::class)
        override fun call(): Void? {
            functionToBlur(_bitmapOut, _radius, _totalCores, _coreIndex, _round)
            return null
        }
    }

    companion object {

        fun create(): NdkStackBlur {
            return NdkStackBlur(1)
        }

        fun createMultithreaded(): NdkStackBlur {
            return NdkStackBlur(Runtime.getRuntime().availableProcessors())
        }

        private external fun functionToBlur(bitmapOut: Bitmap, radius: Int, threadCount: Int, threadIndex: Int, round: Int)

        init {
            System.loadLibrary("blur")
        }
    }
}
