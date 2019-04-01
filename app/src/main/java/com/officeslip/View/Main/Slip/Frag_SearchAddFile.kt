package com.officeslip.View.Main.Slip

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.officeslip.*
import com.sgenc.officeslip.*
import com.officeslip.Adapter.SearchAddfileAdapter
import com.officeslip.Operation.CopyAttach
import com.officeslip.Operation.MoveAttach
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.Common
import kotlinx.android.synthetic.main.fragment_main_searchaddfile.*
import org.w3c.dom.Element
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class Frag_SearchAddFile :Fragment(), View.OnClickListener,  SwipeRefreshLayout.OnRefreshListener, SearchAddfileAdapter.RemoveAddfile, SearchAddfileAdapter.CopyAddfile, SearchAddfileAdapter.MoveAddfile, SearchAddfileAdapter.DownAddfile
{

    companion object {
        const val RESULT_SEARCH_OPTION = 5001
        const val GET_ADDFILELIST_SUCCESS = 5002
        const val GET_ADDFILELIST_FAILED = 5003
        const val NETWORK_DISABLED = 5004
        const val REMOVE_ADDFILE_SUCCESS = 5005
        const val REMOVE_ADDFILE_FAILED = 5006
        const val REMOVE_ADDFILE_NOT_ALLOWED = 5015
        const val DOWNLOAD_ADDFILE_SUCCESS = 5007
        const val DOWNLOAD_ADDFILE_FAILED = 5008
        const val RESULT_MOVE_ATTACH_SELECT_USER = 5009
        const val RESULT_COPY_ATTACH_SELECT_USER = 5010
        const val MOVE_ADDFILE_SUCCESS = 5011
        const val MOVE_ADDFILE_FAILED = 5012
        const val COPY_ADDFILE_SUCCESS = 5013
        const val COPY_ADDFILE_FAILED = 5014

    }

    private val m_C: Common = Common()
    private var m_objSearchOptions = JsonObject()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.fragment_main_searchaddfile, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Restore upload info when screen rotation.
//        savedInstanceState?.apply {
//            getString("UploadInfo")?.apply {
//                m_objUploadInfo = JsonParser().parse(this) as JsonObject
//            }
//        }
        setToolbar()
        view_swipeLayoutAddfile.setOnRefreshListener(this)
        //setupViewUI()
    }

    override fun onRefresh() {
        runSearchAddfile()
    }

    fun runSearchAddfile() {
        //Set default search options.
        if (m_objSearchOptions.size() <= 0) {

            var calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.DATE, DEFAULT_SLIP_SEARCH_PERIOD)
            var strCabinetStart = SimpleDateFormat("yyyyMMdd").format(Date(calendar.timeInMillis))
            m_objSearchOptions.addProperty("CABINET_START", strCabinetStart)
            m_objSearchOptions.addProperty("CABINET_END", m_C.getDate("yyyyMMdd"))

        }

        GetAttachList(this@Frag_SearchAddFile, SocketManager(), m_objSearchOptions).execute()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(isVisibleToUser)
        {
            //Set default search options.
            if (m_objSearchOptions.size() <= 0) {
                var calendar = Calendar.getInstance()
                calendar.time = Date()
                calendar.add(Calendar.DATE, DEFAULT_SLIP_SEARCH_PERIOD)
                var strCabinetStart = SimpleDateFormat("yyyyMMdd").format(Date(calendar.timeInMillis))
                m_objSearchOptions.addProperty("CABINET_START", strCabinetStart)
                m_objSearchOptions.addProperty("CABINET_END", m_C.getDate("yyyyMMdd"))

            }

            toolbar_title?.apply {
                if(g_SysInfo.ServerMode == MODE_PRD)
                {
                    text = getString(R.string.main_tab_search_addfile)
                }
                else
                {
                    text = getString(R.string.main_tab_search_addfile) + " (DEV)"
                }

            }

            GetAttachList(this@Frag_SearchAddFile, SocketManager(), m_objSearchOptions).execute()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            RESULT_SEARCH_OPTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.getStringExtra("SEARCH_OPTION")?.run {
                        m_objSearchOptions = JsonParser().parse(this as String).asJsonObject
                        GetAttachList(this@Frag_SearchAddFile, SocketManager(), m_objSearchOptions).execute()
                     }
                }
            }

            RESULT_MOVE_ATTACH_SELECT_USER -> {
                if (resultCode == Activity.RESULT_OK) {

                    var strSelectedUser = data?.getStringExtra("SELECTED_USER")
                    var objSelectedAddfile = data?.getStringExtra("ADDFILE")

                    if(!m_C.isBlank(strSelectedUser) && !m_C.isBlank(objSelectedAddfile))
                    {
                        var objSelectedUser = JsonParser().parse(strSelectedUser).asJsonObject
                        var objSelectedAddfile = JsonParser().parse(objSelectedAddfile).asJsonObject

                        MoveAttach(this@Frag_SearchAddFile, objSelectedAddfile).execute(objSelectedUser)
                    }
                }
            }

            RESULT_COPY_ATTACH_SELECT_USER -> {
                if (resultCode == Activity.RESULT_OK) {

                    var strSelectedUser = data?.getStringExtra("SELECTED_USER")
                    var objSelectedAddfile = data?.getStringExtra("ADDFILE")

                    if(!m_C.isBlank(strSelectedUser) && !m_C.isBlank(objSelectedAddfile))
                    {
                        var objSelectedUser = JsonParser().parse(strSelectedUser).asJsonObject
                        var objSelectedAddfile = JsonParser().parse(objSelectedAddfile).asJsonObject

                        CopyAttach(this@Frag_SearchAddFile, objSelectedAddfile).execute(objSelectedUser)
                    }
                }
            }
        }
    }

    override fun downAddfile(obj: JsonObject) {

        DownloadAttach(this@Frag_SearchAddFile).execute(obj)

    }

    override fun removeAddfile(obj: JsonObject) {
        AlertDialog.Builder(activity as Context).apply {
            setMessage((activity as Context).getString(R.string.confirm_remove_addfile))
            setPositiveButton((activity as Context).getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                RemoveAddfile(this@Frag_SearchAddFile).execute(obj)
            })
            setNegativeButton((activity as Context).getString(R.string.btn_cancel),null)
        }.show()
    }

    override fun copyAddfile(obj: JsonObject) {

        AlertDialog.Builder(activity as Context).apply {

            setMessage((activity as Context).getString(R.string.confirm_copy_addfile))
            setPositiveButton((activity as Context).getString(R.string.btn_confirm)) { dialog, which ->
                Intent(activity, SelectUserActivity::class.java).run {
                    putExtra("TYPE", "MOVE")
                    putExtra("ADDFILE",obj.toString())
                    startActivityForResult(this, RESULT_COPY_ATTACH_SELECT_USER)
                }
            }
            setNegativeButton((activity as Context).getString(R.string.btn_cancel),null)
        }.show()
    }

    override fun moveAddfile(obj: JsonObject) {


        AlertDialog.Builder(activity as Context).apply {

                setMessage((activity as Context).getString(R.string.confirm_move_addfile))
                setPositiveButton((activity as Context).getString(R.string.btn_confirm)) { dialog, which ->
                    Intent(activity, SelectUserActivity::class.java).run {
                        putExtra("TYPE", "MOVE")
                        putExtra("ADDFILE",obj.toString())
                        startActivityForResult(this, RESULT_MOVE_ATTACH_SELECT_USER)
                }
            }
            setNegativeButton((activity as Context).getString(R.string.btn_cancel),null)
        }.show()
    }

    private fun setupViewUI(objList:JsonArray?) {
        viewManager = LinearLayoutManager(activity)
        viewAdapter = SearchAddfileAdapter(activity as AppCompatActivity, objList, false)

//        intent.getStringExtra("SelectedItem")?.apply {
//            var objSelected = JsonParser().parse(this)
//            var listSelected = JsonArray()
//            listSelected.add(objSelected)
//            (viewAdapter as SDocTypeAdapter).setItem(listSelected)
//        }

        recyclerView = (activity as AppCompatActivity).findViewById<RecyclerView>(R.id.view_recyclerResultAddfile).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

            (adapter as SearchAddfileAdapter).setRemoveAddfileListener(this@Frag_SearchAddFile as SearchAddfileAdapter.RemoveAddfile)
            (adapter as SearchAddfileAdapter).setDownddfileListener(this@Frag_SearchAddFile as SearchAddfileAdapter.DownAddfile)
            (adapter as SearchAddfileAdapter).setMoveAddfileListener(this@Frag_SearchAddFile as SearchAddfileAdapter.MoveAddfile)
            (adapter as SearchAddfileAdapter).setCopyAddfileListener(this@Frag_SearchAddFile as SearchAddfileAdapter.CopyAddfile)

        }
    }

    private fun setToolbar() {
        //run only when current fragment is this class.
        (activity as AppCompatActivity)?.run {
            setSupportActionBar(view_toolbarSearchAddfile)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            setHasOptionsMenu(false)
        }


        view_btnNavi.setOnClickListener(activity as MainActivity)
        view_btnSearch.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when(v?.tag.toString().toUpperCase()) {

            "SEARCH_ADDFILE" -> {
                Intent(activity, SearchOptionActivity::class.java).run {
                    putExtra("SEARCH_UI_SECTION", "ADDFILE")
                    startActivityForResult(this, RESULT_SEARCH_OPTION)
                }
            }
        }
    }

    private class GetAttachList(var fragment: Frag_SearchAddFile, var socketManager: SocketManager, var objSearchOption:JsonObject): AsyncTask<String, Void, JsonArray>() {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = GET_ADDFILELIST_FAILED

        init {
            activityRef = WeakReference(fragment.activity as AppCompatActivity)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            var activity = activityRef?.get()
            builder = AlertDialog.Builder(activity as Context).apply {
                var view = LayoutInflater.from(activity).inflate(R.layout.progress_circle, null)
                view.findViewById<TextView>(R.id.view_textProgressTitle).text = activity.getString(R.string.in_progress)

                setView(view)
                setCancelable(false)
                setNegativeButton(activity.getString(R.string.btn_cancel), DialogInterface.OnClickListener { dialog, which ->

                    if(status == AsyncTask.Status.RUNNING)
                    {
                        cancel(true)
                      //  activity.onBackPressed()
                    }

                    dialog.dismiss()
                })}.create()
            builder?.show()
        }

        override fun doInBackground(vararg params: String?): JsonArray {

            //Thread.sleep(10000)
            var arObjRes = JsonArray()

            if(!isCancelled)
            {
                var activity = activityRef?.get()

                //try connect agent via Wifi Network if Mobile data usage is disabled.
                if(m_C.isNetworkConnected(activity as Context))
                {
                    //consider protocol property
                    if("SOCKET".equals(CONNECTION_PROTOCOL, true))
                    {
                        SocketManager()?.getAttachList(objSearchOption)?.let {

                            for (i in 0 until it.length) {
                                var element = it.item(i) as Element
                                var obj = JsonObject()

                                obj.addProperty("ADD_FILE", element.getElementsByTagName("ADD_FILE").item(0).textContent)
                                obj.addProperty("ADD_STEP", element.getElementsByTagName("ADD_STEP").item(0).textContent)
                                obj.addProperty("ADD_NO", element.getElementsByTagName("ADD_NO").item(0).textContent)
                                obj.addProperty("FILE_SIZE", element.getElementsByTagName("FILE_SIZE").item(0).textContent)
                                obj.addProperty("CABINET", element.getElementsByTagName("CABINET").item(0).textContent)
                                obj.addProperty("REG_USER", element.getElementsByTagName("REG_USER").item(0).textContent)
                                obj.addProperty("REG_USERNM", element.getElementsByTagName("REG_USERNM").item(0).textContent)
                                obj.addProperty("PART_NM", element.getElementsByTagName("PART_NM").item(0).textContent)
                                obj.addProperty("DOC_IRN", element.getElementsByTagName("DOC_IRN").item(0).textContent)
                                arObjRes.add(obj)
                            }
                            nCurrentStatus = GET_ADDFILELIST_SUCCESS
                        }
                    }
                }
                else
                {
                    nCurrentStatus = NETWORK_DISABLED
                }
            }

            return arObjRes
        }

        override fun onPostExecute(result: JsonArray?) {
            super.onPostExecute(result)

            builder?.dismiss()
            val activity = activityRef?.get()

            activity?.findViewById<SwipeRefreshLayout>(R.id.view_swipeLayoutAddfile)?.let {
                it.setRefreshing(false)
            }

            when(nCurrentStatus) {
                GET_ADDFILELIST_SUCCESS -> {

                    fragment.setupViewUI(result)
                    /*activity?.setResult(RESULT_OK)
                    activity?.finish()*/
                }
                GET_ADDFILELIST_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.get_searchaddfile_failed))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                        //    activity.onBackPressed()
                        })
                    }.show()
                }
                NETWORK_DISABLED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.alert_network_disabled_message))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                         //   activity.onBackPressed()
                        })
                    }.show()
                }
            }
        }
    }

    //Download attach Thread proc
    private class DownloadAttach(var fragment: Frag_SearchAddFile): AsyncTask<JsonObject, Void, File?>()
    {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = REMOVE_ADDFILE_FAILED
        var m_strFileName:String? = null
        var m_strFileExt:String? = null

        init {
            activityRef = WeakReference(fragment.activity as AppCompatActivity)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            var activity = activityRef?.get()
            builder = AlertDialog.Builder(activity as Context).apply {
                var view = LayoutInflater.from(activity).inflate(R.layout.progress_circle, null)
                view.findViewById<TextView>(R.id.view_textProgressTitle).text = activity.getString(R.string.in_progress)

                setView(view)
                setCancelable(false)
                setNegativeButton(activity.getString(R.string.btn_cancel), DialogInterface.OnClickListener { dialog, which ->

                    if(status == AsyncTask.Status.RUNNING)
                    {
                        cancel(true)
                        //  activity.onBackPressed()
                    }

                    dialog.dismiss()
                })}.create()
            builder?.show()
        }

        override fun doInBackground(vararg params: JsonObject?): File? {
             var fRes:File? = null
            if (!isCancelled) {
                var activity = activityRef?.get()

                //try connect agent via Wifi Network if Mobile data usage is disabled.
                if (m_C.isNetworkConnected(activity as Context)) {
                    //consider protocol property
                    if ("SOCKET".equals(CONNECTION_PROTOCOL, true)) {

                        var strFileName = (params[0] as JsonObject)?.get("ADD_FILE").asString
                        var strDocIRN =  (params[0] as JsonObject)?.get("DOC_IRN").asString

                        var fileSplit:List<String> = strFileName?.split("")!!

                        m_strFileName = fileSplit[0]
                        m_strFileExt = fileSplit[1]

                        var bFile = SocketManager().Download(strDocIRN, "1", "IMG_ADDFILE_X")?.apply {
                            nCurrentStatus = DOWNLOAD_ADDFILE_SUCCESS
                        }

                        bFile?.run {

                            //Download directory
                            var downPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)


                            if (!downPath.exists()) {
                                downPath.mkdirs()
                            }
//
                            fRes = File(downPath, strFileName)

                            BufferedOutputStream(FileOutputStream(fRes)).use {
                                it.write(this)
                                it.flush()
                            }
                        }
//
                    }
                }
                else
                {
                    nCurrentStatus = NETWORK_DISABLED
                }
            }
            return fRes
        }

        override fun onPostExecute(result: File?) {
            super.onPostExecute(result)

            builder?.dismiss()
            val activity = activityRef?.get()

            when(nCurrentStatus) {
                DOWNLOAD_ADDFILE_SUCCESS -> {

                    var fileUri = FileProvider.getUriForFile(activity as Context,
                            BuildConfig.APPLICATION_ID + ".Util.GenericFileProvider",
                            result!!
                    )

                    //Pass to intent provider
                    var intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply{
                        //data = fileUri
                        //type = "application/*"// + m_strFileExt
                    }

                    var runIntent = Intent.createChooser(intent, "Open \"$m_strFileName\"")
                    activity.startActivity(runIntent)
                }
                DOWNLOAD_ADDFILE_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.failed_download_file))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                            //    activity.onBackPressed()
                        })
                    }.show()
                }
                NETWORK_DISABLED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.alert_network_disabled_message))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                            //   activity.onBackPressed()
                        })
                    }.show()
                }
            }
        }
    }
    //Remove attach Thread proc
    private class RemoveAddfile(var fragment: Frag_SearchAddFile): AsyncTask<JsonObject, Void, Void>()
    {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = REMOVE_ADDFILE_FAILED

        init {
            activityRef = WeakReference(fragment.activity as AppCompatActivity)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            var activity = activityRef?.get()
            builder = AlertDialog.Builder(activity as Context).apply {
                var view = LayoutInflater.from(activity).inflate(R.layout.progress_circle, null)
                view.findViewById<TextView>(R.id.view_textProgressTitle).text = activity.getString(R.string.in_progress)

                setView(view)
                setCancelable(false)
                setNegativeButton(activity.getString(R.string.btn_cancel), DialogInterface.OnClickListener { dialog, which ->

                    if(status == AsyncTask.Status.RUNNING)
                    {
                        cancel(true)
                        //  activity.onBackPressed()
                    }

                    dialog.dismiss()
                })}.create()
            builder?.show()
        }

        override fun doInBackground(vararg params: JsonObject?): Void? {
           // var bRes = false
            if (!isCancelled) {
                var activity = activityRef?.get()

                //try connect agent via Wifi Network if Mobile data usage is disabled.
                if (m_C.isNetworkConnected(activity as Context)) {
                    params[0]?.run {
                        this.get("ADD_STEP")?.asInt?.run {
                            if(this > 1)
                            {
                                nCurrentStatus = REMOVE_ADDFILE_NOT_ALLOWED
                                return null
                            }
                        }

                    }
                    //consider protocol property
                    if ("SOCKET".equals(CONNECTION_PROTOCOL, true)) {

                        if(SocketManager().removeAddfile(params[0]!!))
                        {
                            nCurrentStatus = REMOVE_ADDFILE_SUCCESS
                        }
                    }
                }
                else
                {
                    nCurrentStatus = NETWORK_DISABLED
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            builder?.dismiss()
            val activity = activityRef?.get()

            when(nCurrentStatus) {
                REMOVE_ADDFILE_SUCCESS -> {

                    fragment.runSearchAddfile()
                }
                REMOVE_ADDFILE_NOT_ALLOWED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.only_init_slip_allowed))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                            //    activity.onBackPressed()
                        })
                    }.show()
                }
                REMOVE_ADDFILE_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.failed_remove_addfile))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                            //    activity.onBackPressed()
                        })
                    }.show()
                }
                NETWORK_DISABLED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.alert_network_disabled_message))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                            //   activity.onBackPressed()
                        })
                    }.show()
                }
            }
        }
    }
}