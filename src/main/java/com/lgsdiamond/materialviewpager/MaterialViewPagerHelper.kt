package com.lgsdiamond.materialviewpager

import android.content.Context
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by florentchampigny on 25/04/15.
 *
 *
 * MaterialViewPagerHelper attach a MaterialViewPagerAnimator to an activity
 * You can use MaterialViewPagerHelper to retrieve MaterialViewPagerAnimator from context
 * Or register a scrollable to the current activity's MaterialViewPagerAnimator
 */
object MaterialViewPagerHelper {
    private val hashMap =
        ConcurrentHashMap<Any, MaterialViewPagerAnimator>()

    /**
     * Register an MaterialViewPagerAnimator attached to an activity into the ConcurrentHashMap
     *
     * @param context  the context
     * @param animator the current MaterialViewPagerAnimator
     */
    fun register(context: Context, animator: MaterialViewPagerAnimator) {
        hashMap[context] = animator
    }

    fun unregister(context: Context?) {
        if (context != null) {
            hashMap.remove(context)
        }
    }

    /**
     * Register a RecyclerView to the current MaterialViewPagerAnimator
     * Listen to RecyclerView.OnScrollListener so give to $[onScrollListener] your RecyclerView.OnScrollListener if you already use one
     * For loadmore or anything else
     *
     * @param context      current context
     * @param recyclerView the scrollable
     */
    fun registerRecyclerView(context: Context?, recyclerView: RecyclerView?) {
        if (context != null && hashMap.containsKey(context)) {
            val animator = hashMap[context]
            animator?.registerRecyclerView(recyclerView)
        }
    }

    /**
     * Register a ScrollView to the current MaterialViewPagerAnimator
     * Listen to ObservableScrollViewCallbacks so give to $[observableScrollViewCallbacks] your ObservableScrollViewCallbacks if you already use one
     * For loadmore or anything else
     *
     * @param context    current context
     * @param mScrollView the scrollable
     */
    fun registerScrollView(context: Context?, mScrollView: NestedScrollView?) {
        if (context != null && hashMap.containsKey(context)) {
            val animator = hashMap[context]
            animator?.registerScrollView(mScrollView)
        }
    }

    /**
     * Retrieve the current MaterialViewPagerAnimator used in this context (Activity)
     *
     * @param context the context
     * @return current MaterialViewPagerAnimator
     */
    fun getAnimator(context: Context): MaterialViewPagerAnimator {
        return hashMap[context]!!       // TODO: Check null
    }
}