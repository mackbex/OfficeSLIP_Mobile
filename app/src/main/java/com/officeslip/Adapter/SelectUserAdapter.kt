package com.officeslip.Adapter

import android.content.Context
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
import com.sgenc.officeslip.R
import com.officeslip.Util.Common
import com.officeslip.Util.SQLite


class SelectUserAdapter(val context: Context, data: JsonArray?, var strType: String) : RecyclerView.Adapter<SelectUserAdapter.ViewHolder>(), SelectUserClickListener {

    var mData: JsonArray? = JsonArray()
    var mInflater: LayoutInflater
    var m_objSelectedUser:JsonObject? = null
    var m_C = Common()

    init {
        mData = data
        mInflater = LayoutInflater.from(context)
    }


    class ViewHolder(itemView:View, val listener: SelectUserClickListener) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {
        var view_textUserID:TextView
        var view_textUserName:TextView
        var view_imageUserChecked:ImageView
        var view_layout:ConstraintLayout
        var view_btnRemoveHistory:Button

        init {
            view_textUserID             = itemView.findViewById(R.id.view_textUserID)
            view_textUserName           = itemView.findViewById(R.id.view_textUserName)
            view_imageUserChecked       = itemView.findViewById(R.id.view_imageUserChecked)
            view_layout                 = itemView.findViewById(R.id.view_layout)
            view_btnRemoveHistory       = itemView.findViewById(R.id.view_btnRemoveHistory)

            view_btnRemoveHistory.setOnClickListener(this)
            view_layout.setOnClickListener(this)
         //   view_layout.setOnLongClickListener(this)
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
            when(v?.id)
            {
                view_btnRemoveHistory.id -> {
                    listener.onRemoveFavorite()
                }
                else -> {
                    listener.onTap(adapterPosition)
                }
            }

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

        var nPrevIdx = -1

        m_objSelectedUser?.get("POSITION")?.run {
            nPrevIdx = this.asInt
        }
        m_objSelectedUser = JsonObject().apply {
            addProperty("USER_ID", mData?.get(index)?.asJsonObject?.get("USER_ID")?.asString)
            addProperty("USER_NAME", mData?.get(index)?.asJsonObject?.get("USER_NAME")?.asString)
            addProperty("PART_CD", mData?.get(index)?.asJsonObject?.get("PART_CD")?.asString)
            addProperty("CO_CD", mData?.get(index)?.asJsonObject?.get("CO_CD")?.asString)
            addProperty("PART_NM", mData?.get(index)?.asJsonObject?.get("PART_NM")?.asString)
            addProperty("CO_NAME", mData?.get(index)?.asJsonObject?.get("CO_NAME")?.asString)
            addProperty("POSITION", index.toString())
        }
        notifyItemChanged(nPrevIdx)
        notifyItemChanged(index)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.recycle_search_user_row, parent, false)

        return ViewHolder(view, this)
    }


    fun setItem(selectedItem: JsonArray?)
    {
        selectedItem?.run {
            mData = this
            notifyDataSetChanged()
        }
    }

    fun setFavoriteItem(arItem: JsonArray?)
    {
        arItem?.run {
            mData = this
            var objRemoveHistory = JsonObject()
            objRemoveHistory.addProperty("BTN","REMOVE")
            mData?.add(objRemoveHistory)
            notifyDataSetChanged()
        }
    }

    override fun onRemoveFavorite() {
        var strTableName = ""
        when(strType.toUpperCase())
        {
            "COPY" -> {
                strTableName = "FAVORITE_COPY_T"
            }
            "MOVE" -> {
                strTableName = "FAVORITE_MOVE_T"
            }
        }

        var bSuccess = SQLite(context).setRecord("DROP TABLE IF EXISTS $strTableName", null)
        if(bSuccess)
        {
            mData = JsonArray()
            notifyDataSetChanged()
        }
    }


    override fun onRemove(index: Int) {

    }

    // Replace the contents of a ja.view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the ja.view with that element
        var strUserName:String? =  null//mData?.get(position).asJsonObject.get("name").toString()
        var strUserID:String? =  null//mData?.get(position).asJsonObject.get("code").toString()

        mData?.run {

            get(position).asJsonObject.get("BTN")?.apply {
                holder.view_btnRemoveHistory.visibility = View.VISIBLE
                holder.view_textUserName.visibility = View.GONE
                holder.view_textUserID.visibility = View.GONE
                holder.view_imageUserChecked.visibility = View.GONE

                return
            }

            holder.view_btnRemoveHistory.visibility = View.GONE
            holder.view_textUserName.visibility = View.VISIBLE
            holder.view_textUserID.visibility = View.VISIBLE
            holder.view_imageUserChecked.visibility = View.VISIBLE

            var strSelectedUserName = get(position).asJsonObject.get("USER_NAME").asString.trim()
            var strSelectedUserID = get(position).asJsonObject.get("USER_ID").asString.trim()

            strUserName = strSelectedUserName
            strUserID = "($strSelectedUserID)"

            holder.view_textUserName.text = strUserName
            holder.view_textUserID.text = strUserID

            var strPrevSelectedIdx = m_objSelectedUser?.get("POSITION")?.asString

            if(m_C.isBlank(strPrevSelectedIdx))
            {
                strPrevSelectedIdx = "-1"
            }

            if(position == strPrevSelectedIdx?.toInt())
            {
                holder.view_imageUserChecked.visibility = View.VISIBLE
                setTabBtnTintForLowAPI(holder.view_imageUserChecked)
            }
            else
            {
                holder.view_imageUserChecked.visibility = View.GONE
            }
        }


//        val selectedCode = mData?.get(position)?.asJsonObject?.get("code")?.asString
//
//        if(selectedItem.size() > 0)
//        {
//            for(i in 0 until selectedItem.size())
//            {
//                var strCode = selectedItem.get(i).asJsonObject.get("code").asString
//                if(strCode.equals(selectedCode, true))
//                {
//                    holder.view_imageSDocTypeChecked.visibility = View.VISIBLE
//                    setTabBtnTintForLowAPI(holder.view_imageSDocTypeChecked)
//                    if(!isMultiSelectOn)
//                    {
//                        break
//                    }
//                }
//                else
//                {
//                    holder.view_imageSDocTypeChecked.visibility = View.GONE
//                }
//            }
//        }


    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount():Int {
        var nRes = 0
        mData?.run {
            nRes = size()
        }

        return nRes
    }




    fun setTabBtnTintForLowAPI(view:View) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {

            (view as ImageView)?.apply {
                var color = ContextCompat.getColor(context, R.color.colorPrimary)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }
    }


}


interface SelectUserClickListener
{
    fun onLongTap(index : Int)
    fun onTap(index : Int)
    fun onRemove(index: Int)
    fun onRemoveFavorite()
}



