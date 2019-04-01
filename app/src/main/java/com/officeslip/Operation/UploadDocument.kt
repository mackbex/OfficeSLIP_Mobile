package com.officeslip.Operation

import android.app.Activity
import com.google.gson.JsonObject
import com.officeslip.Agent.WDMAgent
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.Common
import com.google.gson.JsonArray
import com.officeslip.*
import com.officeslip.Socket.XvarmTransfer
import com.officeslip.Util.ARIAChiper
import org.jdom2.Element
import java.io.File


class UploadDocument {

    private var m_C = Common()
    private var m_WDM = WDMAgent()
    private var activity:Activity? = null

    companion object {
        const val UPLOAD_SLIP_SUCCESS   = 111
        const val UPLOAD_SLIP_FAILED_UPLOAD_IMAGE    = 112
        const val UPLOAD_SLIP_FAILED_INSERT_SLIP    = 113
        const val UPLOAD_SLIP_FAILED_INSERT_THUMB    = 115
        const val UPLOAD_SLIP_FAILED_CREATE_KEY    = 114
        const val UPLOAD_SLIP_FAILED_CREATE_XML    = 116
        const val UPLOAD_SLIP_FAILED_INSERT_XML    = 117
        const val UPLOAD_ADDFILE_SUCCESS = 118
        const val UPLOAD_ADDFILE_FAILED  = 119
        const val UPLOAD_ADDFILE_FAILED_CREATE_KEY    = 114
    }

    fun initItem(activity:Activity?, arObjItems:JsonArray): JsonArray?
    {
        this.activity = activity
        var arObjRes = JsonArray()

        var file = File(TEMP_PATH)
        if(!file.exists())
        {
            file.mkdirs()
        }

        for(objItem in arObjItems)
        {
            var objNewItem = JsonObject()
            var strOriginalPath = objItem.asJsonObject.get("OriginalPath").asString
            var strThumbPath    = objItem.asJsonObject.get("ThumbPath").asString
            var strImageSize = objItem.asJsonObject.get("ImageSize").asString
            var strWidth = objItem.asJsonObject.get("Width").asString
            var strHeight = objItem.asJsonObject.get("Height").asString
            var strOldDocIRN       = objItem.asJsonObject.get("DOC_IRN").asString
            var strObjShape = objItem.asJsonObject.get("SHAPE_DATA")

            var nResizedOriginalQuality = m_C.resizeOriginal(activity?.applicationContext!!, File(strOriginalPath))
            strThumbPath = m_C.saveThumb(activity!!, strOldDocIRN, strOriginalPath, nResizedOriginalQuality, objItem.asJsonObject)

            var strDocIRN = WDMAgent().getDocIRN()//m_WDM.getDocIRN(g_UserInfo.strUserID,  m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)
            var strNewOriginalPath = TEMP_PATH + File.separator + strDocIRN + ".J2K"
            var strNewThumbPath    = TEMP_PATH + File.separator + strDocIRN + ".JPG"



            if(m_C.copyFile(strOriginalPath, strNewOriginalPath) && m_C.copyFile(strThumbPath, strNewThumbPath))
            {
                objNewItem.addProperty("SlipPath", strNewOriginalPath)
                objNewItem.addProperty("ThumbPath", strNewThumbPath)
                objNewItem.addProperty("DocIRN", strDocIRN)
                objNewItem.addProperty("ImageSize", strImageSize)
                objNewItem.addProperty("Width", strWidth)
                objNewItem.addProperty("Height", strHeight)
                objNewItem.add("SHAPE_DATA", if(strObjShape != null) strObjShape.asJsonObject else null )
                arObjRes.add(objNewItem)
            }
            else
            {
                return null
            }
        }

        return arObjRes
    }

//    fun getDocIRN():String {
//        return  m_WDM.getDocIRN(g_UserInfo.strUserID,  m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)
//
//    }



    fun uploadSlipDoc(objSlipDocInfo:JsonObject, arObjImages:JsonArray, isSDocOne:Boolean, strSDocNo:String):JsonObject {
        var objRes = JsonObject()
        objRes.addProperty("RESULT", UPLOAD_SLIP_SUCCESS)


        var strSlipDoc_DocIRN = WDMAgent().getDocIRN()//m_WDM.getDocIRN(g_UserInfo.strUserID,  m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)
        var XML = CreateXML_SlipDoc()
        var xmlSlipDoc = Element("Document")
        xmlSlipDoc.setAttribute("Type","DOCFILE")
        //Write DocData node
        var elDocData = XML.getElement_DocData(activity)
        if(elDocData == null)
        {
            objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_CREATE_XML)
            return objRes
        }
        elDocData = XML.setDocData(elDocData, objSlipDocInfo, arObjImages, strSDocNo, strSlipDoc_DocIRN, isSDocOne)
        if(elDocData == null)
        {
            objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_CREATE_XML)
            return objRes
        }

        xmlSlipDoc.addContent(elDocData)

        //Write DocList node
        var elDocList:Element? = Element("DocList")
        elDocList!!.setAttribute("Load","1");
        elDocList!!.setAttribute("Table","img_slip_t");
        elDocList!!.setAttribute("Field","cabinet;folder;doc_irn;sdoc_no;slip_no;file_cnt;file_size;update_time;slip_flag;slip_comment");
        elDocList = XML.getElement_DocListItem(elDocList, arObjImages, strSDocNo, isSDocOne)
        if(elDocList == null)
        {
            objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_CREATE_XML)
            return objRes
        }
        xmlSlipDoc.addContent(elDocList)

        //Write FileList Node
        var elFileList = XML.getElement_FileList(strSlipDoc_DocIRN, arObjImages.size())
        if(elFileList == null)
        {
            objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_CREATE_XML)
            return objRes
        }
        xmlSlipDoc.addContent(elFileList)


        //Write PageList node
        var elPageList = XML.getElement_PageList(arObjImages, isSDocOne)
        if(elPageList == null || elPageList.size <= 0)
        {
            objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_CREATE_XML)
            return objRes
        }
        for(el in elPageList)
        {
            xmlSlipDoc.addContent(el)
        }

        //Write to file
        var strXMLPath = XML.write(xmlSlipDoc, strSlipDoc_DocIRN)
        if(m_C.isBlank(strXMLPath))
        {
            objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_CREATE_XML)
            return objRes
        }

        //Upload XML
        if("SOCKET" == CONNECTION_PROTOCOL)
        {
            if(!SocketManager().upload(strXMLPath!!, strSlipDoc_DocIRN, "1", "IMG_SLIPDOC_X"))
            {
                objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_INSERT_XML)
                return objRes
            }

            if (!SocketManager().insertSlipDocTable(objSlipDocInfo, arObjImages, strSDocNo!!, strSlipDoc_DocIRN, isSDocOne)) {
                objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_INSERT_SLIP)
                return objRes
            }
        }



        return objRes
    }

    fun uploadThumb(arObjImages:JsonArray, isSDocOne: Boolean):JsonObject {
        var objRes = JsonObject()
        objRes.addProperty("RESULT", UPLOAD_SLIP_SUCCESS)
         var nImageCnt = arObjImages!!.size()

        if("SOCKET" == CONNECTION_PROTOCOL)
        {
            //Upload thumbnail image
            for (i in 0 until nImageCnt)
            {
                var objImage = arObjImages.get(i).asJsonObject
                var strThumbPath = objImage.get("ThumbPath").asString
                var strDocIRN = objImage.get("DocIRN").asString

                if(!SocketManager().upload(strThumbPath, strDocIRN, "1", "IMG_SLIP_M"))
                {
                    objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_INSERT_THUMB)
                }

                if(isSDocOne)
                {
                    break
                }
            }
        }

        return objRes
    }

    fun uploadSlip(arObjImages:JsonArray, isSDocOne:Boolean):JsonObject {
        var objRes = JsonObject()
        objRes.addProperty("RESULT", UPLOAD_SLIP_SUCCESS)
        var arResSlip = ArrayList<JsonObject>()
        var nImageCnt = arObjImages!!.size()

        if("SOCKET" == CONNECTION_PROTOCOL) {

            //Upload original image
            for (i in 0 until nImageCnt) {

                var objCurSlipItem = arObjImages.get(i).asJsonObject
                var nDocNo = 1
                var strDocIRN = objCurSlipItem.get("DocIRN").asString

                //When it's OneSlip
                if (isSDocOne)
                {
                    strDocIRN = arObjImages.get(0).asJsonObject.get("DocIRN").asString
                    nDocNo = i + 1
                }

                UploadSlipImage(objCurSlipItem, strDocIRN, nDocNo)?.let {
                    arResSlip.add(it)
                }

            }

            //Check image upload cnt
            var nDBUploadedCnt = CheckUploadSlipImageCount(arResSlip)

            if ((nDBUploadedCnt != nImageCnt) || nDBUploadedCnt <= 0) {
                objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_UPLOAD_IMAGE)
                return objRes
            }




            var strSDocNo = SocketManager().getSDocNo()
            if (!m_C.isBlank(strSDocNo)) {
                for (i in 0 until arObjImages.size()) {

                    var shapeString = m_C.getShapeTag(arObjImages.get(i).asJsonObject.get("SHAPE_DATA").asJsonObject)

                    var nFileCnt = 1
                    if(isSDocOne)
                    {
                        nFileCnt = arObjImages.size()
                    }

                    if (!SocketManager().insertSlipTable(arObjImages.get(i).asJsonObject, strSDocNo!!, i + 1, nFileCnt, shapeString)) {
                        objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_INSERT_SLIP)
                        return objRes
                    }

                    if(isSDocOne)
                    {
                        break
                    }
                }

                objRes.addProperty("SDOC_NO", strSDocNo)
            } else {
                objRes.addProperty("RESULT", UPLOAD_SLIP_FAILED_CREATE_KEY)
                return objRes
            }

        }
        return objRes
    }

    //Upload original image
    fun UploadSlipImage(objOriginal:JsonObject,strDocIRN:String, nDocNo:Int):JsonObject? {

        var objRes:JsonObject? = null

        var strImgPath = objOriginal.get("SlipPath").asString



        if("SOCKET" == CONNECTION_PROTOCOL)
        {
            if(SocketManager().upload(strImgPath, strDocIRN, nDocNo.toString(), "IMG_SLIP_X"))
            {
                objRes = objOriginal
            }
        }
        return objRes
    }

    fun CheckUploadSlipImageCount(arResOriginal:ArrayList<JsonObject>):Int {

        var nResCnt = 0

        if("SOCKET" == CONNECTION_PROTOCOL) {
            if (arResOriginal.size > 0) {
                var strWhere = StringBuffer().apply {

                    for (i in 0 until arResOriginal.size) {
                        var strDocIRN = arResOriginal.get(i).get("DocIRN").asString

                        append(" '$strDocIRN' ")

                        if (i < arResOriginal.size - 1) {
                            append(", ")
                        }
                    }

                }
                if ("SOCKET" == CONNECTION_PROTOCOL) {
                    nResCnt = SocketManager().getSlipCount(strWhere.toString())
                }
            }
        }
        return nResCnt

    }

    fun CheckUploadAddFileCount(arResOriginal:ArrayList<JsonObject>):Int {

        var nResCnt = 0

        if ("SOCKET" == CONNECTION_PROTOCOL) {
            if (arResOriginal.size > 0) {
                var strWhere = StringBuffer().apply {

                    for (i in 0 until arResOriginal.size) {
                        var strDocIRN = arResOriginal.get(i).get("DOC_IRN").asString

                        append(" '$strDocIRN' ")

                        if (i < arResOriginal.size - 1) {
                            append(", ")
                        }
                    }

                }
                if ("SOCKET" == CONNECTION_PROTOCOL) {
                    nResCnt = SocketManager().getAddFileCount(strWhere.toString())
                }
            }
        }
        return nResCnt

    }

    fun uploadAddfile(arObjFiles:JsonArray, objDocumentInfo:JsonObject):JsonObject
    {
        var objRes = JsonObject()
        objRes.addProperty("RESULT", UPLOAD_ADDFILE_SUCCESS)
        var arResAddFile = ArrayList<JsonObject>()
        var nAddFileCnt = arObjFiles!!.size()

        if("SOCKET" == CONNECTION_PROTOCOL) {

            //Upload addfile
            for (i in 0 until nAddFileCnt) {

                var objCurAddFileItem = arObjFiles.get(i).asJsonObject
                var nDocNo = 1
                var strDocIRN = objCurAddFileItem.get("DOC_IRN").asString
                var strFilePath = objCurAddFileItem.get("FILE_PATH").asString

                if (SocketManager().upload(strFilePath, strDocIRN, nDocNo.toString(), "IMG_ADDFILE_X")) {
                    objRes.addProperty("DOC_IRN", strDocIRN)
                    arResAddFile.add(objRes)
                }

//                when(AGENT_EDMS.toUpperCase())
//                {
//                    "W" -> {
//                        if (SocketManager().upload(strFilePath, strDocIRN, nDocNo.toString(), "IMG_ADDFILE_X")) {
//                            objRes.addProperty("DOC_IRN", strDocIRN)
//                            arResAddFile.add(objRes)
//                        }
//                    }
////                    "X" -> {
////                        if (XvarmTransfer().upload(strFilePath, strDocIRN, nDocNo.toString(), "IMG_ADDFILE_X")) {
////                            objRes.addProperty("DOC_IRN", strDocIRN)
////                            arResAddFile.add(objRes)
////                        }
////                    }
//                }

            }

            //Check attach upload cnt
            var nDBUploadedCnt = CheckUploadAddFileCount(arResAddFile)

            if ((nDBUploadedCnt != nAddFileCnt) || nDBUploadedCnt <= 0) {
                objRes.addProperty("RESULT", UPLOAD_ADDFILE_FAILED)
                return objRes
            }



            for (i in 0 until arObjFiles.size()) {


                var strSDocNo = SocketManager().getSDocNo()
                if (!m_C.isBlank(strSDocNo)) {

                    if (!SocketManager().insertAddFileTable(arObjFiles.get(i).asJsonObject, strSDocNo!!, objDocumentInfo)) {
                        objRes.addProperty("RESULT", UPLOAD_ADDFILE_FAILED)
                        return objRes
                    }
                }
                else
                {
                    objRes.addProperty("RESULT", UPLOAD_ADDFILE_FAILED_CREATE_KEY)
                    return objRes
                }
            }
        }
        return objRes
    }




//    //Insert to Table
//    fun insertOriginalImageToTable(strDocIRN:String):String? {
//
//        var sb
//String Slipquery    = "insert into img_slip_t(\"DOC_IRN\",\"CABINET\",\"SDOC_NO\",\"SLIP_NO\",\"UPDATE_TIME\",\"FILE_CNT\",\"FOLDER\",\"FILE_SIZE\",\"SLIP_FLAG\")" +
//		//										              "values('"+arrstrSliptDoc_irn[i]+"','"+common.getToday()+"','"+strSdoc_no+"','"+String.format("%04d", i)+"','"+common.GetCurtime()+"','"+strFileCnt+"','"+common.getContext().getString(R.string.folder)+"','"+common.GetFileSize(newFilePath)+"')";
//															"values('"+arrstrSliptDoc_irn[i]+"','"+TC.getToday()+"','"+strSdoc_no+"','"+String.format("%04d", i)+"','"+TC.GetCurtime()+"','"+strFileCnt+"','"+folder+"','"+TC.GetFileSize(newFilePath)+"','1')";
//
//								strSlipresult = T.SetData("Q", Slipquery, charset);
//        var strRes:String? = null
//
//        var strImgPath = objOriginal.get("OriginalPath").asString
//        var strDocIRN = m_WDM.getDocIRN(g_UserInfo.strUserID,  m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)
//        if("SOCKET" == CONNECTION_PROTOCOL)
//        {
//            if(SocketManager().upload(strImgPath, strDocIRN, nDocNo.toString(), "IMG_SLIP_X"))
//            {
//                strRes = strDocIRN
//            }
//        }
//        return strRes
//    }
}