package com.lgsdiamond.materialviewpager.header

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.flaviofaria.kenburnsview.KenBurnsView
import com.lgsdiamond.materialviewpager.header.MaterialViewPagerImageHelper.setImageDrawable
import com.lgsdiamond.materialviewpager.header.MaterialViewPagerImageHelper.setImageUrl

/**
 * Created by florentchampigny on 29/04/15.
 * The MaterialViewPager animated Header
 * Using com.flaviofaria.kenburnsview.KenBurnsView
 * https://github.com/flavioarfaria/KenBurnsView
 */
class MaterialViewPagerKenBurnsHeader : KenBurnsView {
    //region construct
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    //endregion
    /**
     * change the image with a fade
     *
     * @param urlImage
     * @param fadeDuration TODO : remove Picasso
     */
    fun setImageUrl(urlImage: String, fadeDuration: Int) {
        setImageUrl(this, urlImage, fadeDuration)
    }

    /**
     * change the image with a fade
     *
     * @param drawable
     * @param fadeDuration
     */
    fun setImageDrawable(drawable: Drawable, fadeDuration: Int) {
        setImageDrawable(this, drawable, fadeDuration)
    }
}