package com.inqbarna.tablefixheaders

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Scroller

import com.inqbarna.tablefixheaders.adapters.TableAdapter

import java.util.ArrayList

/**
 * This view shows a table which can scroll in both directions. Also still
 * leaves the headers fixed.
 *
 * @author Brais Gabn (InQBarna)
 */
class TableFixHeaders @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ViewGroup(context, attrs, defStyle) {
    private var currentX: Int = 0
    private var currentY: Int = 0

    /**
     * Returns the adapter currently associated with this widget.
     *
     * @return The adapter used to provide this view's content.
     */
    /**
     * Sets the data behind this TableFixHeaders.
     *
     * @param adapter
     * The TableAdapter which is responsible for maintaining the data
     * backing this list and for producing a view to represent an
     * item in that data set.
     */
    var adapter: TableAdapter? = null
        set(adapter) {
            if (field != null) {
                field!!.unregisterDataSetObserver(tableAdapterDataSetObserver!!)
            }

            field = adapter
            tableAdapterDataSetObserver = TableAdapterDataSetObserver()
            field?.registerDataSetObserver(tableAdapterDataSetObserver!!)

            recycler = Recycler(field!!.viewTypeCount)

            xScroll = 0
            yScroll = 0
            firstColumn = 0
            firstRow = 0

            needRelayout = true
            requestLayout()
        }
    private var xScroll: Int = 0
    private var yScroll: Int = 0
    private var firstRow: Int = 0
    private var firstColumn: Int = 0
    private var widths: IntArray? = null
    private var heights: IntArray? = null

    private var headView: View? = null
    private val rowViewList: MutableList<View>
    private val columnViewList: MutableList<View>
    private val bodyViewTable: MutableList<MutableList<View>>

    private var rowCount: Int = 0
    private var columnCount: Int = 0

    private var totalWidth: Int = 0
    private var totalHeight: Int = 0

    private var recycler: Recycler? = null

    private var tableAdapterDataSetObserver: TableAdapterDataSetObserver? = null
    private var needRelayout: Boolean = false

    private val shadows: Array<ImageView>
    private val shadowSize: Int

    private val minimumVelocity: Int
    private val maximumVelocity: Int

    private val flinger: Flinger

    private var velocityTracker: VelocityTracker? = null

    private val touchSlop: Int

    private val actualScrollX: Int
        get() = xScroll + sumArray(widths, 1, firstColumn)

    private val actualScrollY: Int
        get() = yScroll + sumArray(heights, 1, firstRow)

    private val maxScrollX: Int
        get() = Math.max(0, sumArray(widths) - totalWidth)

    private val maxScrollY: Int
        get() = Math.max(0, sumArray(heights) - totalHeight)

    private val filledWidth: Int
        get() = widths!![0] + sumArray(widths, firstColumn + 1, rowViewList.size) - xScroll

    private val filledHeight: Int
        get() = heights!![0] + sumArray(heights, firstRow + 1, columnViewList.size) - yScroll

    init {

        headView = null
        rowViewList = ArrayList()
        columnViewList = ArrayList()
        bodyViewTable = ArrayList()

        needRelayout = true

        shadows = Array(4) { ImageView(context) }
        shadows[0] = ImageView(context)
        shadows[0].setImageResource(R.drawable.shadow_left)
        shadows[1] = ImageView(context)
        shadows[1].setImageResource(R.drawable.shadow_top)
        shadows[2] = ImageView(context)
        shadows[2].setImageResource(R.drawable.shadow_right)
        shadows[3] = ImageView(context)
        shadows[3].setImageResource(R.drawable.shadow_bottom)

        shadowSize = resources.getDimensionPixelSize(R.dimen.shadow_size)

        flinger = Flinger(context)
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
        minimumVelocity = configuration.scaledMinimumFlingVelocity
        maximumVelocity = configuration.scaledMaximumFlingVelocity
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        var intercept = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentX = event.rawX.toInt()
                currentY = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val x2 = Math.abs(currentX - event.rawX.toInt())
                val y2 = Math.abs(currentY - event.rawY.toInt())
                if (x2 > touchSlop || y2 > touchSlop) {
                    intercept = true
                }
            }
        }
        return intercept
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (velocityTracker == null) { // If we do not have velocity tracker
            velocityTracker = VelocityTracker.obtain() // then get one
        }
        velocityTracker!!.addMovement(event) // add this movement to it

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!flinger.isFinished) { // If scrolling, then stop now
                    flinger.forceFinished()
                }
                currentX = event.rawX.toInt()
                currentY = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val x2 = event.rawX.toInt()
                val y2 = event.rawY.toInt()
                val diffX = currentX - x2
                val diffY = currentY - y2
                currentX = x2
                currentY = y2

                scrollBy(diffX, diffY)
            }
            MotionEvent.ACTION_UP -> {
                var tracker = velocityTracker
                tracker?.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                val velocityX = tracker?.xVelocity?.toInt() ?: 0
                val velocityY = tracker?.yVelocity?.toInt() ?: 0

                if (Math.abs(velocityX) > minimumVelocity || Math.abs(velocityY) > minimumVelocity) {
                    flinger.start(actualScrollX, actualScrollY, velocityX, velocityY, maxScrollX, maxScrollY)
                } else {
                    if (tracker != null) { // If the velocity less than threshold
                        tracker.recycle() // recycle the tracker
                        tracker = null
                    }
                }
            }
        }
        return true
    }

    override fun scrollTo(x: Int, y: Int) {
        if (needRelayout) {
            xScroll = x
            firstColumn = 0

            yScroll = y
            firstRow = 0
        } else {
            scrollBy(x - sumArray(widths, 1, firstColumn) - xScroll, y - sumArray(heights, 1, firstRow) - yScroll)
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        xScroll += x
        yScroll += y

        if (needRelayout) {
            return
        }

        scrollBounds()

        /*
		 * TODO Improve the algorithm. Think big diagonal movements. If we are
		 * in the top left corner and scrollBy to the opposite corner. We will
		 * have created the views from the top right corner on the X part and we
		 * will have eliminated to generate the right at the Y.
		 */
        if (xScroll == 0) {
            // no op
        } else if (xScroll > 0) {
            while (widths!![firstColumn + 1] < xScroll) {
                if (rowViewList.isNotEmpty()) {
                    removeLeft()
                }
                xScroll -= widths!![firstColumn + 1]
                firstColumn++
            }
            while (filledWidth < totalWidth) {
                addRight()
            }
        } else {
            while (rowViewList.isNotEmpty() && filledWidth - widths!![firstColumn + rowViewList.size] >= totalWidth) {
                removeRight()
            }
            if (rowViewList.isEmpty()) {
                while (xScroll < 0) {
                    firstColumn--
                    xScroll += widths!![firstColumn + 1]
                }
                while (filledWidth < totalWidth) {
                    addRight()
                }
            } else {
                while (0 > xScroll) {
                    addLeft()
                    firstColumn--
                    xScroll += widths!![firstColumn + 1]
                }
            }
        }

        if (yScroll == 0) {
            // no op
        } else if (yScroll > 0) {
            while (heights!![firstRow + 1] < yScroll) {
                if (columnViewList.isNotEmpty()) {
                    removeTop()
                }
                yScroll -= heights!![firstRow + 1]
                firstRow++
            }
            while (filledHeight < totalHeight) {
                addBottom()
            }
        } else {
            while (columnViewList.isNotEmpty() && filledHeight - heights!![firstRow + columnViewList.size] >= totalHeight) {
                removeBottom()
            }
            if (columnViewList.isEmpty()) {
                while (yScroll < 0) {
                    firstRow--
                    yScroll += heights!![firstRow + 1]
                }
                while (filledHeight < totalHeight) {
                    addBottom()
                }
            } else {
                while (0 > yScroll) {
                    addTop()
                    firstRow--
                    yScroll += heights!![firstRow + 1]
                }
            }
        }

        repositionViews()

        shadowsVisibility()
    }

    private fun addLeft() {
        addLeftOrRight(firstColumn - 1, 0)
    }

    private fun addTop() {
        addTopAndBottom(firstRow - 1, 0)
    }

    private fun addRight() {
        val size = rowViewList.size
        addLeftOrRight(firstColumn + size, size)
    }

    private fun addBottom() {
        val size = columnViewList.size
        addTopAndBottom(firstRow + size, size)
    }

    private fun addLeftOrRight(column: Int, index: Int) {
        var view = makeView(-1, column, widths!![column + 1], heights!![0])
        rowViewList.add(index, view)

        var i = firstRow
        for (list in bodyViewTable) {
            view = makeView(i, column, widths!![column + 1], heights!![i + 1])
            list.add(index, view)
            i++
        }
    }

    private fun addTopAndBottom(row: Int, index: Int) {
        var view = makeView(row, -1, widths!![0], heights!![row + 1])
        columnViewList.add(index, view)

        val list = ArrayList<View>()
        val size = rowViewList.size + firstColumn
        for (i in firstColumn until size) {
            view = makeView(row, i, widths!![i + 1], heights!![row + 1])
            list.add(view)
        }
        bodyViewTable.add(index, list)
    }

    private fun removeLeft() {
        removeLeftOrRight(0)
    }

    private fun removeTop() {
        removeTopOrBottom(0)
    }

    private fun removeRight() {
        removeLeftOrRight(rowViewList.size - 1)
    }

    private fun removeBottom() {
        removeTopOrBottom(columnViewList.size - 1)
    }

    private fun removeLeftOrRight(position: Int) {
        removeView(rowViewList.removeAt(position))
        for (list in bodyViewTable) {
            removeView(list.removeAt(position))
        }
    }

    private fun removeTopOrBottom(position: Int) {
        removeView(columnViewList.removeAt(position))
        val remove = bodyViewTable.removeAt(position)
        for (view in remove) {
            removeView(view)
        }
    }

    override fun removeView(view: View) {
        super.removeView(view)

        val typeView = view.getTag(R.id.tag_type_view) as Int
        if (typeView != TableAdapter.IGNORE_ITEM_VIEW_TYPE) {
            recycler!!.addRecycledView(view, typeView)
        }
    }

    private fun repositionViews() {
        var left: Int
        var top: Int
        var right: Int
        var bottom: Int
        var i: Int = firstColumn

        left = widths!![0] - xScroll
        for (view in rowViewList) {
            right = left + widths!![++i]
            view.layout(left, 0, right, heights!![0])
            left = right
        }

        top = heights!![0] - yScroll
        i = firstRow
        for (view in columnViewList) {
            bottom = top + heights!![++i]
            view.layout(0, top, widths!![0], bottom)
            top = bottom
        }

        top = heights!![0] - yScroll
        i = firstRow
        for (list in bodyViewTable) {
            bottom = top + heights!![++i]
            left = widths!![0] - xScroll
            var j = firstColumn
            for (view in list) {
                right = left + widths!![++j]
                view.layout(left, top, right, bottom)
                left = right
            }
            top = bottom
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val w: Int
        val h: Int

        if (adapter != null) {
            rowCount = adapter!!.rowCount
            columnCount = adapter!!.columnCount

            widths = IntArray(columnCount + 1)
            for (i in -1 until columnCount) {
                widths!![i + 1] += adapter!!.getWidth(i)
            }
            heights = IntArray(rowCount + 1)
            for (i in -1 until rowCount) {
                heights!![i + 1] += adapter!!.getHeight(i)
            }

            if (widthMode == MeasureSpec.AT_MOST) {
                w = Math.min(widthSize, sumArray(widths))
            } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                w = sumArray(widths)
            } else {
                w = widthSize
                val sumArray = sumArray(widths)
                if (sumArray < widthSize) {
                    val factor = widthSize / sumArray.toFloat()
                    for (i in 1 until widths!!.size) {
                        widths!![i] = Math.round(widths!![i] * factor)
                    }
                    widths!![0] = widthSize - sumArray(widths, 1, widths!!.size - 1)
                }
            }

            h = when (heightMode) {
                MeasureSpec.AT_MOST -> Math.min(heightSize, sumArray(heights))
                MeasureSpec.UNSPECIFIED -> sumArray(heights)
                else -> heightSize
            }
        } else {
            if (heightMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
                w = 0
                h = 0
            } else {
                w = widthSize
                h = heightSize
            }
        }

        if (firstRow >= rowCount || maxScrollY - actualScrollY < 0) {
            firstRow = 0
            yScroll = Integer.MAX_VALUE
        }
        if (firstColumn >= columnCount || maxScrollX - actualScrollX < 0) {
            firstColumn = 0
            xScroll = Integer.MAX_VALUE
        }

        setMeasuredDimension(w, h)
    }

    private fun sumArray(array: IntArray?, firstIndex: Int = 0, count: Int = array!!.size): Int {
        var newCount = count
        var sum = 0
        newCount += firstIndex
        for (i in firstIndex until newCount) {
            sum += array!![i]
        }
        return sum
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (needRelayout || changed) {
            needRelayout = false
            resetTable()

            if (adapter != null) {
                totalWidth = r - l
                totalHeight = b - t

                var left: Int
                var top: Int
                var right: Int
                var bottom: Int

                right = Math.min(totalWidth, sumArray(widths))
                bottom = Math.min(totalHeight, sumArray(heights))
                addShadow(shadows[0], widths!![0], 0, widths!![0] + shadowSize, bottom)
                addShadow(shadows[1], 0, heights!![0], right, heights!![0] + shadowSize)
                addShadow(shadows[2], right - shadowSize, 0, right, bottom)
                addShadow(shadows[3], 0, bottom - shadowSize, right, bottom)

                headView = makeAndSetup(-1, -1, 0, 0, widths!![0], heights!![0])

                scrollBounds()
                adjustFirstCellsAndScroll()

                left = widths!![0] - xScroll
                run {
                    var i = firstColumn
                    while (i < columnCount && left < totalWidth) {
                        right = left + widths!![i + 1]
                        val view = makeAndSetup(-1, i, left, 0, right, heights!![0])
                        rowViewList.add(view)
                        left = right
                        i++
                    }
                }

                top = heights!![0] - yScroll
                run {
                    var i = firstRow
                    while (i < rowCount && top < totalHeight) {
                        bottom = top + heights!![i + 1]
                        val view = makeAndSetup(i, -1, 0, top, widths!![0], bottom)
                        columnViewList.add(view)
                        top = bottom
                        i++
                    }
                }

                top = heights!![0] - yScroll
                var i = firstRow
                while (i < rowCount && top < totalHeight) {
                    bottom = top + heights!![i + 1]
                    left = widths!![0] - xScroll
                    val list = ArrayList<View>()
                    var j = firstColumn
                    while (j < columnCount && left < totalWidth) {
                        right = left + widths!![j + 1]
                        val view = makeAndSetup(i, j, left, top, right, bottom)
                        list.add(view)
                        left = right
                        j++
                    }
                    bodyViewTable.add(list)
                    top = bottom
                    i++
                }

                shadowsVisibility()
            }
        }
    }

    private fun scrollBounds() {
        xScroll = scrollBounds(xScroll, firstColumn, widths, totalWidth)
        yScroll = scrollBounds(yScroll, firstRow, heights, totalHeight)
    }

    private fun scrollBounds(desiredScroll: Int, firstCell: Int, sizes: IntArray?, viewSize: Int): Int {
        var desiredScroll = desiredScroll
        when {
            desiredScroll == 0 -> {
                // no op
            }
            desiredScroll < 0 -> desiredScroll = Math.max(desiredScroll, -sumArray(sizes, 1, firstCell))
            else -> desiredScroll = Math.min(desiredScroll, Math.max(0, sumArray(sizes, firstCell + 1, sizes!!.size - 1 - firstCell) + sizes[0] - viewSize))
        }
        return desiredScroll
    }

    private fun adjustFirstCellsAndScroll() {
        var values: IntArray = adjustFirstCellsAndScroll(xScroll, firstColumn, widths)

        xScroll = values[0]
        firstColumn = values[1]

        values = adjustFirstCellsAndScroll(yScroll, firstRow, heights)
        yScroll = values[0]
        firstRow = values[1]
    }

    private fun adjustFirstCellsAndScroll(scroll: Int, firstCell: Int, sizes: IntArray?): IntArray {
        var scroll = scroll
        var firstCell = firstCell
        when {
            scroll == 0 -> {
                // no op
            }
            scroll > 0 -> while (sizes!![firstCell + 1] < scroll) {
                firstCell++
                scroll -= sizes[firstCell]
            }
            else -> while (scroll < 0) {
                scroll += sizes!![firstCell]
                firstCell--
            }
        }
        return intArrayOf(scroll, firstCell)
    }

    private fun shadowsVisibility() {
        val actualScrollX = actualScrollX
        val actualScrollY = actualScrollY
        val remainPixels = intArrayOf(actualScrollX, actualScrollY, maxScrollX - actualScrollX, maxScrollY - actualScrollY)

        for (i in shadows.indices) {
            setAlpha(shadows[i], Math.min(remainPixels[i] / shadowSize.toFloat(), 1f))
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setAlpha(imageView: ImageView, alpha: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            imageView.alpha = alpha
        } else {
            imageView.setAlpha(Math.round(alpha * 255))
        }
    }

    private fun addShadow(imageView: ImageView, l: Int, t: Int, r: Int, b: Int) {
        imageView.layout(l, t, r, b)
        addView(imageView)
    }

    private fun resetTable() {
        headView = null
        rowViewList.clear()
        columnViewList.clear()
        bodyViewTable.clear()

        removeAllViews()
    }

    private fun makeAndSetup(row: Int, column: Int, left: Int, top: Int, right: Int, bottom: Int): View {
        val view = makeView(row, column, right - left, bottom - top)
        view.layout(left, top, right, bottom)
        return view
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val ret: Boolean

        val row = child.getTag(R.id.tag_row) as Int?
        val column = child.getTag(R.id.tag_column) as Int?
        // row == null => Shadow view
        if (row == null || row == -1 && column == -1) {
            ret = super.drawChild(canvas, child, drawingTime)
        } else {
            canvas.save()
            when {
                row == -1 -> canvas.clipRect(widths!![0], 0, canvas.width, canvas.height)
                column == -1 -> canvas.clipRect(0, heights!![0], canvas.width, canvas.height)
                else -> canvas.clipRect(widths!![0], heights!![0], canvas.width, canvas.height)
            }

            ret = super.drawChild(canvas, child, drawingTime)
            canvas.restore()
        }
        return ret
    }

    private fun makeView(row: Int, column: Int, w: Int, h: Int): View {
        val itemViewType = adapter!!.getItemViewType(row, column)
        val recycledView: View? = if (itemViewType == TableAdapter.IGNORE_ITEM_VIEW_TYPE) {
            null
        } else {
            recycler!!.getRecycledView(itemViewType)
        }
        return adapter!!.getView(row, column, recycledView, this).apply {
            setTag(R.id.tag_type_view, itemViewType)
            setTag(R.id.tag_row, row)
            setTag(R.id.tag_column, column)
            measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))
            addTableView(this, row, column)
        }
    }

    private fun addTableView(view: View, row: Int, column: Int) {
        if (row == -1 && column == -1) {
            addView(view, childCount - 4)
        } else if (row == -1 || column == -1) {
            addView(view, childCount - 5)
        } else {
            addView(view, 0)
        }
    }

    private inner class TableAdapterDataSetObserver : DataSetObserver() {

        override fun onChanged() {
            needRelayout = true
            requestLayout()
        }

        override fun onInvalidated() {
            // Do nothing
        }
    }

    // http://stackoverflow.com/a/6219382/842697
    private inner class Flinger internal constructor(context: Context) : Runnable {
        private val scroller: Scroller = Scroller(context)

        private var lastX = 0
        private var lastY = 0

        internal val isFinished: Boolean
            get() = scroller.isFinished

        internal fun start(initX: Int, initY: Int, initialVelocityX: Int, initialVelocityY: Int, maxX: Int, maxY: Int) {
            scroller.fling(initX, initY, initialVelocityX, initialVelocityY, 0, maxX, 0, maxY)

            lastX = initX
            lastY = initY
            post(this)
        }

        override fun run() {
            if (scroller.isFinished) {
                return
            }

            val more = scroller.computeScrollOffset()
            val x = scroller.currX
            val y = scroller.currY
            val diffX = lastX - x
            val diffY = lastY - y
            if (diffX != 0 || diffY != 0) {
                scrollBy(diffX, diffY)
                lastX = x
                lastY = y
            }

            if (more) {
                post(this)
            }
        }

        internal fun forceFinished() {
            if (!scroller.isFinished) {
                scroller.forceFinished(true)
            }
        }
    }
}