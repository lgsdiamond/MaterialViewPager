package com.lgsdiamond.materialviewpager.header

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.lgsdiamond.materialviewpager.MaterialViewPagerHelper
import com.lgsdiamond.materialviewpager.Utils
import kotlin.math.roundToInt

/**
 * Created by florentchampigny on 27/05/2016.
 */
class MaterialViewPagerHeaderDecorator : ItemDecoration() {
    var registered = false
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        val holder = recyclerView.getChildViewHolder(view)
        val context = recyclerView.context
        if (!registered) {
            MaterialViewPagerHelper.registerRecyclerView(context, recyclerView)
            registered = true
        }
        var headerCells = 1

        //don't work with staged
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            headerCells = layoutManager.spanCount
        }
        val animator = MaterialViewPagerHelper.getAnimator(context)
        if (animator != null) {
            if (holder.adapterPosition < headerCells) {
                outRect.top = Utils.dpToPx(animator.headerHeight + 10.toFloat(), context).roundToInt()
            }
        }
    }
}