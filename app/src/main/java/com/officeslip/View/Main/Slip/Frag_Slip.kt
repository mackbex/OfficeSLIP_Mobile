package com.officeslip.View.Main.Slip

import android.app.Activity
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.balysv.materialripple.MaterialRippleLayout
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.officeslip.Adapter.MainViewPageAdapter
import com.officeslip.Listener.OnNavigationListener
import com.officeslip.Listener.TileClickListener
import com.officeslip.MainActivity
import com.officeslip.Util.Common
import com.officeslip.px
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.fragment_slip.*
import java.io.InputStream
import java.util.*

class Frag_Slip : Fragment(), View.OnClickListener, Frag_Statistics.SearchSelectedItemListener, TileClickListener
{
    private var m_C = Common()
    private var callback_navigation:OnNavigationListener? = null
    var arObjMenuList: JsonArray? = null
    private val directory: Stack<JsonObject> = Stack()

    companion object {
        const val TAB_ADD_SLIP = "ADD_SLIP"
        const val TAB_SEARCH_SLIP = "SEARCH_SLIP"
        const val TAB_SEARCH_ATTACH = "SEARCH_ATTACH"
        const val TAB_STATISTICS = "STATISTICS"
        const val TAB_SLIP_MENU = "SLIP_MENU"
    }

    /**
     * interface for add side navigation.
     */

    fun setNavigationListener(callback : OnNavigationListener) {
        this.callback_navigation = callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.fragment_slip, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        praseMenuList()
        setupViewUI()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(isVisibleToUser)
        {
            activity?.run {
                setNavigationView()
            }
        }
    }

    //Listener of searching slip from statistic fragment.
    override fun searchSelectedSlip(objItem: JsonObject) {

        for(i in 0 until view_pagerSlip.childCount)
        {

            if(view_pagerSlip.getChildAt(i).tag == "MAIN_SEARCH_SLIP")
            {
                view_pagerSlip.setCurrentItem(i, false)
              //  view_naviBottom.selectedItemId = R.id.action_dashboard
                (view_pagerSlip.adapter as MainViewPageAdapter).searchSelectedSlip(objItem)
                break
            }
        }
    }

    /**
     * parse menulist from asset
     */
    private fun praseMenuList()  {
        arObjMenuList = m_C.parseMenuList(activity as Activity, "common/SLIP.json")
    }

    /**
     * Draw side navigation view
     */
    fun setNavigationView() {
        //setNavigationListener(this@Frag_Voucher)
        activity?.assets?.open("common/NAVIGATION.json")?.run {
            callback_navigation?.onAddNavigation(this)
        }

     //   drawTopNavi(directory, view_layoutNavi)
    }

    /**
     * Draw view top navigation
     */

    private fun drawTopNavi(directory:Stack<JsonObject>, viewTarget:LinearLayout) {

        viewTarget.removeAllViews()

        var nIdx = 0
        for(item in directory)
        {
            val tag = item.get("tag")?.asString
            val title = if(item.get("title") == null) "" else item.get("title").asString

            val layoutNaviBtn = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
                setTag(tag)

                //Add icon
                ImageView(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(20.px, 20.px)

                    val iconName = item.get("icon").asString
                    val id = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                    setImageDrawable(ContextCompat.getDrawable(context, id))
                    val drawable = drawable.mutate()
                    drawable.setColorFilter(ContextCompat.getColor(context,R.color.naviBtnTint), PorterDuff.Mode.SRC_IN)
                    setPadding(5.px,0, 0,0)
                    gravity = Gravity.CENTER_VERTICAL

                    addView(this)
                    //      background = (ContextCompat.getDrawable(context, R.color.Black))
                }

                //Add title
                TextView(activity).apply {
                    text = title
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(5.px,0,5.px,0)

                    addView(this)
                }

                //Add arrow icon to left of navi button
                if(nIdx < directory.size - 1)
                {
                    ImageView(activity).apply {
                        layoutParams = LinearLayout.LayoutParams(19.px, 19.px)
                        //    gravity = Gravity.CENTER_VERTICAL
                        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.arrow))
                        setPadding(5.px,0, 5.px,0)
                        gravity = Gravity.CENTER_VERTICAL
                        //      background = (ContextCompat.getDrawable(context, R.color.Black))

                        addView(this)
                    }
                }

                setOnClickListener{

                    while(!directory.empty())
                    {
                        val objItem = directory.peek()
                        val tagNavi = objItem.get("tag").asString
                        if(tagNavi == it.tag)
                        {
                            moveSection(objItem)
                           // attachChildFrag(objItem)
                            break
                        }
                        else
                        {
                            directory.pop()
                        }
                    }
                }
                nIdx++
            }

            //Add ripple layout
            val ripple = MaterialRippleLayout(context)
            ripple.addView(layoutNaviBtn)

            viewTarget.addView(ripple)

            //scrollview move to right
            if(nIdx == directory.size - 1) {
                (viewTarget.parent as? HorizontalScrollView)?.apply {
                    addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                        fullScroll(HorizontalScrollView.FOCUS_RIGHT)
                    }
                }
            }
        }
    }

    private fun setupViewUI()
    {
        view_pagerSlip.apply {
            swipeEnable = false
            offscreenPageLimit = 4

            adapter = MainViewPageAdapter(childFragmentManager).apply {
                addFragment(Frag_Menu().apply {
                    setOnTileButtonClickListener(this@Frag_Slip)
                }, TAB_SLIP_MENU)
                addFragment(Frag_AddSlip(), TAB_ADD_SLIP)
                addFragment(Frag_SearchSlip(), TAB_SEARCH_SLIP)
                addFragment(Frag_SearchAddFile(), TAB_SEARCH_ATTACH)
                addFragment(Frag_Statistics(), TAB_STATISTICS)
            }
        }

        view_btnNavi.setOnClickListener(this)

        //push current path to stack
        arObjMenuList?.run {
            directory.push(get(0).asJsonObject)
            moveSection(get(0).asJsonObject)
        }
    }

    override fun onClick(v: View?) {
        val strTag = v?.tag.toString().toUpperCase()
        when (strTag) {
            "OPEN_NAVI" -> {
                callback_navigation?.onToggleNavigation()
            }
        }
    }

    /**
     * Set Viewpager by TAG
     */
    fun setCurrentViewpagerItem(tag:String, objAttr:JsonObject) {
        val nIdx = (view_pagerSlip.adapter as MainViewPageAdapter).getFragItemPosition(tag)

        if(nIdx != view_pagerSlip.currentItem) view_pagerSlip.setCurrentItem(nIdx, false)

        //Draw top navi
        directory.clear()
        arObjMenuList?.run {
            directory.push(get(0).asJsonObject)
        }

        if(tag != TAB_SLIP_MENU) directory.push(objAttr)

     //   drawTopNavi(directory, view_layoutNavi)

        //Set toolbar title
        toolbar_title.text = objAttr.get("title")?.asString

    }

    override fun onTileButtonClick(objAttr: JsonObject) {
        moveSection(objAttr)
    }

    private fun moveSection(objAttr:JsonObject) {

        val tag = objAttr.get("tag").asString
        var viewTopNavi:LinearLayout? = null
        when(tag.toUpperCase())
        {
            "SLIP_MENU" -> {
                setCurrentViewpagerItem(TAB_SLIP_MENU, objAttr)
                view_toolbarSlip.visibility = View.VISIBLE
                view_scrollNavi.visibility = View.VISIBLE

                viewTopNavi = view_layoutNavi
            }
            "ADD_SLIP" -> {
                setCurrentViewpagerItem(TAB_ADD_SLIP, objAttr)
                view_toolbarSlip.visibility = View.GONE
                view_scrollNavi.visibility = View.GONE


            }
            "SEARCH_SLIP" -> {
                setCurrentViewpagerItem(TAB_SEARCH_SLIP, objAttr)
                view_toolbarSlip.visibility = View.GONE
                view_scrollNavi.visibility = View.GONE
            }
            "SEARCH_ATTACH" -> {
                setCurrentViewpagerItem(TAB_SEARCH_ATTACH, objAttr)
                view_toolbarSlip.visibility = View.GONE
                view_scrollNavi.visibility = View.GONE
            }
            "STATISTICS" -> {
                setCurrentViewpagerItem(TAB_STATISTICS, objAttr)
                view_toolbarSlip.visibility = View.GONE
                view_scrollNavi.visibility = View.GONE
            }
        }

        if(viewTopNavi == null)
        {
            viewTopNavi = (view_pagerSlip.adapter as MainViewPageAdapter).getCurrentFragment()?.view?.findViewWithTag("NAVI_TOP")
        }

        viewTopNavi?.run {
            drawTopNavi(directory, this)
        }

    }

}