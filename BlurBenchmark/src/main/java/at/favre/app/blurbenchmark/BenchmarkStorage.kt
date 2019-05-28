package at.favre.app.blurbenchmark

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import at.favre.app.blurbenchmark.models.BenchmarkResultDatabase
import at.favre.app.blurbenchmark.models.BenchmarkWrapper
import at.favre.app.blurbenchmark.util.BitmapUtil
import at.favre.app.blurbenchmark.util.JsonUtil
import java.io.File

/**
 * This is responsible for storeing and retrieving the benchmark data.
 * As of now, this is a hack, since it stores the data in json format in
 * the shared preference and loads it as a whole in memory. This
 * appoach was used because it is easier to implement than a sophisticated
 * DB solution.
 *
 * @author pfavre
 */
class BenchmarkStorage private constructor(private val ctx: Context) {

    private var db: BenchmarkResultDatabase? = null

    private fun resetCache() {
        db = null
    }

    fun saveTest(wrapperList: List<BenchmarkWrapper>) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                // Restore preferences
                val settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                val resultsString = settings.getString(PREF_RESULTS, null)
                val db: BenchmarkResultDatabase

                db = if (resultsString == null) {
                    BenchmarkResultDatabase()
                } else {
                    JsonUtil.fromJsonString(resultsString, BenchmarkResultDatabase::class.java)
                }

                for (benchmarkWrapper in wrapperList) {
                    if (!benchmarkWrapper.statInfo.isError) {
                        val benchmarkEntry = BenchmarkResultDatabase.BenchmarkEntry(benchmarkWrapper)
                        if (db.entryList.contains(benchmarkEntry)) {
                            db.entryList[db.entryList.indexOf(benchmarkEntry)].wrapper.add(benchmarkWrapper)
                        } else {
                            while (benchmarkEntry.wrapper.size > MAX_SAVED_BENCHMARKS) {
                                benchmarkEntry.wrapper.removeAt(0)
                            }

                            benchmarkEntry.wrapper.add(benchmarkWrapper)
                            db.entryList.add(benchmarkEntry)
                        }
                    }
                }

                settings.edit().putString(PREF_RESULTS, JsonUtil.toJsonString(db)).commit()
                resetCache()
                return null
            }
        }.execute()

    }

    fun deleteData() {
        val settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        settings.edit().putString(PREF_RESULTS, JsonUtil.toJsonString(BenchmarkResultDatabase())).commit()
        BitmapUtil.clearCacheDir(File(BitmapUtil.getCacheDir(ctx)))
        resetCache()
    }

    fun loadResultsDB(): BenchmarkResultDatabase {
        if (db == null) {
            Log.d(TAG, "start load db")
            val settings = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val resultsString = settings.getString(PREF_RESULTS, null)
            if (resultsString != null) {
                db = JsonUtil.fromJsonString(resultsString, BenchmarkResultDatabase::class.java)
                Log.d(TAG, "done load db")
            } else {
                Log.d(TAG, "done load db")
            }
        }
        return db!!
    }

    open class AsyncLoadResults : AsyncTask<Context, Void, BenchmarkResultDatabase>() {
        override fun doInBackground(vararg ctx: Context): BenchmarkResultDatabase? {
            return BenchmarkStorage.getInstance(ctx[0]).loadResultsDB()
        }
    }

    companion object {
        private val TAG = BenchmarkStorage::class.java.simpleName
        private const val PREF_NAME = "at.favre.app.blurbenchmark.sharedpref"
        private const val PREF_RESULTS = "results"
        private const val MAX_SAVED_BENCHMARKS = 3

        private var ourInstance: BenchmarkStorage? = null

        fun getInstance(ctx: Context): BenchmarkStorage {
            if (ourInstance == null) {
                ourInstance = BenchmarkStorage(ctx)
            }
            return ourInstance!!
        }
    }
}
