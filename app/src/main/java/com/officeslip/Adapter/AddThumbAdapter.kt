package com.officeslip.Adapter

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sgenc.officeslip.R
import com.officeslip.Listener.ViewHolderClickListener
import android.view.MotionEvent
import com.officeslip.Subclass.DelegateThumbView
import com.officeslip.Subclass.TouchImageView
import com.officeslip.Util.Common
import java.io.File


//strCategory = IMAGE or FILE
class AddThumbAdapter(val context: Context, data: JsonArray?, var strCategory:String, var isOneSlip:Boolean = false) : RecyclerView.Adapter<AddThumbAdapter.ViewHolder>(), ViewHolderClickListener, View.OnClickListener {


    companion object {
        const val MODE_SELECT = 2220
        const val MODE_VIEW = 2221
        const val CATEGORY_SLIP = 2222
        const val CATEGORY_FILE = 2223

    }

    private var mData: JsonArray
    private var mInflater: LayoutInflater
    private var CURRENT_MODE = MODE_VIEW
    private var CURRENT_CATEGORY = CATEGORY_SLIP
    private var callBack: ChangeSlipMode? = null
    private var callBackViewOriginal: ViewOriginal? = null
    private var m_C: Common = Common()

    init {
        mData = if(data == null) JsonArray() else data
        mInflater = LayoutInflater.from(context)
    }

    interface ChangeSlipMode {
        fun changeSlipMode(mode: Int)
    }

    interface ViewOriginal {
        fun viewOriginal(objItem:JsonObject, nIdx:Int)
    }

    fun setChangeSlipModeListener(listener: ChangeSlipMode) {
        this.callBack = listener
    }

    fun setViewOriginalListener(listner: ViewOriginal) {
        this.callBackViewOriginal = listner
    }

    class ViewHolder(itemView:View, val listener: ViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {

        var view_textItemTitle:TextView
        var view_imageItem: TouchImageView
        var view_fileItem:TextView
        var view_layoutCardBG:LinearLayout
        var view_cardSlipList:CardView

        init {
            view_textItemTitle  = itemView.findViewById(R.id.view_textItemTitle)
            view_imageItem      = itemView.findViewById(R.id.view_imageItem)
            view_fileItem       = itemView.findViewById(R.id.view_fileItem)
            view_layoutCardBG   = itemView.findViewById(R.id.view_layoutCardBG)
            view_cardSlipList   = itemView.findViewById(R.id.view_cardSlipList)

            view_imageItem.setTouchEnable(false)
            view_cardSlipList.setOnClickListener(this)
            view_cardSlipList.setOnLongClickListener(this)
            view_cardSlipList.setOnTouchListener(this)
           // view_imageItem.setOnTouchListener(this)
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

    override fun onLongTap(index: Int) {

        callBack?.changeSlipMode(MODE_SELECT)
        CURRENT_MODE = MODE_SELECT
        addIntoSelected(index)
    }

    override fun onTap(index: Int) {

        when(CURRENT_MODE)
        {
            MODE_SELECT -> {

                addIntoSelected(index)
            }
            MODE_VIEW -> {
                callBackViewOriginal?.viewOriginal(mData.get(index).asJsonObject, index)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.tag) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.recycle_addslip_row, parent, false)
        return ViewHolder(view, this)
    }


    private fun addIntoSelected(index: Int) {

        if(index<0)
        {
            return
        }
        mData.get(index).asJsonObject.run {
            if(get("CHECKED") == null)
            {
                addProperty("CHECKED","1")
            }
            else
            {
                remove("CHECKED")
            }
        }

        notifyItemChanged(index)


      //  notifyItemRangeChanged(0, if(mData == null) 0 else mData!!.size())
    }

//    fun deleteSelectedCodes() {
//        if (selectedCodes.size < 1) return
//
//        for(strCode in selectedCodes)
//        {
//
//        }
//        val selectedIdIteration = selectedCodes.listIterator()
//
//        while (selectedIdIteration.hasNext()) {
//            val selectedItemID = selectedIdIteration.next()
//
//            var indexOfModelList = 0
//            val modelListIteration: MutableListIterator<MyModel> = modelList.listIterator();
//            while (modelListIteration.hasNext()) {
//                val model = modelListIteration.next()
//                if (selectedItemID.equals(model.id)) {
//                    modelListIteration.remove()
//                    selectedIdIteration.remove()
//                    notifyItemRemoved(indexOfModelList)
//                }
//                indexOfModelList++
//            }
//
//            MainActivity.isMultiSelectOn = false
//        }
//    }



    // Replace the contents of a ja.view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when(CURRENT_CATEGORY)
        {
            CATEGORY_SLIP -> {
                var strTitle:String? = context.getString(R.string.segment_image)
                var objImage = mData.get(position).asJsonObject
                var strImagePath:String? = objImage.get("OriginalPath").asString

                holder.view_imageItem.visibility = View.VISIBLE
                holder.view_fileItem.visibility = View.GONE

                if(File(strImagePath).exists()) {
                    //   strImagePath?.run {
                    m_C.decodeSampledBitmapFromResource(strImagePath, 100, 100)?.let {
                        holder.view_imageItem.setImageBitmap(it)
                    }
                    //   }
                }

                holder.view_textItemTitle.text = strTitle

                if(CURRENT_MODE == MODE_SELECT)
                {
                    if (objImage.get("CHECKED") != null) {
                        holder.view_layoutCardBG.setBackgroundResource(R.drawable.layout_shadow_selected)
                    }
                    else
                    {
                        holder.view_layoutCardBG.setBackgroundResource(0)
                    }
                }
                else
                {
                    holder.view_layoutCardBG.setBackgroundResource(0)
                }

             //   holder.view_imageItem.touchDelegate = holder.view_layoutCardBG.touchDelegate
                holder.view_imageItem.setDelegate(DelegateThumbView(holder.view_imageItem, mData.get(position).asJsonObject))

            }
            CATEGORY_FILE -> {
                var strTitle:String? = context.getString(R.string.segment_file)
                var objFile = mData.get(position).asJsonObject

                holder.view_imageItem.visibility = View.GONE
                holder.view_fileItem.visibility = View.VISIBLE
                holder.view_fileItem.text = objFile.get("FILE_NAME").asString
                holder.view_textItemTitle.text = strTitle

                if(CURRENT_MODE == MODE_SELECT)
                {
                    if (objFile.get("CHECKED") != null) {
                        holder.view_layoutCardBG.setBackgroundResource(R.drawable.layout_shadow_selected)
                    }
                    else
                    {
                        holder.view_layoutCardBG.setBackgroundResource(0)
                    }
                }
                else
                {
                    holder.view_layoutCardBG.setBackgroundResource(0)
                }
            }
        }

    }

    fun setMode(mode:Int)
    {
        CURRENT_MODE = mode

        for(i in 0 until mData.size()) {
            mData.get(i).asJsonObject.remove("CHECKED")
            notifyItemChanged(i)
        }
    }

    fun setItem(item:JsonArray?, strCategory: String) {

        this.strCategory = strCategory
        CURRENT_MODE = MODE_VIEW

        notifyItemRangeRemoved(0,mData.size())

        item?.apply {
            mData = item

            for(i in 0 until mData.size())
            {
                mData.get(i).asJsonObject.remove("CHECKED")

                notifyItemInserted(i)
            }
        }
    }

//    fun removeItem(poistion:Int)
//    {
//        mData.remove(poistion)
//    }

    fun removeSelectedItems()
    {
        var mTemp = JsonArray()
        var listCheckedIdx = ArrayList<Int>()
        var nLastIdx = 0
        for(i in 0 until mData.size())
        {
            var objItem = mData.get(0).asJsonObject
            if(objItem.get("CHECKED") == null)
            {
                mTemp.add(objItem)
            }
            else
            {
                var nRemovedIdx = i - nLastIdx
                m_C.removeFile(context, mData.get(0).asJsonObject.get("OriginalPath")?.asString)
                m_C.removeFile(context, mData.get(0).asJsonObject.get("ThumbPath")?.asString)
                m_C.removeFile(context, mData.get(0).asJsonObject.get("FILE_PATH")?.asString)

                notifyItemRemoved(nRemovedIdx)
                nLastIdx++
            }

            mData.remove(0)
        }

        mData.addAll(mTemp)

//        for (objItem in mTemp)
//        {
//         //   m_C.removeFile(context, mTemp)
//        }

    }


    fun addItem(item: JsonObject) {

        mData.add(item)

        notifyItemInserted(mData.size()-1)
    }

    fun setCategory(nCategory:Int) {

        this.CURRENT_CATEGORY = nCategory
    }


    fun getCurrentCategory():String {
        return strCategory
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount():Int {
        return mData.size()
    }



    fun setTabBtnTintForLowAPI(view:View) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {

            (view as ImageView)?.apply {
                var color = ContextCompat.getColor(context, R.color.colorPrimary)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
//            view_imageMagnifier?.apply {
//                var color = ContextCompat.getColor(context, R.color.colorInnerSearchBackground)
//                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
//            }
//            view_btnCheck?.apply {
//                var color = ContextCompat.getColor(context, R.color.colorActionBtnDefault)
//                background.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
//            }
//            view_btnClose?.apply {
//                var color = ContextCompat.getColor(context, R.color.colorActionBtnDefault)
//                background.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
//            }
        }
    }





    override fun onRemove(index: Int) {

    }



}
