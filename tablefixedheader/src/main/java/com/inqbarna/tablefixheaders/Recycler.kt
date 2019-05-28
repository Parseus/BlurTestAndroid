package com.inqbarna.tablefixheaders

import android.view.View

import java.util.Stack

/**
 * The Recycler facilitates reuse of views across layouts.
 *
 * @author Brais Gabn (InQBarna)
 */
class Recycler
/**
 * Constructor
 *
 * @param size
 * The number of types of view to recycle.
 */
(size: Int) {

    private val views: Array<Stack<View>> = Array(size) { Stack<View>() }

    /**
     * Add a view to the Recycler. This view may be reused in the function
     * [.getRecycledView]
     *
     * @param view
     * A view to add to the Recycler. It can no longer be used.
     * @param type
     * the type of the view.
     */
    fun addRecycledView(view: View, type: Int) {
        views[type].push(view)
    }

    /**
     * Returns, if exists, a view of the type `typeView`.
     *
     * @param typeView
     * the type of view that you want.
     * @return a view of the type `typeView`. `null` if
     * not found.
     */
    fun getRecycledView(typeView: Int): View? {
        return try {
            views[typeView].pop()
        } catch (e: java.util.EmptyStackException) {
            null

        }

    }
}
