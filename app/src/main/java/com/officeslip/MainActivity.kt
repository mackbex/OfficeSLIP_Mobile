package com.officeslip

import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView

import com.officeslip.Util.Common
import com.officeslip.Util.UserDefault
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.util.Log
import com.officeslip.Adapter.MainViewPageAdapter
import com.officeslip.Util.Initialize
import com.officeslip.View.Main.*
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import com.officeslip.View.Main.Frag_Dashboard
import com.officeslip.View.Main.Slip.Frag_Slip
import com.officeslip.View.Main.Voucher.Frag_Voucher
import android.widget.GridLayout
import android.widget.TextView
import com.balysv.materialripple.MaterialRippleLayout
import com.google.gson.JsonParser
import com.officeslip.Listener.OnNavigationListener
import java.io.InputStream


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, OnNavigationListener {

    private val m_UserDefault: UserDefault = UserDefault(this@MainActivity)
    private val m_C: Common = Common()
    private var mDrawerToggle: ActionBarDrawerToggle? = null


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        const val RESULT_APP_LOGGED = 1001
        const val RESULT_USER_LOGGED = 1002
        const val NETWORK_DISABLED = 1004
        const val RESULT_PIN_VERIFY = 1003
        const val RESULT_PIN_ENABLE = 1005
        const val RESULT_PIN_DISABLE = 1006
        const val TAB_DASHBOARD = "DASHBOARD"
        const val TAB_VOUCHER = "VOUCHER"
        const val TAB_SLIP = "SLIP"
        const val TAB_SETTING = "SETTING"
//        const val TAB_DASHBOARD = 1010
//        const val TAB_APPROVAL = 1011
//        const val TAB_SLIP = 1012
//        const val TAB_SETTING = 1013
        //Tab View tags
        const val TAB_ADD_SLIP = "TAB_ADD_SLIP"
        const val TAB_SEARCH_SLIP = "TAB_SEARCH_SLIP"
        const val TAB_ADD_FILE = "TAB_ADD_FILE"
        const val TAB_SLIP_STATISTICS = "TAB_SLIP_STATISTICS"
       // const val TAB_SETTING = "TAB_SETTING"



        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        m_C.removeFolder(this@MainActivity, TEMP_PATH)
        m_C.removeFolder(this@MainActivity, UPLOAD_PATH)
        m_C.removeFolder(this@MainActivity, DOWNLOAD_PATH)

        Initialize(this@MainActivity)
        setContentView(R.layout.activity_main)

       // setSupportActionBar(view_toolbar)
        setMainMenu()

    }

    override fun onStart() {
        super.onStart()

        if(g_SysInfo.bPINEnabled && !m_C.isBlank(g_SysInfo.strLockPasswordValue) && !g_SysInfo.bPINLogged)
        {
            Intent(this@MainActivity, PINActivity::class.java).run {
                this.putExtra("MODE","VERIFY")
                startActivityForResult(this, RESULT_PIN_VERIFY)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        for(i in 0 until view_naviBottom.menu.size())
        {
            if(item.itemId == view_naviBottom.menu.getItem(i).itemId)
            {
                view_pagerMain.setCurrentItem(i, false)
                break
            }
        }

        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode)
        {
            RESULT_PIN_ENABLE -> {
                if(resultCode == RESULT_OK)
                {
                    g_SysInfo.bPINEnabled = resultCode == RESULT_OK
                    if(g_SysInfo.bPINEnabled)  g_SysInfo.bPINLogged = true
                }
            }
            RESULT_PIN_DISABLE -> {
                if(resultCode == RESULT_OK)
                {
                    g_SysInfo.bPINEnabled = false
                }
            }
            RESULT_PIN_VERIFY -> {
                if(resultCode == RESULT_OK)
                {
                    g_SysInfo.bPINLogged = true
                }
                else
                {
                    this@MainActivity.finish()
                }
            }
            RESULT_USER_LOGGED -> {
                if(resultCode == RESULT_OK)
                {
                    g_UserInfo.bHasLogged = true
                    this@MainActivity.finish()
                    this@MainActivity.startActivity(intent)
                }
                else
                {
                    this@MainActivity.finish()
                }
            }
        }
    }

    /**
     * loop all subviews from given view group.
     */
    fun disableHightLightBtn(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child is ViewGroup) {
                disableHightLightBtn(child)
                // DO SOMETHING WITH VIEWGROUP, AFTER CHILDREN HAS BEEN LOOPED
            } else {
                if (child != null) {
                    // DO SOMETHING WITH VIEW
                    (child as? ImageView)?.run {
                        clearColorFilter()
                    }
                }
            }
        }
    }

    /**
     * Highlight currently selected navigation button.
     */
    fun highlightButton(viewBtn:ImageView?) {

        disableHightLightBtn(view_naviItemArea) //clear filler all button first.
        viewBtn?.setColorFilter(ContextCompat.getColor(viewBtn.context, R.color.naviBtnHighlight))
    }

    //initialize ja.view
    private fun setMainMenu() {
        //Set navigation drawer
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, R.string.nav_open, R.string.nav_close)
        drawer_layout.addDrawerListener(toggle)

        toggle.syncState()

        view_pagerMain.apply {
            swipeEnable = false
            offscreenPageLimit = 4
            adapter = MainViewPageAdapter(supportFragmentManager).apply {
                addFragment(Frag_Dashboard().apply {
                    setNavigationListener(this@MainActivity)
                }, TAB_DASHBOARD)
                addFragment(Frag_Voucher().apply {
                    setNavigationListener(this@MainActivity)
                }, TAB_VOUCHER)
                addFragment(Frag_Slip().apply {
                    setNavigationListener(this@MainActivity)
                }, TAB_SLIP)
                addFragment(Frag_Setting(), TAB_SETTING)

//                addFragment(Frag_AddSlip())
//                addFragment(Frag_SearchSlip())
//                addFragment(Frag_SearchAddFile())
//                addFragment(Frag_Statistics())
//                addFragment(Frag_Setting())
            }
        }

       // view_pagerMain.setCurrentItem((view_pagerMain.adapter as MainViewPageAdapter).getFragItemPosition(TAB_DASHBOARD), false)


        view_naviBottom.apply {
            disableShiftMode()
            setOnNavigationItemSelectedListener(this@MainActivity)

        }
    }

    private fun BottomNavigationView.disableShiftMode() {
        val menuView = getChildAt(0) as BottomNavigationMenuView
        try {
            val shiftingMode = menuView::class.java.getDeclaredField("mShiftingMode")
            shiftingMode.isAccessible = true
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.isAccessible = false
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView
                item.setShiftingMode(false)
                // set once again checked value, so ja.view will be updated
                item.setChecked(item.itemData.isChecked)
            }
        } catch (e: NoSuchFieldException) {
            Log.e(TAG, "Unable to get shift mode field", e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Unable to change value of shift mode", e)
        }
    }


//    //Listener of searching slip from statistic fragment.
//    override fun searchSelectedSlip(objItem: JsonObject) {
//
//        for(i in 0 until view_pagerMain.childCount)
//        {
//
//            if(view_pagerMain.getChildAt(i).tag == "MAIN_SEARCH_SLIP")
//            {
//                view_pagerMain.setCurrentItem(i, false)
//                view_naviBottom.selectedItemId = action_dashboard
//                (view_pagerMain.adapter as MainViewPageAdapter).searchSelectedSlip(objItem)
//            }
//        }
//    }

    /**
     * Toggle drawer menu
     */
    fun toggleNavigation() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START))
        {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else
        {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    /**
     * Change navigation drawer menu
     */
    fun setNavigationMenu(currentTab : String, viewForNaviItems:View?)
    {
        main_nav.menu.clear()
        view_naviItemArea.removeAllViews()
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        when(currentTab)
        {
            TAB_DASHBOARD -> {
                viewForNaviItems?.run {
                    //        view_naviItemArea.addView(this)
                }
           //     main_nav.inflateMenu(R.menu.activity_camera)
            }
            TAB_VOUCHER -> {
                viewForNaviItems?.run {
            //        view_naviItemArea.addView(this)
                }
            }

            TAB_SLIP -> {
                viewForNaviItems?.run {
             //       view_naviItemArea.addView(this)
                }
            }
            TAB_SETTING -> {

                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.tag.toString().toUpperCase()) {
            "OPEN_NAVI" -> {
                toggleNavigation()
            }
        }
    }

    override fun onAddNavigation(asset:InputStream) {
        setNavigationView(asset)
    }

    override fun onToggleNavigation() {
        toggleNavigation()
    }



    /**
     * Draw side navigation
     */

    private fun setNavigationView(asset : InputStream?) {
        //assets?.open("common/NAVIGATION.json")?
        asset?.use {
            view_naviItemArea.removeAllViews()

            val bytes = ByteArray(it.available())
            it.read(bytes, 0, bytes.size)
            JsonParser().parse(String(bytes))?.asJsonArray?.run {

                val layoutWrapper = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                }

                for(item in this)
                {
                    val objNaviItem = item.asJsonObject

                    //Draw section layout
                    val layoutSection = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                            setMargins(0, 10.px,0,10.px)
                            setPadding(0,0,0,10.px)
                        }
                        background = (ContextCompat.getDrawable(context, R.drawable.border_top_bottom))
                    }

                    //Draw navi title
                    val viewTextTitle = TextView(this@MainActivity).apply {

                        text = objNaviItem.get("title").asString
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        setTextColor(ContextCompat.getColor(context, R.color.Black))

                        setPadding(10.px,10.px,10.px,0)
                    }

                    //Draw submenu layout
                    val layoutSubmenu = GridLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {

                            orientation = LinearLayout.HORIZONTAL
                            columnCount = 5
                        }

                    }

                    val btnWidth = (resources.getDimension(R.dimen.navigation_width) / 5).toInt()
                    //Draw sub buttons
                    objNaviItem.get("submenu")?.asJsonArray?.forEach {

                        val objBtnAttr = it.asJsonObject

                        //Draw button layout
                        val layoutBtn = LinearLayout(this@MainActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

                        }
                        //Draw button title
                        val viewTitle = TextView(this@MainActivity).apply {
                            text = objBtnAttr.get("title").asString
                            gravity = Gravity.CENTER
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                        }
                        //Draw button icon
                        val viewBtn = ImageView(this@MainActivity).apply {

                            val iconName = objBtnAttr.get("icon").asString

                            val id = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                            setImageResource(id)
                            val drawable = drawable.mutate()
                            drawable.setColorFilter(ContextCompat.getColor(context,R.color.naviBtnTint), PorterDuff.Mode.SRC_IN)


                           // setImageDrawable(ContextCompat.get(context, R.drawable.plus))
                            layoutParams = ViewGroup.LayoutParams(btnWidth, btnWidth)
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setPadding(10.px,10.px,10.px,10.px)
                            setOnClickListener {

                                val section = objNaviItem.get("section").asString
                                val fullNodes = objNaviItem.get("node").asString
                                val btnTag = objBtnAttr.get("tag").asString
                                //set click event
                                moveToSection(section, fullNodes, btnTag)
                            }
                        }

                        val ripple = MaterialRippleLayout(this@MainActivity)?.apply {
                            setRippleOverlay(true)
                            addView(viewBtn)
                        }

                        //Add to button layout
                        layoutBtn.addView(ripple)
                        layoutBtn.addView(viewTitle)

                        layoutSubmenu.addView(layoutBtn)

                    }

                    //Add to submenu layout
                    layoutSection.addView(viewTextTitle)
                    layoutSection.addView(layoutSubmenu)

                    layoutWrapper.addView(layoutSection)
                }

                view_naviItemArea.addView(layoutWrapper)
            }
        }
    }

    /**
     * move to view from side navigation
     */
    private fun moveToSection(section:String, nodes:String, btnTag:String) {

        var menuItem:MenuItem? = null
        when(section.toUpperCase())
        {
            "VOUCHER" -> {
                menuItem = this@MainActivity.view_naviBottom.menu.findItem(R.id.action_voucher)
                view_naviBottom.selectedItemId = R.id.action_voucher
            }
            "SLIP" -> {
                menuItem = this@MainActivity.view_naviBottom.menu.findItem(R.id.acton_slip)
                view_naviBottom.selectedItemId = R.id.acton_slip
            }
        }

        menuItem?.run {
            this@MainActivity.onNavigationItemSelected(this)

            val frag: Fragment? = (view_pagerMain.adapter as MainViewPageAdapter).getCurrentFragment()
            frag?.run {
                when(section.toUpperCase())
                {
                    "VOUCHER" -> {
                    (frag as? Frag_Voucher)?.run {
                               moveToSection(nodes, btnTag)
                            }
                    }
//                    "SLIP" -> {
//                           (frag as? Frag_Slip)?.run {
//                      //         moveToSection(nodes, btnTag)
//                           }
//                    }
                    else -> {}
                }
            }
        }

        toggleNavigation()
    }

    /**
     * Check app logged
     */
    private fun checkLogin():Boolean {

        var isLogin = true

        //when user is not logged
        if(!g_SysInfo.bAutoLogin && !g_UserInfo.bHasLogged)
        {
            isLogin = false
            Intent(this@MainActivity, LoginActivity::class.java).run {
                startActivityForResult(this, RESULT_USER_LOGGED)
            }
        }
        return isLogin
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        Initialize(this@MainActivity)
    }


}
