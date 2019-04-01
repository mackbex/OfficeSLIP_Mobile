package com.officeslip.Adapter

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sgenc.officeslip.*
import com.officeslip.Listener.ViewHolderClickListener
import com.officeslip.SDOC_STEP_COLOR_0
import com.officeslip.SDOC_STEP_COLOR_2
import com.officeslip.SDOC_STEP_COLOR_9
import com.officeslip.Util.Common

class StatisticsAdapter(val context: Context, data: JsonArray?) : RecyclerView.Adapter<StatisticsAdapter.ViewHolder>(), ViewHolderClickListener {


    var mData: JsonArray? = JsonArray()
    var mInflater: LayoutInflater
    private var callBack: SearchSlip? = null
    private var m_C = Common()

    init {
        mData = data
        mInflater = LayoutInflater.from(context)
    }

    interface SearchSlip {
        fun searchSlip(objItem:JsonObject)
    }

    fun setSearchSlipListener(listener: SearchSlip) {
        this.callBack = listener
    }

    //Define viewholder
    class ViewHolder(itemView: View, val listener: ViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {

        var view_cardStatsList:CardView
        var view_textItemTitle:TextView
        var view_textTitleStep0:TextView
        var view_textValueStep0:TextView
        var view_textTitleStep1:TextView
        var view_textValueStep1:TextView
        var view_textTitleStep2:TextView
        var view_textValueStep2:TextView
        var view_textTitleStep9:TextView
        var view_textValueStep9:TextView

        init {
            view_cardStatsList = itemView.findViewById(R.id.view_cardStatsList)
            view_textItemTitle = itemView.findViewById(R.id.view_textItemTitle)
            view_textTitleStep0 = itemView.findViewById(R.id.view_textTitleStep0)
            view_textValueStep0 = itemView.findViewById(R.id.view_textValueStep0)
            view_textTitleStep1 = itemView.findViewById(R.id.view_textTitleStep1)
            view_textValueStep1 = itemView.findViewById(R.id.view_textValueStep1)
            view_textTitleStep2 = itemView.findViewById(R.id.view_textTitleStep2)
            view_textValueStep2 = itemView.findViewById(R.id.view_textValueStep2)
            view_textTitleStep9 = itemView.findViewById(R.id.view_textTitleStep9)
            view_textValueStep9 = itemView.findViewById(R.id.view_textValueStep9)

            view_cardStatsList.setOnClickListener(this)
            view_cardStatsList.setOnTouchListener(this)
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when(event?.action)
            {
                MotionEvent.ACTION_DOWN -> {
                    v?.findViewWithTag<TextView>("THUMB_TITLE")?.setBackgroundColor(ContextCompat.getColor(v.context, R.color.colorPrimarySuperLight))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v?.findViewWithTag<TextView>("THUMB_TITLE")?.setBackgroundColor(ContextCompat.getColor(v.context, R.color.colorBtnNormal))

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.recycle_statistics_row, parent, false)
        return ViewHolder(view, this)
    }

    override fun getItemCount(): Int {
        var nRes = 0
        mData?.let {
            nRes = it.size()
        }

        return nRes
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        mData?.apply {


            var objItem = get(position).asJsonObject
            var strCabinet = objItem.get("CABINET")?.asString?.trim()
            var nStep0 = if(m_C.isBlank(objItem.get("STEP0").asString)) 0 else objItem.get("STEP0").asInt
            var nStep1 = if(m_C.isBlank(objItem.get("STEP1").asString)) 0 else objItem.get("STEP1").asInt
            var nStep2 = if(m_C.isBlank(objItem.get("STEP2").asString)) 0 else objItem.get("STEP2").asInt
            var nStep9 = if(m_C.isBlank(objItem.get("STEP9").asString)) 0 else objItem.get("STEP9").asInt

            strCabinet?.apply {
                holder.view_textItemTitle.text = context.getString(R.string.date_format_yyyymmdd,
                        this.substring(0,4), this.substring(4,6), this.substring(6,8))
            }

            holder.view_textTitleStep0?.apply {
                setTextColor(Color.parseColor(SDOC_STEP_COLOR_0))
                text = context.getString(R.string.sdoc_step_0)
            }
            holder.view_textValueStep0.text = (nStep0 + nStep1).toString()


            holder.view_textTitleStep2?.apply {
                setTextColor(Color.parseColor(SDOC_STEP_COLOR_2))
                text = context.getString(R.string.sdoc_step_2)
            }
            holder.view_textValueStep2.text = nStep2.toString()

            holder.view_textTitleStep9?.apply {
                setTextColor(Color.parseColor(SDOC_STEP_COLOR_9))
                text = context.getString(R.string.sdoc_step_9)
            }
            holder.view_textValueStep9.text = nStep9.toString()
        }
    }

    override fun onLongTap(index: Int) {
        mData?.let {
            callBack?.searchSlip(it.get(index).asJsonObject)
           // callBack?.showOriginal(it.get(index).asJsonObject)
        }
    }

    override fun onTap(index: Int) {
        mData?.let {
            callBack?.searchSlip(it.get(index).asJsonObject)
        }
    }

    override fun onRemove(index: Int) {
    }

}