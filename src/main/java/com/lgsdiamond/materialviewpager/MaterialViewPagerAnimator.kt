package com.lgsdiamond.materialviewpager

import android.animation.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * Created by florentchampigny on 24/04/15.
 *
 *
 * Listen to Scrollable inside MaterialViewPager
 * When notified scroll, dispatch the current scroll to other scrollable
 *
 *
 * Note : didn't want to translate the MaterialViewPager or intercept Scroll,
 * so added a ViewPager with scrollables containing a transparent placeholder on top
 *
 *
 * When scroll, animate the MaterialViewPager Header (toolbar, logo, color ...)
 */
class MaterialViewPagerAnimator internal constructor(materialViewPager: MaterialViewPager) {
    //final toolbar layout elevation (if attr viewpager_enableToolbarElevation = true)
    private val elevation: Float

    //max scroll which will be dispatched for all scrollable
    private val scrollMax: Float

    // equals scrollMax in DP (saved to avoir convert to dp anytime I use it)
    private val scrollMaxDp: Float
    var lastYOffset = -1f //the current yOffset
    private var lastPercent = 0f //the current Percent

    //contains the attributes given to MaterialViewPager from layout
    private val settings: MaterialViewPagerSettings = materialViewPager.settings

    //list of all registered scrollers
    private val scrollViewList: MutableList<View?> = mutableListOf()

    //save all yOffsets of scrollables
    private val yOffsets = HashMap<Any, Int>()
    private var followScrollToolbarIsVisible = false
    private var firstScrollValue = Float.MIN_VALUE
    private var justToolbarAnimated = false

    //initial distance between pager & toolbar
    private var initialDistance = -1f

    //contains MaterialViewPager subviews references
    private val mHeader: MaterialViewPagerHeader = materialViewPager.materialViewPagerHeader

    //the tmp headerAnimator (not null if animating, else null)
    private var headerAnimator: ValueAnimator? = null

    /**
     * Called when a scroller(RecyclerView/ListView,ScrollView,WebView) scrolled by the user
     *
     * @param source  the scroller
     * @param yOffset the scroller current yOffset
     */
    private fun onMaterialScrolled(source: Any?, yOffset: Float): Boolean {
        if (initialDistance == -1f || initialDistance == 0f) {
            initialDistance = mHeader!!.mPagerSlidingTabStrip!!.top - mHeader.toolbar.bottom.toFloat()
        }

        //only if yOffset changed
        if (yOffset == lastYOffset) {
            return false
        }
        val scrollTop = -yOffset
        run {
            //parallax scroll of the Background ImageView (the KenBurnsView)
            if (mHeader!!.headerBackground != null) {
                if (this.settings.parallaxHeaderFactor != 0f) {
                    ViewCompat.setTranslationY(mHeader.headerBackground, scrollTop / this.settings.parallaxHeaderFactor)
                }
                if (ViewCompat.getY(mHeader.headerBackground) >= 0) {
                    ViewCompat.setY(mHeader.headerBackground, 0f)
                }
            }
        }
        log("yOffset$yOffset")

        //dispatch the new offset to all registered scrollables
        dispatchScrollOffset(source, Utils.minMax(0f, yOffset, scrollMaxDp))
        var percent = yOffset / scrollMax
        log("percent1$percent")
        if (percent != 0f) {
            //distance between pager & toolbar
            val newDistance = ViewCompat.getY(mHeader!!.mPagerSlidingTabStrip) - mHeader.toolbar.bottom
            percent = 1 - newDistance / initialDistance
            log("percent2$percent")
        }
        if (java.lang.Float.isNaN(percent)) //fix for orientation change
        {
            return false
        }

        //fix quick scroll
        if (percent == 0f && headerAnimator != null) {
            cancelHeaderAnimator()
            ViewCompat.setTranslationY(mHeader!!.toolbarLayout, 0f)
        }
        percent = Utils.minMax(0f, percent, 1f)
        run {
            if (!settings.toolbarTransparent) {
                // change color of toolbar & viewpager indicator &  statusBaground
                setColorPercent(percent)
            } else {
                if (justToolbarAnimated) {
                    if (toolbarJoinsTabs()) {
                        setColorPercent(1f)
                    } else if (lastPercent != percent) {
                        animateColorPercent(0f, 200)
                    }
                }
            }
            lastPercent = percent //save the percent
            if (mHeader!!.mPagerSlidingTabStrip != null) { //move the viewpager indicator
                //float newY = ViewCompat.getY(mHeader.mPagerSlidingTabStrip) + scrollTop;
                log("" + scrollTop)

                //mHeader.mPagerSlidingTabStrip.setTranslationY(mHeader.getToolbar().getBottom()-mHeader.mPagerSlidingTabStrip.getY());
                if (scrollTop <= 0) {
                    ViewCompat.setTranslationY(mHeader.mPagerSlidingTabStrip, scrollTop)
                    ViewCompat.setTranslationY(mHeader.toolbarLayoutBackground, scrollTop)

                    //when
                    if (ViewCompat.getY(mHeader.mPagerSlidingTabStrip) < mHeader.toolbar.bottom) {
                        val ty = mHeader.toolbar.bottom - mHeader.mPagerSlidingTabStrip!!.top.toFloat()
                        ViewCompat.setTranslationY(mHeader.mPagerSlidingTabStrip, ty)
                        ViewCompat.setTranslationY(mHeader.toolbarLayoutBackground, ty)
                    }
                }
            }
            if (mHeader.logo != null) { //move the header logo to toolbar
                if (this.settings.hideLogoWithFade) {
                    ViewCompat.setAlpha(mHeader.logo, 1 - percent)
                    ViewCompat.setTranslationY(mHeader.logo, (mHeader.finalTitleY - mHeader.originalTitleY) * percent)
                } else {
                    ViewCompat.setTranslationY(mHeader.logo, (mHeader.finalTitleY - mHeader.originalTitleY) * percent)
                    ViewCompat.setTranslationX(mHeader.logo, (mHeader.finalTitleX - mHeader.originalTitleX) * percent)
                    val scale = (1 - percent) * (1 - mHeader.finalScale) + mHeader.finalScale
                    Utils.setScale(scale, mHeader.logo)
                }
            }
            if (this.settings.hideToolbarAndTitle && mHeader.toolbarLayout != null) {
                val scrollUp = lastYOffset < yOffset
                if (scrollUp) {
                    scrollUp(yOffset)
                } else {
                    scrollDown(yOffset)
                }
            }
        }
        if (headerAnimator != null && percent < 1) {
            cancelHeaderAnimator()
        }
        lastYOffset = yOffset
        return true
    }

    /**
     * Change the color of the statusbackground, toolbar, toolbarlayout and pagertitlestrip
     * With a color transition animation
     *
     * @param color    the final color
     * @param duration the transition color animation duration
     */
    fun setColor(color: Int, duration: Int) {
        val colorAnim: ValueAnimator = ObjectAnimator.ofInt(mHeader!!.headerBackground, "backgroundColor", settings.color, color)
        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.duration = duration.toLong()
        colorAnim.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            val colorAlpha = Utils.colorWithAlpha(animatedValue, lastPercent)
            mHeader.headerBackground?.setBackgroundColor(colorAlpha)
            mHeader.statusBackground?.setBackgroundColor(colorAlpha)
            mHeader.toolbar.setBackgroundColor(colorAlpha)
            mHeader.toolbarLayoutBackground?.setBackgroundColor(colorAlpha)
            mHeader.mPagerSlidingTabStrip?.setBackgroundColor(colorAlpha)

            //set the new color as MaterialViewPager's color
            settings.color = animatedValue
        }
        colorAnim.start()
    }

    fun animateColorPercent(percent: Float, duration: Int) {
        val valueAnimator = ValueAnimator.ofFloat(lastPercent, percent)
        valueAnimator.addUpdateListener { animation -> setColorPercent(animation.animatedValue as Float) }
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()
    }

    fun setColorPercent(percent: Float) {
        // change color of
        // toolbar & viewpager indicator &  statusBaground
        Utils.setBackgroundColor(
            Utils.colorWithAlpha(settings.color, percent),
            mHeader!!.statusBackground
        )
        if (percent >= 1) {
            Utils.setBackgroundColor(
                Utils.colorWithAlpha(settings.color, percent),
                mHeader.toolbar,
                mHeader.toolbarLayoutBackground,
                mHeader.mPagerSlidingTabStrip
            )
        } else {
            Utils.setBackgroundColor(
                Utils.colorWithAlpha(settings.color, 0f),
                mHeader.toolbar,
                mHeader.toolbarLayoutBackground,
                mHeader.mPagerSlidingTabStrip
            )
        }
        if (settings.enableToolbarElevation && toolbarJoinsTabs()) {
            Utils.setElevation(
                if (percent == 1f) elevation else 0f,
                mHeader.toolbar,
                mHeader.toolbarLayoutBackground,
                mHeader.mPagerSlidingTabStrip,
                mHeader.logo
            )
        }
    }

    val headerHeight: Int
        get() = settings.headerHeight

    /**
     * Register a RecyclerView to the current MaterialViewPagerAnimator
     * Listen to RecyclerView.OnScrollListener so give to $[onScrollListener] your RecyclerView.OnScrollListener if you already use one
     * For loadmore or anything else
     *
     * @param recyclerView the scrollable
     */
    fun registerRecyclerView(recyclerView: RecyclerView?) {
        if (recyclerView != null && !scrollViewList!!.contains(recyclerView)) {
            scrollViewList.add(recyclerView) //add to the scrollable list
            yOffsets[recyclerView] = recyclerView.scrollY //save the initial recyclerview's yOffset (0) into hashmap
            //only necessary for recyclerview

            //listen to scroll
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var firstZeroPassed = false
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    var yOffset = yOffsets[recyclerView]!!
                    if (yOffset < 0) {
                        yOffset = 0
                    }
                    yOffset += dy
                    yOffsets[recyclerView] = yOffset //save the new offset

                    //first time you get 0, don't share it to others scrolls
                    if (yOffset == 0 && !firstZeroPassed) {
                        firstZeroPassed = true
                        return
                    }

                    //only if yOffset changed
                    if (isNewYOffset(yOffset)) {
                        onMaterialScrolled(recyclerView, yOffset.toFloat())
                    }
                }
            })
            recyclerView.post(Runnable { setScrollOffset(recyclerView, lastYOffset) })
        }
    }

    /**
     * Register a ScrollView to the current MaterialViewPagerAnimator
     * Listen to ObservableScrollViewCallbacks so give to $[observableScrollViewCallbacks] your ObservableScrollViewCallbacks if you already use one
     * For loadmore or anything else
     *
     * @param scrollView the scrollable
     */
    fun registerScrollView(scrollView: NestedScrollView?) {
        if (scrollView != null) {
            scrollViewList!!.add(scrollView) //add to the scrollable list
            scrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
                var firstZeroPassed = false
                override fun onScrollChange(v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                    //first time you get 0, don't share it to others scrolls
                    if (scrollY == 0 && !firstZeroPassed) {
                        firstZeroPassed = true
                        return
                    }

                    //only if yOffset changed
                    if (isNewYOffset(scrollY)) {
                        onMaterialScrolled(scrollView, scrollY.toFloat())
                    }
                }
            })
            scrollView.post(Runnable { setScrollOffset(scrollView, lastYOffset) })
        }
    }

    fun restoreScroll(scroll: Float, settings: MaterialViewPagerSettings?) {
        //try to scroll up, on a looper to wait until restored
        Handler(Looper.getMainLooper()).postDelayed({
            if (!onMaterialScrolled(null, scroll)) {
                restoreScroll(scroll, settings)
            }
        }, 100)
    }

    fun onViewPagerPageChanged() {
        scrollDown(lastYOffset)
        val visibleView = Utils.getTheVisibleView(scrollViewList)
        if (!Utils.canScroll(visibleView)) {
            followScrollToolbarLayout(0f)
            onMaterialScrolled(visibleView, 0f)
        }
    }

    /**
     * When notified for scroll, dispatch it to all registered scrollables
     *
     * @param source
     * @param yOffset
     */
    private fun dispatchScrollOffset(source: Any?, yOffset: Float) {
        if (scrollViewList != null) {
            for (scroll in scrollViewList) {

                //do not re-scroll the source
                if (scroll != null && scroll !== source) {
                    setScrollOffset(scroll, yOffset)
                }
            }
        }
    }

    private fun isNewYOffset(yOffset: Int): Boolean {
        return if (lastYOffset == -1f) {
            true
        } else {
            yOffset.toFloat() != lastYOffset
        }
    }

    /**
     * When notified for scroll, dispatch it to all registered scrollables
     *
     * @param scroll
     * @param yOffset
     */
    private fun setScrollOffset(scroll: Any?, yOffset: Float) {
        //do not re-scroll the source
        if (scroll != null && yOffset >= 0) {
            Utils.scrollTo(scroll, yOffset)

            //save the current yOffset of the scrollable on the yOffsets hashmap
            yOffsets[scroll] = yOffset.toInt()
        }
    }

    private fun cancelHeaderAnimator() {
        if (headerAnimator != null) {
            headerAnimator!!.cancel()
            headerAnimator = null
        }
    }

    //region register scrollables
    private fun scrollUp(yOffset: Float) {
        log("scrollUp")
        followScrollToolbarLayout(yOffset)
    }

    private fun log(scrollUp: String) {
        if (ENABLE_LOG) {
            Log.d(TAG, scrollUp)
        }
    }

    private fun scrollDown(yOffset: Float) {
        log("scrollDown")
        if (yOffset > mHeader!!.toolbarLayout.height * 1.5f) {
            animateEnterToolbarLayout(yOffset)
        } else {
            if (headerAnimator != null) {
                followScrollToolbarIsVisible = true
            } else {
                followScrollToolbarLayout(yOffset)
            }
        }
    }

    private fun toolbarJoinsTabs(): Boolean {
        return mHeader!!.toolbar.bottom.toFloat() == mHeader.mPagerSlidingTabStrip!!.top + ViewCompat.getTranslationY(mHeader.mPagerSlidingTabStrip)
    }
    //endregion
    /**
     * move the toolbarlayout (containing toolbar & tabs)
     * following the current scroll
     */
    private fun followScrollToolbarLayout(yOffset: Float) {
        if (mHeader!!.toolbar.bottom == 0) {
            return
        }
        if (toolbarJoinsTabs()) {
            if (firstScrollValue == Float.MIN_VALUE) {
                firstScrollValue = yOffset
            }
            var translationY = firstScrollValue - yOffset
            if (translationY > 0) {
                translationY = 0f
            }
            log("translationY $translationY")
            ViewCompat.setTranslationY(mHeader.toolbarLayout, translationY)
        } else {
            ViewCompat.setTranslationY(mHeader.toolbarLayout, 0f)
            justToolbarAnimated = false
        }
        followScrollToolbarIsVisible = ViewCompat.getY(mHeader.toolbarLayout) >= 0
    }

    /**
     * Animate enter toolbarlayout
     *
     * @param yOffset
     */
    private fun animateEnterToolbarLayout(yOffset: Float) {
        if (!followScrollToolbarIsVisible && headerAnimator != null) {
            headerAnimator!!.cancel()
            headerAnimator = null
        }
        if (headerAnimator == null) {
            headerAnimator = ObjectAnimator.ofFloat(mHeader!!.toolbarLayout, View.TRANSLATION_Y, 0f)
            (headerAnimator as ObjectAnimator).duration = ENTER_TOOLBAR_ANIMATION_DURATION.toLong()
            (headerAnimator as ObjectAnimator).addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    followScrollToolbarIsVisible = true
                    firstScrollValue = Float.MIN_VALUE
                    justToolbarAnimated = true
                }
            })
            (headerAnimator as ObjectAnimator).start()
        }
    }

    companion object {
        private const val TAG = "MaterialViewPager"

        //duration of translate header enter animation
        private const val ENTER_TOOLBAR_ANIMATION_DURATION = 300
        var ENABLE_LOG = false
    }

    init {
        val context = mHeader!!.context

        // initialise the scrollMax to headerHeight, so until the first cell touch the top of the screen
        scrollMax = settings.headerHeight.toFloat()
        //save in into dp once
        scrollMaxDp = Utils.dpToPx(scrollMax, context)

        //heightMaxScrollToolbar = context.getResources().getDimension(R.dimen.material_viewpager_padding_top);
        elevation = Utils.dpToPx(4f, context)
    }
}