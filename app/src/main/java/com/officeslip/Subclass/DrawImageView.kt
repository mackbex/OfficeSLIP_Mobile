package com.officeslip.Subclass

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

class DrawImageView:AppCompatImageView {

    private var delegate: TouchImageView.ImageViewDelegate? = null


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)



    fun setDelegate(delegate: TouchImageView.ImageViewDelegate) {
        this.delegate = delegate
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        delegate?.apply {
            onDraw(canvas!!)
        }
    }
}
