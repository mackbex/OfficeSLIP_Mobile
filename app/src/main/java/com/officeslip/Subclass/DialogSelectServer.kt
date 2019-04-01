package com.officeslip.Subclass

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import com.officeslip.MODE_DEV
import com.officeslip.MODE_PRD
import com.officeslip.Util.Common
import com.officeslip.g_SysInfo
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.dialog_select_servermode.*

class DialogSelectServer(context: Context) : Dialog(context), View.OnClickListener
{
    private var m_C = Common()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.attributes = WindowManager.LayoutParams().apply {
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.8f
        }

        setContentView(R.layout.dialog_select_servermode)

        setupViewUI()
    }

    fun setupViewUI() {
        when(g_SysInfo.ServerMode)
        {
            MODE_PRD -> {
                g_SysInfo.ServerMode = MODE_PRD
                view_radioBtnPRD.isChecked = true
            }
            MODE_DEV -> {
                g_SysInfo.ServerMode = MODE_DEV
                view_radioBtnDEV.isChecked = true
            }
        }

        view_btnConfirm.setOnClickListener(this@DialogSelectServer)
        view_btnCancel.setOnClickListener(this@DialogSelectServer)
    }

    fun applyServerSetting() {
        val strPassword 	= view_editPassword.text.toString()

        if("woonam01!" == strPassword) {
            if(view_radioBtnPRD.isChecked)
            {
                g_SysInfo.ServerMode = MODE_PRD
            }
            else
            {
                g_SysInfo.ServerMode = MODE_DEV
            }
        }
        else
        {
            AlertDialog.Builder(context).run {
                setMessage(context.getString(R.string.alert_password_not_matched))
                setPositiveButton(context.getString(R.string.btn_confirm), null)
            }.show()
        }
    }

    override fun onClick(v: View?) {

        when(v?.id) {
            view_btnConfirm.id -> {
                applyServerSetting()
            }
        }

        dismiss()
    }
}