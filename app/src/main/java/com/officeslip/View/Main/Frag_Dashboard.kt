package com.officeslip.View.Main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.officeslip.DASHBOARD_MAIN_URL
import com.officeslip.Listener.OnNavigationListener
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.fragment_dashboard.*


class Frag_Dashboard : Fragment(), View.OnClickListener
{
    private var callback_navigation: OnNavigationListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.fragment_dashboard, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewUI()

    }

    private fun setupViewUI()
    {
        view_webDashboard.webViewClient = WebViewClient()
        view_webDashboard.loadUrl(DASHBOARD_MAIN_URL)

        setNavigationView()
        view_btnNavi.setOnClickListener(this)
    }

    fun setNavigationListener(callback : OnNavigationListener) {
        this.callback_navigation = callback
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

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(isVisibleToUser)
        {
            activity?.run {
                setNavigationView()
            }
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
}