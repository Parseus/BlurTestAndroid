package at.favre.app.blurbenchmark.util

import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GraphViewSeries

/**
 * Created by PatrickF on 23.04.2014.
 */
object GraphUtil {

    fun getStraightLine(heightY: Int, maxX: Int, name: String, seriesStyle: GraphViewSeries.GraphViewSeriesStyle): GraphViewSeries {
        val data = arrayOfNulls<GraphView.GraphViewData>(2)
        data[0] = GraphView.GraphViewData(0.0, heightY.toDouble())
        data[1] = GraphView.GraphViewData(maxX.toDouble(), heightY.toDouble())
        return GraphViewSeries(name, seriesStyle, data)
    }
}
