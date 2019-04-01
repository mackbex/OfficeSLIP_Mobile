package com.officeslip

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.officeslip.Adapter.SDocTypeAdapter
import com.officeslip.Socket.SocketManager
import com.officeslip.Util.Common
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.activity_sdoctype.*
import org.w3c.dom.Element
import java.lang.ref.WeakReference

class SDocTypeActivity : AppCompatActivity(), TextWatcher, SDocTypeInterface, ActionMode.Callback, View.OnClickListener {

    companion object {
        const val NETWORK_DISABLED = 3001
        const val GET_SDOCTYPE_SUCCESS = 3002
        const val GET_SDOCTYPE_FAILED = 3003
        const val RESULT_FROM_SUBACTIVITY = 3004

    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var m_C = Common()
    var strSDocParentCD:String? = null
    var nSDocLevel = 1
    var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sdoctype)

        setSupportActionBar(view_toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        setTabBtnTintForLowAPI()

        intent.getStringExtra("PARENT_ITEM")?.let {
            val objParent = JsonParser().parse(it as String)?.asJsonObject
            objParent?.let {
                strSDocParentCD = it.get("HAS_CHILD")?.asString

                val strActivityTitle = view_textTitle.text.toString() + "(${it.get("name").asString})"
                view_textTitle.text = strActivityTitle
                nSDocLevel += 1
            }

        }

        GetSDocType(this@SDocTypeActivity, SocketManager(), strSDocParentCD, nSDocLevel).execute()
    }


    fun setupViewUI(jList:JsonArray?) {

        view_btnClose.setOnClickListener(this@SDocTypeActivity)
        view_btnCheck.setOnClickListener(this@SDocTypeActivity)
        view_editSearchKeyword.addTextChangedListener(this@SDocTypeActivity)

        viewManager = LinearLayoutManager(this)
        viewAdapter = SDocTypeAdapter(this@SDocTypeActivity, jList, false)

        intent.getStringExtra("SelectedItem")?.apply {
            var objSelected = JsonParser().parse(this)
            var listSelected = JsonArray()
            listSelected.add(objSelected)
            (viewAdapter as SDocTypeAdapter).setItem(listSelected)
        }
        intent.getStringExtra("Mode")?.apply {
            if("SEARCH" == this.toUpperCase())
            (viewAdapter as SDocTypeAdapter).addAllBtn()
        }

        recyclerView = findViewById<RecyclerView>(R.id.view_recyclerResult).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }

    private fun setTabBtnTintForLowAPI() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {

            view_imageMagnifier?.apply {
                var color = ContextCompat.getColor(this@SDocTypeActivity, R.color.colorInnerSearchBackground)
                setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            }
//            view_btnCheck?.apply {
//                var color = ContextCompat.getColor(this@SDocTypeActivity, R.color.colorActionBtnDefault)
//                background.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
//            }
//            view_btnClose?.apply {
//                var color = ContextCompat.getColor(this@SDocTypeActivity, R.color.colorActionBtnDefault)
//                background.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
//            }
        }
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {

        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        //
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }
    override fun sdocTypeInterFace(size: Int) {
        if (actionMode == null) actionMode = startSupportActionMode(this)
        if (size > 0) actionMode?.title = "$size"
        else actionMode?.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

       // menuInflater.inflate(R.menu.search_sdoctype, menu)
        return true
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        (viewAdapter as SDocTypeAdapter).filter.filter(s)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                RESULT_FROM_SUBACTIVITY -> {
                    var listSelected = data?.getStringExtra("listSelected")
                    if(!m_C.isBlank(listSelected))
                    {
                        var jsonItem = JsonParser().parse(listSelected).asJsonArray
                        (viewAdapter as SDocTypeAdapter).setSelectedItems(jsonItem)
                    }
                    checkProc()
                }
            }
        }
    }

    private fun checkProc() {
        var listSelected =  (viewAdapter as SDocTypeAdapter)?.selectedItem
        if(listSelected.size() > 0)
        {
            intent.putExtra("listSelected",listSelected.toString())
            this@SDocTypeActivity.run {
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        else
        {
            AlertDialog.Builder(this@SDocTypeActivity).run {
                setTitle(this@SDocTypeActivity.getString(R.string.title_sdoctype_activity))
                setMessage(this@SDocTypeActivity.getString(R.string.select_sdoctype_item))
                setPositiveButton(this@SDocTypeActivity.getString(R.string.btn_confirm), null)
            }.show()
        }
    }

    override fun onClick(v: View?) {

        when(v?.id)
        {
            R.id.view_btnClose -> {
                onBackPressed()
            }
            R.id.view_btnCheck -> {
                checkProc()

            }
        }
    }


    private class GetSDocType(activity: Activity, var socketManager: SocketManager, var strSDocParentCD:String?, var nSDocLevel:Int): AsyncTask<String, Void, JsonArray>() {
        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = GET_SDOCTYPE_FAILED

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
                setNegativeButton(activity.getString(R.string.btn_cancel), DialogInterface.OnClickListener { dialog, which ->

                    if(status == AsyncTask.Status.RUNNING)
                    {
                        cancel(true)
                        activity.onBackPressed()
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
                        SocketManager()?.getSDocTypeList(strSDocParentCD, nSDocLevel)?.let {

                            for (i in 0 until it.length) {
                                var element = it.item(i) as Element
                                var obj = JsonObject()
                                obj.addProperty("name",element.getElementsByTagName("SDOC_NAME").item(0).textContent)
                                obj.addProperty("code",element.getElementsByTagName("SDOC_CODE").item(0).textContent)
                                obj.addProperty("HAS_CHILD",element.getElementsByTagName("HASCHILD").item(0).textContent)
                                arObjRes.add(obj)
                            }
                            nCurrentStatus = GET_SDOCTYPE_SUCCESS
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
                GET_SDOCTYPE_SUCCESS -> {

                    (activity as SDocTypeActivity).setupViewUI(result)
                    /*activity?.setResult(RESULT_OK)
                    activity?.finish()*/
                }
                GET_SDOCTYPE_FAILED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.get_sdoctype_failed))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                            activity.onBackPressed()
                        })
                    }.show()
                }
                NETWORK_DISABLED -> {
                    AlertDialog.Builder(activity as Context).run {
                        setMessage(activity.getString(R.string.alert_network_disabled_message))
                        setPositiveButton(activity.getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                            activity.onBackPressed()
                        })
                    }.show()
                }
            }
        }

    }
}

interface SDocTypeInterface
{
    fun sdocTypeInterFace(size:Int)
}