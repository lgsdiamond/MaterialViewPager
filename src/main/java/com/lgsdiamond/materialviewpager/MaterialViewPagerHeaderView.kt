package com.lgsdiamond.materialviewpager

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver

/**
 * Created by florentchampigny on 26/04/15.
 * A placeholder view used to add a transparent padding on top of a Scroller
 * RecyclerView : use RecyclerViewMaterialAdapter
 * ListView : use ListViewMaterialAdapter (smoother if use RecyclerView)
 * ScrollView : add a MaterialViewPagerHeaderView on top of your ScrollView (with LinearLayout vertical)
 */
class MaterialViewPagerHeaderView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    private fun setMaterialHeight() {
        //get the MaterialViewPagerAnimator attached to this activity
        //to retrieve the declared header height
        //and set it as current view height (+10dp margin)
        val animator = MaterialViewPagerHelper.getAnimator(context)
        if (animator != null) {
            val params = layoutParams
            params.height = Math.round(Utils.dpToPx(animator.headerHeight + 10.toFloat(), context))
            super.setLayoutParams(params)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (!isInEditMode) {
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    setMaterialHeight()
                    viewTreeObserver.removeOnPreDrawListener(this)
                    return false
                }
            })
        }
    }
}