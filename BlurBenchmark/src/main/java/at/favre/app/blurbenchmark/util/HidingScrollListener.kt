package at.favre.app.blurbenchmark.util

import android.content.res.Resources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue

/**
 * Created by PatrickF on 27.05.2015.
 */
abstract class HidingScrollListener : RecyclerView.OnScrollListener() {
    private var scrolledDistance = 0
    private var controlsVisible = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val firstVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        //show views if first item is first visible position and views are hidden
        if (firstVisibleItem == 0) {
            if (!controlsVisible) {
                onShow()
                controlsVisible = true
            }
        } else {
            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                onHide()
                controlsVisible = false
                scrolledDistance = 0
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                onShow()
                controlsVisible = true
                scrolledDistance = 0
            }
        }

        if (controlsVisible && dy > 0 || !controlsVisible && dy < 0) {
            scrolledDistance += dy
        }
    }

    abstract fun onHide()

    abstract fun onShow()

    companion object {
        private val HIDE_THRESHOLD = getHideThreshold(60)

        private fun getHideThreshold(valueInDP: Int): Int {
            return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDP.toFloat(), Resources.getSystem().displayMetrics))
        }
    }

}
