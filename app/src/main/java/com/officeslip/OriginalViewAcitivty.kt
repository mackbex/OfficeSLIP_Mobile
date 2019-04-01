package com.officeslip

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.Common
import com.officeslip.View.EditSlip.EditSlipActivity
import com.officeslip.Subclass.TouchImageView
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.activity_origianl_view.*
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class OriginalViewAcitivty : AppCompatActivity(), View.OnClickListener, ViewPager.OnPageChangeListener
{
    //private var m_nCurSlipIdx = 0
    private var m_ObjSlipList = JsonArray()
    private var m_strSDocName:String? = null
    private var m_C = Common()
    private var m_strCategory = "SEARCH"

    companion object {
        var m_nCurSlipIdx = 0
        const val RESULT_EDIT_SLIP = 7001
        const val LEFT_POINT = 1443
        const val TOP_POINT = 1444
        const val RIGHT_POINT = 1445
        const val BOTTOM_POINT = 1446

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_origianl_view)

        setToolbar()
        setupViewUI()
//
//        ThumbActivity.GetThumbList(this@ThumbActivity, SocketManager(), m_objSlipData).execute()
    }

    private  fun setToolbar() {
        //run only when current fragment is this class.
        (this as AppCompatActivity)?.run {
            setSupportActionBar(view_toolbarOriginal)
            supportActionBar?.setDisplayShowTitleEnabled(false)

        }
        view_btnClose.setOnClickListener(this)


    }

    fun setupViewUI() {
        intent.getStringExtra("SLIP_DATA")?.apply {
            var objSlipItem = JsonParser().parse(this)

            m_ObjSlipList = objSlipItem.asJsonObject.get("SLIP_LIST").asJsonArray
            m_nCurSlipIdx = objSlipItem.asJsonObject.get("CUR_IDX").asInt
            m_strSDocName = objSlipItem.asJsonObject.get("SDOC_NAME").asString
            m_strCategory = objSlipItem.asJsonObject.get("CATEGORY").asString
            setViewTitle(m_nCurSlipIdx)
        }

        if(m_ObjSlipList.size() <= 0)
        {
            AlertDialog.Builder(this).run {
                setMessage(getString(R.string.failed_load_thumb))
                setPositiveButton(getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                    finish()
                })
            }.show()
        }

        view_pagerImage?.apply {
            adapter = OriginalViewPageAdapter(this@OriginalViewAcitivty, m_ObjSlipList, m_strCategory)
            addOnPageChangeListener(this@OriginalViewAcitivty)
            swipeEnable = true
            currentItem = m_nCurSlipIdx
            offscreenPageLimit = 1
        }

        if(m_strCategory != "ADD")
        {
            view_btnRemove.visibility = View.GONE
        }

        view_btnEdit.setOnClickListener(this)
        view_btnRemove.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {

            RESULT_EDIT_SLIP -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.extras?.get("SLIP_ITEM")?.apply {
                        var objSlip = JsonParser().parse(this as String)?.asJsonObject

                        m_ObjSlipList[m_nCurSlipIdx] = objSlip?.deepCopy()
                        //(view_pagerImage.adapter as OriginalViewPageAdapter).setItem(m_ObjSlipList)

                        //Refresh viewpager for update shape data
                        view_pagerImage?.apply {
                            adapter = OriginalViewPageAdapter(this@OriginalViewAcitivty, m_ObjSlipList, m_strCategory)
                            addOnPageChangeListener(this@OriginalViewAcitivty)
                            swipeEnable = true
                            currentItem = m_nCurSlipIdx
                            offscreenPageLimit = 1
                        }

                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.tag?.toString()?.toUpperCase())
        {
            "CLOSE" -> {
                closeActivity()
            }
            "EDIT" -> {
                Intent(this@OriginalViewAcitivty, EditSlipActivity::class.java).run {

                    var obj = JsonObject()
                    obj.addProperty("CATEGORY", m_strCategory)
                    obj.addProperty("SDOC_NAME", getString(R.string.slip))
                    obj.add("SLIP_DATA", m_ObjSlipList.get(m_nCurSlipIdx).asJsonObject)
                    putExtra("SLIP_ITEM", obj.toString())

                    //Pass bitmap when Search ja.view
                    if("SEARCH" == m_strCategory)
                    {
                        var imageDrawable = view_pagerImage.findViewWithTag<TouchImageView>("OriginalImageView")?.drawable
                        if(imageDrawable != null)
                        {
                            var imageBitmap = (imageDrawable as BitmapDrawable).bitmap

                            if(imageBitmap != null)
                            {
                                var tempImageName = m_C.getDate("yyyyMMddhhmmsss")+".JPG"

                                var path = File(TEMP_PATH)
                                if(!path.exists())
                                {
                                    path.mkdirs()
                                }

                                var file = File(TEMP_PATH + File.separator + tempImageName)

                                FileOutputStream(file).use{
                                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                                    it.flush()
                                }

                                putExtra("ORIGINAL_IMAGE", TEMP_PATH + File.separator + tempImageName)
                                startActivityForResult(this, RESULT_EDIT_SLIP)
                            }
                            else
                            {
                                AlertDialog.Builder(this@OriginalViewAcitivty).run {
                                    setMessage(getString(R.string.failed_load_image))
                                    setPositiveButton(getString(R.string.btn_confirm)) { dialog, which ->
                                    }
                                }.show()
                            }
                        }
                    }
                    else
                    {
                        startActivityForResult(this, RESULT_EDIT_SLIP)
                    }
                }
            }
            "REMOVE" -> {
                AlertDialog.Builder(this).run {
                    setMessage(getString(R.string.confirm_remove_slip))
                    setPositiveButton(getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->

                        m_ObjSlipList.remove(m_nCurSlipIdx)

                        if(m_nCurSlipIdx > 0)
                        {
                            m_nCurSlipIdx -= 1
                            view_pagerImage.adapter = OriginalViewPageAdapter(this@OriginalViewAcitivty, m_ObjSlipList, m_strCategory)
                            view_pagerImage.currentItem = m_nCurSlipIdx
                            setViewTitle(m_nCurSlipIdx)
                        }
                        else
                        {
                            closeActivity()
                        }


                    })
                    setNegativeButton(getString(R.string.btn_cancel), null)
                }.show()
            }
        }
    }

    fun closeActivity()
    {
        this@OriginalViewAcitivty.run {
            intent.putExtra("LIST_DATA",m_ObjSlipList.toString())
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    fun setViewTitle(nPosition:Int)
    {
        var sbPageTitle = StringBuffer().apply {
            append(m_strSDocName)
            append(" ")
            append("(")
            append(nPosition+1)
            append("/")
            append(m_ObjSlipList.size())
            append(")")
        }

        toolbar_title.text = sbPageTitle.toString()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageSelected(position: Int) {
        setViewTitle(position)

        m_nCurSlipIdx = view_pagerImage.currentItem
        //Reset image zoom
        for (i in 0 until view_pagerImage.childCount) {
            val imageView = view_pagerImage.getChildAt(i).findViewWithTag<TouchImageView>("OriginalImageView")
            imageView?.resetZoom()
        }
    }

    override fun onBackPressed() {
        closeActivity()
    }

//    fun pointToViewPoint(point:Float, imageView:TouchImageView, zoomRatio:Float, nPos:Int ):Float
//    {
//        var space = 0f
//        var curRectPoint = 0f
//
//        when(nPos)
//        {
//            LEFT_POINT -> {
//                space = (imageView.viewWidth - imageView.imageWidth) / 2f
//                if(space < 0f) space = 0f
//                curRectPoint = imageView.currentRect.left * zoomRatio
//            }
//
//            RIGHT_POINT -> {
//                space = (imageView.viewWidth - imageView.imageWidth) / 2f
//                if(space < 0f) space = 0f
//                curRectPoint = imageView.currentRect.right * zoomRatio
//            }
//
//            TOP_POINT -> {
//                space = (imageView.viewHeight - imageView.viewHeight) / 2f
//                if(space < 0f) space = 0f
//                curRectPoint = imageView.currentRect.top * zoomRatio
//            }
//
//            BOTTOM_POINT -> {
//                space = (imageView.viewHeight - imageView.viewHeight) / 2f
//                if(space < 0f) space = 0f
//                curRectPoint = imageView.currentRect.bottom * zoomRatio
//            }
//        }
//
//
//        return  (point * zoomRatio) + space - curRectPoint
//    }

    fun pointToViewPoint(point:Float, imageView: TouchImageView, nPos:Int ):Float
    {
        var space = 0f
        var curRectPoint = 0f
        var zoomedRatio = 0f

        //(shape.leftPoint * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
        when(nPos)
        {
            LEFT_POINT -> {
                zoomedRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth
                space = (imageView.viewWidth - imageView.imageWidth) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.left
            }

            RIGHT_POINT -> {
                zoomedRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth
                space = (imageView.viewWidth - imageView.imageWidth) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.left
            }

            TOP_POINT -> {
                zoomedRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight
                space = (imageView.viewHeight - imageView.imageHeight) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.top
            }

            BOTTOM_POINT -> {
                zoomedRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight
                space = (imageView.viewHeight - imageView.imageHeight) / 2f
                if(space < 0f) space = 0f
                curRectPoint = imageView.currentRect.top
            }
        }


        return  (point * zoomedRatio) + space - (curRectPoint * zoomedRatio)
    }


    /**
     * Delegate rect drawing
     */
    private class delegateImageView(var activity: OriginalViewAcitivty, var imageView: TouchImageView, objImageInfo: JsonObject) : TouchImageView.ImageViewDelegate {

        var classType = object : TypeToken<HashMap<String,MutableList<com.officeslip.Structure.Shape>>>(){}.type
        var m_mapShape =  Gson().fromJson<HashMap<String,MutableList<com.officeslip.Structure.Shape>>>(objImageInfo.get("SHAPE_DATA"), classType)
        var m_paint = Paint()
        var m_textPaint = TextPaint()
        var m_rect = RectF()


        override fun onDraw(canvas: Canvas?) {
            if(m_mapShape == null) return
            var horizontalSpace = (imageView.viewWidth - imageView.imageWidth) / 2f
            var verticalSpace = (imageView.viewHeight - imageView.imageHeight) / 2f


            var zoomedWRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth//.toFloat()
            var zoomedHRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight//.toFloat()

//            var zoomedWRatio = imageView.drawable.intrinsicWidth.toFloat() / imageView.imageWidth
//            var zoomedHRatio = imageView.drawable.intrinsicHeight.toFloat() / imageView.imageHeight

         //   shape.leftPoint = (x - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio

            for ((key, value) in m_mapShape) {
                var strType = key
                for (shape in value) {
                    canvas?.apply {

//                        var leftPoint = (shape.leftPoint -  horizontalspace + (imageView.currentRect.left * wRatio)) * zoomedWRatio //activity.pointToViewPoint(shape.leftPoint, imageView, zoomedWRatio, LEFT_POINT)
//                        var topPoint = (shape.topPoint -  verticalspace + (imageView.currentRect.top * hRatio)) * zoomedHRatio//activity.pointToViewPoint(shape.topPoint, imageView, zoomedHRatio, TOP_POINT)
//                        var rightPoint = (shape.rightPoint -  horizontalspace + (imageView.currentRect.right * wRatio)) * zoomedWRatio//activity.pointToViewPoint(shape.rightPoint, imageView, zoomedWRatio, RIGHT_POINT)
//                        var bottomPoint = (shape.bottomPoint -  verticalspace + (imageView.currentRect.bottom * hRatio)) * zoomedHRatio//activity.pointToViewPoint(shape.bottomPoint, imageView, zoomedHRatio, BOTTOM_POINT)

                        var leftPoint = activity.pointToViewPoint(shape.leftPoint, imageView, LEFT_POINT)
                        var topPoint = activity.pointToViewPoint(shape.topPoint, imageView, TOP_POINT)
                        var rightPoint = activity.pointToViewPoint(shape.rightPoint, imageView, RIGHT_POINT)
                        var bottomPoint = activity.pointToViewPoint(shape.bottomPoint, imageView, BOTTOM_POINT)
//
//                         leftPoint = (shape.leftPoint * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
//                         topPoint = (shape.topPoint * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio)
//                         rightPoint = (shape.rightPoint * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
//                         bottomPoint = (shape.bottomPoint * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedWRatio)

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
                                    strokeWidth = shape.weight.toInt().px.toFloat()
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
                                     strokeWidth = shape.weight.toInt().px.toFloat()
                                    style = Paint.Style.STROKE
                                }
                                drawRect(leftPoint,topPoint, rightPoint, bottomPoint, m_paint)

                                var fTextSize = (shape.fontSize.px.toFloat() * imageView.currentZoom) * 2
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
                                     strokeWidth = shape.weight.toInt().px.toFloat()
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
    }

    //Download or load Original image to viewpager
    private class OriginalViewPageAdapter(var activity: OriginalViewAcitivty, var arOriginalData:JsonArray, var strCategory:String) : PagerAdapter() {

        companion object {
            const val ID_LAYOUT_IMAGE = 10014

        }
        override fun getCount(): Int {
            return arOriginalData.size()
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            //Image position to show
            var layout = RelativeLayout(container.context)?.apply{
                tag = "OriginalLayout_$position"
            }
            layout.setBackgroundColor(Color.parseColor("#222222"))

            // get Original image
            container.addView(layout)

            setImage(layout, arOriginalData.get(position).asJsonObject, position)

            return layout
        }


        fun setItem(arImageList: JsonArray)
        {
            this.arOriginalData =  arImageList
        }

        //Download or load Original image to viewpager
        fun setImage(layout:RelativeLayout, objImageInfo:JsonObject, nImagePosition: Int)
        {
            //Load original image
            when(strCategory)
            {
                "ADD" -> {
                    var imageView = TouchImageView(layout.context)?.apply {
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        tag = "OriginalImageView"
                    }
                    var original = File(UPLOAD_PATH, "${objImageInfo.get("FILE_NAME")?.asString}")
                    if(original.exists())
                    {

                        imageView.setBackgroundColor(Color.parseColor("#222222"))
                        imageView.setImageBitmap(BitmapFactory.decodeFile(original.toString()))
                        imageView.setZoom(1f)
                        layout.addView(imageView)

                        imageView.setDelegate(delegateImageView(activity, imageView, arOriginalData.get(nImagePosition).asJsonObject))
                    }
                    else
                    {
                        AlertDialog.Builder(activity).run {
                            setMessage(activity.getString(R.string.failed_load_image))
                            setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                                activity.finish()
                            })
                        }.show()
                    }
                }
                else -> {
                    var original = File(DOWNLOAD_PATH, "${objImageInfo.get("FILE_NAME")?.asString}")
                    if(original.exists())
                    {
                        var imageView = TouchImageView(layout.context)?.apply {
                            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            tag = "OriginalImageView"
                        }
                        imageView.setBackgroundColor(Color.parseColor("#222222"))
                        imageView.setImageBitmap(BitmapFactory.decodeFile(original.toString()))
                        imageView.setZoom(1f)
                        layout.addView(imageView)
                    }
                    else
                    {
                        DownloadOriginal(activity, objImageInfo, nImagePosition).execute(objImageInfo)
                    }
                }
            }
        }

        private class DownloadOriginal(activity: OriginalViewAcitivty, var objImageInfo:JsonObject, var nImagePosition: Int):AsyncTask<JsonObject, Void, ByteArray?>() {
            var activityRef: WeakReference<Activity>? = null
            var m_C = Common()
            var nCurrentStatus = DOWNLOAD_ORIGINAL_FAILED
            var m_objItem:JsonObject? = null
          //  var m_bImage:ByteArray? = null
            init {
                activityRef = WeakReference(activity)
            }

            companion object {
                const val NETWORK_DISABLED = 3021
                const val DOWNLOAD_ORIGINAL_SUCCESS = 3022
                const val DOWNLOAD_ORIGINAL_FAILED = 3023
            }

            override fun onPreExecute() {
             //   super.onPreExecute()

                activityRef?.get()?.run {

                    var imageView = TouchImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        tag = "OriginalImageView"
                    }
                    imageView.setBackgroundColor(Color.parseColor("#222222"))

                    //Set image thumb before load original
                    var thumbPath = objImageInfo.get("THUMB_PATH")?.asString

                    if(File(thumbPath).exists())
                    {
                        imageView.setImageBitmap(BitmapFactory.decodeFile(thumbPath))
                    }

                    //Attach progress while loading image
                    var viewProgress = ProgressBar(this)?.apply {
                        tag = "LoadingProgress"
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        isIndeterminate = true
                        var size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
                        setPadding(size,size,size,size)
                    }
                    val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)

                    view_pagerImage.findViewWithTag<RelativeLayout>("OriginalLayout_$nImagePosition")?.let {
                        it.addView(imageView)
                        it.addView(viewProgress, params)
                    }

                }
            }

            override fun doInBackground(vararg params: JsonObject): ByteArray? {

//                var strRes:String? = null
                var bRes:ByteArray? = null
                if(!isCancelled)
                {
                    var activity = activityRef?.get()
                    var objImage:JsonObject = params[0]
                    m_objItem = objImage
                    if(m_C.isNetworkConnected(activity as Context))
                    {
                        var strImageDocIRN = objImage?.get("SLIP_DOC_IRN")?.asString
                        var strDocNo = objImage?.get("DOC_NO")?.asString
                        var strFileName = objImage?.get("FILE_NAME")?.asString

                        //consider protocol property
                        if("SOCKET".equals(CONNECTION_PROTOCOL, true))
                        {
                            var bImage:ByteArray? = null
                            bRes = SocketManager()?.Download(strImageDocIRN!!, strDocNo!!, "IMG_SLIP_X")?.apply {
                                nCurrentStatus = DOWNLOAD_ORIGINAL_SUCCESS
                            }
                        }
                    }
                }
                return bRes
            }

            override fun onPostExecute(result: ByteArray?) {
                super.onPostExecute(result)

                activityRef?.get()?.run {

                    view_pagerImage.findViewWithTag<RelativeLayout>("OriginalLayout_$nImagePosition")?.let {
                        it.findViewWithTag<ProgressBar>("LoadingProgress")?.visibility = View.GONE
                        var imageView = it.findViewWithTag<TouchImageView>("OriginalImageView")

                        it.removeView(imageView)

                        imageView = TouchImageView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            tag = "OriginalImageView"
                        }
                        imageView.setBackgroundColor(Color.parseColor("#222222"))

                        //On download success
                        if(result != null)
                        {
                           // imageView.setImageBitmap(BitmapFactory.decodeFile(result))
                            imageView.setImageBitmap(BitmapFactory.decodeByteArray(result, 0, result.size))
                        }
                        else
                        {
                            imageView.setImageResource(R.drawable.close)
                        }

                        it.addView(imageView)

                        imageView.setDelegate(delegateImageView(this as OriginalViewAcitivty, imageView, objImageInfo))
                    }
                }
            }
        }
    }
}
/*

    private class DownloadOriginal(activity: Activity, var socketManager: SocketManager, var holder:SearchThumbAdapter.ViewHolder): AsyncTask<JsonObject, Void, ByteArray?>() {


    }*/
