package com.officeslip.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.officeslip.Listener.ViewHolderClickListener
import com.sgenc.officeslip.R
import com.officeslip.SDocTypeActivity
import com.officeslip.Util.Common




class SDocTypeAdapter(val context: Context, data: JsonArray?, var isMultiSelectOn:Boolean = false) : RecyclerView.Adapter<SDocTypeAdapter.ViewHolder>(), Filterable, ViewHolderClickListener {

    var mData: JsonArray? = JsonArray()
    var mInflater: LayoutInflater
    var mSDocTypeFilter: SDocTypeFilter? = null
    var selectedItem: JsonArray = JsonArray()
    private val m_C: Common = Common()

    init {
        mData = data
        mInflater = LayoutInflater.from(context)
    }


    class ViewHolder(itemView:View, val listener: ViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {
        var view_textSDocTypeName:TextView
        var view_textSDocTypeCode:TextView
        var view_imageSDocTypeChecked:ImageView
        var view_layout:ConstraintLayout

        init {
            view_textSDocTypeName   = itemView.findViewById(R.id.view_textSDocTypeName)
            view_textSDocTypeCode   = itemView.findViewById(R.id.view_textSDocTypeCode)
            view_imageSDocTypeChecked       = itemView.findViewById(R.id.view_imageSDocTypeChecked)
            view_layout             = itemView.findViewById(R.id.view_layout)
            view_layout.setOnClickListener(this)
            view_layout.setOnLongClickListener(this)
            view_layout.setOnTouchListener(this)
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
            listener.onTap(adapterPosition)
        }

        override fun onLongClick(v: View?): Boolean {
            listener.onLongTap(adapterPosition)

            return true
        }
    }

    override fun onLongTap(index: Int) {
        //
//        if (isMultiSelectOn) {
//            addCodeIntoSelectedCodes(index)//MainActivity.isMultiSelectOn = true
//        }
    }

    override fun onTap(index: Int) {
        //
//        if (isMultiSelectOn) {
//            addIDIntoSelectedIds(index)
//        } else {
//            Toast.makeText(context, "Clicked Item @ Position ${index + 1}", Toast.LENGTH_SHORT).show()
//        }

        //Check parent SDocLevel
        var objParent:JsonObject? = null
        mData?.let {
            it.get(index).asJsonObject?.let {
                it.get("HAS_CHILD")?.run {
                    if(!m_C.isBlank(this.asString))
                    {
                        objParent = mData?.get(index)?.asJsonObject
                    }

                }
            }
        }

        if(objParent != null)
        {
            selectedItem = JsonArray()
            notifyItemRangeChanged(0, if(mData == null) 0 else mData!!.size())

            Intent(context, SDocTypeActivity::class.java).run {
                this.putExtra("PARENT_ITEM",objParent?.toString())
               // this.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

                (context as Activity).startActivityForResult(this, SDocTypeActivity.RESULT_FROM_SUBACTIVITY)
            }
        }
        else
        {
            addCodeIntoSelectedCodes(index)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.recycle_sdoctype_row, parent, false)

        return ViewHolder(view, this)
    }



    private fun addCodeIntoSelectedCodes(index: Int) {

        val selectedCode = mData?.get(index)?.asJsonObject?.get("code")?.asString

        if(selectedItem.size() > 0)
        {
            for(i in 0 until selectedItem.size())
            {
                var strCode = selectedItem.get(i).asJsonObject.get("code").asString
                if(strCode.equals(selectedCode,true))
                {
                    if(isMultiSelectOn) selectedItem.remove(i) else selectedItem = JsonArray()
                }
                else
                {
                    val strName = mData?.get(index)?.asJsonObject?.get("name")?.asString

                    var obj = JsonObject()
                    obj.addProperty("name",strName?.trim())
                    obj.addProperty("code",selectedCode?.trim())

                    if(isMultiSelectOn) selectedItem.add(obj) else { selectedItem = JsonArray(); selectedItem.add(obj)}
                }
            }
        }
        else
        {
            val strName = mData?.get(index)?.asJsonObject?.get("name")?.asString

            var obj = JsonObject()
            obj.addProperty("name",strName?.trim())
            obj.addProperty("code",selectedCode?.trim())

            if(isMultiSelectOn) selectedItem.add(obj) else { selectedItem = JsonArray(); selectedItem.add(obj)}
        }

        notifyItemRangeChanged(0, if(mData == null) 0 else mData!!.size())
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

    fun setSelectedItems(items: JsonArray)
    {
        selectedItem = items
    }

    fun addAllBtn()
    {
        var arrList = JsonArray()
        arrList.let {
            var objItem = JsonObject()
            objItem.addProperty("name",context.getString(R.string.sdoctype_all))
            objItem.addProperty("code","-1")
            it.add(objItem)

            arrList.addAll(mData)
        }

        this.mData = arrList
    }


    fun setItem(selectedItem: JsonArray)
    {
        this.selectedItem = selectedItem
    }


    override fun onRemove(index: Int) {

    }

    // Replace the contents of a ja.view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the ja.view with that element
        var strSDocTypeName:String? =  null//mData?.get(position).asJsonObject.get("name").toString()
        var strSDocTypeCode:String? =  null//mData?.get(position).asJsonObject.get("code").toString()

        mData?.run {
            var name = get(position).asJsonObject.get("name").asString.trim()
            var code = get(position).asJsonObject.get("code").asString.trim()

            strSDocTypeName = name
            strSDocTypeCode = try{if(code.toInt() == -1) "" else "($code)"} catch (e:Exception){"($code)" }
        }

        holder.view_textSDocTypeName.text = strSDocTypeName
        holder.view_textSDocTypeCode.text = strSDocTypeCode

        val selectedCode = mData?.get(position)?.asJsonObject?.get("code")?.asString

        if(selectedItem.size() > 0)
        {
            for(i in 0 until selectedItem.size())
            {
                var strCode = selectedItem.get(i).asJsonObject.get("code").asString
                if(strCode.equals(selectedCode, true))
                {
                    holder.view_imageSDocTypeChecked.visibility = View.VISIBLE
                    setTabBtnTintForLowAPI(holder.view_imageSDocTypeChecked)
                    if(!isMultiSelectOn)
                    {
                        break
                    }
                }
                else
                {
                    holder.view_imageSDocTypeChecked.visibility = View.GONE
                }
            }
        }


    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount():Int {
        var nRes = 0
        mData?.run {
            nRes = size()
        }

        return nRes
    }


    override fun getFilter(): Filter {

        if(mSDocTypeFilter == null) {
            mSDocTypeFilter = SDocTypeFilter(mData, this)
        }

        return mSDocTypeFilter as Filter
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




    inner class SDocTypeFilter(var mData:JsonArray?, var adapter: SDocTypeAdapter) : Filter() {


        //Filter list
        override fun performFiltering(constraint: CharSequence?): FilterResults {

            var result = FilterResults()

            var jsonFilteredArray = JsonArray()

            if(!constraint.isNullOrBlank() && mData != null)
            {
                var char = constraint.toString().toUpperCase()
                var filteredList : JsonArray

                for(element in mData!!)
                {
                    var strSDocTypeName = element.asJsonObject.get("name").toString().toUpperCase()
                    var strSDOcTypeCode = element.asJsonObject.get("code").toString().toUpperCase()

                    if(strSDocTypeName.contains(char) || strSDOcTypeCode.contains(char))
                    {
                        jsonFilteredArray.add(element)
                    }
                }

                result.count= jsonFilteredArray.size()
                result.values=jsonFilteredArray
            }
            else
            {
                result.count  = if(mData != null) mData!!.size() else 0
                result.values = mData
            }

            return result
        }

        //return filtered list
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            adapter.mData= results?.values as JsonArray
            adapter.notifyDataSetChanged()
        }
    }
}




