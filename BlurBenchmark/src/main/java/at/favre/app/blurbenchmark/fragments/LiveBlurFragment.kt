package at.favre.app.blurbenchmark.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock

import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.ViewCompat

import com.squareup.picasso.Picasso

import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean

import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.SettingsController
import at.favre.app.blurbenchmark.activities.MainActivity
import at.favre.app.blurbenchmark.util.BlurUtil
import at.favre.app.blurbenchmark.util.TranslucentLayoutUtil
import at.favre.app.blurbenchmark.view.ObservableScrollView

/**
 * A view with a live blur under the actionbar and
 * on the bottom of the screen. This wil draw a blurred
 * version of the content under the blur-areas live when
 * the user manipulates the ui (by eg. scrolling)
 *
 * @author pfavre
 */
class LiveBlurFragment : Fragment(), IFragmentWithBlurSettings {

    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var topBlurView: View
    private lateinit var bottomBlurView: View
    private lateinit var tvPerformance: TextView
    private lateinit var tvImageSizes: TextView

    private val isWorking = AtomicBoolean(false)

    private var dest: Bitmap? = null

    private var max: Long = 0
    private var min: Long = 9999
    private var avgSum = 0.0
    private var avgCount: Long = 0
    private var last: Long = 0

    private lateinit var settingsController: SettingsController

    private var prevFrame: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        dest = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_liveblur, container, false)

        viewPager = v.findViewById(R.id.pager)
        pagerAdapter = ScreenSlidePagerAdapter()
        viewPager.adapter = pagerAdapter
        topBlurView = v.findViewById(R.id.topCanvas)
        bottomBlurView = v.findViewById(R.id.bottomCanvas)

        tvPerformance = v.findViewById(R.id.tv_performance)
        tvImageSizes = v.findViewById(R.id.tv_imagesizes)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                updateBlurView()
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {}
        })

        settingsController = SettingsController(v, object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                updateBlurView()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }, object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prevFrame = null
                updateBlurView()
            }
        }, object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                updateBlurView()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }, null)
        settingsController.setVisibility(inSampleVisible = true, radiusVisibile = true, checkBoxVisible = false, btnVisible = false)
        TranslucentLayoutUtil.setTranslucentThemeInsets(requireActivity(), v.findViewById(R.id.contentWrapper))
        TranslucentLayoutUtil.setTranslucentThemeInsetsWithoutActionbarHeight(requireActivity(), v.findViewById(R.id.topCanvasWrapper), true)
        return v
    }

    override fun onResume() {
        super.onResume()
        //this is a hack so that it will be immediatly blurred, ViewTreeObserver does not work here because the images are loaded async by picasso
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ updateBlurView() }, 300)
        handler.postDelayed({ updateBlurView() }, 600)
        handler.postDelayed({ updateBlurView() }, 900)
        handler.postDelayed({ updateBlurView() }, 1200)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.main_menu, menu)
    }

    @SuppressLint("SetTextI18n")
    private fun updateBlurView(): Boolean {
        try {
            if (view != null && !isWorking.get() && topBlurView.width != 0 && topBlurView.height != 0) {
                isWorking.compareAndSet(false, true)
                val start = SystemClock.elapsedRealtime()
                dest = drawViewToBitmap(dest, view!!.findViewById(R.id.wrapper), settingsController.inSampleSize)
                ViewCompat.setBackground(topBlurView, BitmapDrawable(resources,
                        BlurUtil.blur((requireActivity() as MainActivity).getRs(), crop(dest!!.copy(dest!!.config, true), topBlurView,
                                settingsController.inSampleSize),
                                settingsController.radius, settingsController.algorithm)))
                ViewCompat.setBackground(bottomBlurView, BitmapDrawable(resources,
                        BlurUtil.blur((requireActivity() as MainActivity).getRs(), crop(dest!!.copy(dest!!.config, true), bottomBlurView,
                                settingsController.inSampleSize),
                                settingsController.radius, settingsController.algorithm)))
                checkAndSetPerformanceTextView(SystemClock.elapsedRealtime() - start)
                tvImageSizes.text = "${(topBlurView.background as BitmapDrawable).bitmap.width}x${(topBlurView.background as BitmapDrawable).bitmap.height} / ${(bottomBlurView.background as BitmapDrawable).bitmap.width}x${(bottomBlurView.background as BitmapDrawable).bitmap.height}"
                isWorking.compareAndSet(true, false)
                return true
            } else {
                Log.v(TAG, "skip blur frame")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not create blur view", e)
        }

        return false
    }

    @SuppressLint("SetTextI18n")
    private fun checkAndSetPerformanceTextView(currentRunMs: Long) {
        if (max < currentRunMs) {
            max = currentRunMs
        }
        if (min > currentRunMs) {
            min = currentRunMs
        }
        avgCount++
        avgSum += currentRunMs.toDouble()
        last = currentRunMs
        tvPerformance.text = "last: ${last}ms / avg: ${Math.round(avgSum / avgCount)}ms / min:${min}ms / max:${max}ms"
    }

    private fun drawViewToBitmap(dest: Bitmap?, view: View, downSampling: Int): Bitmap {
        var dstBitmap = dest
        val scale = 1f / downSampling
        val viewWidth = view.width
        val viewHeight = view.height
        val bmpWidth = Math.round(viewWidth * scale)
        val bmpHeight = Math.round(viewHeight * scale)

        if (dstBitmap == null || dstBitmap.width != bmpWidth || dstBitmap.height != bmpHeight) {
            dstBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
        }

        val c = Canvas(dstBitmap!!)
        if (downSampling > 1) {
            c.scale(scale, scale)
        }

        view.draw(c)
        return dstBitmap
    }

    private fun crop(srcBmp: Bitmap, canvasView: View, downsampling: Int): Bitmap {
        val scale = 1f / downsampling
        return Bitmap.createBitmap(
                srcBmp,
                Math.floor((canvasView.x * scale).toDouble()).toInt(),
                Math.floor((canvasView.y * scale).toDouble()).toInt(),
                Math.floor((canvasView.width * scale).toDouble()).toInt(),
                Math.floor((canvasView.height * scale).toDouble()).toInt()
        )
    }

    override fun switchShowSettings() {
        settingsController.switchShow()
    }

    private inner class ScreenSlidePagerAdapter : PagerAdapter() {

        private var scrollViewLayout: FrameLayout? = null
        private var listViewLayout: FrameLayout? = null

        fun getView(position: Int): View {
            return when (position) {
                0 -> createImageView(R.drawable.photo3_med)
                1 -> createImageView(R.drawable.photo2_med)
                2 -> createImageView(R.drawable.photo4_med)
                3 -> createScrollView()
                4 -> createListView()
                else -> createImageView(R.drawable.photo1_med)
            }
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getCount(): Int {
            return 5
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val pager = container as ViewPager
            val view = getView(position)

            pager.addView(view)

            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }

        fun createImageView(drawableResId: Int): View {
            val imageView = ImageView(activity)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = ViewGroup.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT)
            Picasso.get().load(drawableResId).into(imageView)
            return imageView
        }

        fun createScrollView(): View {
            if (scrollViewLayout == null) {
                scrollViewLayout = (requireActivity().layoutInflater.inflate(R.layout.inc_scrollview, viewPager, false) as FrameLayout).apply {
                    (findViewById<View>(R.id.scrollview) as ObservableScrollView).setScrollViewListener(object : ObservableScrollView.ScrollViewListener {
                        override fun onScrollChanged(scrollView: ObservableScrollView, x: Int, y: Int, oldx: Int, oldy: Int) {
                            updateBlurView()
                        }
                    })
                    Picasso.get().load(R.drawable.photo1_med).into(findViewById<View>(R.id.photo1) as ImageView)
                    Picasso.get().load(R.drawable.photo2_med).into(findViewById<View>(R.id.photo2) as ImageView)
                }
            }
            return scrollViewLayout!!
        }

        fun createListView(): View {
            if (listViewLayout == null) {
                val list = ArrayList<String>()
                for (i in 0..19) {
                    list.add("This is a long line of text and so on $i")
                }
                listViewLayout = (requireActivity().layoutInflater.inflate(R.layout.inc_listview, viewPager, false) as FrameLayout).apply {
                    (findViewById<View>(R.id.listview) as ListView).adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, list)
                    (findViewById<View>(R.id.listview) as ListView).setOnScrollListener(object : AbsListView.OnScrollListener {
                        override fun onScrollStateChanged(absListView: AbsListView, i: Int) {}

                        override fun onScroll(absListView: AbsListView, i: Int, i2: Int, i3: Int) {
                            updateBlurView()
                        }
                    })
                }
            }
            return listViewLayout!!
        }
    }

    companion object {
        private val TAG = LiveBlurFragment::class.java.simpleName
    }
}
