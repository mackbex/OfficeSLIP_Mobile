package com.officeslip.Util

import android.app.Activity
import android.content.Context
import com.officeslip.SysInfo
import com.officeslip.UserInfo
import com.officeslip.g_SysInfo
import com.officeslip.g_UserInfo


class Initialize {

    private var m_context:Context
    private var m_C: Common = Common()
    //private lateinit var appLifecycleObserver: AppLifecycleObserver

    constructor(context: Context) : super()
    {
        this.m_context = context

        g_SysInfo = SysInfo(context as Activity)
        g_UserInfo = UserInfo(context as Activity)

        //appLifecycleObserver = AppLifecycleObserver(m_context)
       //ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        getSystemOptions()
    }

    //load saved system options to global variable.
    private fun getSystemOptions() {
//        val sqlObj = SQLite(m_context)
//
//        val sqlDB = sqlObj.openDB("SYSINFO_T")
//        if(sqlDB != null)
//        {
//        //    sqlObj.setRecord("Insert into SYSINFO_T (OPTION, VALUE, TARGET) Values('test2','2','XPI_OptionView')")
//            sqlObj.getRecord("Select * from SYSINFO_T where TARGET= ? ", arrayOf("XPI_OptionView"))?.forEach {
//                it as HashMap<String,String>
//
//                when(it.get("OPTION")?.toUpperCase())
//                {
//                    "OPTION_CELLULAR" -> {
//                        g_SysInfo.bCellular = "1".equals(it.get("VALUE"), true)
//                    }
//                    "OPTION_LOCK_PASSWORD" -> {
//                        g_SysInfo.bPINEnabled = "1".equals(it.get("VALUE"), true)
//
//                    }
//                    "OPTION_SAVE_ID" -> {
//                        g_SysInfo.bSaveID = "1".equals(it.get("VALUE"), true)
//                    }
//                    "OPTION_PASSWORD_KEY" -> {
//                        g_SysInfo.strPassword = it.get("VALUE").toString()
//                    }
//                    "OPTION_AUTO_LOGIN" -> {
//                        g_SysInfo.bAutoLogin = "1".equals(it.get("VALUE"), true)
//                    }
//                }
//            }
//        }

//        if(!m_C.isBlank(g_SysInfo.strDisplayLang))
//        {

        g_SysInfo.strDisplayLang = m_C.getSystemLocale(m_context)
          //      m_C.changeLocale(m_context, g_SysInfo.strDisplayLang)
//        }
    }
}