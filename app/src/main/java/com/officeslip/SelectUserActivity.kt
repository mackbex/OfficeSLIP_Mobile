package com.officeslip

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.officeslip.Adapter.SelectUserAdapter
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.Common
import com.officeslip.Util.SQLite
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.activity_select_user.*
import org.w3c.dom.Element
import java.lang.ref.WeakReference
import java.util.HashMap

class SelectUserActivity:AppCompatActivity(), View.OnClickListener, TextView.OnEditorActionListener
{



    companion object {
        const val NETWORK_DISABLED = 6001
        const val GET_USER_SUCCESS = 6002
        const val GET_USER_FAILED = 6003

    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var m_strSelectType:String? = null
    private var m_C = Common()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)

        setSupportActionBar(view_toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        setTabBtnTintForLowAPI()

        intent.getStringExtra("TYPE")?.apply {
            m_strSelectType = this
        }

        if(m_C.isBlank(m_strSelectType))
        {
            AlertDialog.Builder(this@SelectUserActivity).run {
                setMessage(getString(R.string.get_user_list_failed))
                setPositiveButton(getString(R.string.btn_confirm)) { dialog, which ->
                    finish()
                }
            }.show()
        }
       // GetSDocType(this@SDocTypeActivity, SocketManager()).execute()

        setupViewUI()
    }


    fun setupViewUI() {

        view_btnClose.setOnClickListener(this@SelectUserActivity)
        view_btnCheck.setOnClickListener(this@SelectUserActivity)
        view_editSearchKeyword.setOnEditorActionListener(this@SelectUserActivity)


        var arFavoriteList = getFavoriteList(m_strSelectType!!)

        viewManager = LinearLayoutManager(this@SelectUserActivity)
        viewAdapter = SelectUserAdapter(this@SelectUserActivity, null, m_strSelectType!!)

        recyclerView = findViewById<RecyclerView>(R.id.view_recyclerResult).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }

        if(arFavoriteList != null && arFavoriteList.size() > 0)
        {
            (viewAdapter as SelectUserAdapter).setFavoriteItem(arFavoriteList)
        }
    }


    //Get favorite list from sqlite db
    fun getFavoriteList(strType:String):JsonArray
    {
        var arRes = JsonArray()

        var tableConstant = SQLITE_CREATE_FAVORITE_MOVE_T
        var sbQuery = StringBuffer().apply {
            append(" Select ")
            append(" USER_ID ")
            append(" ,USER_NAME ")
            append(" ,PART_CD ")
            append(" ,PART_NM ")
            append(" ,CO_CD ")
            append(" ,CO_NAME ")
            append(" From ")
            when(strType.toUpperCase())
            {
                "MOVE" -> {
                    append(" FAVORITE_MOVE_T ")
                    tableConstant = SQLITE_CREATE_FAVORITE_MOVE_T
                }
                "COPY" -> {
                    append(" FAVORITE_COPY_T ")
                    tableConstant = SQLITE_CREATE_FAVORITE_COPY_T
                }
            }
            append(" Order by ")
            append(" REG_TIME Desc ")
            append(" LIMIT '$FAVOITELIST_LIMIT' ")

        }

        SQLite(this@SelectUserActivity).instantGetRecord(tableConstant, sbQuery.toString(), null)?.forEach {
            it as HashMap<String, String>

            var objUser = JsonObject()
            objUser.addProperty("USER_ID",it["USER_ID"])
            objUser.addProperty("USER_NAME",it["USER_NAME"])
            objUser.addProperty("PART_CD",it["PART_CD"])
            objUser.addProperty("PART_NM",it["PART_NM"])
            objUser.addProperty("CO_CD",it["CO_CD"])
            objUser.addProperty("CO_NAME",it["CO_NAME"])

            arRes.add(objUser)
        }

        return arRes
    }

    override fun onClick(v: View?) {

        when(v?.id)
        {
            view_btnClose.id -> { finish() }
            view_btnCheck.id -> {
                var objSelectedUser =  (viewAdapter as SelectUserAdapter)?.m_objSelectedUser
                if(objSelectedUser != null)
                {
                    intent.putExtra("SELECTED_USER",objSelectedUser.toString())
                    this@SelectUserActivity.run {
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
                else
                {
                    AlertDialog.Builder(this@SelectUserActivity).run {
                        setMessage(getString(R.string.select_user_item))
                        setPositiveButton(getString(R.string.btn_confirm), null)
                    }.show()
                }
            }
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {

        when(v?.id)
        {
            //Edittext enter event
            view_editSearchKeyword.id -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    var strKeyword = v.text.toString()
                    if(m_C.isBlank(strKeyword))
                    {
                        AlertDialog.Builder(this@SelectUserActivity).run {
                            setMessage(getString(R.string.input_search_text))
                            setPositiveButton(getString(R.string.btn_confirm)) { dialog, which ->

                            }
                        }.show()

                        return true
                    }
                    GetUserList(this@SelectUserActivity).execute(v.text.toString())
                }
            }
        }

        return true
    }

    private fun setTabBtnTintForLowAPI() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {

            view_imageMagnifier?.apply {
                var color = ContextCompat.getColor(context, R.color.colorInnerSearchBackground)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
            view_btnCheck?.apply {
                var color = ContextCompat.getColor(context, R.color.colorActionBtnDefault)
                background.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
            view_btnClose?.apply {
                var color = ContextCompat.getColor(context, R.color.colorActionBtnDefault)
                background.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private class GetUserList(activity: Activity):AsyncTask<String, Void, JsonArray>()
    {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = GET_USER_FAILED

        init {
            activityRef = WeakReference(activity)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            var activity = activityRef?.get()
            builder = AlertDialog.Builder(activity as Context).apply {
                var view = LayoutInflater.from(activity).inflate(R.layout.progress_circle, null)
                view.findViewById<TextView>(R.id.view_textProgressTitle).text = activity.getString(R.string.in_progress)

                setView(view)
                setCancelable(false)
                setNegativeButton(activity.getString(R.string.btn_cancel)) { dialog, which ->

                    if(status == AsyncTask.Status.RUNNING) {
                        cancel(true)
                        activity.onBackPressed()
                    }

                    dialog.dismiss()
                }
            }.create()
            builder?.show()
        }

        override fun doInBackground(vararg params: String?): JsonArray {

            //Thread.sleep(10000)
            var arObjRes = JsonArray()

            if(!isCancelled)
            {
                var activity = activityRef?.get()
                var strKeyWord = params[0]

                //try connect agent via Wifi Network if Mobile data usage is disabled.
                if(m_C.isNetworkConnected(activity as Context))
                {
                    //consider protocol property
                    if("SOCKET".equals(CONNECTION_PROTOCOL, true))
                    {
                        SocketManager()?.getUserList(strKeyWord!!)?.let {

                            for (i in 0 until it.length) {
                                var element = it.item(i) as Element
                                var obj = JsonObject()
                                obj.addProperty("USER_ID",element.getElementsByTagName("USER_ID").item(0).textContent)
                                obj.addProperty("USER_NAME",element.getElementsByTagName("USER_NAME").item(0).textContent)
                                obj.addProperty("PART_CD",element.getElementsByTagName("PART_CD").item(0).textContent)
                                obj.addProperty("CO_CD",element.getElementsByTagName("CO_CD").item(0).textContent)
                                obj.addProperty("PART_NM",element.getElementsByTagName("PART_NM").item(0).textContent)
                                obj.addProperty("CO_NAME",element.getElementsByTagName("CO_NAME").item(0).textContent)
                                arObjRes.add(obj)
                            }
                            nCurrentStatus = GET_USER_SUCCESS
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

            when(nCurrentStatus) {
                GET_USER_SUCCESS -> {
                    ((activity as SelectUserActivity).viewAdapter as SelectUserAdapter).setItem(result)

                //    (activity as SelectUserActivity).setupViewUI(result)
                    /*activity?.setResult(RESULT_OK)
                    activity?.finish()*/
                }
                GET_USER_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.get_user_list_failed))
                        setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                            activity.onBackPressed()
                        }
                    }.show()
                }
                NETWORK_DISABLED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.alert_network_disabled_message))
                        setPositiveButton(activity.getString(R.string.btn_confirm)) { dialog, which ->
                            activity.onBackPressed()
                        }
                    }.show()
                }
            }
        }

    }
}