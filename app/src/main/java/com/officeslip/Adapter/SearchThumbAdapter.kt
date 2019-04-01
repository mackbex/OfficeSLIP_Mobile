package com.officeslip.Adapter

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.officeslip.CONNECTION_PROTOCOL
import com.officeslip.DOWNLOAD_THUMB_PATH
import com.officeslip.Listener.ViewHolderClickListener
import com.sgenc.officeslip.R
import com.officeslip.Socket.SocketManager
import com.officeslip.Subclass.DelegateThumbView
import com.officeslip.Subclass.TouchImageView
import com.officeslip.Util.Common
import com.officeslip.Util.Logger
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.ref.WeakReference

class SearchThumbAdapter(val context: Context, data: JsonArray?) : RecyclerView.Adapter<SearchThumbAdapter.ViewHolder>(), ViewHolderClickListener {

    var mData: JsonArray? = JsonArray()
    var mInflater: LayoutInflater
    private var callBack: ShowOriginal? = null
    var nImageViewWidth = 0
    var nImageViewHeight = 0

    init {
        mData = data
        mInflater = LayoutInflater.from(context)
    }

    companion object {
        const val NETWORK_DISABLED = 3021
        const val DOWNLOAD_THUMB_SUCCESS = 3022
        const val DOWNLOAD_THUMB_FAILED = 3023
    }

    interface ShowOriginal {
        fun showOriginal(idx: Int)
    }

    fun setItemOnClickListener(listener: ShowOriginal) {
        this.callBack = listener
    }

    //Define viewholder
    class ViewHolder(itemView: View, val listener: ViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {

        var view_progress:ProgressBar
        var view_cardThumb:CardView
        var view_imageItem:TouchImageView

        init {
            view_progress = itemView.findViewById(R.id.view_progress)
            view_cardThumb = itemView.findViewById(R.id.view_cardThumb)
            view_imageItem = itemView.findViewById(R.id.view_imageItem)
            view_cardThumb.setOnClickListener(this)
            view_cardThumb.setOnLongClickListener(this)
            view_cardThumb.setOnTouchListener(this)
            view_imageItem.setOnClickListener(this)
            view_imageItem.setOnLongClickListener(this)
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when(event?.action)
            {
                MotionEvent.ACTION_DOWN -> {
                    v?.findViewWithTag<TextView>("THUMB_TITLE")?.setBackgroundColor(ContextCompat.getColor(v.context, R.color.colorPrimarySuperLight))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v?.findViewWithTag<TextView>("THUMB_TITLE")?.setBackgroundColor(ContextCompat.getColor(v.context, R.color.colorHovered))

                }
            }
            return false
        }

        override fun onClick(v: View?) {
            listener.onTap(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            listener.onLongTap(adapterPosition)

            return true
        }
    }
    fun setImageViewWidth(nWidth:Int)
    {
        this.nImageViewWidth = nWidth
    }
    fun setImageViewHeight(nHeight:Int)
    {
        this.nImageViewHeight = nHeight
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.recycle_slipthumb_row, parent, false)
        return ViewHolder(view, this)
    }

    override fun getItemCount(): Int {
        var nRes = 0
        mData?.let {
            nRes = it.size()
        }

        return nRes
    }

    fun changeViewHeight(imageView:ImageView)
    {
        imageView.layoutParams.height = nImageViewHeight
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        mData?.apply {

            var strDocIRN = get(position).asJsonObject.get("DOC_IRN").asString.trim()
            var strSDocNo = get(position).asJsonObject.get("SDOC_NO").asString.trim()
            var strSlipRect = get(position).asJsonObject.get("SLIP_RECT").asString.trim()
            var strSDocType = get(position).asJsonObject.get("SDOC_TYPE").asString.trim()
            var strSlipShape = get(position).asJsonObject.get("SHAPE_DATA")//if(get(position).asJsonObject.get("SHAPE_DATA") == null ) null else get(position).asJsonObject.get("SHAPE_DATA").asJsonObject
            var strSlipDocIRN = get(position).asJsonObject.get("SLIP_DOC_IRN").asString.trim()
            var strDocNo = get(position).asJsonObject.get("DOC_NO").asString.trim()
            var strThumbName = get(position).asJsonObject.get("THUMB_NAME").asString.trim()
            var strThumbPath = get(position).asJsonObject.get("THUMB_PATH").asString.trim()

            holder.view_imageItem.viewWidth = nImageViewWidth
            holder.view_imageItem.viewHeight = nImageViewHeight
            holder.view_imageItem.layoutParams.width = nImageViewWidth
            holder.view_imageItem.layoutParams.height = nImageViewHeight

            var thumb = File(strThumbPath)
            if(thumb.exists())
            {
                holder.view_progress.visibility = View.GONE
                holder.view_imageItem.setImageBitmap(BitmapFactory.decodeFile(strThumbPath))
                holder.view_imageItem.setDelegate(DelegateThumbView(holder.view_imageItem, get(position).asJsonObject, "SEARCH"))
            }
            else
            {
                DownloadThumbImage(context as Activity, SocketManager(), holder, get(position).asJsonObject).execute(get(position).asJsonObject)
            }



            //    changeViewHeight(holder.view_imageItem)
        }



//
//
//            when(strSDocStep)
//            {
//                "0" -> {
//                    strSDocStepName = SDOC_STEP_0
//                    strSDocStepColor = SDOC_STEP_COLOR_0
//                }
//                "1" -> {
//                    strSDocStepName = SDOC_STEP_1
//                    strSDocStepColor = SDOC_STEP_COLOR_1
//                }
//                "2" -> {
//                    strSDocStepName = SDOC_STEP_2
//                    strSDocStepColor = SDOC_STEP_COLOR_2
//                }
//                "3" -> {
//                    strSDocStepName = SDOC_STEP_3
//                    strSDocStepColor = SDOC_STEP_COLOR_3
//                }
//                "4" -> {
//                    strSDocStepName = SDOC_STEP_4
//                    strSDocStepColor = SDOC_STEP_COLOR_4
//                }
//                "7" -> {
//                    strSDocStepName = SDOC_STEP_7
//                    strSDocStepColor = SDOC_STEP_COLOR_7
//                }
//                "9" -> {
//                    strSDocStepName = SDOC_STEP_9
//                    strSDocStepColor = SDOC_STEP_COLOR_9
//                }
//                else -> {
//                    strSDocStepName = SDOC_STEP_0
//                    strSDocStepColor = SDOC_STEP_COLOR_0
//                }
//            }
//        }
//
//        strCabinet = strCabinet?.let {
//            String.format(context.getString(R.string.date_format_yyyymmdd), it.substring(0,4), it.substring(4,6), it.substring(6,8))
//        }
//
//        holder.view_textSDocName.text = strSDocName
//        holder.view_textSDocStep.text = strSDocStepName
//        holder.view_textSDocStep.setTextColor(Color.parseColor(strSDocStepColor))
//        holder.view_textPartName.text = strPartName
//        holder.view_textRegUserName.text = strRegUserName
//        holder.view_textSDocNoDoc.text = strSDocNoDoc
//        holder.view_textSDocTypeName.text = strSDocTypeName
//        holder.view_textSlipCnt.text = strSlipCnt
//        holder.view_textCabinet.text = strCabinet
    }

    override fun onLongTap(index: Int) {
        mData?.let {
            callBack?.showOriginal(index)
           // callBack?.showOriginal(it.get(index).asJsonObject)
        }
    }

    override fun onTap(index: Int) {
        mData?.let {
            callBack?.showOriginal(index)
        }
    }

    override fun onRemove(index: Int) {

    }

    private class DownloadThumbImage(activity: Activity, var socketManager: SocketManager, var holder: ViewHolder, var objImageInfo:JsonObject): AsyncTask<JsonObject, Void, ByteArray?>() {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = DOWNLOAD_THUMB_FAILED

        init {
            activityRef = WeakReference(activity)
        }

        override fun doInBackground(vararg params: JsonObject?): ByteArray? {

            //Thread.sleep(10000)
            var bRes:ByteArray? = null

            if(!isCancelled)
            {
                var activity = activityRef?.get()

                //try connect agent via Wifi Network if Mobile data usage is disabled.
                if(m_C.isNetworkConnected(activity as Context))
                {
                    var strImageDocIRN = params[0]?.get("SLIP_DOC_IRN")?.asString
                    var strDocNo = params[0]?.get("DOC_NO")?.asString

                    //consider protocol property
                    if("SOCKET".equals(CONNECTION_PROTOCOL, true))
                    {
                        bRes = SocketManager()?.Download(strImageDocIRN!!, strDocNo!!, "IMG_SLIP_M")?.apply {
                            nCurrentStatus = DOWNLOAD_THUMB_SUCCESS
                        }
                        bRes?.run {

                            //Create thumb path
                            var path = File(DOWNLOAD_THUMB_PATH)
                            if(!path.exists())
                            {
                                path.mkdirs()
                            }

                            var file = File(DOWNLOAD_THUMB_PATH, params[0]?.get("THUMB_NAME")?.asString)

                            FileOutputStream(file).use {

                                var bitmap = BitmapFactory.decodeByteArray(bRes, 0, bRes!!.size)

                                try
                                {
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)

                                    it.flush()

                                    if (!bitmap.isRecycled) {
                                        bitmap.recycle()
                                    }
                                }
                                catch (e:Exception)
                                {
                                    Logger.WriteException(DownloadThumbImage::javaClass.name, "DownloadThumbImage", e, 5)
                                    return null
                                }
                            }
                        }
                    }
                }
                else
                {
                    nCurrentStatus = NETWORK_DISABLED
                }
            }

            return bRes
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)

            holder.view_progress.visibility = View.GONE


            if(result != null)
            {
                holder.view_imageItem.setImageBitmap(BitmapFactory.decodeByteArray(result, 0, result.size))

            }
            else
            {
                holder.view_imageItem.setImageDrawable(ContextCompat.getDrawable(activityRef?.get()?.applicationContext!!, R.drawable.close))
            }

            holder.view_imageItem.setDelegate(DelegateThumbView(holder.view_imageItem, objImageInfo, "SEARCH"))

          //  holder.view_imageItem.fitImageToView()

//            when(nCurrentStatus) {
//                SDocTypeActivity.GET_SDOCTYPE_SUCCESS -> {
//
//                    (activity as SDocTypeActivity).setupViewUI(result)
//                    /*activity?.setResult(RESULT_OK)
//                    activity?.finish()*/
//                }
//                SDocTypeActivity.GET_SDOCTYPE_FAILED -> {
//                    AlertDialog.Builder(activity as Context).run {
//                        setTitle(activity.getString(R.string.login_alert_failed_title))
//                        setMessage(activity.getString(R.string.get_sdoctype_failed))
//                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
//                            activity.onBackPressed()
//                        })
//                    }.show()
//                }
//                SDocTypeActivity.NETWORK_DISABLED -> {
//                    AlertDialog.Builder(activity as Context).run {
//                        setTitle(activity.getString(R.string.login_alert_failed_title))
//                        setMessage(activity.getString(R.string.alert_network_disabled_message))
//                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
//                            activity.onBackPressed()
//                        })
//                    }.show()
//                }
//            }
        }

    }

}