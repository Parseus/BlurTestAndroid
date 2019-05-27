package at.favre.app.blurbenchmark

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import at.favre.app.blurbenchmark.blur.EBlurAlgorithm
import java.util.*

/**
 * Logic of the top-settings in some fragments
 *
 * @author pfavre
 */
class SettingsController(v: View, radiusChangeListener: SeekBar.OnSeekBarChangeListener, sampleSizeChangeListener: SeekBar.OnSeekBarChangeListener,
                         algorithmSelectListener: AdapterView.OnItemSelectedListener?, btnOnClickListener: View.OnClickListener?) {
    private val settingsWrapper: View = v.findViewById(R.id.settings)
    private val seekRadius: SeekBar = v.findViewById(R.id.seek_radius)
    private val seekInSampleSize: SeekBar = v.findViewById(R.id.seek_insample)
    private val tvRadius: TextView = v.findViewById(R.id.tv_radius_value)
    private val tvInSample: TextView = v.findViewById(R.id.tv_insample_value)
    private val algorithmSpinner: Spinner = v.findViewById(R.id.spinner_algorithm)

    var radius: Int = seekRadius.progress + 1
        private set
    var inSampleSize: Int = seekInSampleSize.progress + 10
        private set
    var algorithm = EBlurAlgorithm.RS_GAUSS_FAST
        private set
    private val algorithmList = Arrays.asList(*EBlurAlgorithm.values())
    var isShowCrossfade = true
        private set

    init {
        tvInSample.text = getInsampleText(inSampleSize)
        tvRadius.text = getRadiusText(radius)

        seekInSampleSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                inSampleSize = i + 1
                tvInSample.text = "1/${inSampleSize * inSampleSize}"
                sampleSizeChangeListener.onProgressChanged(seekBar, i, b)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                sampleSizeChangeListener.onStartTrackingTouch(seekBar)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sampleSizeChangeListener.onStopTrackingTouch(seekBar)
            }
        })
        seekRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                radius = i + 1
                tvRadius.text = getRadiusText(radius)
                radiusChangeListener.onProgressChanged(seekBar, i, b)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                radiusChangeListener.onStartTrackingTouch(seekBar)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                radiusChangeListener.onStopTrackingTouch(seekBar)
            }
        })

        algorithmSpinner.apply {
            adapter = ArrayAdapter(v.context, R.layout.inc_spinner_light, algorithmList)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                    algorithm = algorithmList[i]
                    if (algorithmSelectListener != null && view != null) {
                        algorithmSelectListener.onItemSelected(adapterView, view, i, l)
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {
                    algorithmSelectListener?.onNothingSelected(adapterView)
                }
            }

            setSelection(algorithmList.indexOf(EBlurAlgorithm.RS_GAUSS_FAST))
        }

        (v.findViewById<View>(R.id.cb_crossfade) as CheckBox).isChecked = isShowCrossfade
        (v.findViewById<View>(R.id.cb_crossfade) as CheckBox).setOnCheckedChangeListener { _, b -> isShowCrossfade = b }

        v.findViewById<View>(R.id.btn_redraw).setOnClickListener(btnOnClickListener)

        initializeSettingsPosition()
    }

    private fun initializeSettingsPosition() {
        settingsWrapper.visibility = View.INVISIBLE
    }

    fun switchShow() {
        if (settingsWrapper.visibility == View.VISIBLE) {
            val anim = AnimationUtils.loadAnimation(settingsWrapper.context, R.anim.slide_out_from_top)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    settingsWrapper.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            settingsWrapper.startAnimation(anim)

        } else {
            settingsWrapper.visibility = View.VISIBLE

            val anim = AnimationUtils.loadAnimation(settingsWrapper.context, R.anim.slide_in_from_top)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {}

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            settingsWrapper.startAnimation(anim)
        }
    }

    fun setVisibility(inSampleVisible: Boolean, radiusVisibile: Boolean, checkBoxVisible: Boolean, btnVisible: Boolean) {
        if (!inSampleVisible) {
            seekInSampleSize.visibility = View.GONE
            tvInSample.visibility = View.GONE
            settingsWrapper.findViewById<View>(R.id.tv_insample_label).visibility = View.GONE
        }
        if (!radiusVisibile) {
            seekRadius.visibility = View.GONE
            tvRadius.visibility = View.GONE
            settingsWrapper.findViewById<View>(R.id.tv_radius_label).visibility = View.GONE
        }
        if (!checkBoxVisible) {
            settingsWrapper.findViewById<View>(R.id.cb_crossfade).visibility = View.GONE
        }
        if (!btnVisible) {
            settingsWrapper.findViewById<View>(R.id.btn_redraw).visibility = View.GONE
        }
    }

    fun setBtnText(text: String) {
        (settingsWrapper.findViewById<View>(R.id.btn_redraw) as Button).text = text
    }

    companion object {

        fun getInsampleText(inSampleSize: Int): String {
            return "1/${inSampleSize * inSampleSize}"
        }

        fun getRadiusText(radius: Int): String {
            return "${radius}px"
        }
    }
}
