package com.officeslip.Adapter

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.daimajia.swipe.SwipeLayout
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.officeslip.*
import com.sgenc.officeslip.*

class SearchSlipAdapter(val context: Context, data: JsonArray?, var isMultiSelectOn:Boolean = false) : RecyclerView.Adapter<SearchSlipAdapter.ViewHolder>(), Filterable, ViewHolderClickListener {

    var mData: JsonArray? = JsonArray()
    var mInflater: LayoutInflater
    var mSlipFilter: SlipFilter? = null
    private var callBack: ShowSlip? = null
    private var callBackRemove: RemoveSlip? = null
    private var callBackMove: MoveSlip? = null
    private var callBackCopy: CopySlip? = null

    init {
        mData = data
        mInflater = LayoutInflater.from(context)
    }

    interface ShowSlip {
        fun showSlip(obj: JsonObject)
    }

    interface RemoveSlip {
        fun removeSlip(obj: JsonObject)
    }

    interface MoveSlip {
        fun moveSlip(obj: JsonObject)
    }

    interface CopySlip {
        fun copySlip(obj: JsonObject)
    }


    fun setShowSlipListener(listener: ShowSlip) {
        this.callBack = listener
    }

    fun setRemoveSlipListener(listener: RemoveSlip) {
        this.callBackRemove = listener
    }

    fun setMoveSlipListener(listener: MoveSlip)
    {
        this.callBackMove = listener
    }

    fun setCopySlipListener(listener: CopySlip)
    {
        this.callBackCopy = listener
    }

    //Define viewholder
    class ViewHolder(itemView: View, val listener: ViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {
        var view_layout: LinearLayout
        var view_textSDocName: TextView
        var view_textSDocStep: TextView
        var view_textSlipCnt: TextView
        var view_textSDocTypeName: TextView
        var view_textSDocNoDoc: TextView
        var view_textPartName: TextView
        var view_textRegUserName: TextView
        var view_textCabinet:TextView
        var view_btnRemove:Button
        var view_btnMoveSlip:Button
        var view_btnCopySlip:Button
        var view_swipe:SwipeLayout

        init {
            view_layout   = itemView.findViewById(R.id.view_layout)
            view_textSDocName   = itemView.findViewById(R.id.view_textSDocName)
            view_textSDocStep   = itemView.findViewById(R.id.view_textSDocStep)
            view_textSlipCnt   = itemView.findViewById(R.id.view_textSlipCnt)
            view_textSDocTypeName   = itemView.findViewById(R.id.view_textSDocTypeName)
            view_textSDocNoDoc   = itemView.findViewById(R.id.view_textSDocNoDoc)
            view_textPartName   = itemView.findViewById(R.id.view_textPartName)
            view_textRegUserName   = itemView.findViewById(R.id.view_textRegUserName)
            view_textCabinet        = itemView.findViewById(R.id.view_textCabinet)
            view_btnRemove          = itemView.findViewById(R.id.view_btnRemoveSlip)
            view_btnMoveSlip          = itemView.findViewById(R.id.view_btnMoveSlip)
            view_btnCopySlip          = itemView.findViewById(R.id.view_btnCopySlip)
            view_swipe              = itemView.findViewById(R.id.swipe)

            view_layout.setOnClickListener(this)
            view_layout.setOnLongClickListener(this)
            view_layout.setOnTouchListener(this)
            view_btnRemove.setOnClickListener(this)
            view_btnMoveSlip.setOnClickListener(this)
            view_btnCopySlip.setOnClickListener(this)
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {

            when(event?.action)
            {
                MotionEvent.ACTION_DOWN -> {

                    v?.background = ContextCompat.getDrawable(v!!.context, R.drawable.border_bottom_hover)

                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v?.background = ContextCompat.getDrawable(v!!.context, R.drawable.border_bottom)

                }
            }
            return false
        }

        override fun onClick(v: View?) {

            view_swipe?.close()

            when(v?.tag.toString().toUpperCase())
            {
                "SHOW_SLIP"->{
                    listener.onTap(adapterPosition)
                }
                "REMOVE_SLIP"->{
                    listener.onRemove(adapterPosition)
                }
                "MOVE_SLIP"->{
                    listener.onMove(adapterPosition)
                }
                "COPY_SLIP"->{
                    listener.onCopy(adapterPosition)
                }
            }

        }

        override fun onLongClick(v: View?): Boolean {
//            when(v?.tag.toString().toUpperCase())
//            {
//                "SHOW_SLIP"->{
//                    listener.onTap(adapterPosition)
//                }
//                "REMOVE_SLIP"->{
//                    listener.onRemove(adapterPosition)
//                }
//            }
            return true
        }
    }


    override fun onCopy(index: Int) {
        mData?.let {
            callBackCopy?.copySlip(it.get(index).asJsonObject)
        }
    }

    override fun onMove(index: Int) {
        mData?.let {
            callBackMove?.moveSlip(it.get(index).asJsonObject)
        }
    }

    override fun onRemove(index: Int) {
        mData?.let {
            callBackRemove?.removeSlip(it.get(index).asJsonObject)
        }
    }

    override fun onLongTap(index: Int) {
//
//        mData?.let {
//            callBack?.showSlip(it.get(index).asJsonObject)
//        }
//
//        if (isMultiSelectOn) {
//         //   addCodeIntoSelectedCodes(index)//MainActivity.isMultiSelectOn = true
//        }
    }

    override fun onTap(index: Int) {
        mData?.let {
            callBack?.showSlip(it.get(index).asJsonObject)
        }

        //
//        if (isMultiSelectOn) {
//            addIDIntoSelectedIds(index)
//        } else {
//            Toast.makeText(context, "Clicked Item @ Position ${index + 1}", Toast.LENGTH_SHORT).show()
//        }

      //  addCodeIntoSelectedCodes(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.recycle_searchslip_row, parent, false)
        return ViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var strSDocName:String? = null
        var strSDocStep:String? = null
        var strSlipCnt:String? = null
        var strSDocTypeName:String? = null
        var strSDocNoDoc:String? = null
        var strPartName:String? = null
        var strRegUserName:String? = null
        var strSDocStepName:String? = null
        var strCabinet:String? = null
        var strSDocStepColor:String? = null

        mData?.apply {
            strSDocName = get(position).asJsonObject.get("SDOC_NAME").asString.trim()
            strSDocStep = get(position).asJsonObject.get("SDOC_STEP").asString.trim()
            strSlipCnt = get(position).asJsonObject.get("FILE_CNT").asString.trim()
            strSDocTypeName = get(position).asJsonObject.get("SDOCTYPE_NAME").asString.trim()
            strSDocNoDoc = get(position).asJsonObject.get("SDOCNO_DOC").asString.trim()
            strPartName = get(position).asJsonObject.get("PART_NM").asString.trim()
            strRegUserName = get(position).asJsonObject.get("USER_NAME").asString.trim()
            strCabinet  = get(position).asJsonObject.get("CABINET").asString.trim()
            strSDocStepColor = null


            when(strSDocStep)
            {
                "0" -> {
                    strSDocStepName = context.getString(R.string.sdoc_step_0)
                    strSDocStepColor = SDOC_STEP_COLOR_0
                }
                "1" -> {
                    strSDocStepName = context.getString(R.string.sdoc_step_1)
                    strSDocStepColor = SDOC_STEP_COLOR_0
                }
                "2" -> {
                    strSDocStepName = context.getString(R.string.sdoc_step_2)
                    strSDocStepColor = SDOC_STEP_COLOR_2
                }
                "3" -> {
                    strSDocStepName = context.getString(R.string.sdoc_step_3)
                    strSDocStepColor = SDOC_STEP_COLOR_3
                }
                "4" -> {
                    strSDocStepName = context.getString(R.string.sdoc_step_4)
                    strSDocStepColor = SDOC_STEP_COLOR_4
                }
                "7" -> {
                    strSDocStepName = context.getString(R.string.sdoc_step_7)
                    strSDocStepColor = SDOC_STEP_COLOR_7
                }
                "9" -> {
                    strSDocStepName = context.getString(R.string.sdoc_step_9)
                    strSDocStepColor = SDOC_STEP_COLOR_9
                }
                else -> {
                    strSDocStepName = context.getString(R.string.sdoc_step_0)
                    strSDocStepColor = SDOC_STEP_COLOR_0
                }
            }
        }

        strCabinet = strCabinet?.let {
            String.format(context.getString(R.string.date_format_yyyymmdd), it.substring(0,4), it.substring(4,6), it.substring(6,8))
        }

        holder.view_textSDocName.text = strSDocName
        holder.view_textSDocStep.text = strSDocStepName
        holder.view_textSDocStep.setTextColor(Color.parseColor(strSDocStepColor))
        holder.view_textPartName.text = strPartName
        holder.view_textRegUserName.text = strRegUserName
        holder.view_textSDocNoDoc.text = strSDocNoDoc
        holder.view_textSDocTypeName.text = strSDocTypeName
        holder.view_textSlipCnt.text = strSlipCnt
        holder.view_textCabinet.text = strCabinet
    }

    override fun getItemCount(): Int {
        var nRes = 0
        mData?.let {
            nRes = it.size()
        }

        return nRes
    }

    override fun getFilter(): Filter {
        if(mSlipFilter == null) {
            mSlipFilter = SlipFilter(mData, this)
        }

        return mSlipFilter as Filter
    }

    class SlipFilter(var mData:JsonArray?, var adapter: SearchSlipAdapter) : Filter() {


        //Filter list
        override fun performFiltering(constraint: CharSequence?): FilterResults {

            var result = FilterResults()

            var jsonFilteredArray = JsonArray()

            if (!constraint.isNullOrBlank() && mData != null) {
                var char = constraint.toString().toUpperCase()
                var filteredList: JsonArray

                for (element in mData!!) {
//                    var strSDocTypeName = element.asJsonObject.get("name").toString().toUpperCase()
//                    var strSDOcTypeCode = element.asJsonObject.get("code").toString().toUpperCase()
//
//                    if (strSDocTypeName.contains(char) || strSDOcTypeCode.contains(char)) {
//                        jsonFilteredArray.add(element)
//                    }
                }

                result.count = jsonFilteredArray.size()
                result.values = jsonFilteredArray
            } else {
                result.count = if (mData != null) mData!!.size() else 0
                result.values = mData
            }

            return result
        }

        //return filtered list
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            adapter.mData = results?.values as JsonArray
            adapter.notifyDataSetChanged()
        }
    }
}

interface ViewHolderClickListener {
    fun onLongTap(index : Int)
    fun onTap(index : Int)
    fun onRemove(index: Int)
    fun onMove(index: Int)
    fun onCopy(index:Int)
}
