package at.favre.app.blurbenchmark.fragments

import android.graphics.Paint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.blur.IBlur
import at.favre.app.blurbenchmark.models.BenchmarkWrapper
import at.favre.app.blurbenchmark.util.GraphUtil
import at.favre.app.blurbenchmark.util.JsonUtil
import com.jjoe64.graphview.CustomLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GraphViewSeries
import com.jjoe64.graphview.LineGraphView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_benchmark_details.*

/**
 * A dialog showing the blurred image and a graph
 * representing it's performance
 *
 * @author pfavre
 */
class BenchmarkDetailsDialog: DialogFragment() {

    private lateinit var wrapper: BenchmarkWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            wrapper = JsonUtil.fromJsonString(savedInstanceState.getString(WRAPPER_KEY)!!, BenchmarkWrapper::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_benchmark_details, container, false)
        Picasso.get().load(wrapper.bitmapAsFile).into(image)

        graph.addView(createGraph(wrapper))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return v
    }

    private fun createGraph(wrapper: BenchmarkWrapper): GraphView {
        val res = resources
        val lineThicknessPx = Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, res.displayMetrics).toDouble()).toInt()

        val data = Array(wrapper.statInfo.benchmarkData.size) { i ->
            GraphView.GraphViewData(i.toDouble(), wrapper.statInfo.benchmarkData[i])
        }
        
        return LineGraphView(requireContext(), "").apply {
            val seriesStyle = GraphViewSeries.GraphViewSeriesStyle(ContextCompat.getColor(requireContext(), R.color.graphBgGreen), lineThicknessPx)

            if (wrapper.statInfo.asAvg.min <= IBlur.MS_THRESHOLD_FOR_SMOOTH) {
                addSeries(GraphUtil.getStraightLine(IBlur.MS_THRESHOLD_FOR_SMOOTH, wrapper.statInfo.benchmarkData.size - 1, "16ms", GraphViewSeries.GraphViewSeriesStyle(ContextCompat.getColor(requireContext(), R.color.graphBgRed), lineThicknessPx)))
            }
            addSeries(GraphUtil.getStraightLine(wrapper.statInfo.asAvg.avg!!.toInt(), wrapper.statInfo.benchmarkData.size - 1, "Avg", GraphViewSeries.GraphViewSeriesStyle(ContextCompat.getColor(requireContext(), R.color.graphBlue), lineThicknessPx)))
            addSeries(GraphViewSeries("Blur", seriesStyle, data))
            isScrollable = true
            setScalable(true)
            setManualYAxis(true)
            graphViewStyle.gridColor = ContextCompat.getColor(requireContext(), R.color.transparent)
            customLabelFormatter = CustomLabelFormatter { value, isValueX ->
                if (!isValueX) {
                    Math.round(value).toString() + "ms"
                } else {
                    null
                }
            }
            setManualYAxisBounds(wrapper.statInfo.asAvg.max, Math.max(0.0, wrapper.statInfo.asAvg.min - 3L))
            drawBackground = false
            isShowLegend = true

            graphViewStyle.horizontalLabelsColor = ContextCompat.getColor(requireContext(), R.color.transparent)
            graphViewStyle.numHorizontalLabels = 0
            graphViewStyle.verticalLabelsColor = ContextCompat.getColor(requireContext(), R.color.optionsTextColorDark)
            graphViewStyle.numVerticalLabels = 4
            graphViewStyle.verticalLabelsAlign = Paint.Align.CENTER
            graphViewStyle.verticalLabelsWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, res.displayMetrics).toInt()
            graphViewStyle.textSize = Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, res.displayMetrics).toDouble()).toInt().toFloat()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(WRAPPER_KEY, JsonUtil.toJsonString(wrapper))
    }

    companion object {
        private const val WRAPPER_KEY = "wrapperKey"

        fun createInstance(wrapper: BenchmarkWrapper): BenchmarkDetailsDialog {
            val dialog = BenchmarkDetailsDialog()
            dialog.wrapper = wrapper
            return dialog
        }
    }

}