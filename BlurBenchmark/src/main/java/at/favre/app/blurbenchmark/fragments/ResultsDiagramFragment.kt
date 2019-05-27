package at.favre.app.blurbenchmark.fragments

import android.graphics.Paint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import at.favre.app.blurbenchmark.BenchmarkStorage
import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.blur.EBlurAlgorithm
import at.favre.app.blurbenchmark.blur.IBlur
import at.favre.app.blurbenchmark.models.BenchmarkResultDatabase
import at.favre.app.blurbenchmark.models.ResultTableModel
import at.favre.app.blurbenchmark.util.BenchmarkUtil
import at.favre.app.blurbenchmark.util.GraphUtil
import at.favre.app.blurbenchmark.util.TranslucentLayoutUtil
import com.jjoe64.graphview.CustomLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GraphViewSeries
import com.jjoe64.graphview.LineGraphView
import java.util.*

/**
 * Shows the results of past benchmarks in a Diagram, where misc. statistics can be
 * choosen.
 *
 * @author pfavre
 */
class ResultsDiagramFragment : Fragment() {

    private val dataTypeList = Arrays.asList<ResultTableModel.DataType>(*ResultTableModel.DataType.values())
    private lateinit var radiusList: List<Int>
    private lateinit var radiusSpinner: Spinner
    private lateinit var dataTypeSpinner: Spinner

    private var radius = 16
    private var dataType: ResultTableModel.DataType = ResultTableModel.DataType.AVG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            radius = savedInstanceState.getInt(RADIUS_KEY)
            dataType = ResultTableModel.DataType.valueOf(savedInstanceState.getString(DATATYPE_KEY)!!)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_resultsdiagram, container, false)
        v.findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
        v.findViewById<View>(R.id.contentWrapper).visibility = View.INVISIBLE

        object : BenchmarkStorage.AsyncLoadResults() {
            override fun onPostExecute(db: BenchmarkResultDatabase?) {
                if (isAdded && isVisible) {
                    v.findViewById<View>(R.id.progressBar).visibility = View.GONE
                    if (db == null) {
                        v.findViewById<View>(R.id.contentWrapper).visibility = View.GONE
                        v.findViewById<View>(R.id.tv_noresults).visibility = View.VISIBLE
                    } else {
                        createUI(v, db)
                    }
                }
            }
        }.execute(activity)

        TranslucentLayoutUtil.setTranslucentThemeInsets(requireActivity(), v.findViewById(R.id.contentWrapper))
        return v
    }

    private fun createUI(v: View, db: BenchmarkResultDatabase) {
        v.findViewById<View>(R.id.contentWrapper).visibility = View.VISIBLE

        radiusList = ArrayList(db.allBlurRadii)
        radiusSpinner = (v.findViewById<Spinner>(R.id.spinner_radius)).apply {
            adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, radiusList)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                    radius = adapterView.adapter.getItem(i) as Int
                    updateGraph()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {}
            }
            setSelection(radiusList.indexOf(radius))
        }

        dataTypeSpinner = (v.findViewById<Spinner>(R.id.spinner_datatypes)).apply {
            adapter = ArrayAdapter<ResultTableModel.DataType>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, dataTypeList)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                    dataType = adapterView.adapter.getItem(i) as ResultTableModel.DataType
                    updateGraph()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {}
            }
            setSelection(dataTypeList.indexOf(dataType))
        }
    }

    private fun updateGraph() {
        if (BenchmarkStorage.getInstance(requireActivity()).loadResultsDB() != null) {
            val layout = view!!.findViewById<FrameLayout>(R.id.graph)
            layout.removeAllViews()
            layout.addView(createGraph(BenchmarkStorage.getInstance(requireActivity()).loadResultsDB(), dataType, radius))
        }
    }

    private fun createGraph(database: BenchmarkResultDatabase, dataType: ResultTableModel.DataType, blurRadius: Int): GraphView {
        val res = resources
        val lineThicknessPx = Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, res.displayMetrics).toDouble()).toInt()

        val dataMap = HashMap<EBlurAlgorithm, MutableList<GraphView.GraphViewData>>()
        val imageSizes = ArrayList(database.allImageSizes)
        for (eBlurAlgorithm in EBlurAlgorithm.allAlgorithms) {
            dataMap[eBlurAlgorithm] = ArrayList()
            var i = 0
            for (imageSize in imageSizes) {
                val (value) = ResultTableModel.getValueForType(BenchmarkResultDatabase.getRecentWrapper(database.getByImageSizeAndRadiusAndAlgorithm(imageSize.imageSizeString, blurRadius, eBlurAlgorithm)), dataType)
                if (value != java.lang.Double.NEGATIVE_INFINITY) {
                    dataMap[eBlurAlgorithm]!!.add(i, GraphView.GraphViewData(i.toDouble(), value))
                    i++
                } else {
                    break
                }
            }
        }

        return LineGraphView(activity, "").apply {
            for (eBlurAlgorithm in dataMap.keys) {
                val seriesStyle = GraphViewSeries.GraphViewSeriesStyle(res.getColor(eBlurAlgorithm.colorResId), lineThicknessPx)
                addSeries(GraphViewSeries(eBlurAlgorithm.toString(), seriesStyle, dataMap[eBlurAlgorithm]!!.toTypedArray()))
            }

            isScrollable = true
            setScalable(true)
            drawBackground = false
            isShowLegend = true
            customLabelFormatter = CustomLabelFormatter { value, isValueX ->
                if (!isValueX) {
                    BenchmarkUtil.formatNum(value, "0.0") + dataType.unit
                } else {
                    if (imageSizes.isNotEmpty()) {
                        imageSizes[Math.round(value).toInt()].imageSizeString
                    } else ""
                }
            }
            if (dataType.unit.equals("ms", ignoreCase = true) && dataType.minIsBest && imageSizes.isNotEmpty()) {
                addSeries(GraphUtil.getStraightLine(IBlur.MS_THRESHOLD_FOR_SMOOTH, imageSizes.size - 1, "16ms", GraphViewSeries.GraphViewSeriesStyle(res.getColor(R.color.graphMidnightBlue), lineThicknessPx)))
            }
            graphViewStyle.run {
                horizontalLabelsColor = res.getColor(R.color.optionsTextColor)
                numHorizontalLabels = 6
                verticalLabelsColor = res.getColor(R.color.optionsTextColor)
                numVerticalLabels = 6
                verticalLabelsAlign = Paint.Align.CENTER
                verticalLabelsWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54f, res.displayMetrics).toInt()
                textSize = Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9f, res.displayMetrics).toDouble()).toInt().toFloat()
                legendWidth = Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 115f, res.displayMetrics).toDouble()).toInt()
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(DATATYPE_KEY, dataType.toString())
        outState.putInt(RADIUS_KEY, radius)
    }

    companion object {
        @Suppress("unused")
        private val TAG = ResultsDiagramFragment::class.java.simpleName

        private const val DATATYPE_KEY = "DATATYPE_KEY"
        private const val RADIUS_KEY = "RADIUS_KEY"
    }
}
