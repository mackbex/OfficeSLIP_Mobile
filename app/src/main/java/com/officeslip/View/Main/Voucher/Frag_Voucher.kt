package com.officeslip.View.Main.Voucher

import android.app.Activity
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.balysv.materialripple.MaterialRippleLayout
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.officeslip.Util.Common
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.fragment_voucher.*
import com.officeslip.Adapter.MainViewPageAdapter
import com.officeslip.Listener.OnNavigationListener
import com.officeslip.Listener.TileClickListener
import com.officeslip.px
import java.io.InputStream
import java.util.*


class Frag_Voucher : Fragment(), View.OnClickListener, TileClickListener
{
    companion object {
       // const val TAB_VOUCHER_INIT = "VOUCHER_HOME"
    }

    private val m_C: Common = Common()
    private val directory:Stack<JsonObject> = Stack()
    var arObjMenuList:JsonArray? = null
    private var callback_navigation:OnNavigationListener? = null
    private var callback_openURL:OnOpenURLListener? = null

    /**
     * interface for add side navigation.
     */

    fun setNavigationListener(callback : OnNavigationListener) {
        this.callback_navigation = callback
    }

    /**
     *  interface for open url webview.
     */

    interface OnOpenURLListener {
        fun onOpenURL(url:String)
    }
    fun setOnOpenURLListener(callback : OnOpenURLListener) {
        this.callback_openURL = callback
    }

    /**
     * inter
     */


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_voucher, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Restore upload info when screen rotation.
        savedInstanceState?.apply {
       //     currentTAB = getString("CURRENT_TAB")
        }

        praseMenuList()
        setupViewUI()

    }

    /**
     * parse menulist from asset
     */
    private fun praseMenuList()  {
        arObjMenuList = m_C.parseMenuList(activity as Activity, "common/VOUCHER.json")
    }


    /**
     * Draw side navigation view
     */
    fun setNavigationView() {
        //setNavigationListener(this@Frag_Voucher)
        activity?.assets?.open("common/NAVIGATION.json")?.run {
            callback_navigation?.onAddNavigation(this)
        }

    }

    /**
     * Get parent nodes using recursion
     */
//    var found:Boolean = false
//    fun findNodeByTag(arNestedNodes:ArrayList<JsonObject>, arObjMenu:JsonArray?, tag:String) {
//
//      //  var parentNode:JsonObject? = null
//        arObjMenu?.forEach {
//            val curObj = it.asJsonObject
//
//            if(curObj.get("tag")?.asString == tag)
//            {
//                arNestedNodes.add(curObj)
//                found = true
//                return
//            }
//
//            curObj.get("submenu")?.asJsonArray?.run {
//                if(found) return
//
//                arNestedNodes.add(curObj)
//                findNodeByTag(arNestedNodes, this, tag)
//                if(!found)
//                {
//                    arNestedNodes.removeAt(arNestedNodes.size - 1)
//                }
//            }
//        }
//    }

    fun moveToSection(tag:String, btnTag:String) {

        //Draw top navigation
        directory.clear()

        //Add full node to top navi
        val arSplitTag = tag.split(';')
        arSplitTag?.forEach {
            arObjMenuList?.run {
                findNodeByTag(it, this)?.run {
                    directory.push(this)
                }
            }
        }

        val objFragAttr = arObjMenuList?.run {
            findNodeByTag(btnTag, this)?.apply {
                directory.push(this)
            }
        }

        //Draw fragment
        drawTopNavi(directory)
        objFragAttr?.run {
            attachChildFrag(this)
        }
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

    /**
     * get item menu count
     */
    private fun getMenuCount(arObjjList : JsonArray?):Int {

        var nMenuCnt = 0

        (arObjjList?.get(0) as? JsonObject)?.run {

            var arObjSectionItem = getAsJsonArray("submenu")
//            currentTAB  = get("tag").asString

            for (item in arObjSectionItem) {
                val arObjSubBtn = (item as JsonObject).get("submenu").asJsonArray
                nMenuCnt += arObjSubBtn.size()
            }
        }
        return nMenuCnt
    }

    private fun setupViewUI()
    {
        val menuCnt = getMenuCount(arObjMenuList)

        arObjMenuList?.run {

            view_pagerVoucher.apply {
                swipeEnable = false
                offscreenPageLimit = menuCnt
            }

            //push current path to stack
            directory.push(get(0).asJsonObject)
            //add child fragment to viewpager
            attachChildFrag(get(0).asJsonObject)

            drawTopNavi(directory)
        }

        view_btnNavi.setOnClickListener(this)
    }

    /**
     * Attach child fragment
     */
    private fun attachChildFrag(objAttr: JsonObject) {
        view_pagerVoucher.apply {

            if(adapter == null) {
                adapter = MainViewPageAdapter(childFragmentManager)
            }

            (adapter as MainViewPageAdapter).apply {

                val hasSubmenu = objAttr.get("submenu") != null
                val tag = objAttr.get("tag")?.asString

                val curFragPosition = getFragItemPosition(tag)

                if(curFragPosition == MainViewPageAdapter.NONE_EXISTS) {
                    var frag: Fragment? = null

                    if (hasSubmenu) {
                        frag = Frag_Menu().apply {
                            setOnTileButtonClickListener(this@Frag_Voucher)
                        }
                    } else {
                        frag = Frag_WebView().apply {
                            setOnOpenURLListener(this)

                            objAttr.get("url")?.run {
                                callback_openURL?.onOpenURL(this.asString)
                            }
                        }
                    }

                    addFragment(frag, tag)
                }

                setCurrentItem(getFragItemPosition(tag), false)
                drawTopNavi(directory)

                //Set toolbar title
                toolbar_title.text = objAttr.get("title")?.asString
            }
        }
    }

    /**
     * Receive event from child fragment
     */

    override fun onTileButtonClick(objAttr: JsonObject) {
        //push current path to stack
        directory.push(objAttr)
        attachChildFrag(objAttr)
    }

    fun findNodeByTag(tag: String, arObjMenu:JsonArray) : JsonObject? {

        arObjMenu.forEach {
            val objItem = it.asJsonObject

            val itemValue = objItem.get("tag").asString
            if (itemValue == tag)
                return objItem

            // Item not returned yet. Search its children by recursive call.
            if (objItem.get("tag") != null)
            {
                objItem.get("submenu")?.run {
                    val subresult = findNodeByTag(tag, this.asJsonArray)

                    // If the item was found in the subchildren, return it.
                    if (subresult != null)
                        return subresult
                }

            }
        }

        return null
    }

    /**
     * Draw view top navigation
     */

    private fun drawTopNavi(directory:Stack<JsonObject>) {

        view_layoutNavi.removeAllViews()

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
                            attachChildFrag(objItem)
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

            view_layoutNavi.addView(ripple)

            //scrollview move to right
            view_scrollNavi.apply {
                addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                    fullScroll(HorizontalScrollView.FOCUS_RIGHT)
                }
            }
        }
    }


    override fun onClick(v: View?) {
        val strTag = v?.tag.toString().toUpperCase()
        when (strTag) {
            "OPEN_NAVI" -> {
                callback_navigation?.onToggleNavigation()
            }
            else -> {
               // setCurrentViewpagerItem(strTag, v as? ImageView)
            }
        }
    }

}