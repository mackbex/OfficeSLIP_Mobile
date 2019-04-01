package com.officeslip.Operation

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.TextView
import com.google.gson.JsonObject
import com.sgenc.officeslip.*
import com.officeslip.Agent.WDMAgent
import com.officeslip.CONNECTION_PROTOCOL
import com.officeslip.SQLITE_CREATE_FAVORITE_COPY_T
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.Common
import com.officeslip.Util.Logger
import com.officeslip.Util.SQLite
import com.officeslip.View.Main.Slip.Frag_SearchAddFile
import java.lang.ref.WeakReference


class CopyAttach(var fragment: Frag_SearchAddFile, var objAttachItem: JsonObject): AsyncTask<JsonObject, Void, JsonObject>(){

    var activityRef: WeakReference<Activity>? = null
    var builder: AlertDialog? = null
    var m_C = Common()
    var nCurrentStatus = Frag_SearchAddFile.GET_ADDFILELIST_FAILED

    init {
        activityRef = WeakReference(fragment.activity as AppCompatActivity)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        var activity = activityRef?.get()
        builder = AlertDialog.Builder(activity as Context).apply {
            var view = LayoutInflater.from(activity).inflate(R.layout.progress_circle, null)
            view.findViewById<TextView>(R.id.view_textProgressTitle).text = activity.getString(R.string.in_progress_copy_attach)

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

                    var strDocIRN = objAttachItem.get("DOC_IRN").asString

                    nCurrentStatus = uploadAttach(strDocIRN, objUserInfo)
                }
            }
            else
            {
                nCurrentStatus = Frag_SearchAddFile.NETWORK_DISABLED
            }
        }
        return objUserInfo
    }

    override fun onPostExecute(result: JsonObject) {
        super.onPostExecute(result)

        builder?.dismiss()
        val activity = activityRef?.get()

        when(nCurrentStatus) {
            Frag_SearchAddFile.COPY_ADDFILE_SUCCESS -> {

                var sbQuery = StringBuffer().apply {
                    append(" Insert into ")
                    append(" FAVORITE_COPY_T ")
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


                var bsSqliteRes = SQLite(fragment.context!!).instantSetRecord(SQLITE_CREATE_FAVORITE_COPY_T, sbQuery.toString(), strParamsArray)

                AlertDialog.Builder(activity as Context).run {
                    setMessage(activity.getString(R.string.success_copy_attach))
                    setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->

                        fragment.onRefresh()
                    }
                }.show()

            }
            Frag_SearchAddFile.NETWORK_DISABLED -> {
                AlertDialog.Builder(activity as Context).run {
                    setMessage(activity.getString(R.string.alert_network_disabled_message))
                    setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                        //   activity.onBackPressed()
                    }
                }.show()
            }

            else -> {
                AlertDialog.Builder(activity as Context).run {
                    setMessage(activity.getString(R.string.failed_copy_attach))
                    setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                        //    activity.onBackPressed()
                    }
                }.show()
            }
        }
    }

    fun uploadAttach(strDocIRN:String, objUserInfo:JsonObject):Int {
        var nRes = Frag_SearchAddFile.COPY_ADDFILE_FAILED

        try {
            var strUserID = objUserInfo.get("USER_ID").asString
            var strUserName = objUserInfo.get("USER_NAME").asString
            var strPartCD = objUserInfo.get("PART_CD").asString
            var strCoCD = objUserInfo.get("CO_CD").asString
            var strNewDocIRN =  WDMAgent().getDocIRN()//WDMAgent().getDocIRN(strUserID, m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)
            var strNewSDocNo = SocketManager().getSDocNo()


            var objAttachInfo = JsonObject()

            //Get SLIPDOC Info
            SocketManager().getAttachInfo(strDocIRN)?.let {

                var element = it.item(0) as org.w3c.dom.Element

                var childNodes = element.childNodes
                for(i in 0 until childNodes.length)
                {
                    var strColumnName = childNodes.item(i).nodeName
                    objAttachInfo.addProperty(strColumnName.toUpperCase(), element.getElementsByTagName(strColumnName).item(0).textContent)
                }
            }


            if (!SocketManager().copyAttachTable(objAttachInfo, objUserInfo, strNewSDocNo!!, strNewDocIRN)) {
                nRes = Frag_SearchAddFile.COPY_ADDFILE_FAILED
                return nRes
            }

            nRes = Frag_SearchAddFile.COPY_ADDFILE_SUCCESS
        }
        catch(e:Exception)
        {
            Logger.WriteException(this@CopyAttach::javaClass.name, "uploadSlipXML",e,5)
        }
        return nRes
    }
}