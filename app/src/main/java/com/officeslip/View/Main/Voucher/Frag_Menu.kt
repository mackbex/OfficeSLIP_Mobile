package com.officeslip.View.Main.Voucher

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.balysv.materialripple.MaterialRippleLayout
import com.google.gson.JsonObject
import com.officeslip.Listener.TileClickListener
import com.officeslip.Util.Common
import com.officeslip.px
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.fragment_tile_menu.*


class Frag_Menu : Fragment(), View.OnClickListener
{
    private val m_C: Common = Common()
    private var currentMenuTAG = ""
    private var callBack_tileButtonClick:TileClickListener? = null

    /**
     add interface onto parent fragement
     */

    fun setOnTileButtonClickListener(listener : TileClickListener)
    {
        this.callBack_tileButtonClick = listener
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_tile_menu, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.apply {
            currentMenuTAG = getString("tag")
        }

        setupViewUI()
    }


    /**
     * Draw current activity UI
     */

    private fun setupViewUI() {

        (parentFragment as? Frag_Voucher)?.arObjMenuList?.apply {
            m_C.getChildJsonArray(this, currentMenuTAG, "tag", "submenu")?.run {

                //Draw button menu
                for (menu in this) {
                    drawButton(menu as JsonObject)
                }
            }
        }
    }

    /**
     * Draw tile button
     */

    private fun drawButton(objButtonAttr : JsonObject) {

        val ripple = MaterialRippleLayout(context)
        ripple.apply {
            setRippleOverlay(true)
        }

        LayoutInflater.from(activity).inflate(R.layout.layout_voucher_menu, null)?.apply {

            //Set ripple layout params
            ripple.layoutParams = GridLayout.LayoutParams().apply {

                val span = if(objButtonAttr.get("span") == null) 1f else objButtonAttr.get("span").asFloat
                var colWidth = (m_C.getDisplayWidth(activity) * (span / view_layoutUICell.columnCount.toFloat())).toInt()

                //apply colspan if spanSize is more than 1
                if(view_layoutUICell.columnCount == span.toInt())
                {
                    columnSpec = GridLayout.spec(GridLayout.HORIZONTAL,2)
                }

                width = colWidth
                height = 100.px
                setBackgroundColor(Color.parseColor("#"+objButtonAttr.get("bg").asString)) //= ContextCompat.getColor(context,C)

                val paddingVal = resources.getDimension(R.dimen.tile_menu_padding).toInt()
                setMargins(paddingVal,paddingVal,paddingVal,paddingVal)
            }

            //Set button layout params
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)

            findViewWithTag<ImageView>("BTN_ICON")?.apply {

                val iconName = objButtonAttr.get("icon").asString
                val id = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                setImageResource(id)
           //     drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
               // setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_add))
            }


            findViewWithTag<TextView>("BTN_TITLE")?.apply {
                text = objButtonAttr.get("title").asString
                setTextColor(Color.WHITE)
            }

            //Add onclick listener
            setOnClickListener {
                callBack_tileButtonClick?.onTileButtonClick(objButtonAttr)
            }

            //Add button to ripple layout
            ripple.addView(this)

            view_layoutUICell.addView(ripple)

        }
    }


    /**
     * pass button click event to parent listener
     */
    override fun onClick(v: View?) {

    }

}