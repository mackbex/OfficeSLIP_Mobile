package com.officeslip.View.Main.Slip

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
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
import com.officeslip.Adapter.SearchSlipAdapter
import com.officeslip.Operation.CopySlip
import com.officeslip.Operation.MoveSlip
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.Common
import kotlinx.android.synthetic.main.fragment_main_searchslip.*
import org.w3c.dom.Element
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class Frag_SearchSlip :Fragment(), View.OnClickListener, SearchSlipAdapter.ShowSlip, SearchSlipAdapter.RemoveSlip, SearchSlipAdapter.CopySlip, SearchSlipAdapter.MoveSlip, SwipeRefreshLayout.OnRefreshListener, Frag_Statistics.SearchSelectedItemListener
{

    companion object {
        const val RESULT_SEARCH_OPTION = 5001
        const val GET_SLIPLIST_SUCCESS = 5002
        const val GET_SLIPLIST_FAILED = 5003
        const val NETWORK_DISABLED = 5004
        const val REMOVE_SLIP_SUCCESS = 5005
        const val REMOVE_SLIP_FAILED = 5006
        const val RESULT_MOVE_SLIP_SELECT_USER = 5007
        const val RESULT_COPY_SLIP_SELECT_USER = 5012
        const val REMOVE_SLIP_NOT_ALLOWED = 5020
        const val MOVE_SLIP_SUCCESS = 5008
        const val MOVE_SLIP_FAILED = 5009
        const val COPY_SLIP_SUCCESS = 5010
        const val COPY_SLIP_FAILED = 5011
        const val COPY_SLIP_SDOCNO_EMPTY = 5012
        const val COPY_SLIP_FAILED_CREATE_XML = 5013
        const val COPY_SLIP_FAILED_DOWNLOAD_XML = 5014
        const val COPY_SLIP_FAILED_UPLOAD_XML = 5015
        const val COPY_SLIP_FAILED_INSERT_SLIPDOC = 5016
        const val COPY_SLIP_FAILED_INSERT_SLIP = 5017
        const val COPY_SLIP_FAILED_GET_SLIPDOC_INFO = 5018
        const val COPY_SLIP_FAILED_GET_SLIP_INFO = 5019


    }

    private val m_C: Common = Common()
    private var m_objSearchOptions = JsonObject()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.fragment_main_searchslip, container, false)
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
        view_swipeLayoutSlip.setOnRefreshListener(this)
        //setupViewUI()
    }

    override fun onRefresh() {
        runSearchSlip()
    }

    fun runSearchSlip() {
        //Set default search options.
        if (m_objSearchOptions.size() <= 0) {
            m_objSearchOptions.addProperty("USED_CATEGORY", SearchOptionActivity.SEGMENT_STEP_ALL) // 0 = Unused
            m_objSearchOptions.addProperty("WORK_CATEGORY", SearchOptionActivity.SEGMENT_TASK_COMMON) // 0 = Unused

            var calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.DATE, DEFAULT_SLIP_SEARCH_PERIOD)
            var strCabinetStart = SimpleDateFormat("yyyyMMdd").format(Date(calendar.timeInMillis))
            m_objSearchOptions.addProperty("CABINET_START", strCabinetStart)
            m_objSearchOptions.addProperty("CABINET_END", m_C.getDate("yyyyMMdd"))

        }

        GetSlipList(this@Frag_SearchSlip, SocketManager(), m_objSearchOptions).execute()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(isVisibleToUser)
        {
            //Set default search options.
            if (m_objSearchOptions.size() <= 0) {
                m_objSearchOptions.addProperty("USED_CATEGORY", SearchOptionActivity.SEGMENT_STEP_ALL) // 0 = Unused
                m_objSearchOptions.addProperty("WORK_CATEGORY", SearchOptionActivity.SEGMENT_TASK_COMMON) // 0 = Unused

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
                    text = getString(R.string.main_tab_search_slip)
                }
                else
                {
                    text = getString(R.string.main_tab_search_slip) + " (DEV)"
                }
            }



            GetSlipList(this@Frag_SearchSlip, SocketManager(), m_objSearchOptions).execute()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            RESULT_SEARCH_OPTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.getStringExtra("SEARCH_OPTION")?.run {
                        m_objSearchOptions = JsonParser().parse(this as String).asJsonObject
                        GetSlipList(this@Frag_SearchSlip, SocketManager(), m_objSearchOptions).execute()
                     }
                }
//                else
//                {
//                    AlertDialog.Builder(activity as Context).apply {
//                        setTitle(getString(R.string.error))
//                        setMessage(getString(R.string.error_failed_get_search_option))
//
//                        setPositiveButton("OK", null)
//
//                    }.show()
//                }
            }
            RESULT_COPY_SLIP_SELECT_USER -> {
                if (resultCode == Activity.RESULT_OK) {

                    var strSelectedUser = data?.getStringExtra("SELECTED_USER")
                    var strSelectedSlip = data?.getStringExtra("SLIP")

                    if(!m_C.isBlank(strSelectedUser) && !m_C.isBlank(strSelectedSlip))
                    {
                        var objSelectedUser = JsonParser().parse(strSelectedUser).asJsonObject
                        var objSelectedSlip = JsonParser().parse(strSelectedSlip).asJsonObject

                        CopySlip(this@Frag_SearchSlip, objSelectedSlip).execute(objSelectedUser)
                    }
                }
            }
            RESULT_MOVE_SLIP_SELECT_USER -> {
                if (resultCode == Activity.RESULT_OK) {

                    var strSelectedUser = data?.getStringExtra("SELECTED_USER")
                    var strSelectedSlip = data?.getStringExtra("SLIP")

                    if(!m_C.isBlank(strSelectedUser) && !m_C.isBlank(strSelectedSlip))
                    {
                        var objSelectedUser = JsonParser().parse(strSelectedUser).asJsonObject
                        var objSelectedSlip = JsonParser().parse(strSelectedSlip).asJsonObject

                        MoveSlip(this@Frag_SearchSlip, objSelectedSlip).execute(objSelectedUser)
                    }
                }
            }
        }
    }


    private fun setupViewUI(objList:JsonArray?) {
        viewManager = LinearLayoutManager(activity)
        viewAdapter = SearchSlipAdapter(activity as AppCompatActivity, objList, false)

//        intent.getStringExtra("SelectedItem")?.apply {
//            var objSelected = JsonParser().parse(this)
//            var listSelected = JsonArray()
//            listSelected.add(objSelected)
//            (viewAdapter as SDocTypeAdapter).setItem(listSelected)
//        }

        recyclerView = (activity as AppCompatActivity).findViewById<RecyclerView>(R.id.view_recyclerResultSlip).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

            (adapter as SearchSlipAdapter).setShowSlipListener(this@Frag_SearchSlip as SearchSlipAdapter.ShowSlip)
            (adapter as SearchSlipAdapter).setRemoveSlipListener(this@Frag_SearchSlip as SearchSlipAdapter.RemoveSlip)
            (adapter as SearchSlipAdapter).setMoveSlipListener(this@Frag_SearchSlip as SearchSlipAdapter.MoveSlip)
            (adapter as SearchSlipAdapter).setCopySlipListener(this@Frag_SearchSlip as SearchSlipAdapter.CopySlip)
        }
    }

    private  fun setToolbar() {
        //run only when current fragment is this class.
        (activity as AppCompatActivity)?.run {
            setSupportActionBar(view_toolbarSearchSlip)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            setHasOptionsMenu(false)
        }


        view_btnNavi.setOnClickListener(activity as MainActivity)
        view_btnSearch.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when(v?.tag.toString().toUpperCase()) {

            "SEARCH_SLIP" -> {
                Intent(activity, SearchOptionActivity::class.java).run {
                    putExtra("SEARCH_UI_SECTION", "SLIP")
                    startActivityForResult(this, RESULT_SEARCH_OPTION)
                }
            }
        }
    }

    override fun showSlip(obj: JsonObject) {
        Intent(activity, ThumbActivity::class.java).run {
            putExtra("selectedItem", obj.toString())
            startActivity(this)
        }
    }

    //RemoveSlip
    override fun removeSlip(obj: JsonObject) {
        AlertDialog.Builder(activity as Context).apply {
            setMessage((activity as Context).getString(R.string.confirm_remove_slip))
            setPositiveButton((activity as Context).getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                RemoveSlip(this@Frag_SearchSlip).execute(obj)
            })
            setNegativeButton((activity as Context).getString(R.string.btn_cancel),null)
        }.show()
    }

    override fun copySlip(obj: JsonObject) {

        var strSDocStep = obj.get("SDOC_STEP").asInt

        AlertDialog.Builder(activity as Context).apply {

            if(strSDocStep > 1) {
                setMessage((activity as Context).getString(R.string.copy_cannot_process_not_init))
                setPositiveButton((activity as Context).getString(R.string.btn_confirm), null)
            }
            else
            {
                setMessage((activity as Context).getString(R.string.confirm_copy_slip))
                setPositiveButton((activity as Context).getString(R.string.btn_confirm)) { dialog, which ->
                    Intent(activity, SelectUserActivity::class.java).run {
                        putExtra("TYPE", "COPY")
                        putExtra("SLIP",obj.toString())
                        startActivityForResult(this, RESULT_COPY_SLIP_SELECT_USER)
                    }
                }
                setNegativeButton((activity as Context).getString(R.string.btn_cancel),null)
            }

        }.show()
    }

    override fun moveSlip(obj: JsonObject) {

        var strSDocStep = obj.get("SDOC_STEP").asInt

        AlertDialog.Builder(activity as Context).apply {

            if(strSDocStep > 1) {
                setMessage((activity as Context).getString(R.string.move_cannot_process_not_init))
                setPositiveButton((activity as Context).getString(R.string.btn_confirm), null)
            }
            else
            {
                setMessage((activity as Context).getString(R.string.confirm_move_slip))
                setPositiveButton((activity as Context).getString(R.string.btn_confirm)) { dialog, which ->
                    Intent(activity, SelectUserActivity::class.java).run {
                        putExtra("TYPE", "MOVE")
                        putExtra("SLIP",obj.toString())
                        startActivityForResult(this, RESULT_MOVE_SLIP_SELECT_USER)
                    }
                }
                setNegativeButton((activity as Context).getString(R.string.btn_cancel),null)
            }

        }.show()
    }



    override fun searchSelectedSlip(objItem: JsonObject) {

        objItem.get("CABINET")?.run {
            var strCabinet = this.asString
            m_objSearchOptions = JsonObject()
            m_objSearchOptions.addProperty("USED_CATEGORY", SearchOptionActivity.SEGMENT_STEP_ALL) // 0 = Unused
            m_objSearchOptions.addProperty("WORK_CATEGORY", SearchOptionActivity.SEGMENT_TASK_COMMON) // 0 = Unused
            m_objSearchOptions.addProperty("CABINET_START",strCabinet)
            m_objSearchOptions.addProperty("CABINET_END",strCabinet)

            runSearchSlip()
        }
    }

    private class GetSlipList(var fragment: Frag_SearchSlip, var socketManager: SocketManager, var objSearchOption:JsonObject): AsyncTask<String, Void, JsonArray>() {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = GET_SLIPLIST_FAILED

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
                        SocketManager()?.getSlipList(objSearchOption)?.let {

                            for (i in 0 until it.length) {
                                var element = it.item(i) as Element
                                var obj = JsonObject()
                                obj.addProperty("SDOC_NO", element.getElementsByTagName("SDOC_NO").item(0).textContent)
                                obj.addProperty("DOC_IRN", element.getElementsByTagName("DOC_IRN").item(0).textContent)
                                obj.addProperty("SDOCNO_DOC", element.getElementsByTagName("SDOCNO_DOC").item(0).textContent)
                                obj.addProperty("SDOC_STEP", element.getElementsByTagName("SDOC_STEP").item(0).textContent)
                                obj.addProperty("CABINET", element.getElementsByTagName("CABINET").item(0).textContent)
                                obj.addProperty("SDOC_NAME", element.getElementsByTagName("SDOC_NAME").item(0).textContent)
                                obj.addProperty("FILE_CNT", element.getElementsByTagName("FILE_CNT").item(0).textContent)
                                obj.addProperty("USER_NAME", element.getElementsByTagName("USER_NAME").item(0).textContent)
                                obj.addProperty("SDOCTYPE_NAME", element.getElementsByTagName("SDOCTYPE_NAME").item(0).textContent)
                                obj.addProperty("PART_NM", element.getElementsByTagName("PART_NM").item(0).textContent)
                                arObjRes.add(obj)
                            }
                            nCurrentStatus = GET_SLIPLIST_SUCCESS
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

            activity?.findViewById<SwipeRefreshLayout>(R.id.view_swipeLayoutSlip)?.let {
                it.setRefreshing(false)
            }

            when(nCurrentStatus) {
                GET_SLIPLIST_SUCCESS -> {

                    fragment.setupViewUI(result)
                    /*activity?.setResult(RESULT_OK)
                    activity?.finish()*/
                }
                GET_SLIPLIST_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.get_searchslip_failed))
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

    //Remove slip Thread proc
    private class RemoveSlip(var fragment: Frag_SearchSlip): AsyncTask<JsonObject, Void, Void>()
    {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = REMOVE_SLIP_FAILED

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
                        this.get("SDOC_STEP")?.asInt?.run {
                            if(this > 1)
                            {
                                nCurrentStatus = REMOVE_SLIP_NOT_ALLOWED
                                return null
                            }
                        }

                    }
                    //consider protocol property
                    if ("SOCKET".equals(CONNECTION_PROTOCOL, true)) {
                        if(SocketManager().removeSlip(params[0]!!))
                        {
                            nCurrentStatus = REMOVE_SLIP_SUCCESS
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
                REMOVE_SLIP_SUCCESS -> {

                    fragment.runSearchSlip()
                }
                REMOVE_SLIP_NOT_ALLOWED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.only_init_slip_allowed))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                            //    activity.onBackPressed()
                        })
                    }.show()
                }
                REMOVE_SLIP_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.failed_remove_slip))
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