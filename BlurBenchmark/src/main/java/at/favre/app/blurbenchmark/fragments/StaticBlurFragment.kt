package at.favre.app.blurbenchmark.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.SettingsController
import at.favre.app.blurbenchmark.activities.MainActivity
import at.favre.app.blurbenchmark.util.BenchmarkUtil
import at.favre.app.blurbenchmark.util.BitmapUtil
import at.favre.app.blurbenchmark.util.BlurUtil
import at.favre.app.blurbenchmark.util.TranslucentLayoutUtil

/**
 * Simple canvas with an image that can be blurred with parameters that
 * are set by the user. It also features a simple alpha fade.
 *
 * @author pfavre
 */
class StaticBlurFragment : Fragment(), IFragmentWithBlurSettings {

    private lateinit var imageViewBlur: ImageView
    private lateinit var imageViewNormal: ImageView

    private var blurTemplate: Bitmap? = null
    private lateinit var settingsController: SettingsController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_staticblur, container, false)

        imageViewNormal = v.findViewById(R.id.normal_image)
        imageViewBlur = v.findViewById(R.id.blur_image)

        settingsController = SettingsController(v, object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                reBlur()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }, object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                blurTemplate = null
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                reBlur()
            }
        }, object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                reBlur()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }, View.OnClickListener {
            blurTemplate = null
            startBlur()
        })

        val originalBitmap = (imageViewNormal.drawable as BitmapDrawable).bitmap
         v.findViewById<TextView>(R.id.tv_resolution_normal).text = "Original: ${originalBitmap.width}x${originalBitmap.height} / ${BenchmarkUtil.getScalingUnitByteSize(BitmapUtil.sizeOf(originalBitmap))}"

        TranslucentLayoutUtil.setTranslucentThemeInsets(requireActivity(), v.findViewById(R.id.contentWrapper))
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        startBlur()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.main_menu, menu)
    }

    private fun startBlur() {
        BlurTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun reBlur() {
        BlurTask(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun switchShowSettings() {
        settingsController.switchShow()
    }
    
    inner class BlurTask(private val onlyReBlur: Boolean = false) : AsyncTask<Void, Void, Bitmap>() {

        private var startWholeProcess: Long = 0
        private var readBitmapDuration: Long = 0
        private var blurDuration: Long = 0

        override fun onPreExecute() {
            startWholeProcess = SystemClock.elapsedRealtime()
            if (!onlyReBlur) {
                imageViewNormal.alpha = 1f
                imageViewBlur.alpha = 1f
            }
        }

        override fun doInBackground(vararg voids: Void): Bitmap? {
            if (blurTemplate == null) {
                Log.d(TAG, "Load Bitmap")
                val startReadBitmap = SystemClock.elapsedRealtime()
                val options = BitmapFactory.Options()
                options.inSampleSize = settingsController.inSampleSize
                blurTemplate = BitmapFactory.decodeResource(resources, R.drawable.photo1, options)
                readBitmapDuration = SystemClock.elapsedRealtime() - startReadBitmap
            }

            Log.d(TAG, "Start blur algorithm")
            val startBlur = SystemClock.elapsedRealtime()
            var blurredBitmap: Bitmap? = null

            try {
                blurredBitmap = BlurUtil.blur((requireActivity() as MainActivity).getRs(), requireContext(), blurTemplate!!.copy(blurTemplate!!.config, true), settingsController.radius, settingsController.algorithm)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
            }

            blurDuration = SystemClock.elapsedRealtime() - startBlur
            Log.d(TAG, "Done blur algorithm")
            return blurredBitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            if (bitmap != null) {
                Log.d(TAG, "Set image to imageView")
                imageViewBlur.setImageBitmap(bitmap)
                val duration = SystemClock.elapsedRealtime() - startWholeProcess
                Log.d(TAG, "Bluring duration $duration ms")

                if (settingsController.isShowCrossfade && !onlyReBlur) {
                    val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.alpha_fadeout)
                    anim.fillAfter = true
                    imageViewNormal.startAnimation(anim)
                    val anim2 = AnimationUtils.loadAnimation(requireContext(), R.anim.alpha_fadein)
                    anim2.fillAfter = true
                    imageViewBlur.startAnimation(anim2)
                } else {
                    imageViewBlur.alpha = 1.0f
                    imageViewNormal.alpha = 0.0f
                }

                val blurBitmap = (imageViewBlur.drawable as BitmapDrawable).bitmap
                view!!.findViewById<TextView>(R.id.tv_resolution_blur).text = "${blurBitmap.width}x${blurBitmap.height} / ${BenchmarkUtil.getScalingUnitByteSize(BitmapUtil.sizeOf(blurBitmap))} / ${settingsController.algorithm} / r:${settingsController.radius}px / blur: ${blurDuration}ms / ${duration}ms"
            }
        }
        
    }
    
    companion object {
        private val TAG = StaticBlurFragment::class.java.simpleName
    }
    
}