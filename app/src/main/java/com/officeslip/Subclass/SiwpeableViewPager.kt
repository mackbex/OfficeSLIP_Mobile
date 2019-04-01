package com.officeslip.Subclass

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class SiwpeableViewPager(context: Context?, attrs: AttributeSet?) : ViewPager(context!!, attrs) {

    var swipeEnable:Boolean = true

    override fun onTouchEvent(ev: MotionEvent?): Boolean {

        if(this.swipeEnable)
        {
            return super.onTouchEvent(ev)
        }
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {

        if(this.swipeEnable)
        {
            return super.onInterceptTouchEvent(ev)
        }
        return false
    }

    fun setSwipeEnabled(enable:Boolean){
        this.swipeEnable = enable
    }

}