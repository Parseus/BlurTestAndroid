package at.favre.app.blurbenchmark.fragments

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.activities.BenchmarkResultActivity
import at.favre.app.blurbenchmark.adapter.BenchmarkResultAdapter
import at.favre.app.blurbenchmark.adapter.BenchmarkResultHolder
import at.favre.app.blurbenchmark.models.BenchmarkResultList
import at.favre.app.blurbenchmark.util.JsonUtil
import com.squareup.picasso.Picasso
import java.io.IOException

/**
 * This will show the result of a benchmark in a ListView
 * with some statistics.
 *
 * @author pfavre
 */
class BenchmarkResultFragment : Fragment() {

    private var benchmarkResultList = BenchmarkResultList()

    private var adapter: RecyclerView.Adapter<BenchmarkResultHolder>? = null
    private lateinit var recyclerView: RecyclerView
    private var toolbar: Toolbar? = null

    fun setBenchmarkResultList(benchmarkResultList: BenchmarkResultList) {
        this.benchmarkResultList = benchmarkResultList
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            benchmarkResultList = JsonUtil.fromJsonString(savedInstanceState.getString(BenchmarkResultActivity.BENCHMARK_LIST_KEY)!!, BenchmarkResultList::class.java)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_benchmark_results, container, false)

        toolbar = v.findViewById(R.id.toolbar)

        recyclerView = v.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        setUpListView()
        return v
    }

    private fun setUpListView() {
        if (benchmarkResultList.benchmarkWrappers.isNotEmpty()) {
            adapter = BenchmarkResultAdapter(benchmarkResultList.benchmarkWrappers, requireActivity().supportFragmentManager)
            recyclerView.adapter = adapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as BenchmarkResultActivity).setupToolbar()
    }

    override fun onResume() {
        super.onResume()
        setBackground()
    }

    private fun setBackground() {
        if (benchmarkResultList.benchmarkWrappers.isNotEmpty() && !benchmarkResultList.benchmarkWrappers[benchmarkResultList.benchmarkWrappers.size - 1].statInfo.isError) {
            object : AsyncTask<Void, Void, Bitmap>() {
                override fun doInBackground(vararg voids: Void): Bitmap? {
                    return try {
                        val size = Point()
                        requireActivity().windowManager.defaultDisplay.getSize(size)
                        Picasso.get().load(benchmarkResultList.benchmarkWrappers[benchmarkResultList.benchmarkWrappers.size - 1].bitmapAsFile).noFade().resize(size.x, size.y).centerCrop().get()
                    } catch (e: IOException) {
                        Log.w(TAG, "Could not set background", e)
                        null
                    }

                }

                override fun onPostExecute(bitmap: Bitmap) {
                    if (view != null) {
                        val bitmapDrawable = BitmapDrawable(requireActivity().resources, bitmap)
                        ViewCompat.setBackground(view!!.rootView, LayerDrawable(arrayOf(bitmapDrawable, ColorDrawable(ContextCompat.getColor(requireActivity(), R.color.transparent)))))
                    }
                }
            }.execute()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BenchmarkResultActivity.BENCHMARK_LIST_KEY, JsonUtil.toJsonString(benchmarkResultList))
    }

    companion object {
        private val TAG = BenchmarkResultFragment::class.java.simpleName

        fun createInstance(resultList: BenchmarkResultList): BenchmarkResultFragment {
            val fragment = BenchmarkResultFragment()
            fragment.setBenchmarkResultList(resultList)
            return fragment
        }
    }
}
