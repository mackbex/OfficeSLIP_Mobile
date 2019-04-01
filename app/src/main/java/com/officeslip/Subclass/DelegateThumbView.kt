package com.officeslip.Subclass

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.view.MotionEvent
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.officeslip.CIRCLE_ALPHA_DEFAULT
import com.officeslip.PEN_ALPHA_DEFAULT
import com.officeslip.RECT_ALPHA_DEFAULT
import com.officeslip.Structure.Shape
import com.officeslip.Util.Common
import com.officeslip.Util.Logger
import com.officeslip.View.EditSlip.EditSlipActivity
import com.officeslip.px
import java.lang.Exception

class DelegateThumbView(var imageView: TouchImageView,var objImageInfo: JsonObject, var strSender:String = "ADD") : TouchImageView.ImageViewDelegate {

    var classType = object : TypeToken<HashMap<String, MutableList<Shape>>>(){}.type
    var m_mapShape =  Gson().fromJson<HashMap<String,MutableList<Shape>>>(objImageInfo.get("SHAPE_DATA"), classType)
    var m_paint = Paint()
    var m_textPaint = TextPaint()
    var m_rect = RectF()
    var m_wRatio = 1f
    var m_hRatio = 1f
    var m_C = Common()

    companion object {
        const val LEFT_POINT = 1443
        const val TOP_POINT = 1444
        const val RIGHT_POINT = 1445
        const val BOTTOM_POINT = 1446
    }

    init {
//        try {
//            if("SEARCH" == strSender.toUpperCase())
//            {
//                var pixelImageViewWidth = m_C.pixel2pt(imageView.imageWidth.toInt()).toFloat()
//                var pixelImageViewHeight = m_C.pixel2pt(imageView.imageHeight.toInt()).toFloat()
//
//                var slipRect = objImageInfo.get("SLIP_RECT").asString.split(",")
//                var pixelOrigianlWidth = slipRect[2].toFloat()
//                var pixelOrigianlHeight = slipRect[3].toFloat()
//
//                m_wRatio = pixelImageViewWidth / pixelOrigianlWidth//.toFloat()
//                m_hRatio = pixelImageViewHeight / pixelOrigianlHeight//.toFloat()
//
//            }
//            else
//            {
//                m_wRatio = imageView.imageWidth / objImageInfo.get("Width").asFloat//.toFloat()
//                m_hRatio = imageView.imageHeight / objImageInfo.get("Height").asFloat//.toFloat()
//            }
//        }
//        catch (e:Exception)
//        {
//
//        }
    }

    override fun onDraw(canvas: Canvas?) {

        var horizontalSpace = (imageView.viewWidth - imageView.imageWidth) / 2f
        //if(horizontalSpace < 0) horizontalSpace = 0f
        var verticalSpace = (imageView.viewHeight - imageView.imageHeight) / 2f
        //if(verticalSpace < 0) verticalSpace = 0f

        //Calc thumb imageview ratio
        try {
            if ("ADD" == strSender) {
                m_wRatio = imageView.imageWidth / objImageInfo.get("Width").asFloat//.toFloat()
                m_hRatio = imageView.imageHeight / objImageInfo.get("Height").asFloat//.toFloat()
            } else if ("SEARCH" == strSender) {
                var pixelImageViewWidth = m_C.pixel2pt(imageView.imageWidth.toInt()).toFloat()
                var pixelImageViewHeight = m_C.pixel2pt(imageView.imageHeight.toInt()).toFloat()

                var slipRect = objImageInfo.get("SLIP_RECT").asString.split(",")
                var pixelOrigianlWidth = slipRect[2].toFloat()
                var pixelOrigianlHeight = slipRect[3].toFloat()

                m_wRatio = pixelImageViewWidth / pixelOrigianlWidth//.toFloat()
                m_hRatio = pixelImageViewHeight / pixelOrigianlHeight//.toFloat()
            }
        }
        catch (e:Exception)
        {
            Logger.WriteException(this::javaClass.name, "onDraw", e, 7)
            m_mapShape = null
        }

        if(m_mapShape == null) return

        for ((key, value) in m_mapShape) {
            var strType = key
            for (shape in value) {
                canvas?.apply {

                    var leftPoint = shape.leftPoint * m_wRatio + horizontalSpace
                    var topPoint = shape.topPoint * m_hRatio + verticalSpace
                    var rightPoint = shape.rightPoint * m_wRatio + horizontalSpace
                    var bottomPoint = shape.bottomPoint * m_hRatio + verticalSpace

                    when(strType.toUpperCase()) {
                        EditSlipActivity.MODE_PEN -> {
                            m_paint.apply {
                                color = Color.parseColor(shape.bgColor)
                                alpha = PEN_ALPHA_DEFAULT
                                isAntiAlias = true
                                style = Paint.Style.FILL
                            }
                            drawRect(leftPoint, topPoint, rightPoint, bottomPoint, m_paint)
                        }
                        EditSlipActivity.MODE_RECT -> {
                            if(shape.backFlag == "0")
                            {
                                m_paint.apply {
                                    isAntiAlias = true
                                    style = Paint.Style.FILL
                                    color = Color.parseColor(shape.bgColor)
                                    alpha = RECT_ALPHA_DEFAULT
                                }
                                drawRect(leftPoint,topPoint, rightPoint, bottomPoint, m_paint)
                            }

                            m_paint.apply {
                                color = Color.parseColor(shape.lineColor)
                                alpha = RECT_ALPHA_DEFAULT
                                isAntiAlias = true
                                style = Paint.Style.STROKE
                                strokeWidth = shape.weight.toInt().px.toFloat() * if(m_wRatio > 1f) 1f else m_wRatio
                            }
                            drawRect(leftPoint,topPoint, rightPoint, bottomPoint, m_paint)
                        }
                        EditSlipActivity.MODE_MEMO -> {

                            m_paint.apply {
                                color = Color.parseColor(shape.bgColor)
                                alpha = (255 * shape.alpha).toInt()
                                isAntiAlias = true
                                // strokeWidth = shape.weight.px.toFloat()
                                style = Paint.Style.FILL
                            }
                            drawRect(leftPoint,topPoint, rightPoint, bottomPoint, m_paint)

                            m_paint.apply {
                                color = Color.parseColor(shape.lineColor)
                                alpha = (255 * shape.alpha).toInt()
                                isAntiAlias = true
                                strokeWidth = (shape.weight.toInt().px.toFloat() * if(m_wRatio > 1f) 1f else m_wRatio)
                                style = Paint.Style.STROKE
                            }
                            drawRect(leftPoint,topPoint, rightPoint, bottomPoint, m_paint)

                            var fTextSize = shape.fontSize.px.toFloat() * m_wRatio
                            var text = shape.text
                            m_textPaint.apply {
                                textSize = fTextSize
                                color = Color.parseColor(shape.fontColor)
                                style = Paint.Style.FILL
                                if(shape.bold == "1" && shape.italic == "1")
                                {
                                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                                }
                                else
                                {
                                    if(shape.bold == "1") typeface = Typeface.DEFAULT_BOLD
                                    if(shape.italic == "1") typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                                }
                            }

                            var width = rightPoint - leftPoint
                            var startLeft = leftPoint
                            var startTop = topPoint
                            //sort horizontal points
                            if (leftPoint > rightPoint)
                            {
                                width = leftPoint - rightPoint
                                startLeft = rightPoint
                                startTop = bottomPoint
                            }

//                                val ellipsize = TextUtils.ellipsize(text, m_textPaint, width, TextUtils.TruncateAt.END)
//                                val sl = StaticLayout(ellipsize, m_textPaint, width.toInt(), Layout.Alignment.ALIGN_NORMAL, 1f, 1f, false)

                            val sl = StaticLayout(text, m_textPaint, width.toInt(), Layout.Alignment.ALIGN_NORMAL, 1f, 1f, false)
                            canvas.save()
                            canvas.translate(startLeft, startTop)
                            sl.draw(canvas)
                            canvas.restore()
                        }
                        EditSlipActivity.MODE_CIRCLE -> {
                            if(shape.backFlag == "0")
                            {
                                m_rect.set(leftPoint, topPoint, rightPoint, bottomPoint)
                                m_paint.apply {
                                    color = Color.parseColor(shape.bgColor)
                                    alpha = CIRCLE_ALPHA_DEFAULT
                                    isAntiAlias = true
                                    // strokeWidth = shape.weight.px.toFloat()
                                    style = Paint.Style.FILL
                                }
                                drawOval(m_rect, m_paint)
                            }

                            m_rect.set(leftPoint, topPoint, rightPoint, bottomPoint)
                            m_paint.apply {
                                color = Color.parseColor(shape.lineColor)
                                alpha = CIRCLE_ALPHA_DEFAULT
                                isAntiAlias = true
                                strokeWidth = shape.weight.toInt().px.toFloat() * if(m_wRatio > 1f) 1f else m_wRatio
                                style = Paint.Style.STROKE
                            }
                            drawOval(m_rect, m_paint)
                        }
                    }
                }
            }
        }

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, ildh: Int) {
    }

    override fun onTouchEvent(e: MotionEvent?) {
    }

    fun pointToViewPoint(point:Float, imageView: TouchImageView, nPos:Int ):Float
    {
        var space = 0f
        var curRectPoint = 0f
        var zoomedRatio = 0f


        Log.i("Image",imageView.imageWidth.toString())
        Log.i("Intrisic",imageView.drawable.intrinsicWidth.toString())
        Log.i("ImageView",imageView.viewWidth.toString())
        Log.i("Measured",imageView.measuredWidth.toString())
        Log.i("Measured",imageView.width.toString())
        //(shape.leftPoint * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
        when(nPos)
        {
            LEFT_POINT -> {
                zoomedRatio = imageView.drawable.intrinsicWidth / imageView.imageWidth
                space = (imageView.viewWidth - imageView.imageWidth) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.left
            }

            RIGHT_POINT -> {
                zoomedRatio = imageView.drawable.intrinsicWidth / imageView.imageWidth
                space = (imageView.viewWidth - imageView.imageWidth) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.left
            }

            TOP_POINT -> {
                zoomedRatio = imageView.drawable.intrinsicHeight / imageView.imageHeight
                space = (imageView.viewHeight - imageView.imageHeight) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.top
            }

            BOTTOM_POINT -> {
                zoomedRatio = imageView.drawable.intrinsicHeight / imageView.imageHeight
                space = (imageView.viewHeight - imageView.imageHeight) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.top
            }
        }


        return  (point * zoomedRatio) + space - (curRectPoint * zoomedRatio)
    }
}