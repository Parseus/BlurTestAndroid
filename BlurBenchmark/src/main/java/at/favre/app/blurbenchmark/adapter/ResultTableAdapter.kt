package at.favre.app.blurbenchmark.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.activities.MainActivity
import at.favre.app.blurbenchmark.fragments.BenchmarkDetailsDialog
import at.favre.app.blurbenchmark.models.BenchmarkResultDatabase
import at.favre.app.blurbenchmark.models.ResultTableModel
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter

/**
 * Created by PatrickF on 18.04.2014.
 */
class ResultTableAdapter(private val ctx: Context, db: BenchmarkResultDatabase, private val dataType: ResultTableModel.DataType) : BaseTableAdapter() {

    private val model: ResultTableModel = ResultTableModel(db)

    override val rowCount = model.rows.size
    override val columnCount = model.columns.size

    override fun getView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewType = getItemViewType(row, column)

        if (convertView == null) {
            val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layoutId: Int = when (viewType) {
                0 -> R.layout.inc_result_column_header
                1 -> R.layout.inc_result_row_header
                2 -> R.layout.inc_result_cell
                else -> throw IllegalArgumentException("Could not get layout for table cell")
            }

            convertView = inflater.inflate(layoutId, parent, false)
        }
        convertView!!.setOnClickListener(null)
        if (viewType == 2) {
            when (model.getRelativeType(row, column, dataType, dataType.minIsBest)) {
                ResultTableModel.RelativeType.BEST -> convertView.findViewById<TextView>(R.id.text).setTextColor(ContextCompat.getColor(ctx, R.color.graphBgGreen))
                ResultTableModel.RelativeType.WORST -> convertView.findViewById<TextView>(R.id.text).setTextColor(ContextCompat.getColor(ctx, R.color.graphBgRed))
                else -> convertView.findViewById<TextView>(R.id.text).setTextColor(ContextCompat.getColor(ctx, R.color.tableCellTextColor))
            }
            val value = model.getCell(row, column)
            convertView.tag = value
            if (BenchmarkResultDatabase.getRecentWrapper(value) != null) {
                convertView.setOnClickListener { view ->
                    val dialog = BenchmarkDetailsDialog.createInstance(BenchmarkResultDatabase.getRecentWrapper(view.tag as BenchmarkResultDatabase.BenchmarkEntry)!!)
                    dialog.show((ctx as FragmentActivity).supportFragmentManager, MainActivity.DIALOG_TAG)
                }
            }
        }

        convertView.findViewById<TextView>(R.id.text).text = getText(row, column)
        return convertView
    }

    private fun getText(row: Int, column: Int): String {
        return if (row < 0 && column < 0) {
            ""
        } else if (row < 0) {
            model.columns[column]
        } else if (column < 0) {
            model.rows[row]
        } else {
            model.getValue(row, column, dataType)
        }
    }

    override fun getWidth(column: Int): Int {
        return if (column < 0) {
            ctx.resources.getDimension(R.dimen.table_row_header_width).toInt()
        } else {
            ctx.resources.getDimension(R.dimen.table_cell_width).toInt()
        }
    }

    override fun getHeight(row: Int): Int {
        return if (row < 0) {
            ctx.resources.getDimension(R.dimen.table_column_header_height).toInt()
        } else {
            ctx.resources.getDimension(R.dimen.table_cell_height).toInt()
        }
    }

    override fun getItemViewType(row: Int, column: Int): Int {
        if (row < 0) {
            return 0
        }
        return if (column < 0) {
            1
        } else {
            2
        }
    }

    override val viewTypeCount = 3
}
