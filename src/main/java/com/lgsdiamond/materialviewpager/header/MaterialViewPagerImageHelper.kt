package com.lgsdiamond.materialviewpager.header

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListenerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.lgsdiamond.materialviewpager.MaterialViewPager.OnImageLoadListener

/**
 * Created by florentchampigny on 12/06/15.
 */
object MaterialViewPagerImageHelper {
    private var imageLoadListener: OnImageLoadListener? = null

    /**
     * change the image with a fade
     *
     * @param urlImage
     * @param fadeDuration TODO : remove Picasso
     */
    fun setImageUrl(imageView: ImageView, urlImage: String?, fadeDuration: Int) {
        val alpha = ViewCompat.getAlpha(imageView)

        //fade to alpha=0
        fadeOut(
            imageView,
            fadeDuration,
            object : ViewPropertyAnimatorListenerAdapter() {
                override fun onAnimationEnd(view: View) {
                    super.onAnimationEnd(view)

                    //change the image when alpha=0
                    Glide.with(imageView.context).load(urlImage)
                        .apply(RequestOptions().centerCrop())
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any,
                                target: Target<Drawable?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any,
                                target: Target<Drawable?>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                //then fade to alpha=1
                                object : Handler(Looper.getMainLooper()) {}.post {
                                    fadeIn(
                                        imageView,
                                        alpha,
                                        fadeDuration,
                                        ViewPropertyAnimatorListenerAdapter()
                                    )
                                    if (imageLoadListener != null) {
                                        imageLoadListener!!.onImageLoad(
                                            imageView,
                                            (imageView.drawable as BitmapDrawable).bitmap
                                        )
                                    }
                                }
                                return false
                            }
                        })
                        .into(imageView)
                }
            })
    }

    fun fadeOut(view: View?, fadeDuration: Int, listener: ViewPropertyAnimatorListenerAdapter?) {
        //fade to alpha=0
        ViewCompat.animate(view!!)
            .alpha(0f)
            .setDuration(fadeDuration.toLong())
            .withLayer()
            .setInterpolator(DecelerateInterpolator())
            .setListener(listener)
    }

    fun fadeIn(view: View?, alpha: Float, fadeDuration: Int, listener: ViewPropertyAnimatorListenerAdapter?) {
        //fade to alpha=0
        ViewCompat.animate(view!!)
            .alpha(alpha)
            .setDuration(fadeDuration.toLong())
            .withLayer()
            .setInterpolator(AccelerateInterpolator())
            .setListener(listener)
    }

    /**
     * change the image with a fade
     *
     * @param drawable
     * @param fadeDuration
     */
    fun setImageDrawable(imageView: ImageView, drawable: Drawable?, fadeDuration: Int) {
        val alpha = ViewCompat.getAlpha(imageView)
        fadeOut(
            imageView,
            fadeDuration,
            object : ViewPropertyAnimatorListenerAdapter() {
                override fun onAnimationEnd(view: View) {
                    super.onAnimationEnd(view)
                    //change the image when alpha=0
                    imageView.setImageDrawable(drawable)

                    //then fade to alpha=1
                    fadeIn(
                        imageView,
                        alpha,
                        fadeDuration,
                        ViewPropertyAnimatorListenerAdapter()
                    )
                }
            })
    }

    fun setImageLoadListener(imageLoadListener: OnImageLoadListener?) {
        MaterialViewPagerImageHelper.imageLoadListener = imageLoadListener
    }
}