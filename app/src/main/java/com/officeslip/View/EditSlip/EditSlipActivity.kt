package com.officeslip.View.EditSlip

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.officeslip.Util.Common
import kotlinx.android.synthetic.main.activity_edit_slip.*
import kotlin.collections.ArrayList
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import com.officeslip.*
import com.sgenc.officeslip.*
import com.officeslip.Socket.SocketManager
import com.officeslip.Structure.Shape
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference


class EditSlipActivity:AppCompatActivity(), View.OnClickListener, DrawRect.ChangeMode, View.OnTouchListener
{
    private val m_C = Common()
    private var m_ratio:Double = 0.0
    private var m_points = ArrayList<PointF>()
    private var m_objSlip:JsonObject? = null
    private var m_strTitle:String? = null
    private var m_strCategory:String? = null
    private var bitmap:Bitmap? = null
    private var m_crop: Crop? = null
    private var m_drawRect: DrawRect? = null
    private var m_mapShape = HashMap<String,MutableList<Shape>>()

    init {
        System.loadLibrary("opencv_java3")
    }

    companion object {
        const val MODE_PINCH = 10
        const val MODE_PEN = "PEN"
        const val MODE_RECT = "RECT"
        const val MODE_MEMO = "MEMO"
        const val MODE_CIRCLE = "CIRCLE"
        const val RESULT_SETTING_PEN = 20
        const val RESULT_MODIFY_PEN = 21
        const val RESULT_SETTING_RECT = 22
        const val RESULT_MODIFY_RECT = 23
        const val RESULT_SETTING_CIRCLE = 24
        const val RESULT_MODIFY_CIRCLE = 25
        const val RESULT_SETTING_MEMO = 26
        const val RESULT_MODIFY_MEMO = 27
        const val UPDATE_SHAPEDATA_FAILED = 28
        const val UPDATE_SHAPEDATA_SUCCESS = 29
        const val NETWORK_DISABLED = 30
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_slip)

        intent.getStringExtra("SLIP_ITEM")?.apply {

            JsonParser().parse(this).asJsonObject?.let {

                m_objSlip = it.getAsJsonObject("SLIP_DATA")
                m_strTitle = it.get("SDOC_NAME").asString
                m_strCategory = it.get("CATEGORY").asString
            }

        }

        if(m_objSlip != null) {
            setupViewUI()
            setTabBtnTintForLowAPI()
        }
        else
        {
            AlertDialog.Builder(this@EditSlipActivity).apply {
                setMessage(getString(R.string.failed_load_image))

                setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    finish()
                })
            }.show()
        }
    }

    private fun setTabBtnTintForLowAPI() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {

            view_btnPinch?.apply {
                var color = ContextCompat.getColor(context, R.color.colorBtnDefault)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
            view_btnPen?.apply {
                var color = ContextCompat.getColor(context, R.color.colorBtnDefault)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
            view_btnRectangle?.apply {
                var color = ContextCompat.getColor(context, R.color.colorBtnDefault)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
            view_btnCircle?.apply {
                var color = ContextCompat.getColor(context, R.color.colorBtnDefault)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
            view_btnMemo?.apply {
                var color = ContextCompat.getColor(context, R.color.colorBtnDefault)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
            view_btnRotate?.apply {
                var color = ContextCompat.getColor(context, R.color.colorBtnDefault)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
            view_btnCrop?.apply {
                var color = ContextCompat.getColor(context, R.color.colorBtnDefault)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun setupViewUI() {


        //Receive original image bitmap from extra if edit from Search.
        if(m_strCategory == "SEARCH") {
            bitmap = BitmapFactory.decodeFile(intent.getStringExtra("ORIGINAL_IMAGE"))//intent.getParcelableExtra("ORIGINAL_IMAGE") as Bitmap
        }
        else
        {
            bitmap = BitmapFactory.decodeFile(m_objSlip?.get("OriginalPath")?.asString)
        }

        if(bitmap != null) {
            toolbar_title.text = m_strTitle
            view_btnClose.setOnClickListener(this@EditSlipActivity)
            view_btnCheck.setOnClickListener(this@EditSlipActivity)
            view_btnPinch.setOnClickListener(this@EditSlipActivity)
            view_btnPen.setOnClickListener(this@EditSlipActivity)
            view_btnRectangle.setOnClickListener(this@EditSlipActivity)
            view_imageContents.setImageBitmap(bitmap)
            view_imageContents.setOnTouchListener(this@EditSlipActivity)
            view_btnCircle.setOnClickListener(this@EditSlipActivity)
            view_btnMemo.setOnClickListener(this@EditSlipActivity)

            //Get drawn shape data

            if(m_objSlip!!.get("SHAPE_DATA") == null || m_objSlip!!.get("SHAPE_DATA").isJsonNull)
            {

            }
            else
            {
                var classType = object : TypeToken<HashMap<String, MutableList<Shape>>>(){}.type
                m_mapShape =  Gson().fromJson<HashMap<String,MutableList<Shape>>>(m_objSlip!!.get("SHAPE_DATA"), classType)
            }

            if(m_strCategory == "NEW") {
                view_btnCrop.alpha = 1f
                view_btnRotate.alpha = 1f
                view_btnCrop.setOnClickListener(this@EditSlipActivity)
                view_btnRotate.setOnClickListener(this@EditSlipActivity)
                setCropMode()
            }
            else
            {

                view_btnCrop.alpha = 0.4f
                view_btnRotate.alpha = 0.4f
                //view_btnCrop.setColorFilter(ContextCompat.getColor(this@EditSlipActivity,R.color.colorBtnHover), android.graphics.PorterDuff.Mode.MULTIPLY)
                //view_btnRotate.setColorFilter(ContextCompat.getColor(this@EditSlipActivity, R.color.colorBtnHover), android.graphics.PorterDuff.Mode.MULTIPLY)

                //Temporary route pen for draw rects
                m_drawRect = DrawRect(view_imageContents, m_mapShape, MODE_PEN)
                m_drawRect?.setChangeModeListener(this@EditSlipActivity)
                m_drawRect?.showDrawView()

                setPinchMode()
            }
        }
        else
        {

        }

        view_fabMenu.setOnClickListener(this)
        view_layoutFab.setOnClickListener(this)
    }


    override fun onTouch(v: View?, e: MotionEvent?): Boolean {

        var wRatio = view_imageContents.imageWidth / view_imageContents.drawable.intrinsicWidth//.toFloat()
        var hRatio = view_imageContents.imageHeight / view_imageContents.drawable.intrinsicHeight//.toFloat()
        var zoomedWRatio = view_imageContents.drawable.intrinsicWidth.toFloat() / view_imageContents.imageWidth
        var zoomedHRatio = view_imageContents.drawable.intrinsicHeight.toFloat() / view_imageContents.imageHeight

        var verticalSpace = (view_imageContents.viewHeight - view_imageContents.imageHeight) / 2
        if(verticalSpace < 0f) verticalSpace = 0f
        var horizontalSpace = (view_imageContents.viewWidth - view_imageContents.imageWidth) / 2
        if(horizontalSpace < 0f) horizontalSpace = 0f
        when(v?.id)
        {
            view_imageContents.id -> {
                when(e?.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
//                        if(view_imageContents.state != TouchImageView.State.NONE)
//                        {
//                            return true
//                        }
                        var eX = (e.x - horizontalSpace + (view_imageContents.currentRect.left * wRatio)) * zoomedWRatio
                        var eY = (e.y - verticalSpace + (view_imageContents.currentRect.top * hRatio)) * zoomedHRatio


                        //Detect rect click
                        for ((key, value) in m_mapShape) {
                            var strType = key
                            var list = value as MutableList<Shape>
                            for (shape in list) {
                                var rect = Rect(shape.leftPoint.toInt(), shape.topPoint.toInt(), shape.rightPoint.toInt(), shape.bottomPoint.toInt())
                                if (rect.contains(eX.toInt(), eY.toInt())) {

                                    view_imageContents.setTouchEnable(false)
                                    when(strType.toUpperCase())
                                    {
                                        MODE_PEN -> {
                                            setPenMode(shape)
                                        }
                                        MODE_RECT -> {
                                            setRectMode(shape)
                                        }
                                        MODE_CIRCLE -> {
                                            setCircleMode(shape)
                                        }
                                        MODE_MEMO -> {
                                            setMemoMode(shape)
                                        }
                                    }
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        intent.putExtra("SLIP_ITEM", m_objSlip.toString())
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    //Clear btn colors for focus other btn
    fun clearBtnColorFilter() {
        for(i in 0 until view_layoutEditBtns.childCount)
        {
            var drawable = (view_layoutEditBtns.getChildAt(i) as ImageView).drawable

            drawable?.clearColorFilter()
                   // .clearColorFilter()
        }
    }

    override fun changeMode(mode: Int) {
        when(mode)
        {
            MODE_PINCH -> {
                setPinchMode()
            }
        }
    }

    /**
     * Clear all drawed views before change mode.
     */
    fun clearView() {
        m_crop?.close()
        m_drawRect?.close()
    }

    /**
     * Set rotate mode.
     */
    
    fun setRotateMode() {

        clearView()

        var objFABBtnRotateCW = JsonObject()
        objFABBtnRotateCW.addProperty("NAME",getString(R.string.rotate_cw))
        objFABBtnRotateCW.addProperty("TAG","FAB_ROTATE_CW")
        objFABBtnRotateCW.addProperty("ICON",R.drawable.ic_rotate_cw)

        var objFABBtnRotateCCW = JsonObject()
        objFABBtnRotateCCW.addProperty("NAME",getString(R.string.rotate_ccw))
        objFABBtnRotateCCW.addProperty("TAG","FAB_ROTATE_CCW")
        objFABBtnRotateCCW.addProperty("ICON",R.drawable.ic_rotate_ccw)

        var objFABBtnRotate180 = JsonObject()
        objFABBtnRotate180.addProperty("NAME",getString(R.string.rotate_180))
        objFABBtnRotate180.addProperty("TAG","FAB_ROTATE_180")
        objFABBtnRotate180.addProperty("ICON",R.drawable.ic_rotate_180)

        var arJson = JsonArray()
        arJson.add(objFABBtnRotateCW)
        arJson.add(objFABBtnRotate180)
        arJson.add(objFABBtnRotateCCW)

        addSubButtonToFAB(arJson)

        clearBtnColorFilter()
        setTabBtnTintForLowAPI()
        m_C.setTabBtnTint(view_btnRotate, ContextCompat.getColor(this@EditSlipActivity, R.color.colorPrimary))

        openFABMenu()
    }

    /**
     * Set pen mode.
     */

    fun setPenMode(selectedShape: Shape? = null) {

        clearView()

        var objFABBtnSetting = JsonObject()
        objFABBtnSetting.addProperty("NAME",getString(R.string.setting))
        objFABBtnSetting.addProperty("TAG","FAB_PEN_SETTING")
        objFABBtnSetting.addProperty("ICON",R.drawable.ic_setting)

        var objFABBtnModify = JsonObject()
        objFABBtnModify.addProperty("NAME",getString(R.string.modify))
        objFABBtnModify.addProperty("TAG","FAB_PEN_MODIFY")
        objFABBtnModify.addProperty("ICON",R.drawable.ic_modify)

        var objFABBtnDelete = JsonObject()
        objFABBtnDelete.addProperty("NAME",getString(R.string.remove))
        objFABBtnDelete.addProperty("TAG","FAB_PEN_DELETE")
        objFABBtnDelete.addProperty("ICON",R.drawable.ic_delete)

        var arJson = JsonArray()
        arJson.add(objFABBtnSetting)
        arJson.add(objFABBtnModify)
        arJson.add(objFABBtnDelete)

        addSubButtonToFAB(arJson)

        clearBtnColorFilter()
        setTabBtnTintForLowAPI()
        m_C.setTabBtnTint(view_btnPen, ContextCompat.getColor(this@EditSlipActivity, R.color.colorPrimary))

//        var listPen = m_mapShape.get("PEN")
//        if(listPen == null) {
//            listPen = mutableListOf<Shape>()
//            m_mapShape["PEN"] = listPen
//        }

        //m_drawRect = DrawRect(view_imageContents, listPen as MutableList<Shape>)
        m_drawRect = DrawRect(view_imageContents, m_mapShape, MODE_PEN)
        m_drawRect?.setChangeModeListener(this@EditSlipActivity)
        m_drawRect?.showDrawView(selectedShape)

    }

    /**
     * Set Circle mode.
     */

    fun setCircleMode(selectedShape: Shape? = null) {

        clearView()

        var objFABBtnSetting = JsonObject()
        objFABBtnSetting.addProperty("NAME",getString(R.string.setting))
        objFABBtnSetting.addProperty("TAG","FAB_CIRCLE_SETTING")
        objFABBtnSetting.addProperty("ICON",R.drawable.ic_setting)

        var objFABBtnModify = JsonObject()
        objFABBtnModify.addProperty("NAME",getString(R.string.modify))
        objFABBtnModify.addProperty("TAG","FAB_CIRCLE_MODIFY")
        objFABBtnModify.addProperty("ICON",R.drawable.ic_modify)

        var objFABBtnDelete = JsonObject()
        objFABBtnDelete.addProperty("NAME",getString(R.string.remove))
        objFABBtnDelete.addProperty("TAG","FAB_CIRCLE_DELETE")
        objFABBtnDelete.addProperty("ICON",R.drawable.ic_delete)

        var arJson = JsonArray()
        arJson.add(objFABBtnSetting)
        arJson.add(objFABBtnModify)
        arJson.add(objFABBtnDelete)

        addSubButtonToFAB(arJson)

        clearBtnColorFilter()
        setTabBtnTintForLowAPI()
        m_C.setTabBtnTint(view_btnCircle, ContextCompat.getColor(this@EditSlipActivity, R.color.colorPrimary))

        m_drawRect = DrawRect(view_imageContents, m_mapShape, MODE_CIRCLE)
        m_drawRect?.setChangeModeListener(this@EditSlipActivity)
        m_drawRect?.showDrawView(selectedShape)

    }

    /**
     * Set Rect mode.
     */

    fun setRectMode(selectedShape: Shape? = null) {

        clearView()

        var objFABBtnSetting = JsonObject()
        objFABBtnSetting.addProperty("NAME",getString(R.string.setting))
        objFABBtnSetting.addProperty("TAG","FAB_RECT_SETTING")
        objFABBtnSetting.addProperty("ICON",R.drawable.ic_setting)

        var objFABBtnModify = JsonObject()
        objFABBtnModify.addProperty("NAME",getString(R.string.modify))
        objFABBtnModify.addProperty("TAG","FAB_RECT_MODIFY")
        objFABBtnModify.addProperty("ICON",R.drawable.ic_modify)

        var objFABBtnDelete = JsonObject()
        objFABBtnDelete.addProperty("NAME",getString(R.string.remove))
        objFABBtnDelete.addProperty("TAG","FAB_RECT_DELETE")
        objFABBtnDelete.addProperty("ICON",R.drawable.ic_delete)

        var arJson = JsonArray()
        arJson.add(objFABBtnSetting)
        arJson.add(objFABBtnModify)
        arJson.add(objFABBtnDelete)

        addSubButtonToFAB(arJson)

        clearBtnColorFilter()
        setTabBtnTintForLowAPI()
        m_C.setTabBtnTint(view_btnRectangle, ContextCompat.getColor(this@EditSlipActivity, R.color.colorPrimary))

        m_drawRect = DrawRect(view_imageContents, m_mapShape, MODE_RECT)
        m_drawRect?.setChangeModeListener(this@EditSlipActivity)
        m_drawRect?.showDrawView(selectedShape)

    }

    /**
     * Set Memo mode.
     */

    fun setMemoMode(selectedShape: Shape? = null) {

        clearView()

        var objFABBtnSetting = JsonObject()
        objFABBtnSetting.addProperty("NAME",getString(R.string.setting))
        objFABBtnSetting.addProperty("TAG","FAB_MEMO_SETTING")
        objFABBtnSetting.addProperty("ICON",R.drawable.ic_setting)

        var objFABBtnModify = JsonObject()
        objFABBtnModify.addProperty("NAME",getString(R.string.modify))
        objFABBtnModify.addProperty("TAG","FAB_MEMO_MODIFY")
        objFABBtnModify.addProperty("ICON",R.drawable.ic_modify)

        var objFABBtnDelete = JsonObject()
        objFABBtnDelete.addProperty("NAME",getString(R.string.remove))
        objFABBtnDelete.addProperty("TAG","FAB_MEMO_DELETE")
        objFABBtnDelete.addProperty("ICON",R.drawable.ic_delete)

        var arJson = JsonArray()
        arJson.add(objFABBtnSetting)
        arJson.add(objFABBtnModify)
        arJson.add(objFABBtnDelete)

        addSubButtonToFAB(arJson)

        clearBtnColorFilter()
        setTabBtnTintForLowAPI()
        m_C.setTabBtnTint(view_btnMemo, ContextCompat.getColor(this@EditSlipActivity, R.color.colorPrimary))

        m_drawRect = DrawRect(view_imageContents, m_mapShape, MODE_MEMO)
        m_drawRect?.setChangeModeListener(this@EditSlipActivity)
        m_drawRect?.showDrawView(selectedShape)

    }

    /**
     * Set pinch mode.
     */
    
    fun setPinchMode() {

        clearView()

        view_imageContents.setTouchEnable(true)

        addSubButtonToFAB(null)
        clearBtnColorFilter()
        setTabBtnTintForLowAPI()
        m_C.setTabBtnTint(view_btnPinch, ContextCompat.getColor(this@EditSlipActivity, R.color.colorPrimary))

    }

    /**
     * Set crop mode.
     */
    
    fun setCropMode() {

        clearView()

        var objFABBtnApply = JsonObject()
        objFABBtnApply.addProperty("NAME",getString(R.string.apply))
        objFABBtnApply.addProperty("TAG","FAB_CROP_APPLY")
        objFABBtnApply.addProperty("ICON",R.drawable.ic_check)

        var arJson = JsonArray()
        arJson.add(objFABBtnApply)

        addSubButtonToFAB(arJson)

        clearBtnColorFilter()
        setTabBtnTintForLowAPI()
        m_C.setTabBtnTint(view_btnCrop, ContextCompat.getColor(this@EditSlipActivity, R.color.colorPrimary))
        m_crop = Crop(view_imageContents)
        m_crop?.showCropView()

        view_imageContents.setTouchEnable(true)

    }

    /**
     * Create sub FAB Btns.
     * Hide FAB button if parameter is null.
     */
    @SuppressLint("NewApi")
    fun addSubButtonToFAB(arObjBtn:JsonArray?) {

        view_layoutFab.removeAllViews()

        if(arObjBtn != null) {
            view_fabMenu.visibility = View.VISIBLE
            for (i in 0 until arObjBtn.size()) {
                var strBtnName = arObjBtn.get(i).asJsonObject.get("NAME").asString
                var strBtnTag = arObjBtn.get(i).asJsonObject.get("TAG").asString
                var nBtnIcon = arObjBtn.get(i).asJsonObject.get("ICON").asInt

                var view_layoutFabBtns = ConstraintLayout(this@EditSlipActivity)?.apply {
                    var params = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    id = View.generateViewId()
                    layoutParams = params
                    view_layoutFab.addView(this)

                    var set = ConstraintSet()
                    set.clone(view_layoutFab)
                    set.connect(this.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                    set.connect(this.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
                    set.applyTo(view_layoutFab)

                    var nMarginBottom = 80 + ((i + 1) * FAB_BUTTON_MARGIN_BOTTOM)
                    params.setMargins(0, 0, 11.px, nMarginBottom.px)
                }
                view_layoutFabBtns.setPadding(0, 4.px, 0, 4.px)

                var view_fabApply = FloatingActionButton(this@EditSlipActivity).apply {
                    id = View.generateViewId()
                    var params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    layoutParams = params
                    setImageDrawable(ContextCompat.getDrawable(this@EditSlipActivity, nBtnIcon))
                    size = FloatingActionButton.SIZE_MINI
                    isClickable = true
                    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@EditSlipActivity, R.color.colorFABIconBackground))
                    tag = strBtnTag
                    view_layoutFabBtns.addView(this)

                    var set = ConstraintSet()
                    set.clone(view_layoutFabBtns)
                    set.connect(this.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
                    set.connect(this.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                    set.applyTo(view_layoutFabBtns)
                }
                var marginFABParams = view_fabApply.layoutParams as ViewGroup.MarginLayoutParams
                marginFABParams.setMargins(0, 0, 8.px, 8.px)
                view_fabApply.setOnClickListener(this@EditSlipActivity)

                var view_textTitle = TextView(this@EditSlipActivity).apply {
                    id = View.generateViewId()
                    var params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    text = strBtnName//
                    background = ContextCompat.getDrawable(this@EditSlipActivity, R.drawable.bg_fab_text)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        elevation = 4.dp.toFloat()
                    }

                    setTextColor(ContextCompat.getColor(this@EditSlipActivity, R.color.colorTextDefault))
                    layoutParams = params

                    view_layoutFabBtns.addView(this)

                    var set = ConstraintSet()
                    set.clone(view_layoutFabBtns)
                    set.connect(this.id, ConstraintSet.TOP, view_fabApply.id, ConstraintSet.TOP, 0)
                    set.connect(this.id, ConstraintSet.BOTTOM, view_fabApply.id, ConstraintSet.BOTTOM, 0)
                    set.connect(this.id, ConstraintSet.RIGHT, view_fabApply.id, ConstraintSet.LEFT, 0)
                    set.applyTo(view_layoutFabBtns)


                }
                view_textTitle.setPadding(5.px, 5.px, 5.px, 5.px)
                var marginParams = view_textTitle.layoutParams as ViewGroup.MarginLayoutParams
                marginParams.setMargins(0, 0, 10.px, 0)
            }
        }
        else
        {
            view_fabMenu.visibility = View.GONE
        }
    }

    fun saveBitmap() {
        m_objSlip?.apply {
            var modifiedBitmap = (view_imageContents.drawable as BitmapDrawable).bitmap
            var strImagePath = get("OriginalPath").asString
            var file = File(strImagePath)

            FileOutputStream(file).use{
                modifiedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
                it.flush()
            }

            addProperty("Width", modifiedBitmap?.width)
            addProperty("Height", modifiedBitmap?.height)
            addProperty("ImageSize", m_C.getFileSize(file.absolutePath, "KB"))
        }
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

//    fun shapeToGson():String?
//    {
//        for ((key, value) in m_mapShape) {
//            var strType = key
//            var list = value as MutableList<Shape>
//            for (shape in list) {
//                var rect = Rect(shape.leftPoint.toInt(), shape.topPoint.toInt(), shape.rightPoint.toInt(), shape.bottomPoint.toInt())
//                if (rect.contains(eX.toInt(), eY.toInt())) {
//
//                    view_imageContents.setTouchEnable(false)
//                    when(strType.toUpperCase())
//                    {
//                        MODE_PEN -> {
//                            setPenMode(shape)
//                        }
//                        MODE_RECT -> {
//                            setRectMode(shape)
//                        }
//                        MODE_CIRCLE -> {
//                            setCircleMode(shape)
//                        }
//                        MODE_MEMO -> {
//                            setMemoMode(shape)
//                        }
//                    }
//                    return true
//                }
//            }
//        }
//    }

    override fun onClick(v: View?) {
        when(v?.tag.toString().toUpperCase()) {
            "CLOSE" -> {

                //Remove temp data if Search.
                if(m_strCategory == "SEARCH") m_C.removeFile(this@EditSlipActivity, intent.getStringExtra("ORIGINAL_IMAGE"))

                intent.putExtra("SLIP_ITEM", m_objSlip.toString())
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
            }
            "CONFIRM" -> {

                var strShapeData = Gson().toJson(m_mapShape,HashMap<String,MutableList<Shape>>().javaClass)

                if(m_strCategory == "SEARCH")
                {

                    UpdateShapeData(this@EditSlipActivity, m_objSlip!!, strShapeData!!).execute()
                }
                else {
                    saveBitmap()
                    m_objSlip?.add("SHAPE_DATA", (JsonParser().parse(strShapeData)))
                    intent.putExtra("SLIP_ITEM", m_objSlip.toString())
                    this@EditSlipActivity.run {
                        setResult(RESULT_OK, intent)
                    }

                    finish()
                }
            }
            "CROP" -> {
                bitmap?.run {
                    setCropMode()
                }
            }
            "FAB_MENU" -> {
               if(view_layoutFab.visibility == View.VISIBLE)
               {
                   closeFABMenu()
               }
               else
               {
                   openFABMenu()
               }
            }
            "FAB_BG" -> {
                closeFABMenu()
            }
            //Crop FAB action
            "FAB_CROP_APPLY" -> {
                m_crop?.transformImage()
                m_crop?.close()
                m_mapShape.clear()

                closeFABMenu()
            }
            "PINCH" -> {
                setPinchMode()
            }
            "ROTATE" -> {
                setRotateMode()
            }
            "PEN" -> {
                setPenMode()
            }
            "RECT" -> {
                setRectMode()
            }
            "CIRCLE" -> {
                setCircleMode()
            }
            "MEMO" -> {
                setMemoMode()
            }
            //Rotate FAB action
            "FAB_ROTATE_CW" -> {
                view_imageContents.setImageBitmap((view_imageContents.drawable as BitmapDrawable).bitmap.rotate(90f))
            }
            "FAB_ROTATE_CCW" -> {
                view_imageContents.setImageBitmap((view_imageContents.drawable as BitmapDrawable).bitmap.rotate(-90f))
            }
            "FAB_ROTATE_180" -> {
                view_imageContents.setImageBitmap((view_imageContents.drawable as BitmapDrawable).bitmap.rotate(180f))
            }
            "FAB_MEMO_DELETE" -> {
                m_drawRect?.removeSelectedItem()
                closeFABMenu()
            }
            "FAB_MEMO_MODIFY" -> {
                m_drawRect?.m_selectedShape?.run {
                    var nWeight = weight

                    var strBGColor:String? = null
                    strBGColor = if(bgColor != null) bgColor else MEMO_BACKGROUND_DEFAULT//strBGColor = String.format("#%06X", (0xFFFFFF and bgColor!!.color))

                    var strLineColor:String? = null
                    strLineColor = if(lineColor != null) lineColor else MEMO_LINE_DEFAULT//strLineColor = String.format("#%06X", (0xFFFFFF and lineColor!!.color))

                    var strFontColor:String? = null
                    strFontColor = if(fontColor != null) fontColor else MEMO_FOREGROUND_DEFAULT

                    var nFontSize = fontSize
                    var strText = text

                    Intent(this@EditSlipActivity, DrawSettingActivity::class.java).run {

                        var objProperty = JsonObject().apply {

                            addProperty("WEIGHT", nWeight)
                            addProperty("BackColor", strBGColor)
                            addProperty("LineColor", strLineColor)
                            addProperty("TextColor", strFontColor)
                            addProperty("Bold", bold)
                            addProperty("Italic", italic)
                            addProperty("TEXT_SIZE", nFontSize)
                            addProperty("TEXT_TITLE", strText)
                            addProperty("ALPHA", MEMO_ALPHA_DEFAULT)
                            addProperty("BackFlag", MEMO_BG_ENABLE_DEFAULT)
                        }
                        putExtra("MODE", "MODIFY")
                        putExtra("TYPE", "MEMO")
                        putExtra("PROPERTY", objProperty.toString())

                        startActivityForResult(this, RESULT_MODIFY_MEMO)
                    }
                }

                closeFABMenu()
            }

            "FAB_MEMO_SETTING" -> {
                Intent(this@EditSlipActivity, DrawSettingActivity::class.java).run {
                    putExtra("MODE", "SETTING")
                    putExtra("TYPE", "MEMO")
                    // putExtra("Mode", "SEARCH")
                    startActivityForResult(this, RESULT_SETTING_MEMO)
                }

                closeFABMenu()
            }

            "FAB_CIRCLE_DELETE" -> {
                m_drawRect?.removeSelectedItem()
                closeFABMenu()
            }

            "FAB_CIRCLE_MODIFY" -> {
                m_drawRect?.m_selectedShape?.run {
                    var nWeight = weight

                    var strBGColor:String? = null
                    strBGColor = if(bgColor != null) bgColor else CIRCLE_BACKGROUND_DEFAULT// = String.format("#%06X", (0xFFFFFF and bgColor!!.color))

                    var strLineColor:String? = null
                    strLineColor = if(lineColor != null) lineColor else CIRCLE_LINE_DEFAULT //strLineColor = String.format("#%06X", (0xFFFFFF and lineColor!!.color))

                    var backFlag = backFlag

                    Intent(this@EditSlipActivity, DrawSettingActivity::class.java).run {

                        var objProperty = JsonObject().apply {

                            addProperty("WEIGHT", nWeight)
                            addProperty("BackColor", strBGColor)
                            addProperty("LineColor", strLineColor)
                            addProperty("BackFlag", backFlag)
                        }
                        putExtra("MODE", "MODIFY")
                        putExtra("TYPE", "RECT")
                        putExtra("PROPERTY", objProperty.toString())

                        startActivityForResult(this, RESULT_MODIFY_CIRCLE)
                    }
                }

                closeFABMenu()
            }

            "FAB_CIRCLE_SETTING" -> {
                Intent(this@EditSlipActivity, DrawSettingActivity::class.java).run {
                    putExtra("MODE", "SETTING")
                    putExtra("TYPE", "CIRCLE")
                    // putExtra("Mode", "SEARCH")
                    startActivityForResult(this, RESULT_SETTING_CIRCLE)
                }

                closeFABMenu()
            }

            "FAB_RECT_MODIFY" -> {
                m_drawRect?.m_selectedShape?.run {
                    var nWeight = weight

                    var strBGColor:String? = null
                    strBGColor = if(bgColor != null) bgColor else RECT_BACKGROUND_DEFAULT //= String.format("#%06X", (0xFFFFFF and bgColor!!.color))

                    var strLineColor:String? = null
                    strLineColor = if(lineColor != null) lineColor else RECT_LINE_DEFAULT// = String.format("#%06X", (0xFFFFFF and lineColor!!.color))

                    var backFlag = backFlag
                    //String.format("#%06X", (0xFFFFFF and bgColor!!.color))


                    Intent(this@EditSlipActivity, DrawSettingActivity::class.java).run {

                        var objProperty = JsonObject().apply {

                            addProperty("WEIGHT", nWeight)
                            addProperty("BackColor", strBGColor)
                            addProperty("LineColor", strLineColor)
                            addProperty("BackFlag", backFlag)
                        }
                        putExtra("MODE", "MODIFY")
                        putExtra("TYPE", "RECT")
                        putExtra("PROPERTY", objProperty.toString())

                        startActivityForResult(this, RESULT_MODIFY_RECT)
                    }
                }

                closeFABMenu()
            }

            //DrawRect pen action
            "FAB_RECT_DELETE" -> {
                m_drawRect?.removeSelectedItem()
                closeFABMenu()
            }

            "FAB_RECT_SETTING" -> {
                Intent(this@EditSlipActivity, DrawSettingActivity::class.java).run {
                    putExtra("TYPE", "RECT")
                    putExtra("MODE", "SETTING")
                    // putExtra("Mode", "SEARCH")
                    startActivityForResult(this, RESULT_SETTING_RECT)
                }

                closeFABMenu()
            }

            //DrawRect pen action
            "FAB_PEN_DELETE" -> {
                m_drawRect?.removeSelectedItem()
                closeFABMenu()
            }

            "FAB_PEN_MODIFY" -> {
                m_drawRect?.m_selectedShape?.run {
                    var nWeight = weight
                    var strBGColor:String? = null
                    strBGColor = if(bgColor != null) bgColor else PEN_BACKGROUND_DEFAULT
                    //var nBGColor = String.format("#%06X", (0xFFFFFF and bgColor!!.color))
                    Intent(this@EditSlipActivity, DrawSettingActivity::class.java).run {

                        var objProperty = JsonObject().apply {

                            addProperty("WEIGHT", nWeight)
                            addProperty("BackColor", strBGColor)
                        }

                        putExtra("MODE", "MODIFY")
                        putExtra("TYPE", "PEN")
                        putExtra("PROPERTY", objProperty.toString())

                        startActivityForResult(this, RESULT_MODIFY_PEN)
                    }
                }

                closeFABMenu()

            }
            "FAB_PEN_SETTING" -> {
                Intent(this@EditSlipActivity, DrawSettingActivity::class.java).run {
                    putExtra("TYPE", "PEN")
                    putExtra("MODE", "SETTING")
                    // putExtra("Mode", "SEARCH")
                    startActivityForResult(this, RESULT_SETTING_PEN)
                }

                closeFABMenu()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            //Result of DrawModify
            RESULT_MODIFY_PEN -> {
                if (resultCode == RESULT_OK) {
                    data?.extras?.get("PROPERTY")?.apply {
                        var objProperty = JsonParser().parse(this as String)?.asJsonObject

                        m_drawRect?.m_selectedShape?.let {

                            objProperty?.apply {
                                var color = if(get("BackColor").isJsonNull) PEN_BACKGROUND_DEFAULT else get("BackColor").asString
                                var weight = if(get("WEIGHT").isJsonNull) PEN_WEIGHT_DEFAULT else get("WEIGHT").asFloat

                                it.bgColor = color
//                                it.bgColor?.apply {
//                                    this.color = Color.parseColor(color)
//                                    alpha = PEN_ALPHA_DEFAULT
//                                }
                                it.weight = weight
                                it.bottomPoint = it.topPoint + weight.toInt().px

                                view_imageContents.invalidate()
                            }
                        }
                    }
                }
            }
            RESULT_MODIFY_RECT -> {
                if (resultCode == RESULT_OK) {
                    data?.extras?.get("PROPERTY")?.apply {
                        var objProperty = JsonParser().parse(this as String)?.asJsonObject

                        m_drawRect?.m_selectedShape?.let {

                            objProperty?.apply {
                                var backColor = if(get("BackColor").isJsonNull) RECT_BACKGROUND_DEFAULT else get("BackColor").asString
                                var lineColor = if(get("LineColor").isJsonNull) RECT_LINE_DEFAULT else get("LineColor").asString
                                var weight = if(get("WEIGHT").isJsonNull) RECT_WEIGHT_DEFAULT else get("WEIGHT").asFloat
                                var backFlag = if(get("BackFlag").isJsonNull) RECT_BG_ENABLE_DEFAULT else get("BackFlag").asString

                                it.backFlag = backFlag
                                it.bgColor = backColor
                                it.lineColor = lineColor
                                it.weight = weight
//                                if(backFlag == "0")
//                                {
//                                    it.bgColor = Paint().apply {
//                                        color = Color.parseColor(backColor)
//                                        isAntiAlias = true
//                                        style = Paint.Style.FILL
//                                        alpha = RECT_ALPHA_DEFAULT
//                                    }
//                                }
//
//                                it.backFlag = backFlag
//                                it.lineColor = Paint().apply {
//                                    color = Color.parseColor(lineColor)
//                                    isAntiAlias = true
//                                    style = Paint.Style.STROKE
//                                    alpha = RECT_ALPHA_DEFAULT
//                                    weight?.run {
//                                        strokeWidth = weight.px.toFloat()
//                                    }
//                                }

                                view_imageContents.invalidate()
                            }
                        }
                    }
                }
            }

            RESULT_MODIFY_CIRCLE -> {
                if (resultCode == RESULT_OK) {
                    data?.extras?.get("PROPERTY")?.apply {
                        var objProperty = JsonParser().parse(this as String)?.asJsonObject

                        m_drawRect?.m_selectedShape?.let {

                            objProperty?.apply {
                                var backColor = if(get("BackColor").isJsonNull) CIRCLE_BACKGROUND_DEFAULT else get("BackColor").asString
                                var lineColor = if(get("LineColor").isJsonNull) CIRCLE_LINE_DEFAULT else get("LineColor").asString
                                var weight = if(get("WEIGHT").isJsonNull) CIRCLE_WEIGHT_DEFAULT else get("WEIGHT").asFloat
                                var backFlag = if(get("BackFlag").isJsonNull) CIRCLE_BG_ENABLE_DEFAULT else get("BackFlag").asString

                                it.bgColor = backColor
                                it.lineColor = lineColor
                                it.backFlag = backFlag
                                it.weight = weight
//                                if(backFlag == "0")
//                                {
//                                    it.bgColor = Paint().apply {
//                                        color = Color.parseColor(backColor)
//                                        isAntiAlias = true
//                                        style = Paint.Style.FILL
//                                        alpha = CIRCLE_ALPHA_DEFAULT
//                                    }
//                                }
//
//                                it.backFlag = backFlag
//                                it.lineColor = Paint().apply {
//                                    color = Color.parseColor(lineColor)
//                                    isAntiAlias = true
//                                    style = Paint.Style.STROKE
//                                    alpha = CIRCLE_ALPHA_DEFAULT
//                                    weight?.run {
//                                        strokeWidth = weight.px.toFloat()
//                                    }
//                                }

                                view_imageContents.invalidate()
                            }
                        }
                    }
                }
            }

            RESULT_MODIFY_MEMO -> {
                if (resultCode == RESULT_OK) {
                    data?.extras?.get("PROPERTY")?.apply {
                        var objProperty = JsonParser().parse(this as String)?.asJsonObject

                        m_drawRect?.m_selectedShape?.let {

                            objProperty?.apply {
                                var backColor = if(get("BackColor").isJsonNull) MEMO_BACKGROUND_DEFAULT else get("BackColor").asString
                                var lineColor = if(get("LineColor").isJsonNull) MEMO_LINE_DEFAULT else get("LineColor").asString
                                var fontColor = if(get("TextColor").isJsonNull) MEMO_FOREGROUND_DEFAULT else get("TextColor").asString
                                var fontSize = if(get("TEXT_SIZE").isJsonNull) MEMO_FONTSIZE_DEFAULT else get("TEXT_SIZE").asInt
                                var bold = if(get("Bold").isJsonNull) MEMO_BOLD_DEFAULT else get("Bold").asString
                                var italic = if(get("Italic").isJsonNull) MEMO_ITALIC_DEFAULT else get("Italic").asString
                                var fAlpha = if(get("ALPHA").isJsonNull) MEMO_ALPHA_DEFAULT else get("ALPHA").asFloat
                                var text = if(get("TEXT_TITLE").isJsonNull) getString(R.string.new_memo) else get("TEXT_TITLE").asString

                                it.alpha = fAlpha
                                it.bgColor = backColor
                                it.lineColor = lineColor
                                it.weight = MEMO_WEIGHT_DEFAULT
//                                it.bgColor = Paint().apply {
//                                    color = Color.parseColor(backColor)
//                                    isAntiAlias = true
//                                    style = Paint.Style.FILL
//                                    alpha = (255 * it.alpha!!.toFloat()).toInt()
//                                }
//
//                                it.lineColor = Paint().apply {
//                                    color = Color.parseColor(lineColor)
//                                    isAntiAlias = true
//                                    style = Paint.Style.STROKE
//                                    alpha = (255 * it.alpha!!.toFloat()).toInt()
//                                    strokeWidth = MEMO_WEIGHT_DEFAULT.px.toFloat()
//                                }

                                it.fontColor = fontColor
                                it.bold = bold
                                it.fontSize = fontSize
                                it.italic = italic
                                it.text = text

                                view_imageContents.invalidate()
                            }
                        }
                    }
                }
            }

            RESULT_SETTING_MEMO -> {
                if(resultCode == RESULT_OK)
                {
                    m_drawRect?.loadProperty(MODE_MEMO)
                }
            }
            RESULT_SETTING_PEN -> {
                if(resultCode == RESULT_OK)
                {
                    m_drawRect?.loadProperty(MODE_PEN)
                }
            }

            RESULT_SETTING_CIRCLE -> {
                if(resultCode == RESULT_OK)
                {
                    m_drawRect?.loadProperty(MODE_CIRCLE)
                }
            }

            RESULT_SETTING_RECT -> {
                if(resultCode == RESULT_OK)
                {
                    m_drawRect?.loadProperty(MODE_RECT)
                }
            }
        }
    }

    //Open FAB Menu
    fun openFABMenu() {
        view_layoutFab.animation = AnimationUtils.loadAnimation(this, R.anim.fadein)
        view_layoutFab.visibility = View.VISIBLE
    }
    //Close FAB Menu
    fun closeFABMenu() {
        view_layoutFab.animation = AnimationUtils.loadAnimation(this, R.anim.fadeout)
        view_layoutFab.visibility = View.GONE
    }

    private class UpdateShapeData(activity: EditSlipActivity, var objSlipInfo:JsonObject, var strShapeData:String):AsyncTask<Void, Void, Void>()
    {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = UPDATE_SHAPEDATA_FAILED

        init {
            activityRef = WeakReference(activity as AppCompatActivity)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            var activity = activityRef?.get()
            builder = AlertDialog.Builder(activity as Context).apply {
                var view = LayoutInflater.from(activity).inflate(R.layout.progress_circle, null)
                view.findViewById<TextView>(R.id.view_textProgressTitle).text = activity.getString(R.string.in_progress)

                setView(view)
                setCancelable(false)
                setNegativeButton(activity.getString(R.string.btn_cancel), DialogInterface.OnClickListener { dialog, which ->

                    if(status == AsyncTask.Status.RUNNING)
                    {
                        cancel(true)
                       // activity.onBackPressed()
                    }

                    dialog.dismiss()
                })}.create()
            builder?.show()
        }

        override fun doInBackground(vararg params: Void?): Void? {

            //Thread.sleep(10000)
            var arObjRes = JsonArray()

            if(!isCancelled)
            {
                var activity = activityRef?.get()


                var objShape = JsonParser().parse(strShapeData).asJsonObject
                var strShapeXML = m_C.getShapeTag(objShape)
                if(m_C.isBlank(strShapeXML))
                {
                    nCurrentStatus = UPDATE_SHAPEDATA_SUCCESS
                    return null
                }

                //try connect agent via Wifi Network if Mobile data usage is disabled.
                if(m_C.isNetworkConnected(activity as Context))
                {
                    //consider protocol property
                    if("SOCKET".equals(CONNECTION_PROTOCOL, true))
                    {
                        if(SocketManager()?.updateShapeData(strShapeXML!!, objSlipInfo.get("SDOC_NO").asString, objSlipInfo.get("SLIP_DOC_IRN").asString)) {
                            nCurrentStatus = UPDATE_SHAPEDATA_SUCCESS
                        }
                    }
                }
                else
                {
                    nCurrentStatus = NETWORK_DISABLED
                }
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            builder?.dismiss()
            val activity = activityRef?.get()

            when(nCurrentStatus) {
                UPDATE_SHAPEDATA_SUCCESS -> {

                    var intent = activity?.intent

                    m_C.removeFile(activity?.applicationContext!!, intent?.getStringExtra("ORIGINAL_IMAGE")!!)
                    objSlipInfo?.add("SHAPE_DATA", (JsonParser().parse(strShapeData)))
                    intent.putExtra("SLIP_ITEM", objSlipInfo.toString())
                    activity.run {
                        setResult(RESULT_OK, intent)
                    }
                    activity.finish()
                }
                UPDATE_SHAPEDATA_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.failed_update_data))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->

                        })
                    }.show()
                }
                NETWORK_DISABLED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.alert_network_disabled_message))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->

                        })
                    }.show()
                }
            }
        }
    }
}