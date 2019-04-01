package com.officeslip.Operation

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.TextView
import com.google.gson.JsonObject
import com.officeslip.CONNECTION_PROTOCOL
import com.sgenc.officeslip.R
import com.officeslip.SQLITE_CREATE_FAVORITE_MOVE_T
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.Common
import com.officeslip.Util.SQLite
import com.officeslip.View.Main.Slip.Frag_SearchSlip
import java.lang.ref.WeakReference

class MoveSlip(var fragment: Frag_SearchSlip, var objSlipItem:JsonObject):AsyncTask<JsonObject, Void, JsonObject>(){

    var activityRef: WeakReference<Activity>? = null
    var builder: AlertDialog? = null
    var m_C = Common()
    var nCurrentStatus = Frag_SearchSlip.GET_SLIPLIST_FAILED

    init {
        activityRef = WeakReference(fragment.activity as AppCompatActivity)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        var activity = activityRef?.get()
        builder = AlertDialog.Builder(activity as Context).apply {
            var view = LayoutInflater.from(activity).inflate(R.layout.progress_circle, null)
            view.findViewById<TextView>(R.id.view_textProgressTitle).text = activity.getString(R.string.in_progress_move_slip)

            setView(view)
            setCancelable(false)
            setNegativeButton(activity.getString(R.string.btn_cancel)) { dialog, which ->

                if(status == AsyncTask.Status.RUNNING) {
                    cancel(true)
                    //  activity.onBackPressed()
                }

                dialog.dismiss()
            }
        }.create()
        builder?.show()
    }

    override fun doInBackground(vararg params: JsonObject?): JsonObject {

        var objUserInfo = params[0]!!

        if(!isCancelled)
        {
            var activity = activityRef?.get()

            //try connect agent via Wifi Network if Mobile data usage is disabled.
            if (m_C.isNetworkConnected(activity as Context)) {
                //consider protocol property
                if ("SOCKET".equals(CONNECTION_PROTOCOL, true)) {
                    if(SocketManager().moveSlip(objSlipItem ,objUserInfo))
                    {
                        nCurrentStatus = Frag_SearchSlip.MOVE_SLIP_SUCCESS
                    }
                }
            }
            else
            {
                nCurrentStatus = Frag_SearchSlip.NETWORK_DISABLED
            }
        }
        return objUserInfo
    }

    override fun onPostExecute(result: JsonObject) {
        super.onPostExecute(result)

        builder?.dismiss()
        val activity = activityRef?.get()

        when(nCurrentStatus) {
            Frag_SearchSlip.MOVE_SLIP_SUCCESS -> {

                 var sbQuery = StringBuffer().apply {
                     append(" Insert Or Replace into ")
                     append(" FAVORITE_MOVE_T ")
                     append(" ( ")
                     append(" USER_ID ")
                     append(" ,USER_NAME ")
                     append(" ,PART_CD ")
                     append(" ,PART_NM ")
                     append(" ,CO_CD ")
                     append(" ,CO_NAME ")
                     append(" ,REG_TIME ")
                     append(" ) ")
                     append(" Values ")
                     append(" ( ")
                     append(" ? ")
                     append(" ,? ")
                     append(" ,? ")
                     append(" ,? ")
                     append(" ,? ")
                     append(" ,? ")
                     append(" ,? ")
                     append(" ) ")
                }

                var strParamsArray = arrayOf(
                        result.get("USER_ID").asString
                        ,result.get("USER_NAME").asString
                        ,result.get("PART_CD").asString
                        ,result.get("PART_NM").asString
                        ,result.get("CO_CD").asString
                        ,result.get("CO_NAME").asString
                        ,m_C.getDate("yyyyMMddhhmmsss")
                )


                var bsSqliteRes = SQLite(fragment.context!!).instantSetRecord(SQLITE_CREATE_FAVORITE_MOVE_T, sbQuery.toString(), strParamsArray)

                AlertDialog.Builder(activity as Context).run {
                    setMessage(activity.getString(R.string.success_move_slip))
                    setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->

                        fragment.onRefresh()
                    }
                }.show()
            }
            Frag_SearchSlip.MOVE_SLIP_FAILED -> {
                AlertDialog.Builder(activity as Context).run {
                    setMessage(activity.getString(R.string.failed_move_slip))
                    setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                        //    activity.onBackPressed()
                    }
                }.show()
            }
            Frag_SearchSlip.NETWORK_DISABLED -> {
                AlertDialog.Builder(activity as Context).run {
                    setMessage(activity.getString(R.string.alert_network_disabled_message))
                    setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                        //   activity.onBackPressed()
                    }
                }.show()
            }
        }
    }

}