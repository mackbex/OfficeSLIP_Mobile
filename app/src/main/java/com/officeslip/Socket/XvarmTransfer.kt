package com.officeslip.Socket

import com.google.gson.JsonObject
import com.officeslip.*
import com.officeslip.Util.Common
import com.officeslip.Util.Logger
import com.windfire.apis.asys.asysUsrElement
import com.windfire.apis.asysConnectData
import java.io.File
import java.lang.Exception


class XvarmTransfer {

//    private var conXvarm: asysConnectData? = null
//    private val m_C = Common()
//
//
//    fun upload(strFilePath:String, strDocIRN:String, strDocNo:String, strTable:String):Boolean
//    {
//
//        conXvarm =  asysConnectData(XVARM_IP, XVARM_PORT, XVARM_CLIENT, XVARM_ID, XVARM_PW)
//        var folder:String? = null
//		var strCC:String? = null
//
//        var uploadFile = File(strFilePath)
//		var fileSize = uploadFile.length()
//		var strCabinet = m_C.getDate("yyyyMMdd")
//
//		when(strTable.toUpperCase())
//		{
//			"IMG_SLIP_M" -> {
//				folder		=	XVARM_FOLDER_IMG_SLIP_M
//				strCC		=	XVARM_CC_IMG_SLIP_M
//			}
//			"IMG_SLIP_X" -> {
//				folder		=	XVARM_FOLDER_IMG_SLIP
//				strCC		=	XVARM_CC_IMG_SLIP
//			}
//			"IMG_SLIPDOC_X" -> {
//				folder		=	XVARM_FOLDER_IMG_SLIPDOC
//				strCC		=	XVARM_CC_IMG_SLIPDOC
//			}
//			"IMG_ADDFILE_X" -> {
//				folder		=	XVARM_FOLDER_IMG_ADDFILE
//				strCC		=	XVARM_CC_IMG_ADDFILE
//			}
//			else -> {
//				return false
//			}
//		}
//
//
//		var idxInfo = StringBuffer().apply {
//			append("DOC_IRN=")
//			append(strDocIRN)
//			append(";CABINET=")
//			append(strCabinet)
//			append(";FOLDER=")
//			append(folder)
//			append(";FILE_NAME=")
//			append(uploadFile.name)
//			append(";FILE_SIZE=")
//			append(fileSize)
//			append(";PAGE_NUM=")
//			append(strDocNo)
//			append(";VERIFY=")
//			append("   ")
//
//		}
//
//		var objRes = uploadFile(strTable, idxInfo.toString(), strCC!!, uploadFile)
//
//		var strRes = "F"
//		var strMsg = ""
//		objRes?.let {
//			if(it.get("RESULT") != null) strRes = it.get("RESULT").asString else "F"
//		if(it.get("MSG") != null) strRes = it.get("MSG").asString else "F"
//		}
//
//		return "T".equals(strRes, true)
//    }
//
//
//	fun uploadFile(strIndexId:String, strIndexInfo:String, strCC:String, uploadFile:File):JsonObject {
//		var objRes = JsonObject()
//		objRes.addProperty("RESULT", "F")
//
//		var bRes = true
//		if (conXvarm == null) {
//			objRes.addProperty("MSG", "FILE INSERT FAIL : The xtorm not conneted.")
//			bRes = false
//		}
//		if (m_C.isBlank(strIndexId)) {
//			objRes.addProperty("MSG", "FILE INSERT FAIL : There are no ID of index.")
//			bRes = false
//		}
//		if (m_C.isBlank(strCC)) {
//			objRes.addProperty("MSG", "FILE INSERT FAIL : There are no lifecycle information")
//			bRes = false
//		}
//
//		if(!bRes)
//		{
//			return objRes
//		}
//
//		var listIndexInfo 			= strIndexInfo.split(";")
//		var mapField:HashMap<String, String> 	= HashMap()
//
//		if (listIndexInfo.isNotEmpty()) {
//		//	arrField = new String[arrIndexInfo.length][2];
//
//			for(i in 0 until listIndexInfo.size)
//			{
//				var listTemp = listIndexInfo[i].split("=")
//				if(listTemp.size != 2)
//				{
//					objRes.addProperty("MSG", "FILE INSERT FAIL : invalid index information")
//					return objRes
//				}
//
//				mapField.put(listTemp[0], listTemp[1])
//		//		mapField.put(i, listTemp[1].trim())
//			}
////			for (int i=0; i < arrIndexInfo.length; i++) {
////				String[] arrTemp2 = arrIndexInfo[i].split("=");
////
////				if (2 != arrTemp2.length) {
////
////				}
////
////				arrField[i][0] = arrTemp2[0].trim();
////				arrField[i][1] = arrTemp2[1].trim();
////			}
//		}
//
//		var filePath = uploadFile.absolutePath
//
//		if (!uploadFile.exists()) {
//			objRes.addProperty("MSG", "FILE INSERT FAIL : There are no file for the insert. $filePath")
//			return objRes;
//		}
//		if (uploadFile.length() <= 0) {
//			objRes.addProperty("MSG", "FILE INSERT FAIL : The file size 0 byte. $filePath")
//			return objRes;
//
//		}
//
//		try {
//			val ue = asysUsrElement(conXvarm)
//			ue.setInfo("", "SUPER", "IMAGE", filePath)
//
//			mapField.forEach {
//				(key, value) ->
//					if(!m_C.isBlank(value)) {
//						ue.addIndexValue(strIndexId, key, value)
//					}
//			}
//
//			ue.m_descr 		= "IMAGE"
//			ue.m_cClassId = strCC
//			var nReturn 	= ue.create(XVARM_GATEWAY)
//
//			if (nReturn == 0) {
//				objRes.addProperty("RESULT", "T")
//				objRes.addProperty("MSG", "")
//			} else {
//				objRes.addProperty("MSG", "FILE INSERT FAIL : " + ue.lastError)
//			}
//			ue.clearIndexes()
//		//	ue = null
//		} catch(e:Exception) {
//			//m_strError = "FILE INSERT FAIL [E] : " + e.getMessage();
//			Logger.WriteException(this@XvarmTransfer.javaClass.name, "XvarmFile - importFile", e, 5)
//		}
//
//		return objRes
//	}
}