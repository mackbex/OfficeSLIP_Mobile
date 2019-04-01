package com.officeslip.View.EditSlip

import android.graphics.*
import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import com.officeslip.Structure.Shape
import com.officeslip.Subclass.TouchImageView
import android.graphics.DashPathEffect
import com.sgenc.officeslip.*
import com.officeslip.Util.Common
import com.officeslip.Util.SQLite
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.officeslip.*


class DrawRect(var imageView: TouchImageView, var mapShape:HashMap<String, MutableList<Shape>>, var mode:String) {

    var mapProperty = HashMap<String, String>()
    private var callBack: ChangeMode? = null
    var isClosed = false
    var m_selectedShape: Shape? = null
    var m_borderPaint = Paint()
    var m_borderCircle = Paint()
    var m_shapeBGPaint = Paint()
    var m_shapeLinePaint = Paint()
    var m_shapeTextPaint = TextPaint()
    var m_rectBG = RectF()
    var m_rectLine = RectF()
    private lateinit var m_staticLayout:StaticLayout

    var m_C = Common()


    init {

        m_borderPaint.apply {
            color = ContextCompat.getColor(imageView.context, R.color.Red)
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 6f
            pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
        }

        m_borderCircle.apply {
            color = ContextCompat.getColor(imageView.context, R.color.White)
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 4f
        }
        m_shapeBGPaint.apply {
                isAntiAlias = true
                style = Paint.Style.FILL
        }
        m_shapeLinePaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        loadProperty(mode)
    }

    interface ChangeMode {
        fun changeMode(mode:Int)
    }

    //Load property from SQLite
    fun loadProperty(mode:String) {
        SQLite(imageView.context).instantGetRecord(SQLITE_CREATE_PROPERTY_T, SQLITE_GET_PROPERTY, arrayOf(mode))?.forEach{
            mapProperty = it as HashMap<String, String>
        }
    }

    fun setChangeModeListener(listener: ChangeMode) {
        this.callBack = listener
    }

    fun showDrawView(selectedShape: Shape? = null)
    {
        m_selectedShape = selectedShape
        //Space betwwen imageview and device layout
        var verticalSpace = (imageView.viewHeight - imageView.imageHeight) / 2
        if(verticalSpace < 0f) verticalSpace = 0f
        var horizontalSpace = (imageView.viewWidth - imageView.imageWidth) / 2
        if(horizontalSpace < 0f) horizontalSpace = 0f

        imageView.setDelegate(object : TouchImageView.ImageViewDelegate {

            override fun onDraw(canvas: Canvas?) {

                //Space betwwen imageview and device layout
                verticalSpace = (imageView.viewHeight - imageView.imageHeight) / 2f
                if(verticalSpace < 0f) verticalSpace = 0f
                horizontalSpace = (imageView.viewWidth - imageView.imageWidth) / 2f
                if(horizontalSpace < 0f) horizontalSpace = 0f

                var zoomedWRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth//.toFloat()
                var zoomedHRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight//.toFloat()


                for ((key, value) in mapShape) {
                    var strType = key
                    var list = value as List<Shape>
                    for(shape in list)
                    {
                        canvas?.apply {

                            var leftPoint = (shape.leftPoint * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
                            var topPoint = (shape.topPoint * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio)
                            var rightPoint = (shape.rightPoint * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
                            var bottomPoint = (shape.bottomPoint * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedWRatio)//((shape.leftPoint + shape.weight) * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio)

                            when(strType.toUpperCase())
                            {
                                EditSlipActivity.MODE_PEN -> {

                                    m_shapeBGPaint.apply {
                                        color = Color.parseColor(shape.bgColor)
                                        alpha = PEN_ALPHA_DEFAULT
                                    }
                                    drawRect(leftPoint,topPoint, rightPoint, bottomPoint, m_shapeBGPaint )
                                }
                                EditSlipActivity.MODE_RECT -> {
                                    if(shape.backFlag == "0")
                                    {
                                        m_shapeBGPaint.apply {
                                            color = Color.parseColor(shape.bgColor)
                                            alpha = RECT_ALPHA_DEFAULT
                                        }
                                        drawRect(leftPoint,topPoint, rightPoint, bottomPoint, m_shapeBGPaint )
                                    }

                                    m_shapeLinePaint.apply {
                                        color = Color.parseColor(shape.lineColor)
                                        alpha = RECT_ALPHA_DEFAULT
                                        strokeWidth = shape.weight.toInt().px.toFloat()
                                    }
                                    drawRect(leftPoint,topPoint, rightPoint, bottomPoint, m_shapeLinePaint)
                                }

                                EditSlipActivity.MODE_MEMO -> {

                                    m_shapeBGPaint.apply {
                                        color = Color.parseColor(shape.bgColor)
                                        alpha = (255 * shape.alpha).toInt()
                                    }

                                    m_shapeLinePaint.apply {
                                        color = Color.parseColor(shape.lineColor)
                                        alpha = (255 * shape.alpha).toInt()
                                        strokeWidth = shape.weight.toInt().px.toFloat()
                                    }

                                    drawRect(leftPoint,topPoint, rightPoint, bottomPoint, m_shapeBGPaint)
                                    drawRect(leftPoint,topPoint, rightPoint, bottomPoint, m_shapeLinePaint)

                                    var fTextSize = (shape.fontSize.px.toFloat() * imageView.currentZoom) * 2
                                    var text = shape.text
                                    m_shapeTextPaint.apply {
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

                                        //startTop = bottomPoint
                                    }

                                    if(topPoint > bottomPoint)
                                    {
                                        startTop = bottomPoint
                                    }

                                   // val ellipsize = TextUtils.ellipsize(text, m_shapeTextPaint, width, TextUtils.TruncateAt.END)
                                   // val sl = StaticLayout(ellipsize, m_shapeTextPaint, width.toInt(), Layout.Alignment.ALIGN_NORMAL, 1f, 1f, false)
                                    val sl = StaticLayout(text, m_shapeTextPaint, width.toInt(), Layout.Alignment.ALIGN_NORMAL, 1f, 1f, false)

                                    canvas.save()
                                    canvas.translate(startLeft, startTop)
                                    sl.draw(canvas)
                                    canvas.restore()
                                }

                                EditSlipActivity.MODE_CIRCLE -> {
                                    if(shape.backFlag == "0")
                                    {
                                        m_shapeBGPaint.apply {
                                            color = Color.parseColor(shape.bgColor)
                                            alpha = CIRCLE_ALPHA_DEFAULT
                                        }

                                        m_rectBG.set(leftPoint, topPoint, rightPoint, bottomPoint)
                                        drawOval(m_rectBG, m_shapeBGPaint)
                                    }

                                    m_shapeLinePaint.apply {
                                        color = Color.parseColor(shape.lineColor)
                                        alpha = CIRCLE_ALPHA_DEFAULT
                                        strokeWidth = shape.weight.toInt().px.toFloat()
                                    }

                                    m_rectLine.set(leftPoint, topPoint, rightPoint, bottomPoint)
                                    drawOval(m_rectLine, m_shapeLinePaint)
                                }
                            }

                            //If selected mode
                            if(shape.tag == m_selectedShape?.tag)
                            {

                                var selectedPath = Path().apply {
                                    reset()
                                    fillType = Path.FillType.EVEN_ODD
                                    moveTo(leftPoint, topPoint)
                                    lineTo(rightPoint, topPoint)
                                    lineTo(rightPoint, bottomPoint)
                                    lineTo(leftPoint, bottomPoint)
                                    close()
                                }

                                drawPath(selectedPath, m_borderPaint)
                                drawCircle(leftPoint, topPoint, 6.px.toFloat(), m_borderCircle)
                                drawCircle(rightPoint, topPoint, 6.px.toFloat(), m_borderCircle)
                                drawCircle(leftPoint, bottomPoint, 6.px.toFloat(), m_borderCircle)
                                drawCircle(rightPoint, bottomPoint, 6.px.toFloat(), m_borderCircle)

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

                var wRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth//.toFloat()
                var hRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight//.toFloat()
                var zoomedWRatio = imageView.drawable.intrinsicWidth.toFloat() / imageView.imageWidth
                var zoomedHRatio = imageView.drawable.intrinsicHeight.toFloat() / imageView.imageHeight

                if (isClosed) {
                    return
                }

                when (e?.actionMasked) {
                    MotionEvent.ACTION_POINTER_DOWN -> {

                        mapShape.get(mode.toUpperCase())?.let {
                            var listItem = it as MutableList<Shape>
                            var curShape = listItem[listItem.lastIndex]
                         //   listItem.remove(curShape)
                            callBack?.changeMode(EditSlipActivity.MODE_PINCH)
                            imageView.setTouchEnable(true)
                        }
                    }

                    MotionEvent.ACTION_DOWN -> {

                        //Get pen list from drawn shape map
                        var listItem = mutableListOf<Shape>()
                        mapShape?.let {
                            var item = it.get(mode)
                            if(item != null) listItem = it.get(mode) as MutableList<Shape>
                        }
                        mapShape.put(mode, listItem)

                        var eX = (e.x   - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
                        var eY = (e.y   - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio

                        //Find last touched shape
                        if(m_selectedShape != null)
                        {
                            var shape: Shape? = null
                            listItem?.forEach {
                                if (m_selectedShape == it) {
                                    shape = it
                                }
                            }

                            shape?.run {
                                var rect = Rect(leftPoint.toInt(), topPoint.toInt(), rightPoint.toInt(), bottomPoint.toInt())
                                if(!rect.contains(eX.toInt(), eY.toInt()))
                                {
                                    m_selectedShape = null
                                }
                                else
                                {
                                    //Get a gap between touched points and rect points
                                    leftGap = eX - leftPoint
                                    if(leftGap < 0) leftGap = 0f

                                    topGap = eY - topPoint
                                    if(topGap < 0) topGap = 0f
                                }
                            }
                        }

                        //If no rect selected
                        if(m_selectedShape == null) {

                            imageView.setTouchEnable(false)

                            var shape: Shape? = null

                            when(mode) {
                                EditSlipActivity.MODE_CIRCLE -> {
                                    shape = createCircleShape(m_C.getDate("yyyyMMddhhmmsss"), e.x, e.y, horizontalSpace, verticalSpace, wRatio, hRatio, zoomedWRatio, zoomedHRatio)
                                }
                                EditSlipActivity.MODE_RECT -> {
                                    shape = createRectShape(m_C.getDate("yyyyMMddhhmmsss"), e.x, e.y, horizontalSpace, verticalSpace, wRatio, hRatio, zoomedWRatio, zoomedHRatio)
                                }
                                EditSlipActivity.MODE_MEMO -> {
                                    shape = createMemoShape(m_C.getDate("yyyyMMddhhmmsss"), e.x, e.y, horizontalSpace, verticalSpace, wRatio, hRatio, zoomedWRatio, zoomedHRatio)
                                }
                                EditSlipActivity.MODE_PEN -> {
                                    shape = createPenShape(m_C.getDate("yyyyMMddhhmmsss"), e.x, e.y, horizontalSpace, verticalSpace, wRatio, hRatio, zoomedWRatio, zoomedHRatio)
                                }
                            }

                            shape?.let {
                                listItem.add(it)
                            }

                        }
                    }

                    MotionEvent.ACTION_MOVE -> {

                        var eX = (e.x   - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
                        var eY = (e.y   - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio

                        var shape: Shape? = null
                        var listItem = (mapShape.get(mode) as MutableList<Shape>)

                        //If is draw mode
                        if(m_selectedShape == null)
                        {
                            shape = listItem[listItem.lastIndex]
                        }
                        //else
                        else
                        {
                            for(shapeItem in listItem)
                            {
                                if(m_selectedShape == shapeItem)
                                {
                                    shape = shapeItem
                                }
                            }
                        }

                        shape?.run {
                            when(mode)
                            {
                                EditSlipActivity.MODE_PEN -> {
                                    //If shape is not selected
                                    if(m_selectedShape == null) {
                                        rightPoint = eX
                                    }
                                    else {
                                        leftPoint = eX - leftGap
                                        topPoint = eY - topGap
                                        rightPoint = leftPoint + width
                                        bottomPoint = topPoint + weight.toInt().px
                                    }
                                }
                                else -> {
                                    //If shape is not selected
                                    if(m_selectedShape == null) {
                                        rightPoint = eX
                                        bottomPoint = eY
                                    }
                                    else {
                                        leftPoint = eX - leftGap
                                        topPoint = eY - topGap
                                        rightPoint = leftPoint + width
                                        bottomPoint = topPoint + height
                                    }
                                }
                            }

                            imageView.invalidate()
                        }
                    }
                    MotionEvent.ACTION_UP ->{
                        var listItem = (mapShape.get(mode) as MutableList<Shape>)

                        var eX = (e.x   - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
                        var eY = (e.y   - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio

                        var curShape = listItem.get(listItem.lastIndex)


                        //sort horizontal points
                        if (curShape.leftPoint > curShape.rightPoint)
                        {
                            var right = curShape.leftPoint
                            curShape.leftPoint = curShape.rightPoint
                            curShape.rightPoint = right
                        }

                        //sort vertical points
                        if(curShape.topPoint > curShape.bottomPoint)
                        {
                            var bottom = curShape.topPoint
                            curShape.topPoint = curShape.bottomPoint
                            curShape.bottomPoint = bottom
                        }

                        curShape.width = curShape.rightPoint - curShape.leftPoint
                        curShape.height = curShape.bottomPoint - curShape.topPoint

                        //minimum size
                        if(curShape.width < SHAPE_MIN_SIZE.px || curShape.height < SHAPE_MIN_SIZE.px) {
                            listItem.remove(curShape)
                        }

                        imageView.invalidate()
                    }
                }
            }
        })
    }

    /**
     * Create pen shape
     */
    fun createPenShape(tag:String, x:Float, y:Float, horizontalSpace: Float, verticalSpace: Float, wRatio:Float, hRatio:Float, zoomedWRatio:Float, zoomedHRatio:Float): Shape {
        var shape = Shape()
        var bgColor = mapProperty.get("BackColor")
        if(m_C.isBlank(bgColor)) bgColor = PEN_BACKGROUND_DEFAULT
        shape.bgColor = bgColor

        var weight = mapProperty.get("WEIGHT")
        if(m_C.isBlank(weight)) weight = PEN_WEIGHT_DEFAULT.toString()
        shape.weight = weight!!.toFloat()

        var wRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth//.toFloat()
        var hRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight//.toFloat()

        var zoomedWRatio = imageView.drawable.intrinsicWidth.toFloat() / imageView.imageWidth
        var zoomedHRatio = imageView.drawable.intrinsicHeight.toFloat() / imageView.imageHeight

        shape.leftPoint = (x - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
        shape.rightPoint = (x - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
        shape.topPoint = (y - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio
        shape.bottomPoint = shape.topPoint + shape.weight.toInt().px
        shape.tag = mode + "_" + tag

        return shape
    }

    /**
     * Create rect shape
     */
    fun createRectShape(tag:String, x:Float, y:Float, horizontalSpace: Float, verticalSpace: Float, wRatio:Float, hRatio:Float, zoomedWRatio:Float, zoomedHRatio:Float): Shape {
        var shape = Shape()

        //perhaps bg enable
        var bgFlag = mapProperty.get("BackFlag")
        if(m_C.isBlank(bgFlag)) bgFlag = RECT_BG_ENABLE_DEFAULT
        shape.backFlag = bgFlag!!

        //get line option
        var weight = mapProperty.get("WEIGHT")
        if(m_C.isBlank(weight)) weight = RECT_WEIGHT_DEFAULT.toString()
        shape.weight = weight?.toFloat()!!

        var lineColor = mapProperty.get("LineColor")
        if(m_C.isBlank(lineColor)) lineColor = RECT_LINE_DEFAULT
        shape.lineColor = lineColor

        shape.leftPoint = (x - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
        shape.rightPoint = (x - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
        shape.topPoint = (y - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio
        shape.bottomPoint = (y - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio
        shape.tag = mode + "_" + tag

        return shape
    }

    /**
     * Create memo shape
     */
    fun createMemoShape(tag:String, x:Float, y:Float, horizontalSpace: Float, verticalSpace: Float, wRatio:Float, hRatio:Float, zoomedWRatio:Float, zoomedHRatio:Float): Shape {
        var shape = Shape()

        //Get memo alpha
        var bgAlpha = mapProperty.get("ALPHA")
        if(m_C.isBlank(bgAlpha)) bgAlpha = MEMO_ALPHA_DEFAULT.toString()
        shape.alpha = bgAlpha!!.toFloat()

        var bgColor = mapProperty.get("BackColor")
        if(m_C.isBlank(bgColor)) bgColor = MEMO_BACKGROUND_DEFAULT
        shape.bgColor = bgColor

        //get line option
        var weight = mapProperty.get("WEIGHT")
        if(m_C.isBlank(weight)) weight = MEMO_WEIGHT_DEFAULT.toString()
        shape.weight = weight?.toFloat()!!

        var lineColor = mapProperty.get("LineColor")
        if(m_C.isBlank(lineColor)) lineColor = MEMO_LINE_DEFAULT
        shape.lineColor = lineColor

        //Set font option
        var fontSize = mapProperty.get("TEXT_SIZE")
        if(m_C.isBlank(fontSize)) fontSize = MEMO_FONTSIZE_DEFAULT.toString()

        shape.fontSize = fontSize!!.toInt()

        var bold = mapProperty.get("Bold")
        if(m_C.isBlank(bold)) bold = MEMO_BOLD_DEFAULT

        shape.bold = bold!!

        var italic = mapProperty.get("Italic")
        if(m_C.isBlank(italic)) italic = MEMO_ITALIC_DEFAULT
        shape.italic = italic!!

        var fontColor = mapProperty.get("TextColor")
        if(m_C.isBlank(fontColor)) fontColor = MEMO_FOREGROUND_DEFAULT
        shape.fontColor = fontColor


        var text = mapProperty.get("TEXT_TITLE")
        if(m_C.isBlank(text)) text = imageView.context.getString(R.string.new_memo)
        shape.text = text

        shape.leftPoint = (x - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
        shape.rightPoint = (x - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
        shape.topPoint = (y - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio
        shape.bottomPoint = (y - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio
        shape.tag = mode + "_" + tag

        return shape
    }

    /**
     * Create circle shape
     */
    fun createCircleShape(tag:String, x:Float, y:Float, horizontalSpace: Float, verticalSpace: Float, wRatio:Float, hRatio:Float, zoomedWRatio:Float, zoomedHRatio:Float): Shape {
        var shape = Shape()

        //perhaps bg enable
        var bgFlag = mapProperty.get("BackFlag")
        if(m_C.isBlank(bgFlag)) bgFlag = CIRCLE_BG_ENABLE_DEFAULT
        shape.backFlag = bgFlag!!

        //get line option
        var weight = mapProperty.get("WEIGHT")
        if(m_C.isBlank(weight)) weight = CIRCLE_WEIGHT_DEFAULT.toString()
        shape.weight = weight?.toFloat()!!

        var lineColor = mapProperty.get("LineColor")
        if(m_C.isBlank(lineColor)) lineColor = CIRCLE_LINE_DEFAULT

        shape.lineColor     = lineColor
        shape.leftPoint     = (x - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
        shape.rightPoint    = (x - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
        shape.topPoint      = (y - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio
        shape.bottomPoint   = (y - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio
        shape.tag           = mode + "_" + tag

        return shape
    }

    fun removeSelectedItem()
    {
        if(m_selectedShape != null)
        {
            for ((key, value) in mapShape) {
                var strType = key
                var list = value as MutableList<Shape>
                for (shape in list) {
                    if (m_selectedShape?.tag == shape.tag)
                    {
                        list.remove(shape)
                        imageView.invalidate()
                        return
                    }
                }
            }
        }
    }

    fun close() {
        isClosed = true
        m_selectedShape = null
        imageView.invalidate()
    }
}