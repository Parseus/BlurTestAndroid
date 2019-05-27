package at.favre.app.blurbenchmark.activities

import android.app.ActivityManager
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.renderscript.RenderScript
import at.favre.app.blurbenchmark.R
import at.favre.app.blurbenchmark.fragments.*
import at.favre.lib.hood.Hood
import at.favre.lib.hood.extended.PopHoodActivity
import at.favre.lib.hood.interfaces.actions.ManagerControl
import kotlinx.android.synthetic.main.inc_nav_drawer.*
import kotlinx.android.synthetic.main.inc_toolbar.*

class MainActivity : AppCompatActivity() {

    private var rs: RenderScript? = null

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var currentFragmentTag: String? = null
    private lateinit var control: ManagerControl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar!!.elevation = resources.getDimension(R.dimen.toolbar_elevation)
        initDrawer()

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
            selectView(R.id.navigation_item_1)
        } else {
            currentFragmentTag = savedInstanceState.getString(ARG_VISIBLE_FRAGMENT_TAG)
        }

        control = Hood.ext().registerShakeToOpenDebugActivity(this, PopHoodActivity.createIntent(this, DebugActivity::class.java))
    }

    private fun initDrawer() {
        drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.color_primary))
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)
        navigationView.setNavigationItemSelectedListener { menuItem -> selectView(menuItem.itemId) }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onResume() {
        super.onResume()
        control.start()
    }

    override fun onPause() {
        super.onPause()
        control.stop()
        if (rs != null) {
            rs!!.destroy()
            rs = null
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        return when (item.itemId) {
            R.id.action_settings -> {
                var fragment = supportFragmentManager.findFragmentByTag(LiveBlurFragment::class.java.simpleName)
                (fragment as? IFragmentWithBlurSettings)?.switchShowSettings()

                fragment = supportFragmentManager.findFragmentByTag(StaticBlurFragment::class.java.simpleName)
                (fragment as? IFragmentWithBlurSettings)?.switchShowSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun selectView(@IdRes menuId: Int): Boolean {
        if (currentFragmentTag != null) {
            val f = supportFragmentManager.findFragmentByTag(currentFragmentTag)
            supportFragmentManager.beginTransaction().detach(f!!).commitAllowingStateLoss()
            Log.v(TAG, "detach $currentFragmentTag")
        }

        when (menuId) {
            R.id.navigation_item_1 -> setFragment(BlurBenchmarkFragment::class.java.simpleName, object : FragmentFactory {
                override fun create(): Fragment {
                    return BlurBenchmarkFragment()
                }
            })
            R.id.navigation_item_2 -> setFragment(ResultsBrowserFragment::class.java.simpleName, object : FragmentFactory {
                override fun create(): Fragment {
                    return ResultsBrowserFragment()
                }
            })
            R.id.navigation_item_3 -> setFragment(ResultsDiagramFragment::class.java.simpleName, object : FragmentFactory {
                override fun create(): Fragment {
                    return ResultsDiagramFragment()
                }
            })
            R.id.navigation_item_4 -> setFragment(StaticBlurFragment::class.java.simpleName, object : FragmentFactory {
                override fun create(): Fragment {
                    return StaticBlurFragment()
                }
            })
            R.id.navigation_item_5 -> setFragment(LiveBlurFragment::class.java.simpleName, object : FragmentFactory {
                override fun create(): Fragment {
                    return LiveBlurFragment()
                }
            })
        }
        navigationView.menu.findItem(menuId).isChecked = true
        drawerLayout.closeDrawer(navigationView)
        return true
    }

    private fun setFragment(tag: String, factory: FragmentFactory) {
        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            Log.v(TAG, "add $tag")
            val t = supportFragmentManager.beginTransaction()
            t.add(R.id.root, factory.create(), tag)
            t.commitAllowingStateLoss()
        } else {
            Log.v(TAG, "attach $tag")
            val t = supportFragmentManager.beginTransaction()
            t.attach(supportFragmentManager.findFragmentByTag(tag)!!)
            t.commitAllowingStateLoss()
        }
        currentFragmentTag = tag
    }

    fun getRs(): RenderScript {
        if (rs == null) {
            rs = RenderScript.create(this)
        }
        return rs!!
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_VISIBLE_FRAGMENT_TAG, currentFragmentTag)
    }

    interface FragmentFactory {
        fun create(): Fragment
    }

    companion object {
        const val DIALOG_TAG = "blurdialog"
        private val TAG = MainActivity::class.java.simpleName
        private const val ARG_VISIBLE_FRAGMENT_TAG = "at.favre.app.blurbenchmark.activities.ARG_VISIBLE_FRAGMENT_TAG"
    }

}