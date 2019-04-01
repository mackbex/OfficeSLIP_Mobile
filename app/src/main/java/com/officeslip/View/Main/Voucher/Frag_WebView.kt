package com.officeslip.View.Main.Voucher

import android.app.Activity
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.gson.JsonObject
import com.officeslip.Adapter.AddThumbAdapter
import com.officeslip.Util.Common
import com.officeslip.VOUCHER_MAIN_URL
import com.sgenc.officeslip.R
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.fragment_voucher_webview.*
import java.io.InputStream

class Frag_WebView : Fragment(), Frag_Voucher.OnOpenURLListener, CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, View.OnClickListener,
        AddThumbAdapter.ChangeSlipMode, AddThumbAdapter.ViewOriginal
{
    private var url:String? = null
    private val m_C = Common()
    private var nCurUploadSegmentId = SEGMENT_BTN_IMAGE

    companion object {
        const val SEGMENT_BTN_IMAGE = 100001
        const val SEGMENT_BTN_FILE = 100002
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_voucher_webview, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var strColumnName = m_C.getNodeNameAtIndex(activity as Activity, "common/VIEW_VOUCHER_WEBVIEW.xml", 0)
        m_C.loadUIXML(activity as Activity, view_layoutUICell, this as Frag_WebView,  "common/VIEW_VOUCHER_WEBVIEW.xml", strColumnName)
        setupViewUI()
    }

    private fun setupViewUI() {
        view_webVoucher.webViewClient = WebViewClient()
        view_webVoucher.loadUrl(VOUCHER_MAIN_URL + url)


        //Tint toggle button
        view_btnToggle?.apply {
            val drawable = drawable.mutate()
            drawable.setColorFilter(ContextCompat.getColor(context,R.color.naviBtnTint), PorterDuff.Mode.SRC_IN)
        }

        view_layoutUICell?.run {
            this.findViewWithTag<SegmentedGroup>("SLIP")?.apply {
                setOnCheckedChangeListener(null)
                findViewById<RadioButton>(nCurUploadSegmentId)?.isChecked = true
                setOnCheckedChangeListener(this@Frag_WebView)
            }
        }

        view_btnToggle.setOnClickListener(this)
    }

    /**
     * Toggle slip or addfile attach layout
     */
    private fun toggleAddslipLayout() {
        if(view_layoutAddSlip.visibility == View.GONE)
        {
            view_layoutAddSlip.visibility = View.VISIBLE
            //Rotate button
            view_btnToggle.rotation = 180f
        }
        else
        {
            view_layoutAddSlip.visibility = View.GONE
            //Rotate button
            view_btnToggle.rotation = 0f
        }
    }

    override fun onClick(v: View?) {

        when(v?.tag.toString().toUpperCase())
        {
            "TOGGLE_ADD_LAYOUT" -> {
                toggleAddslipLayout()
            }
        }
    }

    /**
     * Get destination url from parent view
     */
    override fun onOpenURL(url: String) {
        this.url = url
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

    }

    override fun changeSlipMode(mode: Int) {

    }

    override fun viewOriginal(objItem: JsonObject, nIdx: Int) {

    }

}