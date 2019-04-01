package com.officeslip.Operation

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sgenc.officeslip.*
import com.officeslip.Agent.WDMAgent
import com.officeslip.CONNECTION_PROTOCOL
import com.officeslip.SQLITE_CREATE_FAVORITE_COPY_T
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.Common
import com.officeslip.Util.Logger
import com.officeslip.Util.SQLite
import com.officeslip.View.Main.Slip.Frag_SearchSlip
import org.jdom2.Element
import java.lang.ref.WeakReference


class CopySlip(var fragment: Frag_SearchSlip, var objSlipItem: JsonObject): AsyncTask<JsonObject, Void, JsonObject>(){

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
            view.findViewById<TextView>(R.id.view_textProgressTitle).text = activity.getString(R.string.in_progress_copy_slip)

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

                    var strSDocNo = objSlipItem.get("SDOC_NO").asString

                    nCurrentStatus = uploadSlipXML(strSDocNo, objUserInfo)
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
            Frag_SearchSlip.COPY_SLIP_SUCCESS -> {

                var sbQuery = StringBuffer().apply {
                    append(" Insert Or Replace into ")
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
                    setMessage(activity.getString(R.string.success_copy_slip))
                    setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->

                        fragment.onRefresh()
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

            else -> {
                AlertDialog.Builder(activity as Context).run {
                    setMessage(activity.getString(R.string.failed_copy_slip))
                    setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                        //    activity.onBackPressed()
                    }
                }.show()
            }
        }
    }


//    fun uploadSlipXML(strSDocNo:String, objUserInfo:JsonObject):Int {
//        var nRes = Frag_SearchSlip.COPY_SLIP_FAILED
//
//        try {
//
//
//            var XML = CreateXML_SlipDoc()
//            var xmlSlipDoc = org.jdom2.Element("Document")
//            xmlSlipDoc.setAttribute("Type","DOCFILE")
//
//            var strXML = SocketManager().Download()
//            //Write DocData node
//            var elDocData = XML.getElement_DocData(fragment.activity)
//
//
//            SocketManager().getSlipDocInfo(strSDocNo)?.run {
//
//            var strUserID = objUserInfo.get("USER_ID").asString
//            var strDocIRN =  WDMAgent().getDocIRN(strUserID, m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)
//            var strNewSDocNo = SocketManager().getSDocNo()
//
//            if(m_C.isBlank(strNewSDocNo))
//            {
//                nRes = Frag_SearchSlip.COPY_SLIP_SDOCNO_EMPTY
//                return nRes
//            }
//
//            //Create DocData
//            elDocData = XML.setDocData(elDocData, this, objUserInfo, strDocIRN, strNewSDocNo!!)
//            if(elDocData == null)
//            {
//                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_CREATE_XML
//                return nRes
//            }
//
//            //Add DocData to XML
//            xmlSlipDoc.addContent(elDocData)
//
//
//
//        }
//
//        //if(nodeSlipDocInfo == null)
//
//        }
//        catch(e:Exception)
//        {
//            Logger.WriteException(this@CopySlip::javaClass.name, "uploadSlipXML",e,5)
//        }
//        return nRes
//    }

    fun uploadSlipXML(strSDocNo:String, objUserInfo:JsonObject):Int {
        var nRes = Frag_SearchSlip.COPY_SLIP_FAILED

        try {
            var strUserID = objUserInfo.get("USER_ID").asString
            var strUserName = objUserInfo.get("USER_NAME").asString
            var strPartCD = objUserInfo.get("PART_CD").asString
            var strCoCD = objUserInfo.get("CO_CD").asString
            var strNewDocIRN =  WDMAgent().getDocIRN()//WDMAgent().getDocIRN(strUserID, m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)
            var strNewSDocNo = SocketManager().getSDocNo()


            var objSlipDocInfo = JsonObject()

            //Get SLIPDOC Info
            SocketManager().getSlipDocInfo(strSDocNo)?.let {

                var element = it.item(0) as org.w3c.dom.Element

                var childNodes = element.childNodes
                for(i in 0 until childNodes.length)
                {
                    var strColumnName = childNodes.item(i).nodeName
                    objSlipDocInfo.addProperty(strColumnName.toUpperCase(), element.getElementsByTagName(strColumnName).item(0).textContent)
                }
            }

            if(objSlipDocInfo.size() <= 0)
            {
                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_GET_SLIPDOC_INFO
                return nRes
            }

            var strOriginalDocIRN = objSlipDocInfo.get("DOC_IRN").asString
            var strOriginalSDocNo = objSlipDocInfo.get("SDOC_NO").asString
            var isSDocOne = "1" == objSlipDocInfo.get("SDOC_ONE").asString
            var strFileCnt = objSlipDocInfo.get("FILE_CNT").asString


            //Get SLIP info
            var arSlipInfo = JsonArray()
            SocketManager().getSlipListForCopy(strOriginalSDocNo)?.let {

                for (i in 0 until it.length) {
                    var element = it.item(i) as org.w3c.dom.Element

                    var objSlipInfo = JsonObject()
                    var childNodes = element.childNodes
                    for (j in 0 until childNodes.length) {
                        var strColumnName = childNodes.item(j).nodeName
                        objSlipInfo.addProperty(strColumnName.toUpperCase(), element.getElementsByTagName(strColumnName).item(0).textContent)
                    }
                    arSlipInfo.add(objSlipInfo)
                }
            }
            if(arSlipInfo.size() <= 0)
            {
                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_GET_SLIP_INFO
                return nRes
            }



            var XML = CreateXML_SlipDoc()
            var xmlSlipDoc = Element("Document")
            xmlSlipDoc.setAttribute("Type","DOCFILE")
            //Write DocData node
            var elDocData = XML.getElement_DocData(fragment.activity)
            if(elDocData == null)
            {
                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_CREATE_XML
                return nRes
            }
            elDocData = XML.setDocDataForCopy(elDocData, objSlipDocInfo, objUserInfo, strNewDocIRN, strNewSDocNo!!)
            if(elDocData == null)
            {
                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_CREATE_XML
                return nRes
            }
            xmlSlipDoc.addContent(elDocData)

            //Write DocList node
            var elDocList:Element? = Element("DocList")
            elDocList!!.setAttribute("Load","1");
            elDocList!!.setAttribute("Table","img_slip_t");
            elDocList!!.setAttribute("Field","cabinet;folder;doc_irn;sdoc_no;slip_no;file_cnt;file_size;update_time;slip_flag;slip_comment");
            elDocList = XML.getElement_DocListItemForCopy(elDocList, arSlipInfo, strNewSDocNo, isSDocOne)
            if(elDocList == null)
            {
                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_CREATE_XML
                return nRes
            }
            xmlSlipDoc.addContent(elDocList)

            //Write FileList Node
            var elFileList = XML.getElement_FileList(strOriginalDocIRN, strFileCnt.toInt())
            if(elFileList == null)
            {
                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_CREATE_XML
                return nRes
            }
            xmlSlipDoc.addContent(elFileList)

            //Write PageList node
            var elPageList = XML.getElement_PageListForCopy(arSlipInfo, isSDocOne)
            if(elPageList == null || elPageList.size <= 0)
            {
                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_CREATE_XML
                return nRes
            }
            for(el in elPageList)
            {
                xmlSlipDoc.addContent(el)
            }

//            val out = XMLOutputter()
//            val strResXML = out.outputString(xmlSlipDoc)

            //Write to file
            var strXMLPath = XML.write(xmlSlipDoc, strNewDocIRN)
            if(m_C.isBlank(strXMLPath))
            {
                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_CREATE_XML
                return nRes
            }

            arSlipInfo.forEach {
                if (!SocketManager().copySlipTable(it.asJsonObject, strNewSDocNo!!)) {
                    nRes = Frag_SearchSlip.COPY_SLIP_FAILED_INSERT_SLIP
                    return nRes
                }
            }

            if(!SocketManager().upload(strXMLPath!!, strNewDocIRN, "1", "IMG_SLIPDOC_X"))
            {
                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_UPLOAD_XML
                return nRes
            }

            if (!SocketManager().copySlipDocTable(objSlipDocInfo, objUserInfo, strNewSDocNo!!, strNewDocIRN)) {
                nRes = Frag_SearchSlip.COPY_SLIP_FAILED_INSERT_SLIPDOC
                return nRes
            }

            nRes = Frag_SearchSlip.COPY_SLIP_SUCCESS
        }
        catch(e:Exception)
        {
            Logger.WriteException(this@CopySlip::javaClass.name, "uploadSlipXML",e,5)
        }
        return nRes
    }
}