package com.officeslip.View.Main.Slip

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Point
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.officeslip.*
import com.officeslip.Adapter.StatisticsAdapter
import com.sgenc.officeslip.R
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.AutoFitGridLayoutManager
import com.officeslip.Util.Common
import kotlinx.android.synthetic.main.fragment_main_statistics.*
import org.w3c.dom.Element
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class Frag_Statistics : Fragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, StatisticsAdapter.SearchSlip
{
    companion object {
        const val RESULT_SEARCH_OPTION = 9001
        const val GET_STATISTICS_SUCCESS = 9002
        const val GET_STATISTICS_FAILED = 9003
        const val NETWORK_DISABLED = 9004
    }

    private val m_C: Common = Common()
    private var m_objSearchOptions = JsonObject()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    interface SearchSelectedItemListener {
        fun searchSelectedSlip(objItem:JsonObject)
    }

    private var callback: SearchSelectedItemListener? = null


    //Interface with parent activity
//    override fun onAttach(context: Context?) {
//        super.onAttach(context)
//
//        if(activity != null && activity is SearchSelectedItemListener)
//        {
//            callback = activity as SearchSelectedItemListener
//        }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //AGENT_URL
        //  Log.d("TEST", strFragmentName)
        val view = inflater?.inflate(R.layout.fragment_main_statistics, container, false)

        parentFragment?.apply {
            callback = this as SearchSelectedItemListener
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()
        view_swipeLayoutStatistics.setOnRefreshListener(this)
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
                    text = getString(R.string.main_tab_slip_statistics)
                }
                else
                {
                    text = getString(R.string.main_tab_slip_statistics) + " (DEV)"
                }
            }

            //Run query in AsyncTask
            GetStatistic(this@Frag_Statistics, m_objSearchOptions).execute()

        }
    }

    private fun setToolbar() {
        //run only when current fragment is this class.
        (activity as AppCompatActivity)?.run {
            setSupportActionBar(view_toolbarStatistics)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            setHasOptionsMenu(false)
        }

        view_btnNavi.setOnClickListener(activity as MainActivity)
        view_btnSearch.setOnClickListener(this)
    }

    //Set statistics UI
    private fun setupViewUI(arObjList:JsonArray?){

        var display = activity?.windowManager?.defaultDisplay
        var size = Point()
        display?.getSize(size)

        var fPadding = 10
        var nAvrWidth = (size.x / 3) - fPadding

        viewManager = AutoFitGridLayoutManager(activity as Context, nAvrWidth)
        viewAdapter = StatisticsAdapter(activity as Context, arObjList)

        recyclerView = view_recyclerResultStatistics.apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter

            (adapter as StatisticsAdapter).setSearchSlipListener(this@Frag_Statistics as StatisticsAdapter.SearchSlip)

        }
    }

    fun runSearchStatistics() {
        //Set default search options.
        if (m_objSearchOptions.size() <= 0) {

            var calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.DATE, DEFAULT_SLIP_SEARCH_PERIOD)
            var strCabinetStart = SimpleDateFormat("yyyyMMdd").format(Date(calendar.timeInMillis))
            m_objSearchOptions.addProperty("CABINET_START", strCabinetStart)
            m_objSearchOptions.addProperty("CABINET_END", m_C.getDate("yyyyMMdd"))

        }

        GetStatistic(this@Frag_Statistics, m_objSearchOptions).execute()
    }

    //Receive search options.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            RESULT_SEARCH_OPTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.getStringExtra("SEARCH_OPTION")?.run {
                        m_objSearchOptions = JsonParser().parse(this as String).asJsonObject
                        GetStatistic(this@Frag_Statistics, m_objSearchOptions).execute()
                    }
                }
            }
        }
    }

    override fun onRefresh() {
        runSearchStatistics()
    }

    override fun onClick(v: View?) {

        when(v?.tag.toString().toUpperCase()) {

            "SEARCH_STATS" -> {
                Intent(activity, SearchOptionActivity::class.java).run {
                    putExtra("SEARCH_UI_SECTION", "STATS")
                    startActivityForResult(this, RESULT_SEARCH_OPTION)
                }
            }
        }
    }

    override fun searchSlip(objItem: JsonObject) {

        if(callback != null){
            callback?.searchSelectedSlip(objItem)
        }
    }

    private class GetStatistic(var fragment: Frag_Statistics, var objSearchOption:JsonObject): AsyncTask<Void, Void, JsonArray>()
    {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = GET_STATISTICS_FAILED

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
                    }

                    dialog.dismiss()
                })}.create()
            builder?.show()
        }

        override fun doInBackground(vararg params: Void?): JsonArray {
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
                        SocketManager().getStatisticsList(objSearchOption)?.let {

                            for (i in 0 until it.length) {
                                var element = it.item(i) as Element
                                var obj = JsonObject()
                                obj.addProperty("CABINET", element.getElementsByTagName("CABINET").item(0).textContent)
                                obj.addProperty("STEP0", element.getElementsByTagName("STEP0").item(0).textContent)
                                obj.addProperty("STEP1", element.getElementsByTagName("STEP1").item(0).textContent)
                                obj.addProperty("STEP2", element.getElementsByTagName("STEP2").item(0).textContent)
                                obj.addProperty("STEP9", element.getElementsByTagName("STEP9").item(0).textContent)
                                arObjRes.add(obj)
                            }
                            nCurrentStatus = GET_STATISTICS_SUCCESS
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

            activity?.findViewById<SwipeRefreshLayout>(R.id.view_swipeLayoutStatistics)?.let {
                it.isRefreshing = false
            }

            when(nCurrentStatus) {
                GET_STATISTICS_SUCCESS -> {

                    fragment.setupViewUI(result)
                }
                GET_STATISTICS_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.get_searchstatistics_failed))
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