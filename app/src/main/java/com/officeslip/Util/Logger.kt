package com.officeslip.Util

import android.os.Build
import android.util.Log
import com.officeslip.DEBUG_MODE
import com.officeslip.LOG_LEVEL
import com.officeslip.LOG_PATH
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class Logger {

    companion object {

        /**
         * log 파일에 exception 기록
         * @param className
         * 					클래스 이름
         * @param e
         * 					Exception
         */

        fun WriteException(strClassName:String, strFunctionName: String, e:Exception, nLogLevel:Int)
        {
            var swError             = StringWriter()

            e.printStackTrace(PrintWriter(swError))

            WriteLog(strClassName, strFunctionName, swError.toString(), nLogLevel)
        }


        /**
         * Log 생성
         */

        fun WriteLog(strClassName:String, strFunctionName:String, strLog:String, nLogLevel:Int):Boolean {
            var bRes = false

            var file = File(LOG_PATH)
            if(!file.exists())
            {
                bRes = file.mkdirs()
            }

            //show log contents on logcal only if it's debug mode
            if(DEBUG_MODE)
            {
                Log.d(strClassName, strLog)
            }

            if(nLogLevel <= LOG_LEVEL) {
                var date = Date()
                var dateFormat = SimpleDateFormat("yyyyMMdd")
                var strLogFile = LOG_PATH + File.separator + dateFormat.format(date) + ".log"


                try {

                    BufferedWriter(FileWriter(strLogFile, true)).use {
                        dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                        it.write(dateFormat.format(date).toString() + "(" + strClassName + " - " + strFunctionName + ":" + Build.DEVICE + ") : ")
                        it.write(strLog)
                        it.write("\n")
                        it.flush()
                    }
                }
                catch (e:Exception)
                {
                    e.printStackTrace()
                    bRes = false
                }
            }
            return bRes
        }
    }
}