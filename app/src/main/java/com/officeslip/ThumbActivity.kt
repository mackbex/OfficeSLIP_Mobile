package com.officeslip

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Point
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.officeslip.Adapter.SearchThumbAdapter
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.AutoFitGridLayoutManager
import com.officeslip.Util.Common
import com.sgenc.officeslip.R
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.activity_thumbnail.*
import org.w3c.dom.Element
import java.io.File
import java.lang.ref.WeakReference

class ThumbActivity:AppCompatActivity(),View.OnClickListener, SearchThumbAdapter.ShowOriginal, RadioGroup.OnCheckedChangeListener
{


    companion object {
        const val NETWORK_DISABLED = 6001
        const val GET_THUMB_SUCCESS = 6002
        const val GET_THUMB_FAILED = 6003

    }

    private var m_objSlipData = JsonObject()
    private var m_objThumbList:JsonArray? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var m_C: Common = Common()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thumbnail)

        g_SysInfo.strThumbZoomValue = if(m_C.isBlank(g_SysInfo.strThumbZoomValue)) "0" else g_SysInfo.strThumbZoomValue

        setToolbar()
        setupViewUI(null)

       // GetThumbList(this@ThumbActivity, SocketManager(), m_objSlipData).execute()
    }

    override fun onStart() {
        super.onStart()
        GetThumbList(this@ThumbActivity, SocketManager(), m_objSlipData).execute()
    }

    private  fun setToolbar() {
        //run only when current fragment is this class.
        (this@ThumbActivity as AppCompatActivity)?.run {
            setSupportActionBar(view_toolbarSearchThumb)
            supportActionBar?.setDisplayShowTitleEnabled(false)

        }
        view_btnClose.setOnClickListener(this)
    }

    fun setupViewUI(objList: JsonArray?) {

        m_objThumbList = objList

        intent.getStringExtra("selectedItem")?.apply {
            var objSelected = JsonParser().parse(this)
            m_objSlipData = objSelected.asJsonObject
        }

        if(m_objSlipData.size() <= 0)
        {
            AlertDialog.Builder(this).run {
                setMessage(getString(R.string.failed_load_thumb))
                setPositiveButton(getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                    finish()
                })
            }.show()
        }

        //Set thumbnail ja.view title
        toolbar_title.text = m_objSlipData.get("SDOC_NAME").asString.trim()

        viewManager = AutoFitGridLayoutManager(this@ThumbActivity, 500)
        viewAdapter = SearchThumbAdapter(this@ThumbActivity as AppCompatActivity, m_objThumbList)

//        intent.getStringExtra("SelectedItem")?.apply {
//            var objSelected = JsonParser().parse(this)
//            var listSelected = JsonArray()
//            listSelected.add(objSelected)
//            (viewAdapter as SDocTypeAdapter).setItem(listSelected)
//        }

        recyclerView = findViewById<RecyclerView>(R.id.view_recyclerSearchThumb).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a grid layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

            (adapter as SearchThumbAdapter).setItemOnClickListener(this@ThumbActivity as SearchThumbAdapter.ShowOriginal)
        }


        if(objList != null)
        {
            addZoomSegment()
        }

    }

    //Add thunmb zoom buttons
    fun addZoomSegment() {

        var segGroup = findViewById<SegmentedGroup>(R.id.view_segGroup)

        segGroup.removeAllViews()
        for(i in 0 until SLIP_THUMB_ZOOM_MAX) {

            var viewSegmentBtn = (LayoutInflater.from(this@ThumbActivity).inflate(R.layout.thumbactivity_seg_btn, null) as RadioButton).apply {
                text =  (i+1).toString()
                id = i

            }

            //Set checked
            if(i == g_SysInfo.strThumbZoomValue.toInt())
            {
                viewSegmentBtn.isChecked = true
            }

            //add listener
            segGroup.setOnCheckedChangeListener(this)
            segGroup.addView(viewSegmentBtn)
        }

        segGroup.updateBackground()
    }

    fun changeThumbWidth(nThumbSize:Int)
    {
        var display = windowManager.defaultDisplay
        var size = Point()
        display.getSize(size)

        var adapter_thumbImage = (recyclerView.adapter as SearchThumbAdapter)
        var nLayoutMargin = resources.getDimension(R.dimen.search_thumb_layout_margin)
        var nCardViewMargin = resources.getDimension(R.dimen.search_thumb_cardview_margin)


        var fPadding = 10
        var nItemRow = SLIP_THUMB_ZOOM_MAX - nThumbSize
        var paddingSpace = (fPadding * nItemRow) + 5
        var nAvrWidth = size.x - paddingSpace
        var nItemWidth = ((nAvrWidth / nItemRow) - (nLayoutMargin + nCardViewMargin)).toInt()
        var nItemheight = (nItemWidth * 170) / 140
//        var heightDP = m_C.pixelToDp(this@SearchThumbActivity, windowWidth)

        viewManager = AutoFitGridLayoutManager(this@ThumbActivity, nItemWidth)
        recyclerView.layoutManager = viewManager
        adapter_thumbImage.setImageViewWidth(nItemWidth)
        adapter_thumbImage.setImageViewHeight(nItemheight)

    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (group?.tag.toString().toUpperCase()) {
            "THUMB_ZOOM" -> {
                g_SysInfo.strThumbZoomValue = checkedId.toString()
                var view_radioBtn = group?.findViewById<RadioButton>(checkedId)
                view_radioBtn?.isChecked = true

                changeThumbWidth(checkedId)

            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.tag.toString().toUpperCase()) {
            "CLOSE" ->{
                finish()
            }
        }
    }

    override fun showOriginal(nIdx: Int) {

        Intent(this@ThumbActivity, OriginalViewAcitivty::class.java).run {

          //  var strShapeTag:String? = null
            //if(m_objThumbList?.get(nIdx) != null) m_objThumbList?.get(nIdx)?.asJsonObject?.get("SHAPE_DATA")?.asString

            var obj = JsonObject()
            obj.addProperty("CUR_IDX", nIdx)
            obj.addProperty("CATEGORY","SEARCH")
            obj.addProperty("SDOC_NAME", m_objSlipData.get("SDOC_NAME").asString)
          //  obj.addProperty("SHAPE_DATA", strShapeTag)
            obj.add("SLIP_LIST", getOriginalViewList())
            putExtra("SLIP_DATA", obj.toString())
            startActivity(this)
        }
    }

    //Reconstitute slip original item
    fun getOriginalViewList():JsonArray {
        var objOriginalList = JsonArray()
        m_objThumbList?.let {
            for(i in 0 until it.size())
            {

                var nFileCnt = it.get(i).asJsonObject.get("FILE_CNT").asInt
                for(j in 0 until nFileCnt)
                {
                    var objSlipItem = it.get(i).asJsonObject.deepCopy()
                    objSlipItem.addProperty("DOC_NO",j+1)
                    objSlipItem.addProperty("FILE_NAME","${objSlipItem.get("SLIP_DOC_IRN").asString}_$j.J2K")
                    if(j > 0)
                    {
                        objSlipItem.addProperty("THUMB_PATH","")
                    }

                    objOriginalList.add(objSlipItem)

                }
            }
        }
        return objOriginalList
    }

    override fun onStop() {
        super.onStop()
        m_C.removeFolder(this@ThumbActivity, DOWNLOAD_THUMB_PATH)
    }


    private class GetThumbList(activity: Activity, var socketManager: SocketManager, var objSearchOption:JsonObject): AsyncTask<String, Void, JsonArray>() {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = GET_THUMB_FAILED

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
                        activity.onBackPressed()
                    }

                    dialog.dismiss()
                })}.create()
            builder?.show()
        }

        override fun doInBackground(vararg params: String?): JsonArray {

            //Thread.sleep(10000)
            var arObjRes = JsonArray()

            if(!isCancelled)
            {
                var activity = activityRef?.get()

                //try connect agent via Wifi Network if Mobile data usage is disabled.
                if(m_C.isNetworkConnected(activity as Context))
                {
                    //consider protocol property
                    if("SOCKET".equals(CONNECTION_PROTOCOL, true))
                    {
                        SocketManager()?.getThumbList(objSearchOption)?.let {

                            for (i in 0 until it.length) {
                                var element = it.item(i) as Element
                                var strThumbName =
                                        element.getElementsByTagName("SLIP_DOC_IRN").item(0).textContent +
                                        element.getElementsByTagName("DOC_NO").item(0).textContent + ".jpg"

                                var obj = JsonObject()
                                obj.addProperty("SDOC_NO", element.getElementsByTagName("SDOC_NO").item(0).textContent)
                                obj.addProperty("SDOC_TYPE", element.getElementsByTagName("SDOC_TYPE").item(0).textContent)
                                obj.addProperty("SDOC_ONE", element.getElementsByTagName("SDOC_ONE").item(0).textContent)
                                obj.addProperty("DOC_IRN", element.getElementsByTagName("DOC_IRN").item(0).textContent)
                                obj.addProperty("SLIP_RECT", element.getElementsByTagName("SLIP_RECT").item(0).textContent)
                                obj.addProperty("SLIP_DOC_IRN", element.getElementsByTagName("SLIP_DOC_IRN").item(0).textContent)
                                obj.addProperty("DOC_NO", element.getElementsByTagName("DOC_NO").item(0).textContent)
                                obj.addProperty("FILE_CNT", element.getElementsByTagName("FILE_CNT").item(0).textContent)
                                obj.addProperty("THUMB_NAME", strThumbName)
                                obj.addProperty("THUMB_PATH", DOWNLOAD_THUMB_PATH + File.separator + strThumbName)


                                //Parse rect object

                                var strShapeTag = element.getElementsByTagName("SLIP_PAGE").item(0).textContent
                                if(!m_C.isBlank(strShapeTag))
                                {
                                    strShapeTag = strShapeTag.replace("[", "<")
                                    strShapeTag = strShapeTag.replace("]", ">")
                                }

                                strShapeTag = m_C.parseShapeXML(strShapeTag)

                                obj.add("SHAPE_DATA", JsonParser().parse(strShapeTag))

                                arObjRes.add(obj)
                            }
                            nCurrentStatus = GET_THUMB_SUCCESS
                        }
                    }
                }
                else
                {
                    nCurrentStatus = NETWORK_DISABLED
                }
            }

            return arObjRes
        }

        override fun onPostExecute(result: JsonArray?) {
            super.onPostExecute(result)

            builder?.dismiss()
            val activity = activityRef?.get()

            when(nCurrentStatus) {
                GET_THUMB_SUCCESS -> {

                    (activity as ThumbActivity).setupViewUI(result)
                    /*activity?.setResult(RESULT_OK)
                    activity?.finish()*/
                }
                GET_THUMB_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.failed_load_thumb))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                                activity.onBackPressed()
                        })
                    }.show()
                }
                NETWORK_DISABLED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.alert_network_disabled_message))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                               activity.onBackPressed()
                        })
                    }.show()
                }
            }
        }
    }

}
