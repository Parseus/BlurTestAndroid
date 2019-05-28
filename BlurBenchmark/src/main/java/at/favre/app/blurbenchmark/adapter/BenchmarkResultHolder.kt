package at.favre.app.blurbenchmark.adapter

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable

import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import com.squareup.picasso.Picasso

import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.activities.MainActivity
import at.favre.app.blurbenchmark.blur.IBlur
import at.favre.app.blurbenchmark.fragments.BenchmarkDetailsDialog
import at.favre.app.blurbenchmark.models.BenchmarkWrapper
import at.favre.app.blurbenchmark.util.BenchmarkUtil

class BenchmarkResultHolder(private val root: View, private val fragmentManager: FragmentManager) : RecyclerView.ViewHolder(root) {
    private val tvAvg: TextView = root.findViewById(R.id.tv_avg)
    private val tvDeviation: TextView = root.findViewById(R.id.tv_deviation)
    private val tvWidthHeight: TextView = root.findViewById(R.id.tv_width_height)
    private val tvImageInfo: TextView = root.findViewById(R.id.tv_imageInfo)
    private val tvBlurRadius: TextView = root.findViewById(R.id.tv_radius)
    private val tvErrMsg: TextView = root.findViewById(R.id.tv_errMsg)
    private val tvAdditionalInfo: TextView = root.findViewById(R.id.tv_algorithm)
    private val tvOver16ms: TextView = root.findViewById(R.id.tv_over16ms)
    private val imageView: ImageView = root.findViewById(R.id.thumbnail)
    private val frontImageWrapper: FrameLayout = root.findViewById(R.id.thumbnail_front)
    private val backImageWrapper: FrameLayout = root.findViewById(R.id.thumbnail_back)

    private val ctx: Context = root.context

    fun onBind(wrapper: BenchmarkWrapper) {
        if (!wrapper.statInfo.isError) {

            tvErrMsg.visibility = View.GONE
            tvAvg.visibility = View.VISIBLE
            tvDeviation.visibility = View.VISIBLE
            tvWidthHeight.visibility = View.VISIBLE
            tvImageInfo.visibility = View.VISIBLE
            tvBlurRadius.visibility = View.VISIBLE

            tvAdditionalInfo.text = "${BenchmarkUtil.formatNum(wrapper.statInfo.throughputMPixelsPerSec)} MPixS / ${wrapper.statInfo.algorithm}"
            tvAvg.text = "${BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.avg!!)}ms"
            Picasso.get().load(wrapper.bitmapAsFile).placeholder(R.drawable.placeholder).into(imageView)
            Picasso.get().load(wrapper.flippedBitmapAsFile).placeholder(R.drawable.placeholder).into(backImageWrapper.findViewById<View>(R.id.thumbnail2) as ImageView)
            tvDeviation.text = "+/-${BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.ninetyPercentConfidenceIntervall!!.stdError)}ms"
            (backImageWrapper.findViewById<View>(R.id.tv_imageInfo2) as TextView).text = "bmp loading: ${BenchmarkUtil.formatNum(wrapper.statInfo.loadBitmap.toDouble())}ms\nblur min/max: ${BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.min)}ms/${BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.max)}ms\nblur median: ${BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.median)}ms\nblur avg/normalized: ${BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.avg!!)}ms/${BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.avg!!)}ms\nbenchmark: ${BenchmarkUtil.formatNum(wrapper.statInfo.benchmarkDuration.toDouble())}ms\n"
            tvOver16ms.text = "${BenchmarkUtil.formatNum(wrapper.statInfo.asAvg.getPercentageOverGivenValue(IBlur.MS_THRESHOLD_FOR_SMOOTH.toDouble()))}% over ${IBlur.MS_THRESHOLD_FOR_SMOOTH}ms"
            tvOver16ms.layoutParams.height = (frontImageWrapper.layoutParams.height.toDouble() * wrapper.statInfo.asAvg.getPercentageOverGivenValue(IBlur.MS_THRESHOLD_FOR_SMOOTH.toDouble()) / 100.0).toInt()
            tvOver16ms.requestLayout()

            if (!wrapper.isAdditionalInfoVisibility) {
                frontImageWrapper.visibility = View.VISIBLE
                backImageWrapper.visibility = View.GONE
            } else {
                frontImageWrapper.visibility = View.GONE
                backImageWrapper.visibility = View.VISIBLE
            }

            frontImageWrapper.setOnClickListener {
                val set = AnimatorInflater.loadAnimator(ctx, R.animator.card_flip_left_out) as AnimatorSet
                val set2 = AnimatorInflater.loadAnimator(ctx, R.animator.card_flip_left_in) as AnimatorSet
                set.setTarget(frontImageWrapper)
                set2.setTarget(backImageWrapper)
                set2.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}

                    override fun onAnimationEnd(animator: Animator) {
                        frontImageWrapper.visibility = View.GONE
                        frontImageWrapper.alpha = 1.0f
                        frontImageWrapper.rotationY = 0.0f
                    }

                    override fun onAnimationCancel(animator: Animator) {}

                    override fun onAnimationRepeat(animator: Animator) {}
                })
                backImageWrapper.visibility = View.VISIBLE
                set.start()
                set2.start()
                wrapper.isAdditionalInfoVisibility = true
            }
            backImageWrapper.setOnClickListener {
                val set = AnimatorInflater.loadAnimator(ctx, R.animator.card_flip_right_out) as AnimatorSet
                val set2 = AnimatorInflater.loadAnimator(ctx, R.animator.card_flip_right_in) as AnimatorSet
                set.setTarget(backImageWrapper)
                set2.setTarget(frontImageWrapper)
                set2.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}

                    override fun onAnimationEnd(animator: Animator) {
                        backImageWrapper.visibility = View.GONE
                        backImageWrapper.alpha = 1.0f
                        backImageWrapper.rotationY = 0.0f
                    }

                    override fun onAnimationCancel(animator: Animator) {}

                    override fun onAnimationRepeat(animator: Animator) {}
                })
                frontImageWrapper.visibility = View.VISIBLE
                set.start()
                set2.start()
                wrapper.isAdditionalInfoVisibility = false
            }

            root.setOnClickListener {
                val dialog = BenchmarkDetailsDialog.createInstance(wrapper)
                dialog.show(fragmentManager, MainActivity.DIALOG_TAG)
            }
        } else {
            imageView.setImageDrawable(BitmapDrawable(ctx.resources, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)))
            Picasso.get().load(R.drawable.placeholder).into(imageView)
            tvErrMsg.visibility = View.VISIBLE
            tvErrMsg.text = wrapper.statInfo.errorDescription

            tvAvg.visibility = View.GONE
            tvDeviation.visibility = View.GONE
            tvWidthHeight.visibility = View.VISIBLE
            tvImageInfo.visibility = View.VISIBLE
            tvBlurRadius.visibility = View.VISIBLE
            tvAdditionalInfo.visibility = View.VISIBLE
            tvOver16ms.visibility = View.GONE

            tvAdditionalInfo.text = wrapper.statInfo.algorithm.toString()

            frontImageWrapper.setOnClickListener(null)
            backImageWrapper.setOnClickListener(null)
            root.setOnClickListener(null)
        }

        tvBlurRadius.text = "${wrapper.statInfo.blurRadius}px"
        tvImageInfo.text = wrapper.statInfo.bitmapByteSize
        tvWidthHeight.text = "${wrapper.statInfo.bitmapHeight} x ${wrapper.statInfo.bitmapWidth} / ${wrapper.statInfo.megaPixels}"

    }
}
