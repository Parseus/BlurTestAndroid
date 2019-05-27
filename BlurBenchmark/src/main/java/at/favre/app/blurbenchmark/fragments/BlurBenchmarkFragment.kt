package at.favre.app.blurbenchmark.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import java.io.File
import java.util.ArrayList
import java.util.Arrays

import at.favre.app.blurbenchmark.BenchmarkStorage
import at.favre.app.blurbenchmark.BlurBenchmarkTask
import at.favre.app.blurbenchmark.BuildConfig
import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.activities.BenchmarkResultActivity
import at.favre.app.blurbenchmark.activities.MainActivity
import at.favre.app.blurbenchmark.blur.EBlurAlgorithm
import at.favre.app.blurbenchmark.models.BenchmarkImage
import at.favre.app.blurbenchmark.models.BenchmarkResultList
import at.favre.app.blurbenchmark.models.BenchmarkWrapper
import at.favre.app.blurbenchmark.util.BenchmarkUtil
import at.favre.app.blurbenchmark.util.JsonUtil

/**
 * The main view, where you may start a benchmark with custom settings
 *
 * @author pfavre
 */
class BlurBenchmarkFragment : Fragment() {

    private var rounds: Int = 0
    private var run = false
    private var benchmarkResultList = BenchmarkResultList()
    private var task: BlurBenchmarkTask? = null
    private var customPicturePaths: MutableList<File> = ArrayList()

    private lateinit var roundsSpinner: Spinner

    private lateinit var cBradius4px: CheckBox
    private lateinit var cBradius8px: CheckBox
    private lateinit var cBradius16px: CheckBox
    private lateinit var cBradius24px: CheckBox

    private lateinit var cbSize100: CheckBox
    private lateinit var cbSize200: CheckBox
    private lateinit var cbSize300: CheckBox
    private lateinit var cbSize400: CheckBox
    private lateinit var cbSize500: CheckBox
    private lateinit var cbSize600: CheckBox

    private lateinit var algorithmGroup: ViewGroup

    private var progressDialog: ProgressDialog? = null
    private lateinit var fab: FloatingActionButton

    private val imagesFromSettings: MutableList<BenchmarkImage>
        get() {
            val images = ArrayList<BenchmarkImage>()
            if (cbSize100.isChecked) {
                images.add(BenchmarkImage(R.drawable.test_100x100_2))
            }
            if (cbSize200.isChecked) {
                images.add(BenchmarkImage(R.drawable.test_200x200_2))
            }
            if (cbSize300.isChecked) {
                images.add(BenchmarkImage(R.drawable.test_300x300_2))
            }
            if (cbSize400.isChecked) {
                images.add(BenchmarkImage(R.drawable.test_400x400_2))
            }
            if (cbSize500.isChecked) {
                images.add(BenchmarkImage(R.drawable.test_500x500_2))
            }
            if (cbSize600.isChecked) {
                images.add(BenchmarkImage(R.drawable.test_600x600_2))
            }
            return images
        }

    private val customImages: List<BenchmarkImage>
        get() {
            val images = ArrayList<BenchmarkImage>()
            for (customPicturePath in customPicturePaths) {
                images.add(BenchmarkImage(0, customPicturePath.absolutePath))
            }
            return images
        }

    private val radiusSizesFromSettings: List<Int>
        get() {
            val radius = ArrayList<Int>()
            if (cBradius4px.isChecked) {
                radius.add(4)
            }
            if (cBradius8px.isChecked) {
                radius.add(8)
            }
            if (cBradius16px.isChecked) {
                radius.add(16)
            }
            if (cBradius24px.isChecked) {
                radius.add(24)
            }
            return radius
        }

    private val allSelectedAlgorithms: List<EBlurAlgorithm>
        get() {
            val algorithms = ArrayList<EBlurAlgorithm>()
            for (i in 0 until algorithmGroup.childCount) {
                val cb = algorithmGroup.getChildAt(i) as CheckBox
                if (cb.isChecked) {
                    algorithms.add(algorithmGroup.getChildAt(i).tag as EBlurAlgorithm)
                }
            }
            return algorithms
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        rounds = if (BuildConfig.DEBUG) 3 else 100

        if (savedInstanceState != null) {
            rounds = savedInstanceState.getInt(ROUNDS_KEY)
            customPicturePaths = BenchmarkUtil.getAsFiles(savedInstanceState.getString(CUSTOM_IMAGES)!!)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_bechmark_settings, container, false)
        fab = v.findViewById(R.id.fab)
        fab.setOnClickListener { benchmark() }

        cBradius4px = v.findViewById(R.id.cb_r_4px)
        cBradius8px = v.findViewById(R.id.cb_r_8px)
        cBradius16px = v.findViewById(R.id.cb_r_16px)
        cBradius24px = v.findViewById(R.id.cb_r_24px)

        cbSize100 = v.findViewById(R.id.cb_s_100)
        cbSize200 = v.findViewById(R.id.cb_s_200)
        cbSize300 = v.findViewById(R.id.cb_s_300)
        cbSize400 = v.findViewById(R.id.cb_s_400)
        cbSize500 = v.findViewById(R.id.cb_s_500)
        cbSize600 = v.findViewById(R.id.cb_s_600)

        roundsSpinner = v.findViewById<Spinner>(R.id.spinner_rounds).apply {
            adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, roundArray)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                    rounds = (adapterView.adapter.getItem(i) as Rounds).rounds
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {}
            }
            setSelection(Arrays.asList(*roundArray).indexOf(Rounds(rounds)))
        }

        algorithmGroup = v.findViewById(R.id.algorithm_wrapper)
        for (algorithm1 in algorithmList) {
            algorithmGroup.addView(createAlgorithmCheckbox(algorithm1, inflater))
        }
        (algorithmGroup.getChildAt(0) as CheckBox).isChecked = true

        v.findViewById<View>(R.id.btn_addpic).setOnClickListener {
            if (checkHasReadStoragePermission()) {
                startSelectImage()
            }
        }

        v.findViewById<View>(R.id.tv_algo_header).setOnLongClickListener {
            for (i in 0 until algorithmGroup.childCount) {
                (algorithmGroup.getChildAt(i) as CheckBox).isChecked = true
            }
            true
        }
        return v
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult $requestCode")
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permission granted")
                startSelectImage()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d(TAG, "show permission rationale")
                    Snackbar.make(view!!, "You need to allow the app to read your disk if you want to add custom images.", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        Log.d(TAG, "permission denied")
    }

    private fun checkHasReadStoragePermission(): Boolean {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "permission not granted yet, show dialog")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
            }
            return false
        }
        Log.d(TAG, "permission already granted")
        return true
    }

    private fun startSelectImage() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, IMAGE_PICK)
    }

    @SuppressLint("InflateParams")
    private fun createAlgorithmCheckbox(algorithm: EBlurAlgorithm, inflater: LayoutInflater): CheckBox {
        val cb = inflater.inflate(R.layout.inc_algorithm_checkbox, null) as CheckBox
        cb.text = algorithm.toString()
        cb.tag = algorithm
        return cb
    }

    override fun onResume() {
        super.onResume()
        updateCustomPictures()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ROUNDS_KEY, rounds)
        outState.putString(CUSTOM_IMAGES, BenchmarkUtil.saveFiles(customPicturePaths))
    }

    private fun benchmark() {
        Log.d(TAG, "start benchmark")
        run = true
        val radius = radiusSizesFromSettings
        val images = imagesFromSettings
        images.addAll(customImages)
        val algorithms = allSelectedAlgorithms
        val benchmarkCount = radius.size * images.size * algorithms.size
        if (benchmarkCount <= 0) {
            Toast.makeText(activity, "Choose at least one radius and image size or custom image", Toast.LENGTH_SHORT).show()
            return
        }
        showProgressDialog(benchmarkCount)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        benchmarkResultList = BenchmarkResultList()
        nextTest(0, 0, 0, images, radius, algorithms)
    }

    private fun showProgressDialog(max: Int) {
        lockOrientation()
        progressDialog = ProgressDialog(activity).apply {
            isIndeterminate = false
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setMessage("Benchmark in progress")
            this.max = max
            progress = 0
            setCancelable(true)
            show()
            setCanceledOnTouchOutside(false)
            setOnCancelListener { cancelTests() }
        }
        
    }

    private fun lockOrientation() {
        val currentOrientation = resources.configuration.orientation
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }

    private fun nextTest(photoIndex: Int, radiusIndex: Int, algoIndex: Int, imageList: List<BenchmarkImage>, radiusList: List<Int>, algorithmList: List<EBlurAlgorithm>) {
        if (run) {
            if (radiusIndex >= radiusList.size) {
                nextTest(photoIndex + 1, 0, algoIndex, imageList, radiusList, algorithmList)
            } else {
                if (photoIndex >= imageList.size) {
                    nextTest(0, 0, algoIndex + 1, imageList, radiusList, algorithmList)
                } else {
                    if (algoIndex >= algorithmList.size) {
                        testDone()
                    } else {
                        task = object : BlurBenchmarkTask(imageList[photoIndex], rounds, radiusList[radiusIndex], algorithmList[algoIndex], (activity as MainActivity).getRs(), requireActivity().applicationContext) {
                            override fun onPostExecute(bitmap: BenchmarkWrapper) {
                                progressDialog!!.progress = progressDialog!!.progress + 1
                                benchmarkResultList.benchmarkWrappers.add(bitmap)
                                Log.d(TAG, "next test")
                                nextTest(photoIndex, radiusIndex + 1, algoIndex, imageList, radiusList, algorithmList)
                            }
                        }
                        task!!.execute()
                    }
                }
            }
        } else {
            Log.d(TAG, "ignore next test, was canceled")
        }
    }

    private fun testDone() {
        if (run) {
            run = false
            if (isAdded && isVisible) {
                Log.d(TAG, "done benchmark")
                progressDialog!!.progress = progressDialog!!.max
                progressDialog!!.dismiss()
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                saveTest()

                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                val i = Intent(activity, BenchmarkResultActivity::class.java)
                i.putExtra(BenchmarkResultActivity.BENCHMARK_LIST_KEY, JsonUtil.toJsonString(benchmarkResultList))
                startActivity(i)
            }
        }
    }

    private fun saveTest() {
        BenchmarkStorage.getInstance(requireActivity()).saveTest(benchmarkResultList.benchmarkWrappers)
    }

    override fun onPause() {
        super.onPause()
        cancelTests()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK && null != data) {
            try {
                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = requireActivity().contentResolver.query(selectedImage!!,
                        filePathColumn, null, null, null)

                if (cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    val picturePath = cursor.getString(columnIndex)
                    cursor.close()

                    if (picturePath != null && picturePath.isNotEmpty()) {
                        customPicturePaths.add(File(picturePath))
                        updateCustomPictures()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Could not get requested picture", e)
            }

        }
    }

    private fun updateCustomPictures() {
        val tvCustomViews = view!!.findViewById<LinearLayout>(R.id.tv_additionalPics)
        tvCustomViews.removeAllViews()
        for (customPicturePath in customPicturePaths) {
            if (customPicturePath != null && customPicturePath.isFile && customPicturePath.absolutePath.isNotEmpty()) {
                val inflater = LayoutInflater.from(activity)
                val vg = inflater.inflate(R.layout.inc_custom_img, tvCustomViews, false) as ViewGroup

                val tv = vg.findViewById<TextView>(R.id.tv_pic_name)
                tv.text = customPicturePath.name

                vg.findViewById<View>(R.id.btn_remove).tag = customPicturePath
                vg.findViewById<View>(R.id.btn_remove).setOnClickListener { view ->
                    customPicturePaths.remove(view.tag)
                    val tvCustomViews = getView()!!.findViewById<LinearLayout>(R.id.tv_additionalPics)
                    for (i in 0 until tvCustomViews.childCount) {
                        if (view.tag == tvCustomViews.getChildAt(i).findViewById<View>(R.id.btn_remove).tag) {
                            tvCustomViews.removeViewAt(i)
                            break
                        }
                    }
                    checkIfCustomImgBtnShouldBeShown()
                }
                tvCustomViews.addView(vg)
            }
        }
        checkIfCustomImgBtnShouldBeShown()
    }

    private fun checkIfCustomImgBtnShouldBeShown() {

        if (customPicturePaths.size > 5) {
            view!!.findViewById<View>(R.id.btn_addpic).visibility = View.GONE
        } else {
            view!!.findViewById<View>(R.id.btn_addpic).visibility = View.VISIBLE
        }
    }

    private fun cancelTests() {
        Log.d(TAG, "cancel benchmark")
        run = false
        task?.cancelBenchmark()
        progressDialog?.dismiss()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    class Rounds(val rounds: Int) {

        override fun toString(): String {
            return "$rounds Rounds per Benchmark"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false

            val rounds1 = other as Rounds?

            return rounds == rounds1?.rounds
        }

        override fun hashCode(): Int {
            return rounds
        }
    }

    companion object {
        private val TAG = BlurBenchmarkFragment::class.java.simpleName
        private const val IMAGE_PICK = 43762
        const val REQUEST_CODE_PERMISSION = 432
        private val algorithmList = ArrayList(Arrays.asList(*EBlurAlgorithm.values()))
        private val roundArray = arrayOf(Rounds(3), Rounds(10), Rounds(25), Rounds(50), Rounds(100), Rounds(250), Rounds(500), Rounds(1000))

        private const val ROUNDS_KEY = "ROUNDS_KEY"
        private const val CUSTOM_IMAGES = "CUSTOM_IMAGES"
    }

}
