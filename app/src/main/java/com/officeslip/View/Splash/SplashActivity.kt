package com.officeslip.View.Splash

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import com.officeslip.Socket.SocketManager
import org.w3c.dom.Element
import java.lang.ref.WeakReference
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.officeslip.*
import com.officeslip.Util.*
import com.sgenc.officeslip.*
import kotlin.system.exitProcess


class SplashActivity:AppCompatActivity() {

    var m_C = Common()

    companion object {


        const val NETWORK_DISABLED = 1104
        const val UPDATE_USERINFO_SUCCESS = 1005
        const val UPDATE_USERINFO_FAILED = 1006
    }

    var m_arRequiredPermissions = arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Initialize(this@SplashActivity)
        setContentView(R.layout.activity_splash)

        if (checkPermissionsGranted()) {
            if(g_UserInfo.bHasLogged)
            {
                UpdateUserInfo(this@SplashActivity).execute()
            }
            else
            {

                Intent(this@SplashActivity, LoginActivity::class.java)?.run {
                    startActivity(this)
                    finish()
                }
            }
        }
    }

    private fun checkPermissionsGranted(): Boolean {
        var bRes = false
        var nPermissionStatus: Int
        var listPermissionsNeeded: ArrayList<String> = ArrayList()


        m_arRequiredPermissions.forEach { p: String ->
            nPermissionStatus = ContextCompat.checkSelfPermission(this, p)
            if (nPermissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this@SplashActivity, listPermissionsNeeded.toTypedArray(), MULTIPLE_PERMISSIONS)
        } else {
            bRes = true
        }

        return bRes
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {

                var isAllGranted = true
                for (i in 0 until grantResults.size) {
                    if (grantResults[i] != 0) {
                        isAllGranted = false
                        Logger.WriteLog(this@SplashActivity.javaClass.name, "onRequestPermissionsResult", permissions.get(i) + " not granted.", 7)
                        break
                    }
                }

                if (grantResults.isNotEmpty() && isAllGranted) {
                    if(g_UserInfo.bHasLogged)
                    {
                        UpdateUserInfo(this@SplashActivity).execute()
                    }
                    else
                    {
                        Intent(this@SplashActivity, LoginActivity::class.java)?.run {
                            startActivity(this)
                            finish()
                        }

                    }
                } else {
                    exitProcess(0)
                }
            }
        }
    }



    //UpdateUserInfo
    private class UpdateUserInfo(activity: Activity) : AsyncTask<Void, Void, Void>() {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = UPDATE_USERINFO_FAILED

        init {
            activityRef = WeakReference(activity as AppCompatActivity)
        }

        override fun doInBackground(vararg params: Void): Void? {

            if (!isCancelled) {
                var activity = activityRef?.get()
                //try connect agent via Wifi Network if Mobile data usage is disabled.
                //if (m_C.isNetworkConnected(activity!!)) {
                    //consider protocol property
                    if ("SOCKET".equals(CONNECTION_PROTOCOL, true)) {

                        SocketManager()?.updateUserInfo(g_UserInfo.strUserID!!)?.let {

                            for (i in 0 until it.length) {
                                var element = it.item(i) as Element

                                g_UserInfo.strUserID = element.getElementsByTagName("USER_ID").item(0).textContent
                                g_UserInfo.strUserName = element.getElementsByTagName("USER_NAME").item(0).textContent
                                g_UserInfo.strCoID = element.getElementsByTagName("CO_CD").item(0).textContent
                                g_UserInfo.strCoName = element.getElementsByTagName("CO_NAME").item(0).textContent
                                g_UserInfo.strPartID = element.getElementsByTagName("PART_CD").item(0).textContent
                                g_UserInfo.strPartName = element.getElementsByTagName("PART_NM").item(0).textContent
                                g_UserInfo.strIsManager = "0"//element.getElementsByTagName("MANAGER").item(0).textContent
                                g_SysInfo.strDisplayLang = element.getElementsByTagName("LANG").item(0).textContent


                            }
                            nCurrentStatus = UPDATE_USERINFO_SUCCESS
                        }
                   // }
                } else {
                    nCurrentStatus = MainActivity.NETWORK_DISABLED
                }

            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            builder?.dismiss()
            var activity = activityRef?.get()
            when (nCurrentStatus) {
                UPDATE_USERINFO_SUCCESS -> {
                //    m_C.changeLocale(activity!!, g_SysInfo.strDisplayLang)
                    Intent(activity, MainActivity::class.java)?.run {
                        activity!!.startActivity(this)
                        activity!!.finish()
                    }
                }
                NETWORK_DISABLED -> {
                    AlertDialog.Builder(activity!!).run {
                        setMessage(activity.getString(R.string.alert_network_disabled_message))
                        setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                              activity.finish()
                        }
                    }.show()
                }

                UPDATE_USERINFO_FAILED -> {
                    AlertDialog.Builder(activity!!).run {
                        setMessage(activity.getString(R.string.failed_update_userinfo))
                        setCancelable(false)
                        setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                            Intent(activity!!, LoginActivity::class.java).run {

                                //Remove user info
                                g_UserInfo.bHasLogged = false
                                SQLite(activity).removeAll()
                                UserDefault(activity).removeAll()

                                var intent = Intent(activity, LoginActivity::class.java)
                                activity!!.startActivity(intent)
                                activity!!.finish()
                            }
                        }
                    }.show()
                }
            }
        }
    }

}