package com.lgsdiamond.materialviewpager

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.astuetz.PagerSlidingTabStrip
import com.lgsdiamond.materialviewpager.header.HeaderDesign
import com.lgsdiamond.materialviewpager.header.MaterialViewPagerImageHelper.setImageDrawable
import com.lgsdiamond.materialviewpager.header.MaterialViewPagerImageHelper.setImageLoadListener
import com.lgsdiamond.materialviewpager.header.MaterialViewPagerImageHelper.setImageUrl
import kotlin.math.roundToInt

/**
 * Created by florentchampigny on 28/04/15.
 *
 *
 * The main class of MaterialViewPager
 * To use in an xml layout with attributes viewpager_*
 *
 *
 * Display a preview with header, actual logo and fake cells
 */
class MaterialViewPager : FrameLayout, OnPageChangeListener {
    //region construct
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        settings.handleAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        settings.handleAttributes(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        settings.handleAttributes(context, attrs)
    }
    //endregion

    /**
     * Contains all references to MaterialViewPager's header views
     */
    var materialViewPagerHeader: MaterialViewPagerHeader? = null

    //the child toolbar
    var toolbar: Toolbar? = null

    //the child viewpager
    lateinit var viewPager: ViewPager

    //a view used to add placeholder color below the header
    private var headerBackground: View? = null

    //a view used to add fading color over the headerBackgroundContainer
    private var toolbarLayoutBackground: View? = null

    //Class containing the configuration of the MaterialViewPager
    var settings = MaterialViewPagerSettings()
    private var headListener: MaterialViewPager.HeadListener? = null
    var lastPosition = -1
    var currentPagerState = Int.MIN_VALUE

    /**
     * the layout containing the header
     * default : add @layout/material_view_pager_default_header
     * with viewpager_header you can set your own layout
     */
    private var headerBackgroundContainer: ViewGroup? = null

    /**
     * the layout containing tabs
     * default : add @layout/material_view_pager_pagertitlestrip_standard
     * with viewpager_pagerTitleStrip you can set your own layout
     */
    private var pagerTitleStripContainer: ViewGroup? = null

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss: MaterialViewPager.SavedState = state as MaterialViewPager.SavedState
        super.onRestoreInstanceState(ss.superState)
        settings = ss.settings!!
        if (headerBackground != null) {
            headerBackground!!.setBackgroundColor(settings.color)
        }
        val animator = MaterialViewPagerHelper.getAnimator(this.context)

        //-1*ss.yOffset restore to 0
        animator.restoreScroll(-1 * ss.yOffset, ss.settings)
        MaterialViewPagerHelper.register(context, animator)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss: MaterialViewPager.SavedState = MaterialViewPager.SavedState(superState)
        //end
        ss.settings = settings
        ss.yOffset = MaterialViewPagerHelper.getAnimator(context).lastYOffset
        return ss
    }

    /**
     * Retrieve the displayed tabs
     *
     * @return the displayed tabs
     */
    fun getPagerTitleStrip(): PagerSlidingTabStrip {
        return pagerTitleStripContainer!!.findViewById<View>(R.id.materialviewpager_pagerTitleStrip) as PagerSlidingTabStrip
    }

    /**
     * change the header displayed image with a fade
     * may remove Picasso
     */
    fun setImageUrl(imageUrl: String?, fadeDuration: Int) {
        if (imageUrl != null) {
            val headerBackgroundImage =
                findViewById<View>(R.id.materialviewpager_imageHeader) as ImageView?
            //if using MaterialViewPagerImageHeader
            if (headerBackgroundImage != null) {
                ViewCompat.setAlpha(headerBackgroundImage, settings.headerAlpha)
                setImageUrl(headerBackgroundImage, imageUrl, fadeDuration)
                setImageHeaderDarkLayerAlpha()
            }
        }
    }

    /**
     * change the header displayed image with a fade and an OnLoadListener
     * may remove Picasso
     */
    fun setImageUrl(imageUrl: String?, fadeDuration: Int, imageLoadListener: OnImageLoadListener?) {
        imageLoadListener?.let { setImageLoadListener(it) }
        setImageUrl(imageUrl, fadeDuration)
    }

    /**
     * change the header displayed image with a fade
     * may remove Picasso
     */
    fun setImageDrawable(drawable: Drawable?, fadeDuration: Int) {
        if (drawable != null) {
            val headerBackgroundImage =
                findViewById<View>(R.id.materialviewpager_imageHeader) as ImageView?
            //if using MaterialViewPagerImageHeader
            if (headerBackgroundImage != null) {
                ViewCompat.setAlpha(headerBackgroundImage, settings.headerAlpha)
                setImageDrawable(headerBackgroundImage, drawable, fadeDuration)
                setImageHeaderDarkLayerAlpha()
            }
        }
    }

    /**
     * Change alpha of the header image dark layer to reveal text.
     */
    fun setImageHeaderDarkLayerAlpha() {
        val headerImageDarkLayerView =
            findViewById<View>(R.id.materialviewpager_headerImageDarkLayer)
        //if using MaterialViewPagerImageHeader
        if (headerImageDarkLayerView != null) {
            headerImageDarkLayerView.setBackgroundColor(resources.getColor(android.R.color.black))
            ViewCompat.setAlpha(headerImageDarkLayerView, settings.imageHeaderDarkLayerAlpha)
        }
    }

    /**
     * Change the header color
     */
    fun setColor(color: Int, fadeDuration: Int) {
        if (MaterialViewPagerHelper.getAnimator(context) != null) {
            MaterialViewPagerHelper.getAnimator(context).setColor(color, fadeDuration * 2)
        }
    }

    fun getHeaderBackgroundContainer(): ViewGroup? {
        return headerBackgroundContainer
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (currentPagerState != ViewPager.SCROLL_STATE_SETTLING) {
            if (positionOffset >= 0.5) {
                onPageSelected(position + 1)
            } else if (positionOffset <= -0.5) {
                onPageSelected(position - 1)
            } else {
                onPageSelected(position)
            }
        }
    }

    fun notifyHeaderChanged() {
        val position = lastPosition
        lastPosition = -1
        onPageSelected(position)
    }

    //region ViewPagerOnPageListener

    //region ViewPagerOnPageListener
    override fun onPageSelected(position: Int) {
        if (position == lastPosition || headListener == null) {
            return
        }
        val headerDesign = headListener!!.getHeaderDesign(position) ?: return
        val fadeDuration = 400
        var color = headerDesign.color
        if (headerDesign.colorRes != 0) {
            color = ContextCompat.getColor(context, headerDesign.colorRes)
        }
        if (headerDesign.drawable != null) {
            setImageDrawable(headerDesign.drawable, fadeDuration)
        } else {
            setImageUrl(headerDesign.imageUrl, fadeDuration)
        }
        setColor(color, fadeDuration)
        lastPosition = position
    }

    override fun onPageScrollStateChanged(state: Int) {
        currentPagerState = state
        if (settings.displayToolbarWhenSwipe) {
            MaterialViewPagerHelper.getAnimator(context).onViewPagerPageChanged()
        }
    }

    fun setMaterialViewPagerListener(headListener: HeadListener?) {
        this.headListener = headListener
    }

    override fun onDetachedFromWindow() {
        MaterialViewPagerHelper.unregister(context)
        headListener = null
        super.onDetachedFromWindow()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        //add @layout/material_view_pager_layout as child, containing all the MaterialViewPager views
        addView(
            LayoutInflater.from(context).inflate(R.layout.material_view_pager_layout, this, false)
        )
        headerBackgroundContainer = findViewById<View>(R.id.headerBackgroundContainer) as ViewGroup
        pagerTitleStripContainer = findViewById<View>(R.id.pagerTitleStripContainer) as ViewGroup
        /**
         * the layout containing the viewpager, can be replaced to add your own implementation of viewpager
         */
        val viewpagerContainer = findViewById<View>(R.id.viewpager_layout) as ViewGroup

        /**
         * the layout containing logo
         * default : empty
         * with viewpager_logo you can set your own layout
         */
        val logoContainer = findViewById<View>(R.id.logoContainer) as ViewGroup
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        if (settings.disableToolbar) {
            toolbar!!.visibility = View.INVISIBLE
        }
        run {
            //replace the viewpager ?
            val viewPagerLayoutId = settings.viewpagerId
            if (viewPagerLayoutId != -1) {
                viewpagerContainer.removeAllViews()
                viewpagerContainer.addView(
                    LayoutInflater.from(context)
                        .inflate(viewPagerLayoutId, viewpagerContainer, false)
                )
            }
        }
        viewPager = findViewById<View>(R.id.material_view_pager_viewpager) as ViewPager
        viewPager.addOnPageChangeListener(this)

        //inflate subviews defined in attributes
        run {
            var headerId = settings.headerLayoutId
            if (headerId == -1) {
                headerId = if (settings.animatedHeaderImage) {
                    R.layout.material_view_pager_moving_header
                } else {
                    R.layout.material_view_pager_imageview_header
                }
            }
            headerBackgroundContainer!!.addView(
                LayoutInflater.from(context).inflate(headerId, headerBackgroundContainer, false)
            )
        }
        if (isInEditMode) { //preview titlestrip
            //add fake tabs on edit mode
            settings.pagerTitleStripId = R.layout.tools_material_view_pager_pagertitlestrip
        }
        if (settings.pagerTitleStripId != -1) {
            pagerTitleStripContainer!!.addView(
                LayoutInflater.from(context)
                    .inflate(settings.pagerTitleStripId, pagerTitleStripContainer, false)
            )
        }
        if (settings.logoLayoutId != -1) {
            logoContainer.addView(
                LayoutInflater.from(context).inflate(settings.logoLayoutId, logoContainer, false)
            )
            if (settings.logoMarginTop != 0) {
                val layoutParams = logoContainer.layoutParams as RelativeLayout.LayoutParams
                layoutParams.setMargins(0, settings.logoMarginTop, 0, 0)
                logoContainer.layoutParams = layoutParams
            }
        }
        headerBackground = findViewById(R.id.headerBackground)
        toolbarLayoutBackground = findViewById(R.id.toolbar_layout_background)
        initialiseHeights()

        //construct the materialViewPagerHeader with subviews
        if (!isInEditMode) {
            materialViewPagerHeader = MaterialViewPagerHeader
                .withToolbar(toolbar!!)
                .withToolbarLayoutBackground(toolbarLayoutBackground)
                .withPagerSlidingTabStrip(pagerTitleStripContainer)
                .withHeaderBackground(headerBackground)
                .withStatusBackground(findViewById(R.id.statusBackground))
                .withLogo(logoContainer)

            //and construct the MaterialViewPagerAnimator
            //attach it to the activity to enable MaterialViewPagerHeaderView.setMaterialHeight();
            MaterialViewPagerHelper.register(context, MaterialViewPagerAnimator(this))
        } else {

            //if in edit mode, add fake cardsviews
            val sample = LayoutInflater.from(context)
                .inflate(R.layout.tools_list_items, pagerTitleStripContainer, false)
            val params = sample.layoutParams as LayoutParams
            val marginTop = Utils.dpToPx(settings.headerHeight + 10.toFloat(), context).roundToInt()
            params.setMargins(0, marginTop, 0, 0)
            super.setLayoutParams(params)
            addView(sample)
        }
    }

    //endregion

    //endregion
    private fun initialiseHeights() {
        if (headerBackground != null) {
            headerBackground!!.setBackgroundColor(settings.color)
            val layoutParams = headerBackground!!.layoutParams
            layoutParams.height =
                Utils.dpToPx(
                    settings.headerHeight + settings.headerAdditionalHeight.toFloat(),
                    context
                ).toInt()
            headerBackground!!.layoutParams = layoutParams
        }
        if (pagerTitleStripContainer != null) {
            val layoutParams =
                pagerTitleStripContainer!!.layoutParams as RelativeLayout.LayoutParams
            val marginTop = Utils.dpToPx(settings.headerHeight - 40.toFloat(), context).toInt()
            layoutParams.setMargins(0, marginTop, 0, 0)
            pagerTitleStripContainer!!.layoutParams = layoutParams
        }
        if (toolbarLayoutBackground != null) {
            val layoutParams = toolbarLayoutBackground!!.layoutParams
            layoutParams.height = Utils.dpToPx(settings.headerHeight.toFloat(), context).toInt()
            toolbarLayoutBackground!!.layoutParams = layoutParams
        }
    }

    interface HeadListener {
        fun getHeaderDesign(page: Int): HeaderDesign?
    }

    interface OnImageLoadListener {
        fun onImageLoad(imageView: ImageView?, bitmap: Bitmap?)
    }

    internal class SavedState : BaseSavedState {
        var settings: MaterialViewPagerSettings? = null
        var yOffset = 0f

        constructor(superState: Parcelable?) : super(superState) {}
        private constructor(`in`: Parcel) : super(`in`) {
            settings = `in`.readParcelable(MaterialViewPagerSettings::class.java.classLoader)
            yOffset = `in`.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(settings, flags)
            out.writeFloat(yOffset)
        }

        companion object {
            //required field that makes Parcelables from a Parcel
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState? {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}