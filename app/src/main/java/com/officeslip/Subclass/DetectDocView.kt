package com.officeslip.Subclass;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.officeslip.DETECT_RECTANGLE_LIMIT


import com.sgenc.officeslip.R;
import com.officeslip.px

import java.util.ArrayList;

import java.util.HashMap


/**
 * An image ja.view subclass which allows for selection of a portion of the image using a
 * convex quadrilateral
 */

class DetectDocView : AppCompatImageView {

    private var mBackgroundPaint:Paint = Paint()
    private var mBorderPaint:Paint = Paint()
    private var mCircleBorderPaint:Paint = Paint()
    private var mSelectionPath:Path = Path()
    private var mBackgroundPath:Path = Path()

    //Paints for each circle
    private var mUpperLeftCirclePaint:Paint = Paint()
    private var mUpperRightCirclePaint:Paint = Paint()
    private var mLowerRightCirclePaint:Paint = Paint()
    private var mLowerLeftCirclePaint:Paint = Paint()

    private var m_points: List<PointF>? = null
    private var m_orientation = 0

    //Square positions
    private var mUpperLeftPoint:PointF? = null
    private var mUpperRightPoint:PointF? = null
    private var mLowerLeftPoint:PointF? = null
    private var mLowerRightPoint:PointF? = null
    //Last position
    private var mLastTouchedPoint:PointF? = null

    private var m_isTouched = false
    private var m_nCirCleWidth = 15

    //Set last action
    private var m_lastAction = ACTION_MOVE

    companion object {
        const val  ACTION_TOUCH = 1001
        const val ACTION_MOVE = 1002
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    {
        init(attributeSet, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle:Int) : super(context, attributeSet, defStyle)
    {
        init(attributeSet, defStyle)
    }

    fun init(attrs:AttributeSet?, defStyle:Int) {
        mBackgroundPaint.apply {
            color = ContextCompat.getColor(context, R.color.backgroundOverlay)
        }

        mBorderPaint.apply {
            color = ContextCompat.getColor(context, R.color.Red)
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        mUpperLeftCirclePaint.apply {
            color = ContextCompat.getColor(context, R.color.docDetectCircle)
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 4f
        }
        mUpperRightCirclePaint.apply {
            color = ContextCompat.getColor(context, R.color.docDetectCircle)
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 4f
        }
        mLowerLeftCirclePaint.apply {
            color = ContextCompat.getColor(context, R.color.docDetectCircle)
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 4f
        }
        mLowerRightCirclePaint.apply {
            color = ContextCompat.getColor(context, R.color.docDetectCircle)
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 4f
        }

        mCircleBorderPaint.apply {
            color = ContextCompat.getColor(context, R.color.White)
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
    }

    fun setCurPoints(points:List<PointF>)
    {
        this.m_points = points
    }

    fun setOrienTation(nOrientation:Int)
    {
        this.m_orientation = nOrientation
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (mUpperLeftPoint == null || mUpperRightPoint == null || mLowerRightPoint == null || mLowerLeftPoint == null) {
            setDefaultSelection()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        var pts = FloatArray(9)
        imageMatrix.getValues(pts)

        if(m_points != null) setPoints(m_points)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var mMargin = getImageMargin()

        val nLeftMargin = if(mMargin["Left"] == null) 0 else mMargin["Left"]!!
        val nTopMargin = if(mMargin["Top"] == null) 0 else mMargin["Top"]!!

        mSelectionPath.apply {
            reset()
            fillType = Path.FillType.EVEN_ODD
            moveTo(mUpperLeftPoint!!.x+paddingLeft, mUpperLeftPoint!!.y+paddingTop)
            lineTo(mUpperRightPoint!!.x+paddingLeft, mUpperRightPoint!!.y+paddingTop)
            lineTo(mLowerRightPoint!!.x+paddingLeft, mLowerRightPoint!!.y+paddingTop)
            lineTo(mLowerLeftPoint!!.x+paddingLeft, mLowerLeftPoint!!.y+paddingTop)
            close()
        }

        mBackgroundPath.apply {
            reset()
            fillType = Path.FillType.EVEN_ODD

            var conf = resources.configuration

            when(conf.orientation)
            {
                Configuration.ORIENTATION_PORTRAIT -> {
                    addRect(paddingLeft.toFloat(), nTopMargin.toFloat(), (width - paddingRight).toFloat(), (height - nTopMargin).toFloat(), Path.Direction.CW)
                }
                else -> {
                    addRect(nLeftMargin.toFloat(), paddingTop.toFloat(), (width - nLeftMargin).toFloat(), (height - nTopMargin).toFloat(), Path.Direction.CW)
                }
            }

            addPath(mSelectionPath)
        }

        canvas?.apply {
            drawPath(mBackgroundPath, mBackgroundPaint)
            drawPath(mSelectionPath, mBorderPaint)

            drawCircle(mUpperLeftPoint!!.x+paddingLeft, mUpperLeftPoint!!.y+paddingTop, (m_nCirCleWidth-3).px.toFloat(), mUpperLeftCirclePaint)
            drawCircle(mUpperRightPoint!!.x+paddingLeft, mUpperRightPoint!!.y+paddingTop, (m_nCirCleWidth-3).px.toFloat(), mUpperRightCirclePaint)
            drawCircle(mLowerRightPoint!!.x+paddingLeft, mLowerRightPoint!!.y+paddingTop, (m_nCirCleWidth-3).px.toFloat(), mLowerRightCirclePaint)
            drawCircle(mLowerLeftPoint!!.x+paddingLeft, mLowerLeftPoint!!.y+paddingTop, (m_nCirCleWidth-3).px.toFloat(), mLowerLeftCirclePaint)

            drawCircle(mUpperLeftPoint!!.x+paddingLeft, mUpperLeftPoint!!.y+paddingTop, m_nCirCleWidth.px.toFloat(), mCircleBorderPaint)
            drawCircle(mUpperRightPoint!!.x+paddingLeft, mUpperRightPoint!!.y+paddingTop, m_nCirCleWidth.px.toFloat(), mCircleBorderPaint)
            drawCircle(mLowerRightPoint!!.x+paddingLeft, mLowerRightPoint!!.y+paddingTop, m_nCirCleWidth.px.toFloat(), mCircleBorderPaint)
            drawCircle(mLowerLeftPoint!!.x+paddingLeft, mLowerLeftPoint!!.y+paddingTop, m_nCirCleWidth.px.toFloat(), mCircleBorderPaint)

        }
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        super.onTouchEvent(e)

        when(e?.actionMasked)
        {
            MotionEvent.ACTION_MOVE -> {
                m_lastAction = ACTION_MOVE

                var isConvex = false
                var eventPoint = PointF(e.x, e.y)

                // Determine if the shape will still be convex when we apply the users next drag
                when (mLastTouchedPoint) {
                    mUpperLeftPoint -> isConvex = isConvexQuadrilateral(eventPoint, mUpperRightPoint!!, mLowerRightPoint!!, mLowerLeftPoint!!)
                    mUpperRightPoint -> isConvex = isConvexQuadrilateral(mUpperLeftPoint!!, eventPoint, mLowerRightPoint!!, mLowerLeftPoint!!)
                    mLowerRightPoint -> isConvex = isConvexQuadrilateral(mUpperLeftPoint!!, mUpperRightPoint!!, eventPoint, mLowerLeftPoint!!)
                    mLowerLeftPoint -> isConvex = isConvexQuadrilateral(mUpperLeftPoint!!, mUpperRightPoint!!, mLowerRightPoint!!, eventPoint)
                }

                mLastTouchedPoint?.let {
                    if (isConvex) it.set(e.x, e.y)
                }
            }

            MotionEvent.ACTION_UP ->{

                setDefaultCircleColor()
//                if(m_lastAction == ACTION_MOVE) {
//
//
//                }
            }

            MotionEvent.ACTION_DOWN -> {
                m_lastAction = ACTION_TOUCH

                var p = m_nCirCleWidth.px * 2
                var pCur:PointF? = null

                if(e.x < mUpperLeftPoint!!.x + p && e.x > mUpperLeftPoint!!.x - p
                        && e.y < mUpperLeftPoint!!.y + p
                        && e.y > mUpperLeftPoint!!.y - p)
                {

                    mLastTouchedPoint = mUpperLeftPoint
                    mUpperLeftCirclePaint.color = ContextCompat.getColor(context, R.color.colorAccent)
                }
                else if(e.x < mUpperRightPoint!!.x + p
                        && e.x > mUpperRightPoint!!.x - p
                        && e.y < mUpperRightPoint!!.y + p
                        && e.y > mUpperRightPoint!!.y - p) {
                    mLastTouchedPoint = mUpperRightPoint
                    mUpperRightCirclePaint.color = ContextCompat.getColor(context, R.color.colorAccent)
                }
                else if (e.x < mLowerRightPoint!!.x + p
                        && e.x > mLowerRightPoint!!.x - p
                        && e.y < mLowerRightPoint!!.y + p
                        && e.y > mLowerRightPoint!!.y - p) {
                    mLastTouchedPoint = mLowerRightPoint
                    mLowerRightCirclePaint.color = ContextCompat.getColor(context, R.color.colorAccent)
                }
                else if (e.x < mLowerLeftPoint!!.x + p
                        && e.x > mLowerLeftPoint!!.x - p
                        && e.y < mLowerLeftPoint!!.y + p
                        && e.y > mLowerLeftPoint!!.y - p) {
                    mLastTouchedPoint = mLowerLeftPoint
                    mLowerLeftCirclePaint.color = ContextCompat.getColor(context, R.color.colorAccent)
                }
                else
                {
                    mLastTouchedPoint = null
                }
            }
        }

        invalidate()
        return true
    }

    fun setDefaultCircleColor()
    {
        mUpperLeftCirclePaint.color = ContextCompat.getColor(context, R.color.docDetectCircle)
        mUpperRightCirclePaint.color = ContextCompat.getColor(context, R.color.docDetectCircle)
        mLowerRightCirclePaint.color = ContextCompat.getColor(context, R.color.docDetectCircle)
        mLowerLeftCirclePaint.color = ContextCompat.getColor(context, R.color.docDetectCircle)
    }

    /**
     * Set the points in order to control where the selection will be drawn.  The points should
     * be represented in regards to the image, not the ja.view.  This method will translate from image
     * coordinates to ja.view coordinates.
     *
     * NOTE: Calling this method will invalidate the ja.view
     *
     * @param points A list of points. Passing null will set the selector to the default selection.
     */
    fun setPoints(points: List<PointF>?) {
        if (points != null) {
            mUpperLeftPoint = imagePointToViewPoint(points[0])
            mUpperRightPoint = imagePointToViewPoint(points[1])
            mLowerRightPoint = imagePointToViewPoint(points[2])
            mLowerLeftPoint = imagePointToViewPoint(points[3])

            if(
                    (mLowerLeftPoint!!.y - mUpperLeftPoint!!.y) < DETECT_RECTANGLE_LIMIT
                    || (mLowerRightPoint!!.y - mUpperRightPoint!!.y) < DETECT_RECTANGLE_LIMIT
                    || (mUpperRightPoint!!.x - mUpperLeftPoint!!.x) < DETECT_RECTANGLE_LIMIT
                    || (mLowerRightPoint!!.x - mLowerLeftPoint!!.x) < DETECT_RECTANGLE_LIMIT
            )
            {
                setDefaultSelection()
            }
        } else {
            setDefaultSelection()
        }

        invalidate()
    }

    /**
     * Translate the given point from image coordinates to ja.view coordinates
     *
     * @param imgPoint The point to translate
     * @return The translated point
     */
    private fun imagePointToViewPoint(imgPoint: PointF): PointF? {
        return mapPointToMatrix(imgPoint, imageMatrix)
    }

    /**
     * Helper to map a given PointF to a given Matrix
     *
     * NOTE: http://stackoverflow.com/questions/19958256/custom-imageview-imagematrix-mappoints-and-invert-inaccurate
     *
     * @param point The point to map
     * @param matrix The matrix
     * @return The mapped point
     */
    private fun mapPointToMatrix(point: PointF, matrix: Matrix): PointF? {
        val points = floatArrayOf(point.x, point.y)
        matrix.mapPoints(points)
        return if (points.size > 1) {
            PointF(points[0], points[1])
        } else {
            null
        }
    }

    /**
     * Sets the points into a default state (A rectangle following the image ja.view frame with
     * padding)
     */
    fun setDefaultSelection() {
        var mMargin = getImageMargin()

        val nLeftMargin = if(mMargin["Left"] == null) 0 else mMargin["Left"]!!
        val nTopMargin = if(mMargin["Top"] == null) 0 else mMargin["Top"]!!

        mUpperLeftPoint = PointF(0f, (nTopMargin - paddingTop).toFloat())
        mUpperRightPoint = PointF((width - paddingLeft - paddingRight).toFloat(), (nTopMargin - paddingTop).toFloat())
        mLowerRightPoint = PointF((width - paddingLeft - paddingRight).toFloat(), (height - nTopMargin - paddingTop).toFloat())
        mLowerLeftPoint = PointF(0f, (height - nTopMargin - paddingBottom).toFloat())
    }

    //returns offset
    fun getImageMargin():Map<String, Int>
    {
        var mRes: HashMap<String, Int> = HashMap()
        var fValues = FloatArray(9)

        var matrix = imageMatrix
        matrix.getValues(fValues)

        var nPaddingTop = paddingTop
        var nPaddingLeft = paddingLeft

        mRes["Left"] = fValues[2].toInt() + nPaddingLeft
        mRes["Top"] = fValues[5].toInt() + nPaddingTop

        return mRes
    }

    /**
     * Determine if the given points are a convex quadrilateral.  This is used to prevent the
     * selection from being dragged into an invalid state.
     *
     * @param ul The upper left point
     * @param ur The upper right point
     * @param lr The lower right point
     * @param ll The lower left point
     * @return True is the quadrilateral is convex
     */
    fun isConvexQuadrilateral(ul:PointF, ur:PointF, lr:PointF, ll:PointF):Boolean {
        // http://stackoverflow.com/questions/9513107/find-if-4-points-form-a-quadrilateral

        var p = ll
        var q = lr
        var r = subtractPoints(ur, ll)
        var s = subtractPoints(ul, lr)

        var s_r_crossProduct = crossProduct(r, s)
        var t = crossProduct(subtractPoints(q, p), s) / s_r_crossProduct
        var u = crossProduct(subtractPoints(q, p), r) / s_r_crossProduct

        if (t < 0 || t > 1.0 || u < 0 || u > 1.0) {
            return false
        } else {
            return isOverflow(ul, ur, lr, ll)
            //return true
        }
    }

    /**
     * Translate the given point from ja.view coordinates to image coordinates
     *
     * @param point The point to translate
     * @return The translated point
     */
    fun viewPointToImagePoint(point:PointF?): PointF? {
        var matrix = Matrix()
        imageMatrix.invert(matrix)
        return mapPointToMatrix(point!!, matrix)
    }

    /**
     * Gets the coordinates representing a rectangles corners.
     *
     * The order of the points is
     * 0------->1
     * ^        |
     * |        v
     * 3<-------2
     *
     * @param rect The rectangle
     * @return An array of 8 floats
     */
    fun getCornersFromRect(rect:RectF):FloatArray {

        return floatArrayOf(
                rect.left, rect.top,
                rect.right, rect.top,
                rect.right, rect.bottom,
                rect.left, rect.bottom
        )
    }

    /**
     * Returns a list of points representing the quadrilateral.  The points are converted to represent
     * the location on the image itself, not the ja.view.
     *
     * @return A list of points translated to map to the image
     */
    fun getPoints():List<PointF> {
        var list = ArrayList<PointF>()
        list.add(viewPointToImagePoint(mUpperLeftPoint)!!)
        list.add(viewPointToImagePoint(mUpperRightPoint)!!)
        list.add(viewPointToImagePoint(mLowerRightPoint)!!)
        list.add(viewPointToImagePoint(mLowerLeftPoint)!!)
        return list;
    }

    fun subtractPoints(p1:PointF, p2:PointF):PointF {
        return PointF(p1.x - p2.x, p1.y - p2.y)
    }

    fun crossProduct(v1:PointF, v2:PointF):Float {
        return v1.x * v2.y - v1.y * v2.x
    }

    fun isOverflow(ul:PointF, ur:PointF, lr:PointF, ll:PointF):Boolean
    {
        var mMargin = getImageMargin()

        val nLeftMargin = if(mMargin["Left"] == null) 0 else mMargin["Left"]!!
        val nTopMargin = if(mMargin["Top"] == null) 0 else mMargin["Top"]!!

        var nMargin = 10
        if(ul.x < nLeftMargin - paddingLeft - nMargin
                || ul.x > (width - nLeftMargin - paddingRight + nMargin)
                || ul.y < nTopMargin - paddingTop - nMargin
                || ul.y > (height - nTopMargin - paddingBottom + nMargin))
        {
            return false
        }

        else if(ur.x < nLeftMargin - paddingLeft - nMargin
                || ur.x > (width - nLeftMargin - paddingRight + nMargin)
                || ur.y < nTopMargin - paddingTop  - nMargin
                || ur.y > (height - nTopMargin - paddingBottom  + nMargin))
        {
            return false
        }

        else if(lr.x < nLeftMargin - paddingLeft - nMargin
                || lr.x > (width - nLeftMargin - paddingRight + nMargin)
                || lr.y < nTopMargin-paddingTop  - nMargin
                || lr.y > (height - nTopMargin - paddingBottom  + nMargin))
        {
            return false
        }

        else if(ll.x < nLeftMargin - paddingLeft - nMargin
                || ll.x > (width - nLeftMargin - paddingRight + nMargin)
                || ll.y < nTopMargin-paddingTop  - nMargin
                || ll.y > (height - nTopMargin - paddingBottom  + nMargin))
        {
            return false
        }

        return true
    }
}