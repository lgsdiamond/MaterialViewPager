package com.lgsdiamond.materialviewpager

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet

/**
 * Created by florentchampigny on 29/04/15.
 *
 *
 * Save attributes given to MaterialViewPager from layout
 */
class MaterialViewPagerSettings : Parcelable {
    var headerLayoutId = 0
    var pagerTitleStripId = 0
    var viewpagerId = 0
    var logoLayoutId = 0
    var logoMarginTop = 0
    var headerAdditionalHeight = 0
    var headerHeight = 0
    var headerHeightPx = 0
    var color = 0
    var headerAlpha = 0f
    var parallaxHeaderFactor = 0f
    var imageHeaderDarkLayerAlpha = 0f
    var hideToolbarAndTitle = false
    var hideLogoWithFade = false
    var enableToolbarElevation = false
    var displayToolbarWhenSwipe = false
    var toolbarTransparent = false
    var animatedHeaderImage = false
    var disableToolbar = false

    //region parcelable
    constructor()
    private constructor(`in`: Parcel) {
        headerLayoutId = `in`.readInt()
        pagerTitleStripId = `in`.readInt()
        viewpagerId = `in`.readInt()
        logoLayoutId = `in`.readInt()
        logoMarginTop = `in`.readInt()
        headerAdditionalHeight = `in`.readInt()
        headerHeight = `in`.readInt()
        headerHeightPx = `in`.readInt()
        color = `in`.readInt()
        headerAlpha = `in`.readFloat()
        imageHeaderDarkLayerAlpha = `in`.readFloat()
        parallaxHeaderFactor = `in`.readFloat()
        hideToolbarAndTitle = `in`.readByte().toInt() != 0
        hideLogoWithFade = `in`.readByte().toInt() != 0
        enableToolbarElevation = `in`.readByte().toInt() != 0
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(headerLayoutId)
        dest.writeInt(pagerTitleStripId)
        dest.writeInt(viewpagerId)
        dest.writeInt(logoLayoutId)
        dest.writeInt(logoMarginTop)
        dest.writeInt(headerAdditionalHeight)
        dest.writeInt(headerHeight)
        dest.writeInt(headerHeightPx)
        dest.writeInt(color)
        dest.writeFloat(headerAlpha)
        dest.writeFloat(imageHeaderDarkLayerAlpha)
        dest.writeFloat(parallaxHeaderFactor)
        dest.writeByte(if (hideToolbarAndTitle) 1.toByte() else 0.toByte())
        dest.writeByte(if (hideLogoWithFade) 1.toByte() else 0.toByte())
        dest.writeByte(if (enableToolbarElevation) 1.toByte() else 0.toByte())
    }

    /**
     * Retrieve attributes from the MaterialViewPager
     *
     * @param context
     * @param attrs
     */
    fun handleAttributes(context: Context, attrs: AttributeSet?) {
        try {
            val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaterialViewPager)
            run {
                headerLayoutId =
                    styledAttrs.getResourceId(R.styleable.MaterialViewPager_viewpager_header, -1)
            }
            run {
                pagerTitleStripId = styledAttrs.getResourceId(
                    R.styleable.MaterialViewPager_viewpager_pagerTitleStrip,
                    -1
                )
                if (pagerTitleStripId == -1) {
                    pagerTitleStripId = R.layout.material_view_pager_pagertitlestrip_standard
                }
            }
            run {
                viewpagerId =
                    styledAttrs.getResourceId(R.styleable.MaterialViewPager_viewpager_viewpager, -1)
            }
            run {
                logoLayoutId =
                    styledAttrs.getResourceId(R.styleable.MaterialViewPager_viewpager_logo, -1)
                logoMarginTop = styledAttrs.getDimensionPixelSize(
                    R.styleable.MaterialViewPager_viewpager_logoMarginTop,
                    0
                )
            }
            run { color = styledAttrs.getColor(R.styleable.MaterialViewPager_viewpager_color, 0) }
            run {
                headerHeightPx = styledAttrs.getDimensionPixelOffset(
                    R.styleable.MaterialViewPager_viewpager_headerHeight,
                    200
                )
                headerHeight =
                    Math.round(Utils.pxToDp(headerHeightPx.toFloat(), context)) //convert to dp
            }
            run {
                headerAdditionalHeight = styledAttrs.getDimensionPixelOffset(
                    R.styleable.MaterialViewPager_viewpager_headerAdditionalHeight,
                    60
                )
            }
            run {
                headerAlpha =
                    styledAttrs.getFloat(R.styleable.MaterialViewPager_viewpager_headerAlpha, 0.5f)
            }
            run {
                imageHeaderDarkLayerAlpha = styledAttrs.getFloat(
                    R.styleable.MaterialViewPager_viewpager_imageHeaderDarkLayerAlpha,
                    0.0f
                )
            }
            run {
                parallaxHeaderFactor = styledAttrs.getFloat(
                    R.styleable.MaterialViewPager_viewpager_parallaxHeaderFactor,
                    1.5f
                )
                parallaxHeaderFactor = Math.max(parallaxHeaderFactor, 1f) //min=1
            }
            run {
                hideToolbarAndTitle = styledAttrs.getBoolean(
                    R.styleable.MaterialViewPager_viewpager_hideToolbarAndTitle,
                    false
                )
                hideLogoWithFade = styledAttrs.getBoolean(
                    R.styleable.MaterialViewPager_viewpager_hideLogoWithFade,
                    false
                )
            }
            run {
                enableToolbarElevation = styledAttrs.getBoolean(
                    R.styleable.MaterialViewPager_viewpager_enableToolbarElevation,
                    false
                )
            }
            run {
                displayToolbarWhenSwipe = styledAttrs.getBoolean(
                    R.styleable.MaterialViewPager_viewpager_displayToolbarWhenSwipe,
                    false
                )
            }
            run {
                toolbarTransparent = styledAttrs.getBoolean(
                    R.styleable.MaterialViewPager_viewpager_transparentToolbar,
                    false
                )
            }
            run {
                animatedHeaderImage = styledAttrs.getBoolean(
                    R.styleable.MaterialViewPager_viewpager_animatedHeaderImage,
                    true
                )
            }
            run {
                disableToolbar = styledAttrs.getBoolean(
                    R.styleable.MaterialViewPager_viewpager_disableToolbar,
                    false
                )
            }
            styledAttrs.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } //endregion

    companion object {
        //attributes are protected and can be used by class from the same package
        //com.lgsdiamond.materialviewpager
        val CREATOR: Parcelable.Creator<MaterialViewPagerSettings> =
            object : Parcelable.Creator<MaterialViewPagerSettings> {
                override fun createFromParcel(source: Parcel): MaterialViewPagerSettings? {
                    return MaterialViewPagerSettings(source)
                }

                override fun newArray(size: Int): Array<MaterialViewPagerSettings?> {
                    return arrayOfNulls(size)
                }
            }
    }
}