package com.lgsdiamond.materialviewpager

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat

/**
 * Created by florentchampigny on 25/04/15.
 * A class containing references to views inside MaterialViewPager's header
 */
class MaterialViewPagerHeader private constructor(var toolbar: Toolbar) {
    var context: Context = toolbar.context
    var toolbarLayout: View = toolbar.parent as View
    var mPagerSlidingTabStrip: View? = null
    var toolbarLayoutBackground: View? = null
    var headerBackground: View? = null
    var statusBackground: View? = null
    var logo: View? = null

    //positions used to animate views during scroll
    var finalTabsY = 0f
    var finalTitleY = 0f
    var finalTitleHeight = 0f
    var finalTitleX = 0f
    var originalTitleY = 0f
    var originalTitleHeight = 0f
    var originalTitleX = 0f
    var finalScale = 0f

    fun withPagerSlidingTabStrip(pagerSlidingTabStrip: View?): MaterialViewPagerHeader {
        mPagerSlidingTabStrip = pagerSlidingTabStrip
        mPagerSlidingTabStrip!!.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                finalTabsY = Utils.dpToPx(-2f, context)
                mPagerSlidingTabStrip!!.viewTreeObserver.removeOnPreDrawListener(this)
                return false
            }
        })
        return this
    }

    fun withHeaderBackground(headerBackground: View?): MaterialViewPagerHeader {
        this.headerBackground = headerBackground
        return this
    }

    fun withStatusBackground(statusBackground: View?): MaterialViewPagerHeader {
        this.statusBackground = statusBackground
        return this
    }

    fun withToolbarLayoutBackground(toolbarLayoutBackground: View?): MaterialViewPagerHeader {
        this.toolbarLayoutBackground = toolbarLayoutBackground
        return this
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun withLogo(logo: View?): MaterialViewPagerHeader {
        this.logo = logo

        //when logo get a height, initialise initial & final logo positions
        toolbarLayout.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                //rotation fix, if not set, originalTitleY = Na
                ViewCompat.setTranslationY(this@MaterialViewPagerHeader.logo, 0f)
                ViewCompat.setTranslationX(this@MaterialViewPagerHeader.logo, 0f)
                originalTitleY = ViewCompat.getY(this@MaterialViewPagerHeader.logo)
                originalTitleX = ViewCompat.getX(this@MaterialViewPagerHeader.logo)
                originalTitleHeight = logo!!.height.toFloat()
                finalTitleHeight = Utils.dpToPx(21f, context)

                //the final scale of the logo
                finalScale = finalTitleHeight / originalTitleHeight
                finalTitleY =
                    (toolbar.paddingTop + toolbar.height) / 2 - finalTitleHeight / 2 - (1 - finalScale) * finalTitleHeight

                //(mLogo.getWidth()/2) *(1-finalScale) is the margin left added by the scale() on the logo
                //when logo scale down, the content stay in center, so we have to annually remove the left padding
                finalTitleX = Utils.dpToPx(52f, context) - logo.width / 2 * (1 - finalScale)
                toolbarLayout.viewTreeObserver.removeOnPreDrawListener(this)
                return false
            }
        })
        return this
    }

    companion object {
        fun withToolbar(toolbar: Toolbar): MaterialViewPagerHeader {
            return MaterialViewPagerHeader(toolbar)
        }
    }

}