package com.officeslip

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Environment
import com.officeslip.Util.Common
import com.officeslip.Util.SQLite
import com.officeslip.Util.UserDefault
import java.io.File
import java.util.HashMap



/**
 * App option value
 */
const val SLIP_THUMB_ZOOM_MAX = 5
const val TAG ="OfficeSLIP"
const val MODE_PRD = 998
const val MODE_DEV = 997

/**
 * App permission. do not modify
 */
const val MULTIPLE_PERMISSIONS = 10

/**
 * UserDefault info.  do not modify
 */
const val APP_BUNDLE_ID = "OfficeSLIP"


/**
 * Connection info
 */
const val CONNECTION_PROTOCOL = "SOCKET"
//const val CONNECTION_PRD_IP = "10.253.42.129"
//const val CONNECTION_PRD_PORT = 9788
const val CONNECTION_PRD_IP = "woonamsoft01.iptime.org"
const val CONNECTION_PRD_PORT = 14588
const val CONNECTION_DEV_IP = "10.11.50.21"
const val CONNECTION_DEV_PORT = 9788
const val CONNECTION_TIMEOUT =15000
const val CONNECTION_CHARSET ="EUC-KR"
const val CONNECTION_DB = "ORACLE"
const val AGENT_SERVERKEY = "SGENC"
const val AGENT_EDMS = "V" //W = Agent, V = Xvarm
const val AGENT_RECEIPT = "SLIPDOC"
const val AGENT_FOLDER = "SLIP"
const val AGENT_ADDFILE = "ADDFILE"
const val SYSTEM_DEFAULT_LANGUAGE = "ko"
const val VOUCHER_MAIN_URL = "http://10.253.43.244:9999/ssg_mobile.html?menu="
const val DASHBOARD_MAIN_URL = "https://www.naver.com"
//
/**
 * XVarm info
 */

const val XVARM_SCHEMA = "XVARM"
const val XVARM_TABLE_SLIP = "SLIP_T"
const val XVARM_TABLE_SLIP_M = "SLIP_M"
const val XVARM_TABLE_SLIPDOC = "SLIPDOC_T"
const val XVARM_TABLE_ADDFILE = "ADDFILE_T"

//
//const val XVARM_IP = "10.253.42.129"
//const val XVARM_PORT = 2102
//const val XVARM_CLIENT = "Java App"
//const val XVARM_ID = "SUPER"
//const val XVARM_PW = "SUPER"
//const val XVARM_GATEWAY = "XVARM_MAIN"
//const val XVARM_FOLDER_IMG_SLIP_M = "썸네일"
//const val XVARM_FOLDER_IMG_SLIP = "증빙"
//const val XVARM_FOLDER_IMG_SLIPDOC = "증빙서"
//const val XVARM_FOLDER_IMG_ADDFILE = "첨부"
//const val XVARM_CC_IMG_SLIP_M = "SLIP_M_CC"
//const val XVARM_CC_IMG_SLIP = "SLIP_CC"
//const val XVARM_CC_IMG_SLIPDOC = "SLIPDOC_CC"
//const val XVARM_CC_IMG_ADDFILE = "ADDFILE_CC"

/**
 * Upload info
 */

const val ENCRYPTION_ENABLE = false
const val ENCRYPTION_KEY = "#WOONAMSOFT1998#%WOONAMSOFT1998%"

/**
 * Log info
 */
const val DEBUG_MODE = true
const val LOG_LEVEL = 9

/**
 * Path Info.  do not modify
 */
var UPLOAD_PATH = Environment.getExternalStorageDirectory().absolutePath + File.separator + AGENT_SERVERKEY + File.separator +".Upload"
var UPLOAD_THUMB_PATH = UPLOAD_PATH + File.separator + ".Thumb"
var DOWNLOAD_PATH = Environment.getExternalStorageDirectory().absolutePath + File.separator + AGENT_SERVERKEY + File.separator +".Download"
var DOWNLOAD_THUMB_PATH = DOWNLOAD_PATH + File.separator +".Thumb"
var LOG_PATH = Environment.getExternalStorageDirectory().absolutePath + File.separator + AGENT_SERVERKEY + File.separator + "Logs"
var TEMP_PATH = Environment.getExternalStorageDirectory().absolutePath + File.separator + AGENT_SERVERKEY + File.separator + ".Temp"


//var THUMB_SIZE = 500 //Pixel

/**
 * Etc
 */

var UPLOAD_SLIP_SIZE_LIMIT = 500 //KB
const val DETECT_RECTANGLE_LIMIT = 50f
val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()


const val FILE_EXPLORER_LIMIT = 1024 * 1024 * 1024
var CALL_ANOTHER_APPLICATION = false
const val FAVOITELIST_LIMIT = 10

const val APP_UPDATE_URL = "http://woonamsoft01.iptime.org:14240/demo/OfficeSLIP.apk"
const val FAB_BUTTON_MARGIN_BOTTOM = 50
const val COLOR_PICKER_CIRCLE_SIZE = 50

const val SHAPE_MIN_SIZE = 6
/**
 * Pen default
 */
const val PEN_WEIGHT_DEFAULT = 25f
const val PEN_BACKGROUND_DEFAULT = "#FFFF8D"
const val PEN_ALPHA_DEFAULT = 200

/**
 * Rect default
 */
const val RECT_WEIGHT_DEFAULT = 3f
const val RECT_BACKGROUND_DEFAULT = "#BDBDBD"
const val RECT_ALPHA_DEFAULT = 200
const val RECT_LINE_DEFAULT = "#FF1744"
const val RECT_BG_ENABLE_DEFAULT = "1"

/**
 * Circle default
 */
const val CIRCLE_WEIGHT_DEFAULT = 3f
const val CIRCLE_BACKGROUND_DEFAULT = "#BDBDBD"
const val CIRCLE_ALPHA_DEFAULT = 200
const val CIRCLE_LINE_DEFAULT = "#FF1744"
const val CIRCLE_BG_ENABLE_DEFAULT = "1"

/**
 * Memo default
 */
const val MEMO_FONTSIZE_DEFAULT = 10
const val MEMO_BACKGROUND_DEFAULT = "#FFD600"
const val MEMO_FOREGROUND_DEFAULT = "#FF1744"
const val MEMO_ALPHA_DEFAULT = 0.5f
const val MEMO_LINE_DEFAULT = "#BDBDBD"
const val MEMO_WEIGHT_DEFAULT = 1f
const val MEMO_BOLD_DEFAULT = "0"
const val MEMO_ITALIC_DEFAULT = "0"
const val MEMO_BG_ENABLE_DEFAULT = "0"



/**
 * DB Constants. do not modify
 */
const val SQLITE_DB_NAME = "OFFICE_SLIP.db"
const val SQLITE_DB_VERSION = 1
const val DEFAULT_SLIP_SEARCH_PERIOD = -30 //Days


/**
 * SDOC_STEP Color Value
 */



const val SDOC_STEP_COLOR_0 = "#FF40FF"
const val SDOC_STEP_COLOR_1 = "#FF40FF"
const val SDOC_STEP_COLOR_2 = "#0000ff"
const val SDOC_STEP_COLOR_3 = "#800080"
const val SDOC_STEP_COLOR_4 = "#008F00"
const val SDOC_STEP_COLOR_7 = "#A9A9A9"
const val SDOC_STEP_COLOR_9 = "#ff0000"


/**
 *
 * SQLite info. do not modify
 */
const val SQLITE_DB_DELETE_TABLE =  """
                                        DROP TABLE IF EXISTS CARDUSER_T, PROPERTY_T, SYSINFO_T, FAVORITE_MOVE_T, FAVORITE_COPY_T
                                    """
const val SQLITE_CREATE_CARDUSER_T = """
                        CREATE TABLE IF NOT EXISTS CARDUSER_T(
                        USER_ID TEXT PRIMARY KEY NOT NULL,
                        USER_NM TEXT NOT NULL,
                        PART_CD TEXT NOT NULL,
                        PART_NM TEXT NOT NULL,
                        REG_TIME TEXT NOT NULL
                        )
                    """

const val SQLITE_CREATE_SYSINFO_T = """
                       CREATE TABLE IF NOT EXISTS SYSINFO_T(
                       OPTION TEXT PRIMARY KEY NOT NULL,
                        VALUE TEXT,
                        TARGET TEXT
                        )
                    """
const val SQLITE_INSERT_OR_UPDATE_SYSINFO = """
    Insert Or Replace Into
    SYSINFO_T
    (
        OPTION
        ,VALUE
        ,TARGET
    )
    Values
    (
        COALESCE((Select OPTION From SYSINFO_T Where OPTION = ?), ?)
        ,?
        ,'XPI_OptionView'
    )
"""
const val SQLITE_GET_SYSINFO = """
    Select *
        from SYSINFO_T
    where
        TARGET= ?
        And OPTION = ?
     """

const val SQLITE_GET_PROPERTY = """
    Select *
        from PROPERTY_T
    where
        MODE= ?
     """

const val SQLITE_CREATE_FAVORITE_MOVE_T = """
                        CREATE TABLE IF NOT EXISTS FAVORITE_MOVE_T(
                        USER_ID TEXT PRIMARY KEY,
                        USER_NAME TEXT NOT NULL,
                        PART_CD TEXT NOT NULL,
                        PART_NM TEXT NOT NULL,
                        CO_CD TEXT NOT NULL,
                        CO_NAME TEXT NOT NULL,
                        REG_TIME TEXT NOT NULL
                        )
                    """

const val SQLITE_CREATE_FAVORITE_COPY_T = """
                        CREATE TABLE IF NOT EXISTS FAVORITE_COPY_T(
                        USER_ID TEXT PRIMARY KEY,
                        USER_NAME TEXT NOT NULL,
                        PART_CD TEXT NOT NULL,
                        PART_NM TEXT NOT NULL,
                        CO_CD TEXT NOT NULL,
                        CO_NAME TEXT NOT NULL,
                        REG_TIME TEXT NOT NULL
                        )
                    """

const val SQLITE_CREATE_PROPERTY_T = """
                        CREATE TABLE IF NOT EXISTS PROPERTY_T(
                        MODE TEXT PRIMARY KEY NOT NULL,
                        WEIGHT TEXT,
                        ALPHA TEXT,
                        LineColor TEXT,
                        BackColor TEXT,
                        TextColor TEXT,
                        TEXT_TITLE TEXT,
                        TEXT_SIZE TEXT,
                        Italic,
                        Bold,
                        BackFlag
                        )
                    """


const val SQLITE_INSERT_PROPERTY_T = """
                        Insert Or Replace into PROPERTY_T(
                        MODE ,
                        WEIGHT ,
                        ALPHA ,
                        LineColor ,
                        BackColor ,
                        TextColor ,
                        TEXT_TITLE ,
                        TEXT_SIZE ,
                        Italic,
                        Bold,
                        BackFlag
                        )
                        Values
                        (
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?
                        )
                    """

lateinit var g_UserInfo: UserInfo
lateinit var g_SysInfo: SysInfo


class UserInfo(activity: Activity) {

    private val userDefault: UserDefault = UserDefault(activity)
    private val common: Common = Common()

    var strUserID:String
        set(value) {
            if(!common.isBlank(value)) userDefault.setString(value, "XPI_SLIP.UserID")
        }
        get() {
            return userDefault.getString("XPI_SLIP.UserID")
        }

    var strUserName:String
        set(value) {
            if(!common.isBlank(value)) userDefault.setString(value, "XPI_SLIP.UserName")
        }
        get() {
            return userDefault.getString("XPI_SLIP.UserName")
        }
    var strPartID:String
        set(value) {
            if(!common.isBlank(value)) userDefault.setString(value, "XPI_SLIP.PartID")
        }
        get() {
            return userDefault.getString("XPI_SLIP.PartID")
        }
    var strPartName:String
        set(value) {
            if(!common.isBlank(value)) userDefault.setString(value, "XPI_SLIP.PartName")
        }
        get() {
            return userDefault.getString("XPI_SLIP.PartName")
        }
    var strCoID:String
        set(value) {
            if(!common.isBlank(value)) userDefault.setString(value, "XPI_SLIP.CoID")
        }
        get() {
            return userDefault.getString("XPI_SLIP.CoID")
        }
    var strCoName:String
        set(value) {
            if(!common.isBlank(value)) userDefault.setString(value, "XPI_SLIP.CoName")
        }
        get() {
            return userDefault.getString("XPI_SLIP.CoName")
        }
    var strIsManager:String //1 or 0
        set(value) {
            if(!common.isBlank(value)) userDefault.setString(value,"XPI_SLIP.Manager")
        }
        get() {
            return userDefault.getString("XPI_SLIP.Manager")
        }
    var bHasLogged:Boolean // T or F
        set(value) {
            var strLogged = "F"
            if(value)
            {
                strLogged = "T"
            }
            userDefault.setString(strLogged,"XPI_SLIP.HasLogged")
        }
        get() {
            var bRes = false

            var strLogged = userDefault.getString("XPI_SLIP.HasLogged")
            if(!common.isBlank(strLogged) && strLogged == "T") bRes = true
            return bRes
        }

}
class SysInfo(activity: Activity) {

    private val userDefault: UserDefault = UserDefault(activity as Context)
    private val common: Common = Common()
    private val  m_activity = activity

    var bCellular:Boolean
        set(value) {
            var strVal = if(value) "1" else "0"
            SQLite(m_activity).instantSetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_INSERT_OR_UPDATE_SYSINFO, arrayOf("OPTION_CELLULAR","OPTION_CELLULAR",strVal))
        }
        get() {
            var res = true
            SQLite(m_activity).instantGetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_GET_SYSINFO, arrayOf("XPI_OptionView", "OPTION_CELLULAR"))?.forEach{
                it as HashMap<String, String>

                if(!common.isBlank(it.get("VALUE"))) {
                    res = "1".equals(it.get("VALUE"), true)
                }
                else
                {
                    res = true
                }
            }
            return res
        }

    var bPINEnabled:Boolean
        set(value) {

            var strVal = if(value) "1" else "0"
            SQLite(m_activity).instantSetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_INSERT_OR_UPDATE_SYSINFO, arrayOf("OPTION_LOCK_PASSWORD","OPTION_LOCK_PASSWORD",strVal))

        }
        get() {
            var res = false
            SQLite(m_activity).instantGetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_GET_SYSINFO, arrayOf("XPI_OptionView", "OPTION_LOCK_PASSWORD"))?.forEach{
                it as HashMap<String, String>

                if(!common.isBlank(it.get("VALUE"))) {
                    res = "1".equals(it.get("VALUE"), true)
                }
            }
            return res
        }

    var bSaveID:Boolean
        set(value) {
            var strVal = if(value) "1" else "0"
            SQLite(m_activity).instantSetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_INSERT_OR_UPDATE_SYSINFO, arrayOf("OPTION_SAVE_ID","OPTION_SAVE_ID",strVal))
        }
        get() {
            var res = false
            SQLite(m_activity).instantGetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_GET_SYSINFO, arrayOf("XPI_OptionView", "OPTION_SAVE_ID"))?.forEach{
                it as HashMap<String, String>

                if(!common.isBlank(it.get("VALUE"))) {
                    res = "1".equals(it.get("VALUE"), true)
                }
            }
            return res
        }

    var bAutoLogin:Boolean
        set(value) {
            var strVal = if(value) "1" else "0"
            SQLite(m_activity).instantSetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_INSERT_OR_UPDATE_SYSINFO, arrayOf("OPTION_AUTO_LOGIN","OPTION_AUTO_LOGIN",strVal))
        }
        get() {
            var res = false
            SQLite(m_activity).instantGetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_GET_SYSINFO, arrayOf("XPI_OptionView", "OPTION_AUTO_LOGIN"))?.forEach{
                it as HashMap<String, String>

                if(!common.isBlank(it.get("VALUE"))) {
                    res = "1".equals(it.get("VALUE"), true)
                }
            }
            return res
        }

    var strDisplayLang:String
        set(value) {
            SQLite(m_activity).instantSetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_INSERT_OR_UPDATE_SYSINFO, arrayOf("OPTION_DISPLAY_LANGUAGE","OPTION_DISPLAY_LANGUAGE",value.toLowerCase()))
        }
        get() {
            var res = SYSTEM_DEFAULT_LANGUAGE
            SQLite(m_activity).instantGetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_GET_SYSINFO, arrayOf("XPI_OptionView", "OPTION_DISPLAY_LANGUAGE"))?.forEach{
                it as HashMap<String, String>

                if(!common.isBlank(it.get("VALUE"))) {
                    res = it.get("VALUE")!!.toLowerCase()
                }
            }
            return res
        }

    var bPINLogged:Boolean
        set(value) {
            var strLogged = "F"
            if(value)
            {
                strLogged = "T"
            }
            userDefault.setString(strLogged,"XPI_SLIP.LockLogged")
        }
        get() {
            var bRes = false

            var strLogged = userDefault.getString("XPI_SLIP.LockLogged")
            if(!common.isBlank(strLogged) && strLogged == "T") bRes = true
            return bRes
        }

    var strLockPasswordValue:String
        set(value) {
            SQLite(m_activity).instantSetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_INSERT_OR_UPDATE_SYSINFO, arrayOf("OPTION_APP_PASSWORD","OPTION_APP_PASSWORD",value.toLowerCase()))
        }
        get() {
            var res = ""
            SQLite(m_activity).instantGetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_GET_SYSINFO, arrayOf("XPI_OptionView", "OPTION_APP_PASSWORD"))?.forEach {
                it as HashMap<String, String>

                if (!common.isBlank(it.get("VALUE"))) {
                    res = it.get("VALUE")!!.toLowerCase()
                }
            }
            return res
        }

    var strThumbZoomValue:String
        set(value) {
            SQLite(m_activity).instantSetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_INSERT_OR_UPDATE_SYSINFO, arrayOf("THUMB_CELL_SIZE","THUMB_CELL_SIZE",value.toLowerCase()))
        }
        get() {
            var res = "2"
            SQLite(m_activity).instantGetRecord(SQLITE_CREATE_SYSINFO_T, SQLITE_GET_SYSINFO, arrayOf("XPI_OptionView", "THUMB_CELL_SIZE"))?.forEach{
                it as HashMap<String, String>

                if(!common.isBlank(it.get("VALUE"))) {
                    res = it.get("VALUE")!!.toLowerCase()
                }
            }
            return res
        }
    var ServerMode:Int
        set(value) {
            userDefault.setString(value.toString(), "XPI_SLIP.ServerMode")
        }
        get(){

            var resVal = userDefault.getString("XPI_SLIP.ServerMode")
            if(resVal != "")
            {
                return resVal.toInt()
            }
            else
            {
                return MODE_PRD
            }
        }
}

