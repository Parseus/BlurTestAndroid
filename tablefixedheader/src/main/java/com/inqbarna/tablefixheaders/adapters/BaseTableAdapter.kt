package com.inqbarna.tablefixheaders.adapters

import android.database.DataSetObservable
import android.database.DataSetObserver

/**
 * Common base class of common implementation for an [TableAdapter] that
 * can be used in [TableFixHeaders].
 *
 * @author Brais Gabn (InQBarna)
 */
abstract class BaseTableAdapter : TableAdapter {
    private val dataSetObservable = DataSetObservable()

    override fun registerDataSetObserver(observer: DataSetObserver) {
        dataSetObservable.registerObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        dataSetObservable.unregisterObserver(observer)
    }

    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    fun notifyDataSetChanged() {
        dataSetObservable.notifyChanged()
    }

    /**
     * Notifies the attached observers that the underlying data is no longer
     * valid or available. Once invoked this adapter is no longer valid and
     * should not report further data set changes.
     */
    fun notifyDataSetInvalidated() {
        dataSetObservable.notifyInvalidated()
    }
}
