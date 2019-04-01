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

class SearchAddfileAdapter(val context: Context, data: JsonArray?, var isMultiSelectOn:Boolean = false) : RecyclerView.Adapter<SearchAddfileAdapter.ViewHolder>(), Filterable, ViewHolderAddFileClickListener {

    var mData: JsonArray? = JsonArray()
    var mInflater: LayoutInflater
    var mSlipFilter: SlipFilter? = null
    private var callBack: DownAddfile? = null
    private var callBackRemove: RemoveAddfile? = null
    private var callBackMove: MoveAddfile? = null
    private var callBackCopy: CopyAddfile? = null
    init {
        mData = data
        mInflater = LayoutInflater.from(context)
    }

    interface DownAddfile {
        fun downAddfile(obj: JsonObject)
    }

    interface RemoveAddfile {
        fun removeAddfile(obj: JsonObject)
    }

    interface MoveAddfile {
        fun moveAddfile(obj: JsonObject)
    }

    interface CopyAddfile {
        fun copyAddfile(obj: JsonObject)
    }

    fun setDownddfileListener(listener: DownAddfile) {
        this.callBack = listener
    }

    fun setRemoveAddfileListener(listener: RemoveAddfile) {
        this.callBackRemove = listener
    }
    fun setMoveAddfileListener(listener: MoveAddfile)
    {
        this.callBackMove = listener
    }

    fun setCopyAddfileListener(listener: CopyAddfile)
    {
        this.callBackCopy = listener
    }


    //Define viewholder
    class ViewHolder(itemView: View, val listener: SearchAddfileAdapter) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {
        var view_layout: LinearLayout
        var view_textAttachName: TextView
        var view_textAddStep: TextView
        var view_textExtension: TextView
        var view_textSize: TextView
        var view_textPartName: TextView
        var view_textRegUserName: TextView
        var view_btnRemove :Button
        var view_btnCopyAttach :Button
        var view_btnMoveAttach :Button
        var view_swipe: SwipeLayout

        init {
            view_layout   = itemView.findViewById(R.id.view_layout)
            view_textAttachName   = itemView.findViewById(R.id.view_textAttachName)
            view_textAddStep   = itemView.findViewById(R.id.view_textAddStep)
            view_textExtension   = itemView.findViewById(R.id.view_textExtension)
            view_textSize   = itemView.findViewById(R.id.view_textSize)
            view_textPartName   = itemView.findViewById(R.id.view_textPartName)
            view_textRegUserName   = itemView.findViewById(R.id.view_textRegUserName)
            view_btnRemove          = itemView.findViewById(R.id.view_btnRemoveAttach)
            view_btnCopyAttach          = itemView.findViewById(R.id.view_btnCopyAttach)
            view_btnMoveAttach          = itemView.findViewById(R.id.view_btnMoveAttach)
            view_swipe              = itemView.findViewById(R.id.swipe)

            view_layout.setOnClickListener(this)
            view_layout.setOnLongClickListener(this)
            view_layout.setOnTouchListener(this)
            view_btnRemove.setOnClickListener(this)
            view_btnCopyAttach.setOnClickListener(this)
            view_btnMoveAttach.setOnClickListener(this)
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
                "DOWN_ATTACH"->{
                    listener.onTap(adapterPosition)
                }
                "REMOVE_ATTACH"->{
                    listener.onRemove(adapterPosition)
                }
                "MOVE_ATTACH"->{
                    listener.onMove(adapterPosition)
                }
                "COPY_ATTACH"->{
                    listener.onCopy(adapterPosition)
                }
            }

        }

        override fun onLongClick(v: View?): Boolean {
//            when(v?.tag.toString().toUpperCase())
//            {
//                "DOWN_ATTACH"->{
//                    listener.onTap(adapterPosition)
//                }
//                "REMOVE_ATTACH"->{
//                    listener.onRemove(adapterPosition)
//                }
//            }
            return true
        }
    }

    override fun onCopy(index: Int) {
        mData?.let {
            callBackCopy?.copyAddfile(it.get(index).asJsonObject)
        }
    }

    override fun onMove(index: Int) {
        mData?.let {
            callBackMove?.moveAddfile(it.get(index).asJsonObject)
        }
    }

    override fun onRemove(index: Int) {
        mData?.let {
            callBackRemove?.removeAddfile(it.get(index).asJsonObject)
        }
    }

    override fun onLongTap(index: Int) {
//
//        mData?.let {
//            callBack?.downAddfile(it.get(index).asJsonObject)
//        }

        if (isMultiSelectOn) {
         //   addCodeIntoSelectedCodes(index)//MainActivity.isMultiSelectOn = true
        }
    }

    override fun onTap(index: Int) {
        mData?.let {
            callBack?.downAddfile(it.get(index).asJsonObject)
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
        val view = mInflater.inflate(R.layout.recycle_addfileresult_row, parent, false)
        return ViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var strAddFile:String? = null
        var strAddName:String? = null
        var strAddExt:String? = null
        var strAddStep:String? = null
        var strAddNo:String? = null
        var strFileSize:String? = null
        var strCabinet:String? = null
        var strRegUserName:String? = null
        var strPartName:String? = null
        var strAddStepName:String? = null
        var strAddStepColor:String? = null

        mData?.apply {
            strAddFile = get(position).asJsonObject.get("ADD_FILE").asString.trim()
            strAddStep = get(position).asJsonObject.get("ADD_STEP").asString.trim()
            strAddNo = get(position).asJsonObject.get("ADD_NO").asString.trim()
            strFileSize = get(position).asJsonObject.get("FILE_SIZE").asString.trim()
            strPartName = get(position).asJsonObject.get("PART_NM").asString.trim()
            strRegUserName = get(position).asJsonObject.get("REG_USERNM").asString.trim()
            strCabinet  = get(position).asJsonObject.get("CABINET").asString.trim()
            strAddStepColor = null

            var fileName:List<String> = strAddFile?.split("")!!

            strAddExt = strAddFile?.substring(strAddFile?.lastIndexOf(".")!! + 1, strAddFile?.length!!)?.toUpperCase()



            when(strAddStep)
            {
                "0" -> {
                    strAddStepName = context.getString(R.string.sdoc_step_0)
                    strAddStepColor = SDOC_STEP_COLOR_0
                }
                "1" -> {
                    strAddStepName = context.getString(R.string.sdoc_step_1)
                    strAddStepColor = SDOC_STEP_COLOR_0
                }
                "2" -> {
                    strAddStepName = context.getString(R.string.sdoc_step_2)
                    strAddStepColor = SDOC_STEP_COLOR_2
                }
                "3" -> {
                    strAddStepName = context.getString(R.string.sdoc_step_3)
                    strAddStepColor = SDOC_STEP_COLOR_3
                }
                "4" -> {
                    strAddStepName = context.getString(R.string.sdoc_step_4)
                    strAddStepColor = SDOC_STEP_COLOR_4
                }
                "7" -> {
                    strAddStepName = context.getString(R.string.sdoc_step_7)
                    strAddStepColor = SDOC_STEP_COLOR_7
                }
                "9" -> {
                    strAddStepName = context.getString(R.string.sdoc_step_9)
                    strAddStepColor = SDOC_STEP_COLOR_9
                }
                else -> {
                    strAddStepName = context.getString(R.string.sdoc_step_0)
                    strAddStepColor = SDOC_STEP_COLOR_0
                }
            }
        }

        strCabinet = strCabinet?.let {
            String.format(context.getString(R.string.date_format_yyyymmdd), it.substring(0,4), it.substring(4,6), it.substring(6,8))
        }

        holder.view_textAttachName.text = strAddFile
        holder.view_textExtension.text = strAddExt
        holder.view_textAddStep.setTextColor(Color.parseColor(strAddStepColor))
        holder.view_textAddStep.text = strAddStepName
        holder.view_textPartName.text = strPartName
        holder.view_textRegUserName.text = strRegUserName
        holder.view_textSize.text = strFileSize
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

    class SlipFilter(var mData:JsonArray?, var adapter: SearchAddfileAdapter) : Filter() {


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

interface ViewHolderAddFileClickListener {
    fun onLongTap(index : Int)
    fun onTap(index : Int)
    fun onRemove(index: Int)
    fun onMove(index: Int)
    fun onCopy(index:Int)
}
