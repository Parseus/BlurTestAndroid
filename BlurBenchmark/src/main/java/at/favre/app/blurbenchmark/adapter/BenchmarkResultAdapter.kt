package at.favre.app.blurbenchmark.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.models.BenchmarkWrapper

/**
 * Created by PatrickF on 25.05.2015.
 */
class BenchmarkResultAdapter(private val results: List<BenchmarkWrapper>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<BenchmarkResultHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): BenchmarkResultHolder {
        val inflater = viewGroup.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val convertView = inflater.inflate(R.layout.list_benchmark_result, viewGroup, false)
        return BenchmarkResultHolder(convertView, fragmentManager)
    }

    override fun onBindViewHolder(benchmarkResultHolder: BenchmarkResultHolder, i: Int) {
        benchmarkResultHolder.onBind(results[i])
    }

    override fun getItemCount(): Int {
        return results.size
    }
}
