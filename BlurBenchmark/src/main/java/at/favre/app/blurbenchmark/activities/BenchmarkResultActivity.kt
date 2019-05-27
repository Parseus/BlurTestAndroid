package at.favre.app.blurbenchmark.activities

import android.app.ActivityManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.fragments.BenchmarkResultFragment
import at.favre.app.blurbenchmark.models.BenchmarkResultList
import at.favre.app.blurbenchmark.util.JsonUtil
import kotlinx.android.synthetic.main.activity_benchmark_result.*
import kotlinx.android.synthetic.main.fragment_benchmark_results.*

class BenchmarkResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_benchmark_result)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setTaskDescription(ActivityManager.TaskDescription(getString(R.string.app_name),
                        R.mipmap.ic_launcher,
                        ContextCompat.getColor(this, R.color.color_primary_dark)))
            } else {
                @Suppress("DEPRECATION")
                setTaskDescription(ActivityManager.TaskDescription(getString(R.string.app_name),
                        BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher),
                        ContextCompat.getColor(this, R.color.color_primary_dark)))
            }
        }

        if (savedInstanceState == null) {
            val t = supportFragmentManager.beginTransaction()
            t.add(R.id.root, BenchmarkResultFragment.createInstance(JsonUtil.fromJsonString(intent.getStringExtra(BENCHMARK_LIST_KEY), BenchmarkResultList::class.java)), BenchmarkResultFragment::class.java.simpleName)
            t.commit()
        }
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            root.requestLayout()
            root.invalidate()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            if (Build.VERSION.SDK_INT >= 16) {
                NavUtils.navigateUpFromSameTask(this)
            } else {
                finish()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Benchmark Results"
            elevation = resources.getDimension(R.dimen.toolbar_elevation)
        }
    }

    companion object {
        const val BENCHMARK_LIST_KEY = "benchmark_list"
    }

}