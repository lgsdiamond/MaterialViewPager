package com.lgsdiamond.materialviewpager.header

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

/**
 * Created by florentchampigny on 10/06/15.
 */
class HeaderDesign private constructor() {
    var color = 0
    var colorRes = 0
    var imageUrl: String? = null
    var drawable: Drawable? = null

    companion object {
        fun fromColorAndUrl(@ColorInt color: Int, imageUrl: String): HeaderDesign {
            val headerDesign = HeaderDesign()
            headerDesign.color = color
            headerDesign.imageUrl = imageUrl
            return headerDesign
        }

        fun fromColorResAndUrl(@ColorRes colorRes: Int, imageUrl: String): HeaderDesign {
            val headerDesign = HeaderDesign()
            headerDesign.colorRes = colorRes
            headerDesign.imageUrl = imageUrl
            return headerDesign
        }

        fun fromColorAndDrawable(@ColorInt color: Int, drawable: Drawable): HeaderDesign {
            val headerDesign = HeaderDesign()
            headerDesign.drawable = drawable
            headerDesign.color = color
            return headerDesign
        }

        fun fromColorResAndDrawable(@ColorRes colorRes: Int, drawable: Drawable): HeaderDesign {
            val headerDesign = HeaderDesign()
            headerDesign.colorRes = colorRes
            headerDesign.drawable = drawable
            return headerDesign
        }
    }
}