package com.officeslip.Application


import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.sgenc.officeslip.*
import com.officeslip.Socket.SocketManager
import org.w3c.dom.Element
import java.lang.ref.WeakReference
import android.os.Looper.getMainLooper
import android.content.Intent
import com.officeslip.CALL_ANOTHER_APPLICATION
import com.officeslip.CONNECTION_PROTOCOL
import com.officeslip.Util.Common
import com.officeslip.g_SysInfo


class AppLifecycleTracker : Application.ActivityLifecycleCallbacks {

    private var nStarted = -1

    companion object {
        const val RESULT_CHECK_UPDATE_AVALIABLE = 1101
        const val RESULT_CHECK_UPDATE_NEWEST = 1103
        const val RESULT_CHECK_UPDATE_FAILED = 1102
        const val NETWORK_DISABLED = 1104
    }

    //Activity called from background
    override fun onActivityStarted(activity: Activity?) {

        CALL_ANOTHER_APPLICATION = false
        if(nStarted == 0)
        {
//            g_SysInfo = SysInfo(activity!!)
//            g_UserInfo = UserInfo(activity!!)
      //      CheckAppUpdate(activity!!).execute()

        }

        nStarted++
    }

    //Activity went to background
    override fun onActivityStopped(activity: Activity?) {
        nStarted--
        if(nStarted == 0)
        {
            if(!CALL_ANOTHER_APPLICATION) g_SysInfo.bPINLogged = false
        }
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {

        if(nStarted < 0)
        {
            nStarted ++
        }
    }

    override fun onActivityDestroyed(activity: Activity?) {

    }

    override fun onActivityPaused(activity: Activity?) {

    }

    override fun onActivityResumed(activity: Activity?) {

    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {

    }

    //Check application version
    private class CheckAppUpdate(activity: Activity) : AsyncTask<Void, Void, Void>() {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = RESULT_CHECK_UPDATE_FAILED

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

                        SocketManager()?.checkApplicationVersion()?.let {

                            var strServerVersion = (it.item(0) as Element).getElementsByTagName("ENV_VALUE").item(0).textContent.toFloat()

                            val pInfo = activity?.packageManager?.getPackageInfo(activity?.packageName, 0)
                            val strLocalVersion = pInfo?.versionName?.toFloat()

                            if (strLocalVersion != strServerVersion) {
                                nCurrentStatus = RESULT_CHECK_UPDATE_AVALIABLE
                            } else {
                                nCurrentStatus = RESULT_CHECK_UPDATE_NEWEST
                            }
                        }
                  //  }
                } else {
                    nCurrentStatus = NETWORK_DISABLED
                }

            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            builder?.dismiss()
            var activity = activityRef?.get()
            when (nCurrentStatus) {
                RESULT_CHECK_UPDATE_FAILED -> {
                    AlertDialog.Builder(activity!!).run {
                        setMessage(activity.getString(R.string.failed_application_udate))
                        setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                            activity.finish()
                        }
                    }.show()
                }
                NETWORK_DISABLED -> {
                    AlertDialog.Builder(activity!!).run {
                        setMessage(activity.getString(R.string.alert_network_disabled_message))
                        setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                            activity.finish()
                        }
                    }.show()
                }

                RESULT_CHECK_UPDATE_AVALIABLE -> {
                    AlertDialog.Builder(activity!!).run {
                        setMessage(activity.getString(R.string.download_new_version))
                        setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->

                            activity?.findViewById<TextView>(R.id.view_textSplash)?.text = activity?.getString(R.string.downloading_application)
                            //Download newest app
                            val mHandler = Handler(getMainLooper())
                            mHandler.post {
                                m_C.DownloadApp(activity)
                                val i = Intent()
                                i.action = Intent.ACTION_MAIN
                                i.addCategory(Intent.CATEGORY_HOME)
                                activity.startActivity(i)
                            }
                        }
                    }.show()
                }

                RESULT_CHECK_UPDATE_NEWEST -> {

//                    if (g_UserInfo.bHasLogged) {
//                        activity?.findViewById<TextView>(R.id.view_textSplash)?.text = activity?.getString(R.string.update_userinfo)
//                        UpdateUserInfo(activity!!).execute()
//
//                    } else {
//                        var intent = Intent(activity, LoginActivity::class.java)
//                        activity!!.startActivity(intent)
//                        activity!!.finish()
//                    }
                }
            }
        }
    }
}