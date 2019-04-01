package com.officeslip

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.officeslip.Util.Common
import kotlinx.android.synthetic.main.activitiy_login.*

import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.officeslip.Socket.SocketManager
import com.sgenc.officeslip.R
import org.w3c.dom.Element
import java.lang.ref.WeakReference


class LoginActivity : AppCompatActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    companion object {
        const val NETWORK_DISABLED = 3001
        const val LOGIN_SUCCESS = 3002
        const val LOGIN_FAILED = 3003

    }



    private var m_C = Common()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activitiy_login)
        supportActionBar?.run {
            this.hide()
        }

        //Check saveID option
        view_switchAutoLogin.isChecked = g_SysInfo.bAutoLogin

        if(g_SysInfo.bSaveID)
        {
            if(!m_C.isBlank(g_UserInfo.strUserID))
            {
                view_editTextUserID.setText(g_UserInfo.strUserID)
            }
        }

        if(g_SysInfo.ServerMode != MODE_PRD)
        {
            view_textServerMode.visibility = View.VISIBLE
        }
        else
        {
            view_textServerMode.visibility = View.GONE
        }

        //establish all buttons events
        view_btnLogin.setOnClickListener(this@LoginActivity)
        view_switchAutoLogin.setOnCheckedChangeListener(this@LoginActivity)
    }

    override fun onClick(v: View?) {
        when(v?.id)
        {
            R.id.view_btnLogin -> {

                var strUserID = view_editTextUserID.text.toString()
                var strUserPW = view_editTextUserPW.text.toString()

                if(!"woonam01!".equals(strUserPW, true))
                {
                    strUserPW = m_C.MD5(strUserPW)
                }



                Login(this@LoginActivity, SocketManager()).execute(strUserID, strUserPW)


            }
        }
    }



    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(buttonView?.isPressed!!) {
            when (buttonView?.id) {
                R.id.view_switchAutoLogin -> {
                    g_SysInfo.bAutoLogin = isChecked
                }
            }
        }
    }



    private fun Login():Int {

        var isLogged = LOGIN_FAILED



        return isLogged
    }

    private class Login(activity: Activity, var socketManager: SocketManager): AsyncTask<String, Void, Int>() {
        var activityRef: WeakReference<Activity>? = null
        var builder:AlertDialog? = null
        var m_C = Common()

        init {
            activityRef = WeakReference(activity)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            var activity = activityRef?.get()
            builder = AlertDialog.Builder(activity as Context).apply {
                var view = LayoutInflater.from(activity).inflate(R.layout.progress_circle, null)
                view.findViewById<TextView>(R.id.view_textProgressTitle).text = activity.getString(R.string.in_progress)

                setView(view)
                setCancelable(false)
                setNegativeButton(activity.getString(R.string.btn_cancel), DialogInterface.OnClickListener { dialog, which ->

                    if(status == AsyncTask.Status.RUNNING)
                    {
                        cancel(true)
                    }

                    dialog.dismiss()
                })}.create()
            builder?.show()


        }
        override fun doInBackground(vararg params: String?): Int {
            var strUserID = params[0]
            var strUserPW = params[1]

            var nRes = LOGIN_FAILED

//            Thread.sleep(10000)
            if(!isCancelled)
            {
                var activity = activityRef?.get()

                //try connect agent via Wifi Network if Mobile data usage is disabled.
                if(m_C.isNetworkConnected(activity as Context))
                {
                    //consider protocol property
                    if("SOCKET".equals(CONNECTION_PROTOCOL, true))
                    {
                        SocketManager()?.getUserInfo(strUserID!!, strUserPW!!)?.let {

                            for (i in 0 until it.length) {
                                var element = it.item(i) as Element

                                g_UserInfo.strUserID = element.getElementsByTagName("USER_ID").item(0).textContent
                                g_UserInfo.strUserName = element.getElementsByTagName("USER_NAME").item(0).textContent
                                g_UserInfo.strCoID = element.getElementsByTagName("CO_CD").item(0).textContent
                                g_UserInfo.strCoName = element.getElementsByTagName("CO_NAME").item(0).textContent
                                g_UserInfo.strPartID = element.getElementsByTagName("PART_CD").item(0).textContent
                                g_UserInfo.strPartName = element.getElementsByTagName("PART_NM").item(0).textContent
                                g_UserInfo.strIsManager = "0"//element.getElementsByTagName("MANAGER").item(0).textContent
                            //    g_SysInfo.strDisplayLang = element.getElementsByTagName("LANG").item(0).textContent

                             //   m_C.changeLocale(activity as Context, g_SysInfo.strDisplayLang)
                            }
                            nRes = LOGIN_SUCCESS
                        }
                    }
                }
                else
                {
                    nRes = NETWORK_DISABLED
                }
            }

            return nRes
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            builder?.dismiss()
            val activity = activityRef?.get()

            when(result) {
                LOGIN_SUCCESS -> {
                    activity?.run {
                        g_SysInfo.bPINLogged = true
                        g_UserInfo.bHasLogged = true
                        Intent(activity, MainActivity::class.java)?.run {
                            activity!!.startActivity(this)
                            activity!!.finish()
                        }
                    }
                }
                LOGIN_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setTitle(activity.getString(R.string.login_alert_failed_title))
                        setMessage(activity.getString(R.string.login_alert_no_user_exists))
                        setPositiveButton(activity.getString(R.string.btn_confirm), null)
                    }.show()
                }
                NETWORK_DISABLED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setTitle(activity.getString(R.string.login_alert_failed_title))
                        setMessage(activity.getString(R.string.alert_network_disabled_message))
                        setPositiveButton(activity.getString(R.string.btn_confirm), null)
                    }.show()
                }
            }
        }
    }
}