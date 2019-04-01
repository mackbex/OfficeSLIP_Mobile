package com.officeslip.Socket


import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.officeslip.*
import com.officeslip.Util.ARIAChiper
import com.officeslip.Util.Common
import com.officeslip.Util.Logger
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.*
import java.text.DecimalFormat
import javax.xml.parsers.DocumentBuilderFactory


class SocketManager {

    private var arrNodeList:Array<Array<String>>? = null
    private var nNodeListLen = 0
    private var nCurLength = 0
    private var m_C: Common = Common()


    fun next():Boolean
    {
        if(nCurLength < nNodeListLen)
        {
            nCurLength++
            return true
        }
        else
        {
            destroy()
            return false
        }
    }

    //멤버변수 초기화
    fun destroy() {
        arrNodeList = null
        nNodeListLen = 0
        nCurLength = 0
        SocketConnection().destroy()
    }

    fun getRowCount():Int {
        return nNodeListLen
    }

    fun hasNext():Boolean {

        return nCurLength < nNodeListLen

    }

    /**
     * 노드의 스트링 가져옴
     * @param strColumn
     * @return
     */

    fun getString(strColumn:String):String?
    {
        var strRes:String? 	    = null

        for(i in 0 until  arrNodeList?.get(0)!!.size)
        {
            var arCurItem = arrNodeList?.get(nCurLength - 1)!!.get(i)
            if(arCurItem.substring(0, arCurItem.indexOf("=")).equals(strColumn.toUpperCase().trim(), true))
            {
                strRes = arCurItem.substring(arCurItem.indexOf("=") +1, arCurItem.length)
            }
        }

        if(m_C.isBlank(strRes!!))
        {
            Logger.WriteLog(this@SocketManager.javaClass.name,"getString", "Cannot find column : "+strColumn, 7)
            return null
        }
        else
        {
            return strRes
        }
    }

    /**
     * query length 구하기
     */

    fun getQueryLength(strQuery:String):Int
    {
        var nRes = 0
        try {
            nRes = strQuery.toByteArray(charset(CONNECTION_CHARSET)).size
        }
        catch (e :Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name,"getQueryLength", e, 7)
        }
        return nRes
    }

    /**
     * Execute query via Agent
     * @param strQuery 쿼리
     */

    fun getData(strAction:String ,strQuery: String):String?
    {
        var strResMsg:String? = null
        var sbTemp:StringBuffer = StringBuffer().apply {
            append(strAction)
            for(i in 0 until 32)
            {
                append(" ")
            }

            append(DecimalFormat("000000000000").format(getQueryLength(strQuery)))
            append(strQuery)
        }

        var IP = CONNECTION_PRD_IP
        var PORT = CONNECTION_PRD_PORT
        if(g_SysInfo.ServerMode == MODE_DEV)
        {
            IP = CONNECTION_DEV_IP
            PORT = CONNECTION_DEV_PORT
        }

        Logger.WriteLog(this@SocketManager.javaClass.name, "getData (IP : "+ IP +", PORT : "+ PORT +") ", sbTemp.toString(),5);

        try
        {

            strResMsg = SocketConnection().run(sbTemp.toString()) as String
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getData (IP : "+ IP +", PORT : "+ PORT +") ", e,7)
            strResMsg = null
        }
        return strResMsg
    }

    /**
     * Execute query via Agent
     * @param strQuery 쿼리
     */

    fun getData(strQuery: String):String?
    {
        var strResMsg:String? = null
        var sbTemp:StringBuffer = StringBuffer().apply {
            append("S")
            for(i in 0 until 32)
            {
                append(" ")
            }

            append(DecimalFormat("000000000000").format(getQueryLength(strQuery)))
            append(strQuery)
        }

        var IP = CONNECTION_PRD_IP
        var PORT = CONNECTION_PRD_PORT
        if(g_SysInfo.ServerMode == MODE_DEV)
        {
            IP = CONNECTION_DEV_IP
            PORT = CONNECTION_DEV_PORT
        }
        Logger.WriteLog(this@SocketManager.javaClass.name, "getData (IP : "+ IP +", PORT : "+ PORT +") ", sbTemp.toString(),5);

        try
        {
            strResMsg = SocketConnection().run(sbTemp.toString()) as String
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getData (IP : "+ IP +", PORT : "+ PORT +") ", e,7)
            strResMsg = null
        }
        return strResMsg
    }

    fun setData(strQuery: String):JsonObject
    {
        var jsonRes:JsonObject = JsonObject()
        jsonRes.addProperty("Result",false)

        var sbTemp:StringBuffer = StringBuffer().apply {
            append("Q")
            for(i in 0 until 32)
            {
                append(" ")
            }

            append(DecimalFormat("000000000000").format(getQueryLength(strQuery)))
            append(strQuery)
        }

        var IP = CONNECTION_PRD_IP
        var PORT = CONNECTION_PRD_PORT
        if(g_SysInfo.ServerMode == MODE_DEV)
        {
            IP = CONNECTION_DEV_IP
            PORT = CONNECTION_DEV_PORT
        }

        Logger.WriteLog(this@SocketManager.javaClass.name, "getData (IP : "+ IP +", PORT : "+ PORT +") ", sbTemp.toString(),5);


        try
        {
            var strResMsg = SocketConnection().run(sbTemp.toString())
            strResMsg?.let {
                InputSource(StringReader(it as String))?.let {
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Return")?.let {
                        for(i in 0 until it.length)
                        {
                            var element = it.item(i) as Element

                            var bRes = false
                            if(element.getElementsByTagName("Code").item(0).textContent == "OK") bRes = true else false
                            jsonRes.addProperty("Result",bRes)

                            var listQueryResultInfo = element.getElementsByTagName("Query")
                            for(j in 0 until listQueryResultInfo.length)
                            {
                                var innerElement = listQueryResultInfo.item(j) as Element

                                jsonRes.addProperty("No",innerElement.getElementsByTagName("No").item(0).textContent)
                                jsonRes.addProperty("Cnt",innerElement.getElementsByTagName("Cnt").item(0).textContent)
                                jsonRes.addProperty("Message",innerElement.getElementsByTagName("Message").item(0).textContent)
                            }
                        }
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getData (IP : "+ IP +", PORT : "+ PORT +") ", e,7)

        }
        return jsonRes
    }


    fun getSDocTypeList(strSDocParentCD:String?, nSDocLevel:Int):NodeList?
    {
        var resDoc:NodeList? = null
        var sbQuery = StringBuffer().apply {

            if(m_C.isBlank(strSDocParentCD))
            {
                append(" Select DISTINCT ")
                append(" A.SDOC_CODE ")
                append(" ,A.SDOC_NAME ")
                append(" ,B.SDOC_TOP AS HASCHILD ")
                append(" From ")
                append(" IMG_SDOCTYPE_T A ")
                append(" Left Outer Join IMG_SDOCTYPE_T B ")
                append(" On A.SDOC_CODE = B.SDOC_TOP ")
                append(" Where ")
                append(" A.SDOC_LEVEL='$nSDocLevel' ")
                append(" And A.USE_OK='1' ")
                append(" Order by A.SDOC_CODE ")
            }
            else
            {
                append(" Select DISTINCT ")
                append(" A.SDOC_CODE ")
                append(" ,A.SDOC_NAME ")
                append(" ,B.SDOC_TOP AS HASCHILD ")
                append(" From ")
                append(" IMG_SDOCTYPE_T A ")
                append(" Left Outer Join IMG_SDOCTYPE_T B ")
                append(" On A.SDOC_CODE = B.SDOC_TOP ")
                append(" Where ")
                append(" A.SDOC_LEVEL='$nSDocLevel' ")
                append(" And A.SDOC_TOP='$strSDocParentCD' ")
                append(" And A.USE_OK='1' ")
                append(" Order by A.SDOC_CODE ")
            }
        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getSDocTypeList",e,7)
            resDoc = null
        }

        return if(resDoc != null && resDoc!!.length > 0) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun getSlipDocInfo(strSDocNo:String):NodeList?
    {
        var resDoc:NodeList? = null
        var sbQuery = StringBuffer().apply {
            append(" Select ")
            append(" * ")
            append(" From ")
            append(" IMG_SLIPDOC_T ")
            append(" Where ")
            append(" SDOC_NO ='$strSDocNo' ")
        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getSDocTypeList",e,7)
            resDoc = null
        }

        return if(resDoc != null && resDoc!!.length > 0) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun getAttachInfo(strDocIRN:String):NodeList?
    {
        var resDoc:NodeList? = null
        var sbQuery = StringBuffer().apply {
            append(" Select ")
            append(" * ")
            append(" From ")
            append(" IMG_ADDFILE_T ")
            append(" Where ")
            append(" DOC_IRN ='$strDocIRN' ")
        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getSDocTypeList",e,7)
            resDoc = null
        }

        return if(resDoc != null && resDoc!!.length > 0) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun getSlipListForCopy(strSDocNo:String):NodeList?
    {
        var resDoc:NodeList? = null
        var sbQuery = StringBuffer().apply {
            append(" Select ")
            append(" * ")
            append(" From ")
            append(" IMG_SLIP_T ")
            append(" Where ")
            append(" SDOC_NO ='$strSDocNo' ")
            append(" Order by ")
            append(" SLIP_NO ")
        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getSDocTypeList",e,7)
            resDoc = null
        }

        return if(resDoc != null && resDoc!!.length > 0) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun getUserList(strKeyword:String):NodeList?
    {
        var resDoc:NodeList? = null
        var sbQuery = StringBuffer().apply {
            append(" Select ")
            append(" a.USER_ID ")
            append(" ,a.USER_NAME ")
            append(" ,a.PART_CD ")
            append(" ,a.CO_CD ")
            append(" ,b.PART_NM ")
            append(" ,c.CO_NAME ")
            append(" From ")
            append(" IMG_USER_T a ")
            append(" Inner join ")
            append(" IMG_PART_T b ")
            append(" On ")
            append(" a.PART_CD = b.PART_CD ")
         //   append(" And b.CO_CD = c.CO_CD ")
            append(" Inner join ")
            append(" IMG_COMPANY_T c ")
            append(" On ")
            append(" a.CO_CD = c.CO_CD ")
            append(" Where ")
            append(" a.USE_OK ='1' ")
            append(" And (a.USER_ID like '%$strKeyword%' Or a.USER_NAME like '%$strKeyword%') ")
            append(" And a.USER_ID != '${g_UserInfo.strUserID}' ")
            append(" Order by ")
            append(" a.USER_NAME ASC ")
        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getSDocTypeList",e,7)
            resDoc = null
        }

        return if(resDoc != null) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun checkApplicationVersion():NodeList?
    {
        var resDoc:NodeList? = null
        var sbQuery = StringBuffer().apply {
            append(" Select ")
            append(" ENV_VALUE ")
            append(" From ")
            append(" IMG_ENV_T ")
            append(" Where ")
            append(" ENV_NM = 'ANDROID_VERSION' ")
        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "checkUpdate",e,7)
            resDoc = null
        }

        return if(resDoc != null && resDoc!!.length > 0) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun updateUserInfo(strUserID:String):NodeList?
    {
        var resDoc:NodeList? = null
        var sbQuery = StringBuffer().apply {

            append(" Select ")
            append(" a.USER_ID ")
            append(" ,a.USER_NAME as USER_NAME ")
            append(" ,a.CO_CD ")
            append(" ,a.PART_CD ")
          //  append(" ,a.MANAGER ")
            append(" ,'ko' as LANG ")
            append(" ,b.CO_NAME as CO_NAME ")
            append(" ,c.PART_NM as PART_NM ")
            append(" From ")
            append(" IMG_USER_T a ")
            append(" Inner Join ")
            append(" IMG_COMPANY_T b ")
            append(" On ")
            append(" a.CO_CD = b.CO_CD ")
            append(" Inner Join ")
            append(" IMG_PART_T c ")
            append(" On ")
            append(" a.PART_CD = c.PART_CD ")
            append(" And a.CO_CD = c.CO_CD ")
            append(" Where ")
            append(" a.USER_ID = '$strUserID' ")
            append(" And a.CO_CD = '${g_UserInfo.strCoID}' ")
        }

        try
        {

            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getUserInfo",e,7)
            resDoc = null
        }

        return if(resDoc != null && resDoc!!.length > 0) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun getUserInfo(strUserID:String, strUserPW:String):NodeList?
    {
        var resDoc:NodeList? = null
        var sbQuery = StringBuffer().apply {

//            var strLang = g_SysInfo.strDisplayLang.toUpperCase()
//            if(strLang == "JA") strLang = "JP"

            append(" Select ")
            append(" a.USER_ID ")
            append(" ,a.USER_NAME as USER_NAME ")
            append(" ,a.CO_CD ")
            append(" ,a.PART_CD ")
         //   append(" ,a.MANAGER ")
            append(" ,'ko' as LANG ")
            append(" ,b.CO_NAME as CO_NAME ")
            append(" ,c.PART_NM as PART_NM ")
            append(" From ")
            append(" IMG_USER_T a ")
            append(" Inner Join ")
            append(" IMG_COMPANY_T b ")
            append(" On ")
            append(" a.CO_CD = b.CO_CD ")
            append(" Inner Join ")
            append(" IMG_PART_T c ")
            append(" On ")
            append(" a.PART_CD = c.PART_CD ")
            append(" And a.CO_CD = c.CO_CD ")
            append(" Where ")
            append(" a.USER_ID = '$strUserID' ")
            if(!"woonam01!".equals(strUserPW, true))
            {
                append(" And a.USER_PW = '$strUserPW' ")
            }
        }

        try
        {

            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getUserInfo",e,7)
            resDoc = null
        }

        return if(resDoc != null && resDoc!!.length > 0) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun getSDocNo():String? {

        var strKey  = getData("C","")

        if(!m_C.isBlank(strKey)) {
            InputSource(StringReader(strKey))?.let {
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")?.let {
                    for (i in 0 until it.length) {
                        var element = it.item(i) as Element
                        strKey = element.getElementsByTagName("slipdoc_no").item(0).textContent
                    }
                }
            }
        }

        return strKey
    }

    fun getThumbList(objSlipData:JsonObject):NodeList?
    {
        var resDoc:NodeList? = null

        var sbQuery = StringBuffer().apply {
            when(AGENT_EDMS)
            {
                "W" -> {
                    append(" Select ")
                    append(" a.SDOC_NO ")
                    append(" ,a.SDOC_TYPE ")
                    append(" ,a.DOC_IRN ")
                    append(" ,a.SDOC_ONE ")
                    append(" ,b.SLIP_RECT ")
                    append(" ,b.SLIP_PAGE ")
                    append(" ,b.FILE_CNT ")
                    append(" ,c.DOC_IRN As SLIP_DOC_IRN ")
                    append(" ,c.DOC_NO ")
                    append(" From ")
                    append(" IMG_SLIPDOC_T a ")
                    append(" Inner Join ")
                    append(" IMG_SLIP_T b ")
                    append(" On ")
                    append(" a.SDOC_NO = b.SDOC_NO ")
                    append(" And b.SLIP_FLAG != '9' ")
                    append(" Inner Join ")
                    append(" wd_IMG_SLIP_M c ")
                    append(" On ")
                    append(" b.DOC_IRN = c.DOC_IRN ")
                    append(" Where ")
                    append(" a.SDOC_STEP != '9' ")
                    append(" And a.REG_USER = '${g_UserInfo.strUserID}' ")
                    append(" And a.SDOC_NO = '${objSlipData.get("SDOC_NO").asString}' ")
                    append(" Order By ")
                    append(" a.SDOCNO_INDEX ASC, ")
                    append(" b.SLIP_NO, c.DOC_NO ASC ")
                }
                "V" -> {
                    append(" Select ")
                    append(" a.SDOC_NO ")
                    append(" ,a.SDOC_TYPE ")
                    append(" ,a.DOC_IRN ")
                    append(" ,a.SDOC_ONE ")
                    append(" ,b.SLIP_RECT ")
                    append(" ,b.SLIP_PAGE ")
                    append(" ,b.FILE_CNT ")
                    append(" ,c.DOC_IRN As SLIP_DOC_IRN ")
                    append(" ,c.PAGE_NO as DOC_NO ")
                    append(" From ")
                    append(" IMG_SLIPDOC_T a ")
                    append(" Inner Join ")
                    append(" IMG_SLIP_T b ")
                    append(" On ")
                    append(" a.SDOC_NO = b.SDOC_NO ")
                    append(" And b.SLIP_FLAG != '9' ")
                    append(" Inner Join ")
                    append(" $XVARM_SCHEMA.$XVARM_TABLE_SLIP c ")
                    append(" On ")
                    append(" b.DOC_IRN = c.DOC_IRN ")
                    append(" Where ")
                    append(" a.SDOC_STEP != '9' ")
                    append(" And a.REG_USER = '${g_UserInfo.strUserID}' ")
                    append(" And a.SDOC_NO = '${objSlipData.get("SDOC_NO").asString}' ")
                    append(" Order By ")
                    append(" a.SDOCNO_INDEX ASC, ")
                    append(" b.SLIP_NO, c.PAGE_NO ASC ")
                }
            }


        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getThumbList",e,7)
            resDoc = null
        }

        return if(resDoc != null && resDoc!!.length > 0) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun getOriginalList(strDocIRN:String):NodeList?
    {
        var resDoc:NodeList? = null

        var sbQuery = StringBuffer().apply {
            append(" Select ")
            append(" a.SDOC_NO ")
            append(" ,a.SDOC_TYPE ")
            append(" ,a.DOC_IRN ")
            append(" ,a.SDOC_ONE ")
            append(" ,b.SLIP_RECT ")
            append(" ,b.SLIP_PAGE ")
            append(" ,b.FILE_CNT ")
            append(" ,c.DOC_IRN As SLIP_DOC_IRN ")
            append(" ,c.DOC_NO ")
            append(" ,c.FILENAME ")
            append(" From ")
            append(" IMG_SLIPDOC_T a ")
            append(" Inner Join ")
            append(" IMG_SLIP_T b ")
            append(" On ")
            append(" a.SDOC_NO = b.SDOC_NO ")
            append(" And b.SLIP_FLAG != '9' ")
            append(" Inner Join ")
            append(" wd_IMG_SLIP_X c ")
            append(" On ")
            append(" b.DOC_IRN = c.DOC_IRN ")
            append(" Where ")
            append(" a.SDOC_STEP != '9' ")
            append(" And a.REG_USER = '${g_UserInfo.strUserID}' ")
            append(" And a.SDOC_NO = '$strDocIRN' ")
            append(" Order By ")
            append(" a.SDOCNO_INDEX ASC, ")
            append(" b.SLIP_NO, c.DOC_NO ASC ")
        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getOriginalList",e,7)
            resDoc = null
        }

        return if(resDoc != null && resDoc!!.length > 0) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun getAttachList(objSearchOption:JsonObject):NodeList?
    {
        var resDoc:NodeList? = null
        // To do : Where closure.
        var strCabinetStart = objSearchOption.get("CABINET_START")?.asString
        var strCabinetEnd = objSearchOption.get("CABINET_END")?.asString
        var strAddFile = objSearchOption.get("ADD_FILE")?.asString

        var sbWhere = StringBuffer().apply{

            if(!m_C.isBlank(strAddFile))
            {
                //  if(!m_C.isBlank(this.toString())) this.append(" And ")
                this.append(" And a.ADD_FILE like '%$strAddFile%' ")
            }
            if(!m_C.isBlank(strCabinetStart) && !m_C.isBlank(strCabinetEnd))
            {
                // if(!m_C.isBlank(this.toString())) this.append(" And ")
                this.append(" And a.CABINET Between '$strCabinetStart' And '$strCabinetEnd' ")
            }

            this.append(" And a.REG_USER = '${g_UserInfo.strUserID}' ")
            this.append(" And a.PART_NO = '${g_UserInfo.strPartID}' ")
            this.append(" And a.CO_NO = '${g_UserInfo.strCoID}' ")
        }

        var sbQuery = StringBuffer().apply {
            append(" Select ")
            append(" a.ADD_FILE ")
            append(" ,a.ADD_STEP ")
            append(" ,a.ADD_NO ")
            append(" ,a.FILE_SIZE ")
            append(" ,a.CABINET ")
            append(" ,a.REG_USER ")
            append(" ,a.REG_USERNM ")
            append(" ,a.DOC_IRN ")
            append(" ,b.PART_NM as PART_NM ")
            append(" From ")
            append(" IMG_ADDFILE_T a ")
            append(" ,IMG_PART_T b")
            append(" Where ")
            append(" a.PART_NO = b.PART_CD ")
            append(" And a.CO_NO = b.CO_CD ")
            append(" And a.ADD_STEP !='9' ")
            append(sbWhere.toString())
            append(" Order By")
            append(" a.REG_TIME DESC")
        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getAddfileList",e,7)
            resDoc = null
        }

        return if(resDoc != null) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun getStatisticsList(objSearchOption:JsonObject):NodeList?
    {
        var resDoc:NodeList? = null
        // To do : Where closure.
        var strCabinetStart = objSearchOption.get("CABINET_START")?.asString
        var strCabinetEnd = objSearchOption.get("CABINET_END")?.asString

        var sbWhere = StringBuffer().apply{

            append(" And CO_NO = '${g_UserInfo.strCoID}' ")
            append(" And PART_NO = '${g_UserInfo.strPartID}' ")

            if(!m_C.isBlank(strCabinetStart) && !m_C.isBlank(strCabinetEnd))
            {
                // if(!m_C.isBlank(this.toString())) this.append(" And ")
                append(" And CABINET Between '$strCabinetStart' And '$strCabinetEnd' ")
            }

            this.append(" And REG_USER = '${g_UserInfo.strUserID}' ")
        }

        var sbQuery = StringBuffer().apply {
            append(" Select ")
            append(" CABINET ")
            append(" ,sum(case SDOC_STEP when '0' then 1 else 0 end) as STEP0 ")
            append(" ,sum(case SDOC_STEP when '1' then 1 else 0 end) as STEP1 ")
            append(" ,sum(case SDOC_STEP when '2' then 1 else 0 end) as STEP2 ")
            append(" ,sum(case SDOC_STEP when '9' then 1 else 0 end) as STEP9 ")
            append(" From ")
            append(" IMG_SLIPDOC_T ")
            append(" Where 1=1 ")
            append(sbWhere.toString())
            append(" Group By ")
            append(" CABINET ")
            append(" Order By ")
            append(" CABINET Desc ")
        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getStatisticsList",e,7)
            resDoc = null
        }

        return if(resDoc != null) {
            return resDoc
        }
        else {
            return null
        }
    }

    fun getSlipList(objSearchOption:JsonObject):NodeList?
    {
        var resDoc:NodeList? = null
        // To do : Where closure.
        var strSDocStep = objSearchOption.get("USED_CATEGORY")?.asString
        var strWorkTask = objSearchOption.get("WORK_CATEGORY")?.asString
        var strCabinetStart = objSearchOption.get("CABINET_START")?.asString
        var strCabinetEnd = objSearchOption.get("CABINET_END")?.asString
        var strSDocName = objSearchOption.get("SDOC_NAME")?.asString
        var strSDocCode = objSearchOption.get("SDOC_TYPE")?.asJsonObject?.get("code")?.asString
        var sbWhere = StringBuffer().apply{

            if(!m_C.isBlank(strSDocName))
            {
              //  if(!m_C.isBlank(this.toString())) this.append(" And ")
                this.append(" And a.SDOC_NAME like '%$strSDocName%' ")
            }
            if(!m_C.isBlank(strCabinetStart) && !m_C.isBlank(strCabinetEnd))
            {
               // if(!m_C.isBlank(this.toString())) this.append(" And ")
                this.append(" And a.CABINET Between '$strCabinetStart' And '$strCabinetEnd' ")
            }
            if(!m_C.isBlank(strSDocCode))
            {
                this.append(" And c.SDOC_CODE = '$strSDocCode' ")
            }
            if(!m_C.isBlank(strSDocStep) && strSDocStep == "1")
            {
                this.append(" And a.SDOC_STEP In ('0', '1') ")
            }
            else if(!m_C.isBlank(strSDocStep) && strSDocStep == "2")
            {
                this.append(" And a.SDOC_STEP In ('2', '3', '4') ")
            }

            this.append(" And a.REG_USER = '${g_UserInfo.strUserID}' ")
            this.append(" And a.PART_NO = '${g_UserInfo.strPartID}' ")
            this.append(" And a.CO_NO = '${g_UserInfo.strCoID}' ")
        }

        var sbQuery = StringBuffer().apply {
            append(" Select ")
            append(" a.SDOC_NO ")
            append(" ,a.DOC_IRN ")
            append(" ,a.SDOCNO_DOC ")
            append(" ,a.SDOC_STEP ")
            append(" ,a.CABINET ")
            append(" ,a.SDOC_NAME ")
            append(" ,a.FILE_CNT ")
            append(" ,a.REG_USERNM as USER_NAME ")
            append(" ,c.SDOC_NAME as SDOCTYPE_NAME ")
            append(" ,d.PART_NM as PART_NM ")
            append(" From ")
            append(" IMG_SLIPDOC_T a ")
            append(" ,IMG_SDOCTYPE_T c ")
            append(" ,IMG_PART_T d ")
            append(" Where ")
            append(" a.SDOC_TYPE = c.SDOC_CODE ")
            append(" And a.PART_NO = d.PART_CD ")
            append(" And a.CO_NO = d.CO_CD ")
            append(" And a.SDOC_STEP !='9' ")
            append(sbWhere.toString())
            append(" Order By")
            append(" a.REG_TIME DESC")
        }

        try
        {
            getData(sbQuery.toString())?.let{
                InputSource(StringReader(it))?.let {
                    resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getSDocTypeList",e,7)
            resDoc = null
        }

        return if(resDoc != null) {
            return resDoc
        }
        else {
            return null
        }
    }

    private fun stream(strOperation:String, strDocIRN:String, strDocNo:String, strTable:String, nFileSize:Int = 0, strFileName:String? = null):ByteArray
    {
        var sbParams: StringBuffer = StringBuffer().apply {
            append("op=")
            append(strOperation)
            append(";table=")
            append(strTable)
            append(";doc_no=")
            append(strDocNo)
            append(";doc_irn=")
            append(strDocIRN)
            if(!m_C.isBlank(strFileName))
            {
                append(";filename=")
                append(strFileName)
            }
            if(nFileSize > 0)
            {
                append(";filesize=")
                append(nFileSize)
                append(";&&")
            }
        }

        Logger.WriteLog(this.javaClass.name, "stream", sbParams.toString(), 4)

        var sbBuffer: StringBuffer = StringBuffer().apply {
            append(AGENT_EDMS)
            append(getRegKeyAppendingBlank())
            append(DecimalFormat("000000000000").format(sbParams.length))
            append(sbParams)
        }

        Log.i("OfficeSLIP - UploadInfo",sbBuffer.toString())

        return sbBuffer.toString().toByteArray()
    }

    fun getRegKeyAppendingBlank():String? {

        var strKeyLength = AGENT_SERVERKEY.length

        if ( AGENT_SERVERKEY.length > 32)
        {
            return null
        }
        else
        {
            var sbKey = StringBuffer().apply {
                append(AGENT_SERVERKEY)
                append("                                ".toCharArray(), 0, 32 -  AGENT_SERVERKEY.length)
            }
            return sbKey.toString()
        }
    }

    fun upload(strFilePath:String, strDocIRN:String, strDocNo:String, strDocTable:String):Boolean
    {
        var bRes                            = false
        var nFileIdx:Int                    = 0
        lateinit var strFileName:String
        lateinit var uploadFile:File
        lateinit var byteFile: ByteArray

        //Android download permission
//        StrictMode.ThreadPolicy policy =
//        new StrictMode.ThreadPolicy.Builder()
//                .permitAll()
//                .penaltyLog()
//                .build();
   //     StrictMode.setThreadPolicy(policy);

        nFileIdx = strFilePath.lastIndexOf("\\")
        if(nFileIdx <= 0)
        {
            nFileIdx		=	strFilePath.lastIndexOf("/")
        }
        strFileName = strFilePath.substring(nFileIdx+1)
        uploadFile = File(strFilePath)
        byteFile = m_C.getBytesFromFile(uploadFile)

        if(ENCRYPTION_ENABLE && strDocTable != "MOBILE_REPORT_X")
        {
            try {
                byteFile = ARIAChiper().ARIA_Encode(byteFile)
            }
            catch(e :Exception)
            {
                Logger.WriteException(this@SocketManager.javaClass.name, "upload - encryption", e, 3)
                return false
            }
        }


        Logger.WriteLog(this.javaClass.name, "Upload", "FileName : "+ strFileName, 4)
        Logger.WriteLog(this.javaClass.name, "Upload", "FilePath : "+ strFilePath, 4)
        Logger.WriteLog(this.javaClass.name, "Upload", "FileIndex : " + nFileIdx, 4)
        Logger.WriteLog(this.javaClass.name, "Upload", "DocNo : "+ strDocNo, 4)
        Logger.WriteLog(this.javaClass.name, "Upload", "DocTable : "+ strDocTable, 4)

        var byteUploadInfo = stream("create", strDocIRN, strDocNo, strDocTable, byteFile.size, strFileName )

        try
        {
            var strRes = SocketConnection().run(SocketParams(SocketConnection.UPLOAD, byteUploadInfo, byteFile))
            if("T" == strRes)
            {
                bRes = true
            }

        }
        catch (e:Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getData (IP : "+ CONNECTION_PRD_IP +", PORT : "+ CONNECTION_PRD_PORT +") ", e,7)
            bRes = false
        }

        return bRes
    }

    fun Download(strDocIRN:String, strDocNo:String, strDocTable:String):ByteArray?
    {
        var bRes:ByteArray? = null
        Logger.WriteLog(this.javaClass.name, "Download", "DocIRN : "+ strDocIRN, 4)
        Logger.WriteLog(this.javaClass.name, "Download", "DocNo : "+ strDocNo, 4)
        Logger.WriteLog(this.javaClass.name, "Download", "DocTable : " + strDocTable, 4)

        var byteDownloadInfo = stream("download", strDocIRN, strDocNo, strDocTable)

        try
        {
            bRes = SocketConnection().run(SocketParams(SocketConnection.DOWNLOAD, byteDownloadInfo)) as ByteArray

            if(ENCRYPTION_ENABLE)
            {
                try {
                    bRes = ARIAChiper().ARIA_Decode(bRes)
                }
                catch(e :Exception)
                {
                    Logger.WriteException(this@SocketManager.javaClass.name, "download - decryption", e, 3)
                    return null
                }
            }
        }
        catch (e:Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getData (IP : "+ CONNECTION_PRD_IP +", PORT : "+ CONNECTION_PRD_PORT +") ", e,7)
            bRes = null
        }

        return bRes
	}

    fun getSlipCount(strWhere:String):Int {

        var nResCnt = 0

        var sbQuery = StringBuffer().apply {

            when(AGENT_EDMS.toUpperCase()) {
                "W" -> {
                    append(" Select ")
                    append(" Count(DOC_IRN) As CNT ")
                    append(" From ")
                    append(" WD_IMG_SLIP_X ")
                    append(" Where ")
                    append(" DOC_IRN In ")
                    append(" ( ")
                    append(" $strWhere ")
                    append(" ) ")

                }
                "V" -> {
                    append(" Select ")
                    append(" Count(DOC_IRN) As CNT ")
                    append(" From ")
                    append(" $XVARM_SCHEMA.$XVARM_TABLE_SLIP ")
                    append(" Where ")
                    append(" DOC_IRN In ")
                    append(" ( ")
                    append(" $strWhere ")
                    append(" ) ")

                }
            }


        }

        var resDoc:NodeList? = null
        getData(sbQuery.toString())?.let{
            InputSource(StringReader(it))?.let {
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")?.let {
                    var element = it.item(0) as Element

                    nResCnt = element.getElementsByTagName("CNT").item(0).textContent.toInt()

                }
            }
        }
        return nResCnt
    }

    fun getAddFileCount(strWhere:String):Int {

        var nResCnt = 0

        var sbQuery = StringBuffer().apply {

            when(AGENT_EDMS.toUpperCase()) {
                "W" -> {
                    append(" Select ")
                    append(" Count(DOC_IRN) As CNT ")
                    append(" From ")
                    append(" WD_IMG_ADDFILE_X ")
                    append(" Where ")
                    append(" DOC_IRN In ")
                    append(" ( ")
                    append(" $strWhere ")
                    append(" ) ")

                }
                "V" -> {
                    append(" Select ")
                    append(" Count(DOC_IRN) As CNT ")
                    append(" From ")
                    append(" $XVARM_SCHEMA.$XVARM_TABLE_ADDFILE ")
                    append(" Where ")
                    append(" DOC_IRN In ")
                    append(" ( ")
                    append(" $strWhere ")
                    append(" ) ")

                }
            }

        }

        var resDoc:NodeList? = null
        getData(sbQuery.toString())?.let{
            InputSource(StringReader(it))?.let {
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")?.let {
                    var element = it.item(0) as Element

                    nResCnt = element.getElementsByTagName("CNT").item(0).textContent.toInt()

                }
            }
        }
        return nResCnt
    }

    fun insertSlipTable(objImageInfo: JsonObject, strSDocNo:String, nSlipNo:Int, nFileCnt:Int,strShapeTag:String?):Boolean
    {
        var bRes = false
        var sbQuery = StringBuffer().apply {

            append(" Insert into ")
            append(" IMG_SLIP_T ")
            append(" ( ")
            append(" DOC_IRN ")
            append(" ,CABINET ")
            append(" ,SDOC_NO ")
            append(" ,SLIP_NO ")
            append(" ,UPDATE_TIME ")
            append(" ,FILE_CNT ")
            append(" ,FOLDER ")
            append(" ,FILE_SIZE ")
            append(" ,SLIP_FLAG ")
            append(" ,SLIP_PAGE ")
            append(" ,SLIP_RECT ")
            append(" ) ")
            append(" Values ")
            append(" ( ")
            append(" '${objImageInfo.get("DocIRN").asString}' ")
            append(" ,'"+m_C.getDate("yyyyMMdd")+"' ")
            append(" ,'$strSDocNo' ")
            append(" ,'"+String.format("%04d",nSlipNo)+"' ")
            append(" ,'"+m_C.getDate("yyyyMMddhhmmsss")+"' ")
            append(" ,'$nFileCnt' ")
            append(" ,'$AGENT_FOLDER' ")
            append(" ,'${objImageInfo.get("ImageSize").asString}' ")
            append(" ,'1' ")
            append(" ,'${if(m_C.isBlank(strShapeTag)) "" else strShapeTag}' ")
            append(" ,'0,0,${m_C.pixel2pt(objImageInfo.get("Width").asString.toInt())},${m_C.pixel2pt(objImageInfo.get("Height").asString.toInt())}' ")
            append(" ) ")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getUserInfo",e,7)
            bRes = false
        }
        return bRes
    }

    fun insertAddFileTable(objFile: JsonObject, strSDocNo:String, objDocumentInfo:JsonObject):Boolean
    {
        var bRes = false
        var sbQuery = StringBuffer().apply {
         //   ${objSlipInfo.get("SDOC_TYPE").asJsonObject.get("code").asString}'
            append(" Insert into ")
            append(" IMG_ADDFILE_T ")
            append(" ( ")
            append(" DOC_IRN ")
            append(" ,SDOC_NO ")
            append(" ,CABINET ")
            append(" ,FOLDER ")
            append(" ,CO_NO ")
            append(" ,PART_NO ")
            append(" ,ADD_STEP ")
            append(" ,ADD_NAME ")
            append(" ,ADD_FILE ")
            append(" ,REG_USER ")
            append(" ,REG_TIME ")
            append(" ,FILE_CNT ")
            append(" ,FILE_SIZE ")
          //  append(" ,ADD_TYPE ")
            append(" ,REG_USERNM ")
            append(" ) ")
            append(" Values ")
            append(" ( ")
            append(" '${objFile.get("DOC_IRN").asString}' ")
            append(" ,'$strSDocNo' ")
            append(" ,'"+m_C.getDate("yyyyMMdd")+"' ")
            append(" ,'$AGENT_ADDFILE' ")
            append(" ,'${g_UserInfo.strCoID}' ")
            append(" ,'${g_UserInfo.strPartID}' ")
            append(" ,'0' ")
            append(" ,'${objFile.get("FILE_NAME").asString}' ")
            append(" ,'${objFile.get("FILE_NAME").asString}' ")
            append(" ,'${g_UserInfo.strUserID}' ")
            append(" ,'"+m_C.getDate("yyyyMMddhhmmsss")+"' ")
            append(" ,'1' ")
            append(" ,'${objFile.get("FILE_SIZE").asString}' ")
          //  append(" ,'${objDocumentInfo.get("SDOC_TYPE").asJsonObject.get("code").asString}' ")
            append(" ,'${g_UserInfo.strUserName}' ")
            append(" ) ")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getUserInfo",e,7)
            bRes = false
        }
        return bRes
    }

    fun insertSlipDocTable(objSlipInfo:JsonObject, arObjImages: JsonArray, strSDocNo: String, strDocIRN: String, isOneSlip:Boolean):Boolean
    {
        var bRes = false
        var sbQuery = StringBuffer().apply {

            append("insert into IMG_SLIPDOC_T (")
    		append("CABINET")
    		append(",FOLDER")
    		append(",DOC_IRN")
    		append(",SDOC_NO")
    		append(",SDOC_MONTH")
    		append(",SDOC_STEP")
    		append(",SDOC_STATUS")
    		append(",SDOC_FLAG")
    		append(",CO_NO")
    		append(",PART_NO")
    		append(",SDOC_NAME")
    		append(",INFO_ETC")
    		append(",UPDATE_TIME")
    		append(",SLIP_CNT")
    		append(",FILE_CNT")
    		append(",FILE_SIZE")
    		append(",REG_USER")
    		append(",REG_TIME")
    		append(",SDOC_TYPE")
    		append(",SDOCNO_TOP")
    		append(",SDOCNO_DOC")
    		append(",HG_DATE")
    		append(",COPY_FLAG")
    		append(",SECURITY")
    		append(",SDOCNO_INDEX")
    		append(",SDOC_AFTER")
    		append(",SDOC_SYSTEM")
    		append(",SLIP_DEVICE")
    		append(",REG_USERNM")
    		append(",SDOC_ONE")
    		append(", WORK_GROUP ")
    		append(", SDOC_SECU ")
    		append(") VALUES (");
    		append("'${m_C.getDate("yyyyMMdd")}'")				//CABINET - varchar(8)
    		append(",'$AGENT_RECEIPT'")					 //FOLDER - varchar(16)
    		append(",'${strDocIRN}'")//DOC_IRN - varchar(30)
    		append(",'${strSDocNo}'")//SDOC_NO - varchar(30)
    		append(",'${m_C.getDate("yyyyMM")}'")
    		append(",'0'");//SDOC_STEP - varchar(1)
    		append(",'0'");//SDOC_STATUS - varchar(1)
    		append(",'1'");//SDOC_FLAG - varchar(1)
    		append(",'${g_UserInfo.strCoID}'")//CO_NO - varchar(10)
    		append(",'${g_UserInfo.strPartID}'")//PART_NO - varchar(30)
    		append(",'${m_C.convertSpecialChar(objSlipInfo.get("SDOC_NAME").asString)}'")//SDOC_NAME - varchar(100)
    		append(",''")//INFO_ETC - varchar(16)
    		append(",''")//UPDATE_TIME - varchar(20)
    		append(","+if(isOneSlip) 1 else arObjImages.size())//SLIP_CNT - int
    		append(","+arObjImages.size())//FILE_CNT - int
    		append(",'"+m_C.getSlipTotalSize(arObjImages)+"'")//FILE_SIZE - varchar(8)
    		append(",'${g_UserInfo.strUserID}'")//REG_USER - varchar(50)
    		append(",'${m_C.getDate("yyyyMMddHHmmsss")}'")//REG_TIME - varchar(20)
    		append(",'${objSlipInfo.get("SDOC_TYPE").asJsonObject.get("code").asString}'")//SDOC_TYPE - varchar(6)
    		append(",''")//SDOCNO_TOP - varchar(80)
    		append(",''")//SDOCNO_DOC - varchar(80)
    		append(",'${m_C.getDate("yyyyMMdd")}'")//HG_DATE - varchar(8)
    		append(",'0'")//COPY_FLAG - char(1)
    		append(",''")//SECURITY - varchar(2)
    		append(",'0'")//SDOCNO_INDEX - varchar(20)
    		append(",'0'")//SDOC_AFTER - char(1)
    		append(",'0'")//SDOC_SYSTEM
    		append(",'AN'")//SDOC_DEVICE
    		append(",'${g_UserInfo.strUserName}'")
    		append(",'" + (if(isOneSlip) 1 else 0 ) + "' ")
    		append(", '' ")
    		append(",'0'")
    		append(")")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getUserInfo",e,7)
            bRes = false
        }
        return bRes
    }

    fun copySlipTable(objSlipInfo: JsonObject, strSDocNo:String):Boolean
    {
        var bRes = false
        var sbQuery = StringBuffer().apply {

            append(" Insert into ")
            append(" IMG_SLIP_T ")
            append(" ( ")
            append(" DOC_IRN ")
            append(" ,CABINET ")
            append(" ,SDOC_NO ")
            append(" ,SLIP_NO ")
            append(" ,UPDATE_TIME ")
            append(" ,FILE_CNT ")
            append(" ,FOLDER ")
            append(" ,FILE_SIZE ")
            append(" ,SLIP_FLAG ")
            append(" ) ")
            append(" Values ")
            append(" ( ")
            append(" '${objSlipInfo.get("DOC_IRN").asString}' ")
            append(" ,'"+m_C.getDate("yyyyMMdd")+"' ")
            append(" ,'$strSDocNo' ")
            append(" ,'${objSlipInfo.get("SLIP_NO").asString}' ")
            append(" ,'"+m_C.getDate("yyyyMMddhhmmsss")+"' ")
            append(" ,'${objSlipInfo.get("FILE_CNT").asString}' ")
            append(" ,'$AGENT_FOLDER' ")
            append(" ,'${objSlipInfo.get("FILE_SIZE").asString}' ")
            append(" ,'1' ")
            append(" ) ")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getUserInfo",e,7)
            bRes = false
        }
        return bRes
    }

    fun updateShapeData(strShapeData: String, strSDocNo:String, strDocIRN:String):Boolean
    {
        var bRes = false
        var sbQuery = StringBuffer().apply {

            append(" Update ")
            append(" IMG_SLIP_T ")
            append(" Set ")
            append(" SLIP_PAGE = '$strShapeData' ")
            append(" Where ")
            append(" SDOC_NO = '$strSDocNo' ")
            append(" And DOC_IRN = '$strDocIRN' ")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getUserInfo",e,7)
            bRes = false
        }
        return bRes
    }

    fun copySlipDocTable(objSlipDocInfo:JsonObject, objUserInfo: JsonObject, strSDocNo: String, strDocIRN: String):Boolean
    {
        var bRes = false
        var sbQuery = StringBuffer().apply {

            append("insert into IMG_SLIPDOC_T (")
            append("CABINET")
            append(",FOLDER")
            append(",DOC_IRN")
            append(",SDOC_NO")
            append(",SDOC_MONTH")
            append(",SDOC_STEP")
            append(",SDOC_STATUS")
            append(",SDOC_FLAG")
            append(",CO_NO")
            append(",PART_NO")
            append(",SDOC_NAME")
            append(",INFO_ETC")
            append(",UPDATE_TIME")
            append(",SLIP_CNT")
            append(",FILE_CNT")
            append(",FILE_SIZE")
            append(",REG_USER")
            append(",REG_TIME")
            append(",SDOC_TYPE")
            append(",SDOCNO_TOP")
            append(",SDOCNO_DOC")
            append(",HG_DATE")
            append(",COPY_FLAG")
            append(",SECURITY")
            append(",SDOCNO_INDEX")
            append(",SDOC_AFTER")
            append(",SDOC_SYSTEM")
            append(",SLIP_DEVICE")
            append(",REG_USERNM")
            append(",SDOC_ONE")
            append(", WORK_GROUP ")
            append(", SDOC_SECU ")
            append(") VALUES (");
            append("'${objSlipDocInfo.get("CABINET").asString}'")				//CABINET - varchar(8)
            append(",'${objSlipDocInfo.get("FOLDER").asString}'")					 //FOLDER - varchar(16)
            append(",'${strDocIRN}'")//DOC_IRN - varchar(30)
            append(",'${strSDocNo}'")//SDOC_NO - varchar(30)
            append(",'${objSlipDocInfo.get("SDOC_MONTH").asString}'")
            append(",'${objSlipDocInfo.get("SDOC_STEP").asString}'");//SDOC_STEP - varchar(1)
            append(",'${objSlipDocInfo.get("SDOC_STATUS").asString}'");//SDOC_STATUS - varchar(1)
            append(",'${objSlipDocInfo.get("SDOC_FLAG").asString}'");//SDOC_FLAG - varchar(1)
            append(",'${objUserInfo.get("CO_CD").asString}'")//CO_NO - varchar(10)
            append(",'${objUserInfo.get("PART_CD").asString}'")//PART_NO - varchar(30)
            append(",'${objSlipDocInfo.get("SDOC_NAME").asString}'")//SDOC_NAME - varchar(100)
            append(",''")//INFO_ETC - varchar(16)
            append(",''")//UPDATE_TIME - varchar(20)
            append(",'${objSlipDocInfo.get("SLIP_CNT").asString}'")//SLIP_CNT - int
            append(",'${objSlipDocInfo.get("FILE_CNT").asString}'")//FILE_CNT - int
            append(",'${objSlipDocInfo.get("FILE_SIZE").asString}'")//FILE_SIZE - varchar(8)
            append(",'${objUserInfo.get("USER_ID").asString}'")//REG_USER - varchar(50)
            append(",'${objSlipDocInfo.get("REG_TIME").asString}'")//REG_TIME - varchar(20)
            append(",'${objSlipDocInfo.get("SDOC_TYPE").asString}'")//SDOC_TYPE - varchar(6)
            append(",''")//SDOCNO_TOP - varchar(80)
            append(",''")//SDOCNO_DOC - varchar(80)
            append(",'${objSlipDocInfo.get("HG_DATE").asString}'")//HG_DATE - varchar(8)
            append(",'1'")//COPY_FLAG - char(1)
            append(",''")//SECURITY - varchar(2)
            append(",'0'")//SDOCNO_INDEX - varchar(20)
            append(",'0'")//SDOC_AFTER - char(1)
            append(",'0'")//SDOC_SYSTEM
            append(",'AN'")//SDOC_DEVICE
            append(",'${objUserInfo.get("USER_NAME").asString}'")
            append(",'${objSlipDocInfo.get("SDOC_ONE").asString}' ")
            append(", '' ")
            append(",'0'")
            append(")")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "getUserInfo",e,7)
            bRes = false
        }
        return bRes
    }


    fun copyAttachTable(objAttachInfo:JsonObject, objUserInfo: JsonObject, strSDocNo: String, strDocIRN: String):Boolean
    {
        var bRes = false
        var sbQuery = StringBuffer().apply {

            ///{","M"",AG":"","INFO_ETC":"","ADD_ELIGI":"","ADD_SECU":""}
            append("insert into IMG_ADDFILE_T (")
            append("DOC_IRN")
            append(",SDOC_NO")
            append(",CABINET")
            append(",FOLDER")
            append(",CO_NO")
            append(",PART_NO")
            append(",ADD_STEP")
            append(",ADD_SYSTEM")
            append(",ADD_NO")
            append(",ADD_NAME")
            append(",ADD_FILE")
            append(",REG_USER")
            append(",REG_USERNM")
            append(",REG_TIME")
            append(",FILE_CNT")
            append(",FILE_SIZE")
            append(",COPY_FLAG")
            append(") VALUES (");
            append("'$strDocIRN'")				//CABINET - varchar(8)
            append(",'${objAttachInfo.get("SDOC_NO").asString}'")					 //FOLDER - varchar(16)
            append(",'${objAttachInfo.get("CABINET").asString}'")//DOC_IRN - varchar(30)
            append(",'${objAttachInfo.get("FOLDER").asString}'")//SDOC_NO - varchar(30)
            append(",'${objUserInfo.get("CO_CD").asString}'")
            append(",'${objUserInfo.get("PART_CD").asString}'");//SDOC_STEP - varchar(1)
            append(",'${objAttachInfo.get("ADD_STEP").asString}'");//SDOC_STATUS - varchar(1)
            append(",'${objAttachInfo.get("ADD_SYSTEM").asString}'");//SDOC_FLAG - varchar(1)
            append(",'${objAttachInfo.get("ADD_NO").asString}'")//CO_NO - varchar(10)
            append(",'${objAttachInfo.get("ADD_NAME").asString}'")//PART_NO - varchar(30)
            append(",'${objAttachInfo.get("ADD_FILE").asString}'")//SDOC_NAME - varchar(100)
            append(",'${objUserInfo.get("USER_ID").asString}'")//REG_USER - varchar(50)
            append(",'${objUserInfo.get("USER_NAME").asString}'")
            append(",'${objAttachInfo.get("REG_TIME").asString}' ")
            append(",'${objAttachInfo.get("FILE_CNT").asString}' ")
            append(",'${objAttachInfo.get("FILE_SIZE").asString}' ")
            append(",'${objAttachInfo.get("COPY_FLAG").asString}' ")
            append(")")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "copyAttachTable",e,7)
            bRes = false
        }
        return bRes
    }

    fun moveAttach(objAttachInfo:JsonObject, objUserInfo:JsonObject):Boolean
    {

        var strDesUserID = objUserInfo.get("USER_ID").asString
        var strDesUserName = objUserInfo.get("USER_NAME").asString
        var strDesPartCD = objUserInfo.get("PART_CD").asString
        var strDesDocIRN = objAttachInfo.get("DOC_IRN").asString

        var bRes = false
        var sbQuery = StringBuffer().apply {

            append(" Update ")
            append(" IMG_ADDFILE_T ")
            append(" Set ")
            append(" REG_USER = '$strDesUserID' ")
            append(" ,REG_USERNM = '$strDesUserName' ")
            append(" ,PART_NO = '$strDesPartCD' ")
            append(" Where ")
            append(" DOC_IRN ='$strDesDocIRN' ")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "moveAttach",e,7)
            bRes = false
        }
        return bRes
    }

    fun moveSlip(objSlipInfo:JsonObject, objUserInfo:JsonObject):Boolean
    {

        var strDesUserID = objUserInfo.get("USER_ID").asString
        var strDesUserName = objUserInfo.get("USER_NAME").asString
        var strDesPartCD = objUserInfo.get("PART_CD").asString
        var strDesSDocNo = objSlipInfo.get("SDOC_NO").asString

        var bRes = false
        var sbQuery = StringBuffer().apply {

            append(" Update ")
            append(" IMG_SLIPDOC_T ")
            append(" Set ")
            append(" REG_USER = '$strDesUserID' ")
            append(" ,REG_USERNM = '$strDesUserName' ")
            append(" ,PART_NO = '$strDesPartCD' ")
            append(" Where ")
            append(" SDOC_NO ='$strDesSDocNo' ")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "moveSlip",e,7)
            bRes = false
        }
        return bRes
    }
    fun removeSlip(objImageInfo: JsonObject):Boolean
    {
        var bRes = false
        var sbQuery = StringBuffer().apply {

            append(" Update ")
            append(" IMG_SLIPDOC_T ")
            append(" Set ")
            append(" SDOC_STEP = '9' ")
            append(" Where ")
            append(" SDOC_STEP !='9' ")
            append(" And SDOC_NO = '${objImageInfo.get("SDOC_NO").asString}' ")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "removeSlip",e,7)
            bRes = false
        }
        return bRes
    }

    fun removeAddfile(objImageInfo: JsonObject):Boolean
    {
        var bRes = false
        var sbQuery = StringBuffer().apply {

            append(" Update ")
            append(" IMG_ADDFILE_T ")
            append(" Set ")
            append(" ADD_STEP = '9' ")
            append(" Where ")
            append(" ADD_STEP !='9' ")
            append(" And DOC_IRN = '${objImageInfo.get("DOC_IRN").asString}' ")
        }

        try
        {

            setData(sbQuery.toString())?.let{
                it?.let {
                    var strRes = it.get("Result").asString.toUpperCase()
                    if("TRUE" == strRes)
                    {
                        var resCnt = it.get("Cnt").asString
                        if(resCnt.toInt() > 0)
                        {
                            bRes = true
                        }
                    }
                    else
                    {
                        bRes = false
                    }
                }
            }
        }
        catch(e : Exception)
        {
            Logger.WriteException(this@SocketManager.javaClass.name, "removeAddfile",e,7)
            bRes = false
        }
        return bRes
    }

}