package at.favre.app.blurbenchmark.util

import android.app.Activity
import android.view.View

/**
 * Created by PatrickF on 22.04.2014.
 */
object TranslucentLayoutUtil {
    fun setTranslucentThemeInsets(context: Activity, view: View) {

        //		SystemBarTintManager tintManager = new SystemBarTintManager(context);
        //		SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
        //
        //		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        //			view.setPadding(0, config.getPixelInsetTop(true) + config.getStatusBarHeight(), config.getPixelInsetRight(), config.getPixelInsetBottom());
        //		} else {
        //			view.setPadding(0, context.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height), 0, 0);
        //		}
    }

    fun setTranslucentThemeInsetsWithoutActionbarHeight(context: Activity, view: View, addAdditionalStatusBarHeight: Boolean) {
        //		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        //
        //		SystemBarTintManager tintManager = new SystemBarTintManager(context);
        //		SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
        //		view.setPadding(0, config.getPixelInsetTop(false)+ (addAdditionalStatusBarHeight ? config.getStatusBarHeight(): 0), config.getPixelInsetRight(), config.getPixelInsetBottom());
    }

}
