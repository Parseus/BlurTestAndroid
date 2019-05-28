package at.favre.app.blurbenchmark.fragments

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat

import com.inqbarna.tablefixheaders.TableFixHeaders

import at.favre.app.blurbenchmark.BenchmarkStorage
import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.adapter.ResultTableAdapter
import at.favre.app.blurbenchmark.models.BenchmarkResultDatabase
import at.favre.app.blurbenchmark.models.ResultTableModel
import at.favre.app.blurbenchmark.util.TranslucentLayoutUtil

/**
 * A table view that shows misc. statistics for the benchmark categories (size& blur radius)
 *
 * @author pfavre
 */
class ResultsBrowserFragment : Fragment() {

    private lateinit var table: TableFixHeaders
    private var dataType: ResultTableModel.DataType = ResultTableModel.DataType.AVG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_resultbrowser, container, false)
        table = v.findViewById(R.id.table)
        table.visibility = View.GONE

        v.findViewById<View>(R.id.progressBar).visibility = View.VISIBLE

        object : BenchmarkStorage.AsyncLoadResults() {
            override fun onPostExecute(benchmarkResultDatabase: BenchmarkResultDatabase?) {
                if (isAdded && isVisible) {
                    v.findViewById<View>(R.id.progressBar).visibility = View.GONE
                    if (benchmarkResultDatabase == null) {
                        table.visibility = View.GONE
                        v.findViewById<View>(R.id.tv_noresults).visibility = View.VISIBLE
                    } else {
                        table.visibility = View.VISIBLE
                        table.adapter = ResultTableAdapter(requireActivity(), benchmarkResultDatabase, dataType)
                        TranslucentLayoutUtil.setTranslucentThemeInsets(requireActivity(), v.findViewById(R.id.tableWrapper))
                    }
                }
            }
        }.execute(activity)

        return v
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.results_menu, menu)
        (menu!!.findItem(R.id.action_select_datatype).actionView as Spinner).apply {
            val adapter = ArrayAdapter<ResultTableModel.DataType>((requireActivity() as AppCompatActivity).supportActionBar!!.themedContext, R.layout.inc_spinner_light, ResultTableModel.DataType.values())
            this.adapter = adapter

            if (Build.VERSION.SDK_INT >= 16) {
                setPopupBackgroundDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.spinner_popup_dark))
            }
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                    setNewDataType(adapterView.adapter.getItem(i) as ResultTableModel.DataType)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {}
            }
            gravity = Gravity.RIGHT
        }
    }

    private fun setNewDataType(type: ResultTableModel.DataType) {
        dataType = type
        table.adapter = ResultTableAdapter(requireActivity(), BenchmarkStorage.getInstance(requireActivity()).loadResultsDB(), dataType)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                deleteData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteData() {
        BenchmarkStorage.getInstance(requireActivity()).deleteData()
        table.adapter = ResultTableAdapter(requireActivity(), BenchmarkStorage.getInstance(requireActivity()).loadResultsDB(), dataType)
    }

    companion object {
        @Suppress("unused")
        private val TAG = ResultsBrowserFragment::class.java.simpleName
    }
}
