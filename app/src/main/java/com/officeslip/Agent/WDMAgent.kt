package com.officeslip.Agent

import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID.randomUUID



class WDMAgent {


    /**
     * IRN 생성 (client에서 IRN을 생성함)
     * @param ip
     *
     * @param port
     *
     * @param strServerKey
     *
     * @return doc_irn
     */

    fun getDocIRN():String
    {
        var dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA)
        var dateCur = Date()
        var strToday = dateFormat.format(dateCur)
        var lTime = System.currentTimeMillis()
        var strMill = lTime.toString()
        strMill = strMill.substring(strMill.length - 3, strMill.length)
        strToday += strMill
        var strDate = make_date_irn(strToday.substring(0, 8))
        var strTime = make_time_irn(strToday.substring(8, strToday.length))

        var strUUID = UUID.randomUUID().toString()
		var strDocIrn	=	strUUID.substring(strUUID.length - 10).toUpperCase()

        return strDate+strDocIrn+strTime
    }

    /**
     * date_irn
     * @param strDate
     * @return
     */

    private fun make_date_irn(strDate:String):String
    {
        var nLength = strDate.length
        if(nLength == 8)
        {
            var year    = strDate.substring(0, 4).toInt()
            var month   = strDate.substring(4, 6).toInt()
            var day     = strDate.substring(6, 8).toInt()
            var value   = (year - 2000)

            value *= 12
            value += month
            value *= 31
            value += day
            value += 47952 // to avoid default IRN value.

            return make_irn_str(value , 4)
        }

        return "0000"
    }


    /**
     * itm_irn
     * @param strTime
     * @return
     */


    private fun make_time_irn(strTime:String):String
    {
        var hour		= strTime.substring(0,2).toInt()
        var min		    = strTime.substring(2,4).toInt()
        var sec		    = strTime.substring(4,6).toInt()
        var mill		= strTime.substring(6,strTime.length).toInt()
        var value		= hour

        value *= 60
        value += min
        value *= 60
        value += sec
        value *= 1000
        value += mill

        return make_irn_str(value, 6)
    }


    /**
     * make result irn
     *
     * @param value
     * @param digit
     * @return
     */

    private fun make_irn_str(nValue:Int, nDigit:Int):String
    {
        var strIRN = ""
        var nReceivedVal = nValue
        do
        {

            var m              = nReceivedVal % 36
            var ch           = if (m >= 10)  ('A'+(m-10)) else ('0'+m)
            strIRN                  = ch + strIRN
            nReceivedVal /= 36
        }
        while( nReceivedVal > 0 )

        strIRN     =     zeroPlus(nDigit)+strIRN
        strIRN     =     strIRN.substring(strIRN.length-nDigit,strIRN.length)

        return strIRN
    }

    /**
     * make make_ip_2_irn
     *
     * @return
     */

    private fun make_ip_2_irn(strIP:String):String
    {
        var nIP1=0
        var nIP2=0
        var nIP3=0
        var nIP4=0

        var arrIp	= strIP.split(".")

        nIP1 =      arrIp[0].toInt()
        nIP2 =      arrIp[1].toInt()
        nIP3 =      arrIp[2].toInt()
        nIP4 =      arrIp[3].toInt()

        var nValue			= nIP2 * 256 * 256 + nIP3 * 256 + nIP4

        return make_irn_str(nValue, 5)
    }


    /**
     * make make_ip_2_irn
     *
     * @return
     */

    private fun zeroPlus(nLength:Int):String
    {
        var strRntZero = ""
        for(i in 0 until nLength)
        {
            strRntZero += "0"
        }
        return strRntZero
    }
}