package com.officeslip.View.EditSlip

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.MotionEvent
import com.officeslip.DETECT_RECTANGLE_LIMIT
import com.sgenc.officeslip.R
import com.officeslip.Util.Common
import com.officeslip.Util.RectFinder
import com.officeslip.Subclass.TouchImageView
import com.officeslip.dp
import com.officeslip.px
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.util.*
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable




class Crop(var imageView: TouchImageView) {

    private var mBackgroundPaint: Paint = Paint()
    private var mBorderPaint: Paint = Paint()
    private var mCircleBorderPaint: Paint = Paint()
    private var mSelectionPath: Path = Path()
    private var mBackgroundPath: Path = Path()

    //Paints for each circle
    private var mUpperLeftCirclePaint: Paint = Paint()
    private var mUpperRightCirclePaint: Paint = Paint()
    private var mLowerRightCirclePaint: Paint = Paint()
    private var mLowerLeftCirclePaint: Paint = Paint()

    private var m_points: ArrayList<PointF>? = null
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
    private var isClosed = false

    //Set last action
    private var m_lastAction = ACTION_MOVE

    companion object {
        const val  ACTION_TOUCH = 1001
        const val ACTION_MOVE = 1002
    }

    private val m_C = Common()
    private var m_ratio:Double = 0.0
    private var m_detectPoints = ArrayList<PointF>()
    private var context:Context = imageView.context
    private var bitmap:Bitmap? = null

    private var nDefaultVertialSpace = 0f
    private var nDefaultHorizontalSpace = 0f

    fun showCropView() {

        //Space betwwen imageview and device layout
        var verticalSpace = (imageView.viewHeight - imageView.imageHeight) / 2
        if(verticalSpace < 0f) verticalSpace = 0f
        var horizontalSpace = (imageView.viewWidth - imageView.imageWidth) / 2
        if(horizontalSpace < 0f) horizontalSpace = 0f

        init()

        imageView.setDelegate(object : TouchImageView.ImageViewDelegate {

            var upperLeftPosX = 0f
            var upperLeftPosY = 0f
            var upperRightPosX = 0f
            var upperRightPosY = 0f
            var lowerRightPosX = 0f
            var lowerRightPosY = 0f
            var lowerLeftPosX = 0f
            var lowerLeftPosY = 0f

            var verticalSpace = 0f
            var horizontalSpace = 0f

            override fun onSizeChanged(w: Int, h: Int, oldw: Int, ildh: Int) {
                if (mUpperLeftPoint == null || mUpperRightPoint == null || mLowerRightPoint == null || mLowerLeftPoint == null) {
                    setDefaultSelection()
                }

            }

            override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

                var pts = FloatArray(9)
                imageView.imageMatrix.getValues(pts)

                if(m_points != null) setPoints(m_points)
            }


            override fun onDraw(canvas: Canvas?) {

                if(isClosed) {
                    //canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    return
                }
                var zoomedWRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth//.toFloat()
                var zoomedHRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight//.toFloat()

                //Space betwwen imageview and device layout
                verticalSpace = (imageView.viewHeight - imageView.imageHeight) / 2f
                if(verticalSpace < 0f) verticalSpace = 0f
                horizontalSpace = (imageView.viewWidth - imageView.imageWidth) / 2f
                if(horizontalSpace < 0f) horizontalSpace = 0f

                var mMargin = getImageMargin()
                val nLeftMargin = if(mMargin["Left"] == null) 0 else mMargin["Left"]!!
                val nTopMargin = if(mMargin["Top"] == null) 0 else mMargin["Top"]!!

                /**
                 *  Calculate Detected relative position
                 */
                if (mUpperLeftPoint == null || mUpperRightPoint == null || mLowerRightPoint == null || mLowerLeftPoint == null) {
                    setDefaultSelection()
                }

                upperLeftPosX = (mUpperLeftPoint!!.x * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio) //mUpperLeftPoint!!.x//* zoomedWRatio//* imageView.currentZoom   + imageView.currentRect.left
                upperLeftPosY = (mUpperLeftPoint!!.y * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio) //* zoomedHRatio //+ imageView.currentRect.top // * imageView.currentZoom
                upperRightPosX = (mUpperRightPoint!!.x * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)  //* imageView.currentZoom
                upperRightPosY = (mUpperRightPoint!!.y * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio) // * imageView.currentZoom
                lowerRightPosX = (mLowerRightPoint!!.x * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)  //* imageView.currentZoom
                lowerRightPosY = (mLowerRightPoint!!.y * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio) // * imageView.currentZoom
                lowerLeftPosX = (mLowerLeftPoint!!.x * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)  //* imageView.currentZoom
                lowerLeftPosY = (mLowerLeftPoint!!.y * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio) // * imageView.currentZoom

                mSelectionPath.apply {
                    reset()
                    fillType = Path.FillType.EVEN_ODD
                    moveTo(upperLeftPosX, upperLeftPosY)
                    lineTo(upperRightPosX, upperRightPosY)
                    lineTo(lowerRightPosX, lowerRightPosY)
                    lineTo(lowerLeftPosX, lowerLeftPosY)
                    close()
                }

                //Draw black alphaed bg
                mBackgroundPath.apply {
                    reset()
                    fillType = Path.FillType.EVEN_ODD

                    var conf = imageView.resources.configuration

                    when(conf.orientation)
                    {
                        Configuration.ORIENTATION_PORTRAIT -> {
                            addRect(imageView.paddingLeft.toFloat(), nTopMargin.toFloat(), (imageView.width - imageView.paddingRight).toFloat(), (imageView.height - nTopMargin).toFloat(), Path.Direction.CW)
                        }
                        else -> {
                            addRect(nLeftMargin.toFloat(), imageView.paddingTop.toFloat(), (imageView.width - nLeftMargin).toFloat(), (imageView.height - nTopMargin).toFloat(), Path.Direction.CW)
                        }
                    }

                    addPath(mSelectionPath)
                }

                canvas?.apply {
                    drawPath(mBackgroundPath, mBackgroundPaint)
                    drawPath(mSelectionPath, mBorderPaint)

                    drawCircle(upperLeftPosX, upperLeftPosY, (m_nCirCleWidth-3).px.toFloat(), mUpperLeftCirclePaint)
                    drawCircle(upperRightPosX, upperRightPosY, (m_nCirCleWidth-3).px.toFloat(), mUpperRightCirclePaint)
                    drawCircle(lowerRightPosX, lowerRightPosY, (m_nCirCleWidth-3).px.toFloat(), mLowerRightCirclePaint)
                    drawCircle(lowerLeftPosX, lowerLeftPosY, (m_nCirCleWidth-3).px.toFloat(), mLowerLeftCirclePaint)

                    drawCircle(upperLeftPosX, upperLeftPosY, m_nCirCleWidth.px.toFloat(), mCircleBorderPaint)
                    drawCircle(upperRightPosX, upperRightPosY, m_nCirCleWidth.px.toFloat(), mCircleBorderPaint)
                    drawCircle(lowerRightPosX, lowerRightPosY, m_nCirCleWidth.px.toFloat(), mCircleBorderPaint)
                    drawCircle(lowerLeftPosX, lowerLeftPosY, m_nCirCleWidth.px.toFloat(), mCircleBorderPaint)

                }
            }

            override fun onTouchEvent(e: MotionEvent?) {




                when(e?.actionMasked)
                {
                    MotionEvent.ACTION_MOVE -> {
                        m_lastAction = ACTION_MOVE

                        var isConvex = true

                        var wRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth//.toFloat()
                        var hRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight//.toFloat()


                        var zoomedWRatio = imageView.drawable.intrinsicWidth.toFloat() / imageView.imageWidth
                        var zoomedHRatio = imageView.drawable.intrinsicHeight.toFloat() / imageView.imageHeight

                        var eX = (e.x   - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
                        var eY = (e.y   - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio


                        mLastTouchedPoint?.let {
                            if (isConvex) it.set(eX, eY)
                        }
                    }

                    MotionEvent.ACTION_UP ->{

                        setDefaultCircleColor()
                        imageView.setTouchEnable(true)
                    }

                    MotionEvent.ACTION_DOWN -> {
                        m_lastAction = ACTION_TOUCH

                        var p = m_nCirCleWidth.px * 2
                        var pCur:PointF? = null

                        if(e.x < upperLeftPosX + p && e.x > upperLeftPosX - p
                                && e.y < upperLeftPosY + p
                                && e.y > upperLeftPosY - p)
                        {
                            imageView.setTouchEnable(false)

                            mLastTouchedPoint = mUpperLeftPoint
                            mUpperLeftCirclePaint.color = ContextCompat.getColor(context, R.color.colorAccent)
                        }
                        else if(e.x < upperRightPosX + p
                                && e.x > upperRightPosX - p
                                && e.y < upperRightPosY + p
                                && e.y > upperRightPosY - p) {
                            imageView.setTouchEnable(false)

//                            mUpperRightPoint!!.x = upperRightPosX
//                            mUpperRightPoint!!.y = upperRightPosY

                            mLastTouchedPoint = mUpperRightPoint
                            mUpperRightCirclePaint.color = ContextCompat.getColor(context, R.color.colorAccent)
                        }
                        else if (e.x < lowerRightPosX + p
                                && e.x > lowerRightPosX - p
                                && e.y < lowerRightPosY + p
                                && e.y > lowerRightPosY - p) {
                            imageView.setTouchEnable(false)

//                            mLowerRightPoint!!.x = lowerRightPosX
//                            mLowerRightPoint!!.y = lowerRightPosY

                            mLastTouchedPoint = mLowerRightPoint
                            mLowerRightCirclePaint.color = ContextCompat.getColor(context, R.color.colorAccent)
                        }
                        else if (e.x < lowerLeftPosX + p
                                && e.x > lowerLeftPosX - p
                                && e.y < lowerLeftPosY + p
                                && e.y > lowerLeftPosY - p) {
                            imageView.setTouchEnable(false)

//                            mLowerLeftPoint!!.x = lowerLeftPosX
//                            mLowerLeftPoint!!.y = lowerLeftPosY

                            mLastTouchedPoint = mLowerLeftPoint
                            mLowerLeftCirclePaint.color = ContextCompat.getColor(context, R.color.colorAccent)
                        }
                        else
                        {
                            mLastTouchedPoint = null
                        }
                    }
                }

                imageView.invalidate()

            }
        })

        Thread().run {
            imageView.post{
                Runnable {
                    m_ratio = getViewRatio()
                    if(m_ratio > 0)
                    {
                        //If already detected
                        if(m_detectPoints.size > 0)
                        {
                            setPoints(m_detectPoints)
                        }
                        else
                        {
                            var srcMat = bitmapToMat(bitmap!!)
                            var rectFinder = RectFinder(0.2, 0.98)
                            var rectangle = rectFinder.findRectangle(srcMat)

                            if(rectangle != null)
                            {
                                m_detectPoints = ArrayList()
                                sortPoints(rectangle.toList())?.forEach {
                                    if (it != null) {
                                        m_detectPoints.add(PointF(it.x.toFloat(), it.y.toFloat()))
                                    }
                                }

                                //If point is not square
                                if(m_detectPoints.size != 4)
                                {
                                    AlertDialog.Builder(context).apply {
                                        setMessage(context.getString(R.string.failed_load_image))

                                        setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                            close()
                                        })

                                    }.show()
                                }
                                srcMat.release()
                                rectangle.release()
                                setPoints(m_detectPoints)
                            }
                            else
                            {
                                SelectEntireArea()
                            }
                        }
                    }
                    else
                    {
                        AlertDialog.Builder(context).apply {
                            setMessage(context.getString(R.string.failed_load_image))

                            setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                               close()
                            })

                        }.show()
                    }
                }.run()
            }
        }
    }



    fun close() {
        isClosed = true
        imageView.invalidate()
    }

    fun init() {
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

        bitmap = (imageView.drawable as BitmapDrawable).bitmap

        //setDefaultSelection()
    }
    /**
     * transform ja.view image to pointed.
     */
    fun transformImage() {

        var matOriginal = Mat()
        org.opencv.android.Utils.bitmapToMat(bitmap!!, matOriginal)

        var listPoints = listOf<PointF>(mUpperLeftPoint!!, mUpperRightPoint!!, mLowerRightPoint!!, mLowerLeftPoint!!)

        var matTransFormed = perspectiveTransform(matOriginal, listPoints)
        bitmap = Bitmap.createBitmap(matTransFormed.size().width.toInt(), matTransFormed.size().height.toInt(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matTransFormed, bitmap)

        imageView.setImageBitmap(bitmap)
        matOriginal.release()
        matTransFormed.release()
        imageView.invalidate()
    }

    //returns offset
    fun getImageMargin():Map<String, Int>
    {
        var mRes: HashMap<String, Int> = HashMap()
        var fValues = FloatArray(9)

        var matrix = imageView.imageMatrix
        matrix.getValues(fValues)

        var nPaddingTop = imageView.paddingTop
        var nPaddingLeft = imageView.paddingLeft

        mRes["Left"] = fValues[2].toInt() + nPaddingLeft
        mRes["Top"] = fValues[5].toInt() + nPaddingTop

        return mRes
    }

    private fun getViewRatio():Double {
        var nViewWidth = imageView.width.dp//weight of imageView
        var nViewHeight = imageView.height.dp//width of imageView


        val nRealWidth = imageView.drawable.intrinsicWidth//original width of underlying image
        val nRealHeight = imageView.drawable.intrinsicHeight//original width of underlying image

        val dWRatio = nViewWidth.toDouble() / nRealWidth
        val dHRatio = nViewHeight.toDouble() / nRealHeight

        var resRatio = if (dWRatio < dHRatio) dWRatio else dHRatio
        //double dRatio = (double)() < (double)(nViewHeight / nRealHeight) ? (double)(nViewWidth / nRealWidth) : (double)(nViewHeight / nRealHeight);

        if (resRatio >= 1.0) {
            resRatio = 1.0
        }
        return resRatio
    }

    fun bitmapToMat(bitmap: Bitmap): Mat {

        val nScaledWidth = bitmap.width
        val nScaledHeight = bitmap.height
        val mat = Mat(nScaledHeight.toInt(), nScaledWidth.toInt(), CvType.CV_8UC4, Scalar(4.0))

        var bitmap32 = bitmap.copy(bitmap.config, true)
        bitmap32 = Bitmap.createScaledBitmap(bitmap32, nScaledWidth.toInt(), nScaledHeight.toInt(), false)

        Utils.bitmapToMat(bitmap32, mat)

        val mat2 = Mat()
        Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_BGR2RGB)
        mat.release()
        /* File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	        Imgcodecs.imwrite(path+File.separator+"des1.jpg", mat);

	        Imgcodecs.imwrite(path+File.separator+"des2.jpg", mat2);*/

        return mat2
    }

    /**
     * Sort the points
     *
     * The order of the points after sorting:
     * 0------->1
     * ^        |
     * |        v
     * 3<-------2
     *
     * NOTE:
     * Based off of http://www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/
     *
     * @param src The points to sort
     * @return An array of sorted points
     */
    private fun sortPoints(src:List<Point>):Array<Point?> {
        var srcPoints = src
        var result = arrayOfNulls<Point>(4)


        var sumComp = kotlin.Comparator { lhs:Point,rhs:Point ->

            (lhs.y + lhs.x).compareTo(rhs.y + rhs.x)
        }

        var diffComp = kotlin.Comparator { lhs:Point, rhs:Point ->
            (lhs.y - lhs.x).compareTo(rhs.y - rhs.x)
        }

        result[0] = Collections.min(srcPoints, sumComp)
        result[2] = Collections.max(srcPoints, sumComp)        // Lower right has the maximal sum
        result[1] = Collections.min(srcPoints, diffComp) // Upper right has the minimal difference
        result[3] = Collections.max(srcPoints, diffComp) // Lower left has the maximal difference

       // result = getScaledPoint(result)
        return result
    }

    fun getScaledPoint(p:Array<Point?>):Array<Point?>
    {
        var result = arrayOfNulls<Point>(4)

        for(i in 0 until p.size)
        {
            //    		double dX = mCommon.DPtoPixel((int)p[i].x) * (1 + (1-dRatio));
//        	double dY = mCommon.DPtoPixel((int)p[i].y) * (1 + (1-dRatio));
            /*double dX = p[i].x * (1 + (1-dRatio));
            double dY = p[i].y * (1 + (1-dRatio));*/
            var dX = p[i]!!.x /  (m_ratio * 100) * 100
            var dY = p[i]!!.y / (m_ratio * 100) * 100

            result[i] = Point()
            result[i]!!.x = dX
            result[i]!!.y = dY
        }

        return result

    }

    fun setDefaultCircleColor()
    {
        mUpperLeftCirclePaint.color = ContextCompat.getColor(context, R.color.docDetectCircle)
        mUpperRightCirclePaint.color = ContextCompat.getColor(context, R.color.docDetectCircle)
        mLowerRightCirclePaint.color = ContextCompat.getColor(context, R.color.docDetectCircle)
        mLowerLeftCirclePaint.color = ContextCompat.getColor(context, R.color.docDetectCircle)
    }

    /**
     * Sets the points into a default state (A rectangle following the image ja.view frame with
     * padding)
     */
    fun setDefaultSelection() {
        var mMargin = getImageMargin()

        val nLeftMargin = if(mMargin["Left"] == null) 0 else mMargin["Left"]!!
        val nTopMargin = if(mMargin["Top"] == null) 0 else mMargin["Top"]!!

        imageView?.apply {
            mUpperLeftPoint = PointF(0f, 0f)
            mUpperRightPoint = PointF(drawable.intrinsicWidth.toFloat(), 0f)
            mLowerRightPoint = PointF(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
            mLowerLeftPoint = PointF(0f, drawable.intrinsicHeight.toFloat())

        }



//        imageView?.apply {
//            mUpperLeftPoint = PointF(0f, (nTopMargin - paddingTop).toFloat())
//            mUpperRightPoint = PointF((width - paddingLeft - paddingRight).toFloat(), (nTopMargin - paddingTop).toFloat())
//            mLowerRightPoint = PointF((width - paddingLeft - paddingRight).toFloat(), (weight - nTopMargin - paddingTop).toFloat())
//            mLowerLeftPoint = PointF(0f, (weight - nTopMargin - paddingBottom).toFloat())
//        }
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
        imageView?.apply {
            if (points != null) {
                mUpperLeftPoint = PointF(points[0].x, points[0].y)
                mUpperRightPoint = PointF(points[1].x, points[1].y)
                mLowerRightPoint = PointF(points[2].x, points[2].y)
                mLowerLeftPoint = PointF(points[3].x, points[3].y)
//                mUpperLeftPoint = imagePointToViewPoint(points[0])
//                mUpperRightPoint = imagePointToViewPoint(points[1])
//                mLowerRightPoint = imagePointToViewPoint(points[2])
//                mLowerLeftPoint = imagePointToViewPoint(points[3])

                nDefaultVertialSpace = (imageView.viewHeight - imageView.imageHeight) / 2
                nDefaultHorizontalSpace = (imageView.viewWidth - imageView.imageWidth) / 2
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
    }

    private fun SelectEntireArea() {
        var mMargin = getImageMargin()

        val nLeftMargin = if(mMargin["Left"] == null) 0 else mMargin["Left"]!!
        val nTopMargin = if(mMargin["Top"] == null) 0 else mMargin["Top"]!!

        val nViewWidth = imageView.width//weight of imageView
        val nViewHeight = imageView.height//width of imageView

        m_points = ArrayList<PointF>()

        m_points!!.add(viewPointToImagePoint(PointF(nLeftMargin.toFloat() - imageView.paddingLeft, nTopMargin.toFloat() - imageView.paddingTop)))
        m_points!!.add(viewPointToImagePoint(PointF(nViewWidth - nLeftMargin.toFloat() - imageView.paddingLeft, nTopMargin.toFloat() - imageView.paddingTop)))
        m_points!!.add(viewPointToImagePoint(PointF(nViewWidth - nLeftMargin.toFloat() - imageView.paddingLeft, nViewHeight - nTopMargin.toFloat() - imageView.paddingTop)))
        m_points!!.add(viewPointToImagePoint(PointF(nLeftMargin.toFloat() - imageView.paddingLeft, nViewHeight - nTopMargin.toFloat() - imageView.paddingTop)))

        setPoints(m_points)
    }

    /**
     * Translate the given point from image coordinates to ja.view coordinates
     *
     * @param imgPoint The point to translate
     * @return The translated point
     */
    private fun imagePointToViewPoint(imgPoint: PointF): PointF? {
        return mapPointToMatrix(imgPoint, imageView.imageMatrix)
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
     * Translate the given point from ja.view coordinates to image coordinates
     *
     * @param point The point to translate
     * @return The translated point
     */
    fun viewPointToImagePoint(point:PointF?): PointF {
        var matrix = Matrix()
        imageView.imageMatrix.invert(matrix)
        return mapPointToMatrix(point!!, matrix)!!
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
        if(ul.x < nLeftMargin - imageView.paddingLeft - nMargin
                || ul.x > (imageView.width - nLeftMargin - imageView.paddingRight + nMargin)
                || ul.y < nTopMargin - imageView.paddingTop - nMargin
                || ul.y > (imageView.height - nTopMargin - imageView.paddingBottom + nMargin))
        {
            return false
        }

        else if(ur.x < nLeftMargin - imageView.paddingLeft - nMargin
                || ur.x > (imageView.width - nLeftMargin - imageView.paddingRight + nMargin)
                || ur.y < nTopMargin - imageView.paddingTop  - nMargin
                || ur.y > (imageView.height - nTopMargin - imageView.paddingBottom  + nMargin))
        {
            return false
        }

        else if(lr.x < nLeftMargin - imageView.paddingLeft - nMargin
                || lr.x > (imageView.width - nLeftMargin - imageView.paddingRight + nMargin)
                || lr.y < nTopMargin-imageView.paddingTop  - nMargin
                || lr.y > (imageView.height - nTopMargin - imageView.paddingBottom  + nMargin))
        {
            return false
        }

        else if(ll.x < nLeftMargin - imageView.paddingLeft - nMargin
                || ll.x > (imageView.width - nLeftMargin - imageView.paddingRight + nMargin)
                || ll.y < nTopMargin-imageView.paddingTop  - nMargin
                || ll.y > (imageView.height - nTopMargin - imageView.paddingBottom  + nMargin))
        {
            return false
        }

        return true
    }

    /**
     * Transform the coordinates on the given Mat to correct the perspective.
     *
     * @param src A valid Mat
     * @param points A list of coordinates from the given Mat to adjust the perspective
     * @return A perspective transformed Mat
     */
    private fun perspectiveTransform(src:Mat, points:List<PointF>):Mat {
        var point1 = Point(points.get(0).x.toDouble(), points.get(0).y.toDouble())
        var point2 = Point(points.get(1).x.toDouble(), points.get(1).y.toDouble())
        var point3 = Point(points.get(2).x.toDouble(), points.get(2).y.toDouble())
        var point4 = Point(points.get(3).x.toDouble(), points.get(3).y.toDouble())
        var pts = listOf(point1, point2, point3, point4)

        return fourPointTransform(src, sortPoints(pts))
    }

    /**
     * NOTE:
     * Based off of http://www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/
     *
     * @param src
     * @param pts
     * @return
     */
    private fun fourPointTransform(src:Mat, pts:Array<Point?>):Mat {
        var ratio = 1.0

        var ul = pts[0]!!
        var ur = pts[1]!!
        var lr = pts[2]!!
        var ll = pts[3]!!

        var widthA = Math.sqrt(Math.pow(lr.x - ll.x, 2.0) + Math.pow(lr.y - ll.y, 2.0))
        var widthB = Math.sqrt(Math.pow(ur.x - ul.x, 2.0) + Math.pow(ur.y - ul.y, 2.0))
        var maxWidth = Math.max(widthA, widthB) * ratio;

        var heightA = Math.sqrt(Math.pow(ur.x - lr.x, 2.0) + Math.pow(ur.y - lr.y, 2.0))
        var heightB = Math.sqrt(Math.pow(ul.x - ll.x, 2.0) + Math.pow(ul.y - ll.y, 2.0))
        var maxHeight = Math.max(heightA, heightB) * ratio

        var resultMat = Mat(maxHeight.toInt(), maxWidth.toInt(), CvType.CV_8UC4)

        var srcMat = Mat(4, 1, CvType.CV_32FC2)
        var dstMat = Mat(4, 1, CvType.CV_32FC2)
        srcMat.put(0, 0, ul.x * ratio, ul.y * ratio, ur.x * ratio, ur.y * ratio, lr.x * ratio, lr.y * ratio, ll.x * ratio, ll.y * ratio)
        dstMat.put(0, 0, 0.0, 0.0, maxWidth, 0.0, maxWidth, maxHeight, 0.0, maxHeight)

        var M = Imgproc.getPerspectiveTransform(srcMat, dstMat)
        Imgproc.warpPerspective(src, resultMat, M, resultMat.size())

        srcMat.release()
        dstMat.release()
        M.release()

        return resultMat
    }
}