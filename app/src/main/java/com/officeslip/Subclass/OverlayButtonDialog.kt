package com.officeslip.Subclass

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import com.sgenc.officeslip.R
import android.widget.LinearLayout
import com.google.gson.JsonObject
import android.view.*
import kotlinx.android.synthetic.main.dilaog_layout_bg.*
import android.view.WindowManager
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button


class OverlayButtonDialog(context: Context) : Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
{

    private var callBack: OnClickButton? = null
    private var objBtns:JsonObject? = null
    private var strLayoutTItle:String? = null


    interface OnClickButton {
        fun onClickButton(dialog:Dialog, strTag:String)
    }

    fun setOnClickButtonListener(listener: OnClickButton) {
        this.callBack = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.dilaog_layout_bg)
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        window.attributes.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes.dimAmount = 0.5f
        window.setGravity(Gravity.CENTER)



        setUI()

    }

    fun setUI() {


        var nTitleId = 9999

        //Set title
        val parent = findViewById<View>(R.id.view_layoutBtnDialog) as ViewGroup
        var viewTitle = (LayoutInflater.from(context).inflate(R.layout.dialog_btn_default, parent, false) as Button).apply {
            text = strLayoutTItle
            id = nTitleId
            background = ContextCompat.getDrawable(context, R.drawable.rounded_bg_top)
            setTextColor(ContextCompat.getColor(context, R.color.colorUISectionTitle))
        }
        viewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f)

        (view_layoutBtnDialog as LinearLayout).addView(viewTitle)

        //Set btn listener
        var nId = 0
        objBtns?.run {
            for(strTag in keySet())
            {
                var strBtnTitle = get(strTag)

                var viewBtn = (LayoutInflater.from(context).inflate(R.layout.dialog_btn_default, parent, false) as Button).apply {
                    tag = strTag
                    text = strBtnTitle.asString
                    id = nId
                    if(nId+1 == objBtns?.size())
                    {
                        background = ContextCompat.getDrawable(context, R.drawable.rounded_bg_bottom)
                    }
                    else {
                        background = ContextCompat.getDrawable(context, R.drawable.rounded_bg_middle)
                    }
                    setOnClickListener {
                        callBack?.onClickButton(this@OverlayButtonDialog,it.tag.toString())
                    }
                }

                (view_layoutBtnDialog as LinearLayout).addView(viewBtn)
                nId++
            }
        }

        //Set cancel btn listener
        view_btnCancel.setOnClickListener {
            callBack?.onClickButton(this@OverlayButtonDialog,it.tag.toString())
        }
        view_layoutBtnDialog.setOnClickListener(View.OnClickListener {
            dismiss()
        })


    }
    fun setButton(strTitle:String, objButton:JsonObject) {
        //view_layoutParent
        strLayoutTItle = strTitle
        objBtns = objButton
    }
}