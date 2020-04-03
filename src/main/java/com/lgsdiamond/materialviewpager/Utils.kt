package com.lgsdiamond.materialviewpager

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.widget.ScrollView
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlin.math.roundToInt

/**
 * Created by florentchampigny on 24/04/15.
 */
object Utils {
    /**
     * convert dp to px
     */
    fun dpToPx(dp: Float, context: Context): Float {
        return dp * context.resources.displayMetrics.density
    }

    /**
     * convert px to dp
     */
    fun pxToDp(px: Float, context: Context): Float {
        return px / context.resources.displayMetrics.density
    }

    /*
     * Create a color from [$color].RGB and then add an alpha with 255*[$percent]
     */
    fun colorWithAlpha(color: Int, percent: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val alpha = (percent * 255).roundToInt()
        return Color.argb(alpha, r, g, b)
    }

    fun minMax(min: Float, value: Float, max: Float): Float {
        var aValue = value
        aValue = aValue.coerceAtMost(max)
        aValue = min.coerceAtLeast(aValue)
        return aValue
    }

    /**
     * modify the scale of multiples views
     *
     * @param scale the new scale
     * @param views
     */
    fun setScale(scale: Float, vararg views: View?) {
        for (view in views) {
            if (view != null) {
                ViewCompat.setScaleX(view, scale)
                ViewCompat.setScaleY(view, scale)
            }
        }
    }

    /**
     * modify the elevation of multiples views
     *
     * @param elevation the new elevation
     * @param views
     */
    fun setElevation(elevation: Float, vararg views: View?) {
        for (view in views) {
            if (view != null) {
                ViewCompat.setElevation(view, elevation)
            }
        }
    }

    /**
     * modify the backgroundColor of multiples views
     *
     * @param color the new backgroundColor
     * @param views
     */
    fun setBackgroundColor(color: Int, vararg views: View?) {
        for (view in views) {
            view?.setBackgroundColor(color)
        }
    }

    fun canScroll(view: View?): Boolean {
        if (view is ScrollView) {
            val child = view.getChildAt(0)
            if (child != null) {
                val childHeight = child.height
                return view.height < childHeight + view.paddingTop + view.paddingBottom
            }
            return false
        } else if (view is RecyclerView) {
            val yOffset = view.computeVerticalScrollOffset()
            return yOffset != 0
        }
        return true
    }

    fun scrollTo(scroll: Any?, yOffset: Float) {
        if (scroll is RecyclerView) {
            //RecyclerView.scrollTo : UnsupportedOperationException
            //Moved to the RecyclerView.LayoutManager.scrollToPositionWithOffset
            //Have to be instanceOf RecyclerView.LayoutManager to work (so work with RecyclerView.GridLayoutManager)
            val layoutManager = scroll.layoutManager
            if (layoutManager is LinearLayoutManager) {
                layoutManager.scrollToPositionWithOffset(0, (-yOffset).toInt())
            } else if (layoutManager is StaggeredGridLayoutManager) {
                layoutManager.scrollToPositionWithOffset(0, (-yOffset).toInt())
            }
        } else if (scroll is NestedScrollView) {
            scroll.scrollTo(0, yOffset.toInt())
        }
    }

    fun getTheVisibleView(viewList: MutableList<View?>): View? {
        val scrollBounds = Rect()
        val listSize = viewList.size
        for (i in 0 until listSize) {
            val view = viewList[i]
            if (view != null) {
                view.getHitRect(scrollBounds)
                if (view.getLocalVisibleRect(scrollBounds)) {
                    return view
                }
            }
        }
        return null
    }
}