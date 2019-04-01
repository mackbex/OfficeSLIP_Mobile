package com.officeslip.View.Main

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.sgenc.officeslip.*
import com.officeslip.Util.Common
import com.officeslip.Util.UserDefault
import kotlinx.android.synthetic.main.fragment_main_setting.*
import android.support.constraint.ConstraintSet
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.SwitchCompat
import android.widget.*
import com.officeslip.*
import com.officeslip.Adapter.MainViewPageAdapter
import com.officeslip.Agent.WDMAgent
import com.officeslip.Socket.SocketManager
import com.officeslip.Subclass.DialogSelectServer
import com.officeslip.Util.Logger
import com.officeslip.Util.SQLite
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.ref.WeakReference


class Frag_Setting:Fragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener
{

    private val m_C: Common = Common()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_main_setting, container, false)

        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        m_C.loadUIXML(activity as FragmentActivity, view_layoutUICell, this as Frag_Setting,  g_SysInfo.strDisplayLang+"/view/VIEW_SETTING.xml")
        setupViewUI()
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(isVisibleToUser) {
          //  setToolbar()
            activity?.run {
                (this as MainActivity).setNavigationMenu(MainActivity.TAB_SETTING, null)
            }

            toolbar_title?.apply {
                if(g_SysInfo.ServerMode == MODE_PRD)
                {
                    text = getString(R.string.main_tab_setting)
                }
                else
                {
                    text = getString(R.string.main_tab_setting) + " (DEV)"
                }
            }

        }
    }

    override fun onStart() {
        super.onStart()

        setupViewUI()
    }

    private fun setupViewUI() {


        view_layoutUICell?.run {


            this.findViewWithTag<SwitchCompat>("OPTION_CELLULAR")?.let {
                it.setOnCheckedChangeListener(null)
                it.isChecked = g_SysInfo.bCellular
                it.setOnCheckedChangeListener(this@Frag_Setting)
            }

            this.findViewWithTag<SwitchCompat>("OPTION_PASSWORD")?.let{
                it.setOnCheckedChangeListener(null)
                it.isChecked = g_SysInfo.bPINEnabled
                it.setOnCheckedChangeListener(this@Frag_Setting)
            }

            this.findViewWithTag<ConstraintLayout>("LOGOUT")?.let{

                var viewButtonTitle = it.findViewById<ImageView>(R.id.view_imageCellContents)
                viewButtonTitle.visibility = View.GONE

                var viewTextTitle = it.findViewById<TextView>(R.id.view_textCellTitle)
                viewTextTitle.setTextColor(ContextCompat.getColor(context, R.color.colorLogout))
                centerCellItem(viewTextTitle, it)
            }

            this.findViewWithTag<ConstraintLayout>("PASSCHANGE")?.let{

                var viewButtonTitle = it.findViewById<ImageView>(R.id.view_imageCellContents)
                viewButtonTitle.visibility = View.GONE

                var viewTextTitle = it.findViewById<TextView>(R.id.view_textCellTitle)
                viewTextTitle.setTextColor(ContextCompat.getColor(context, R.color.colorPassChange))
                centerCellItem(viewTextTitle, it)
            }

            this.findViewWithTag<ConstraintLayout>("CASH")?.let{

                var viewButtonTitle = it.findViewById<ImageView>(R.id.view_imageCellContents)
                viewButtonTitle.visibility = View.GONE

                var viewTextTitle = it.findViewById<TextView>(R.id.view_textCellTitle)
                viewTextTitle.setTextColor(ContextCompat.getColor(context, R.color.colorLogout))
                centerCellItem(viewTextTitle, it)
            }

            this.findViewWithTag<ConstraintLayout>("VERSION")?.let{

                var viewTextTitle = it.findViewById<TextView>(R.id.view_textCellContents)
                viewTextTitle.text = activity?.packageManager?.getPackageInfo(activity?.packageName, 0)?.versionName.toString()

                it.setOnClickListener(this@Frag_Setting)
            }

            this.findViewWithTag<ConstraintLayout>("DEV")?.let{

                var viewTextTitle = it.findViewById<TextView>(R.id.view_textCellContents)
                viewTextTitle.text = getString(R.string.developer)
            }
        }
    }

    private fun centerCellItem(view:View, parent:ConstraintLayout) {

        val constraintSet = ConstraintSet()
        constraintSet.clone(parent)
        constraintSet.connect(view.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0)
        constraintSet.connect(view.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
        constraintSet.applyTo(parent)
    }

    private  fun setToolbar() {
        setHasOptionsMenu(true)
        var curFrag = activity?.view_pagerMain?.run {
            (adapter as MainViewPageAdapter).getItem(this.currentItem)
        }

        if(curFrag?.javaClass == this@Frag_Setting.javaClass) {

            (activity as AppCompatActivity).supportActionBar?.let {
                it.show()
                it.title = getString(R.string.main_tab_setting)
            }
        }
    }

    override fun onClick(v: View?) {
    //    Log.d("test",v?.tag.toString())
        when(v?.tag.toString().toUpperCase())
        {
            "LOGOUT" -> {

                AlertDialog.Builder(activity as FragmentActivity).apply {
                    setTitle(getString(R.string.logout_alert_title))
                    setMessage(getString(R.string.logout_alert_contents))

                setPositiveButton(getString(R.string.btn_confirm)) { dialogInterface, i ->
                    g_UserInfo.bHasLogged = false
                    g_SysInfo.strDisplayLang = m_C.getSystemLocale(context)

                    SQLite(activity as FragmentActivity).removeAll()
                    UserDefault(activity as FragmentActivity).removeAll()

                    Intent(activity, LoginActivity::class.java).run {
                        activity?.startActivityForResult(this, MainActivity.RESULT_USER_LOGGED)
                    }
                }

                setNegativeButton(getString(R.string.btn_cancel), null)
                }.create().show()


            }
            "VERSION" -> {
                DialogSelectServer(activity as FragmentActivity)?.run {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    show()
                }
            }
             "PASSCHANGE" -> {
                Intent(activity, PINActivity::class.java).run {
                   // if(g_SysInfo.str)
                    //g_SysInfo.strLockPasswordValue = ""
                    this.putExtra("MODE","CHANGE")
                    startActivity(this)
                }
            }
            "BUG" -> {
                //try connect agent via Wifi Network if Mobile data usage is disabled.
                var alert = AlertDialog.Builder(activity as FragmentActivity).apply {
                    setTitle(getString(R.string.btn_confirm))
                    setMessage(getString(R.string.confirm_send_log_contents))
                    setPositiveButton(getString(R.string.btn_confirm), DialogInterface.OnClickListener { dialog, which ->
                        var socketManager = SocketManager()
                        var listLogFiles = File(LOG_PATH).listFiles()

                        if(listLogFiles.size > 0)
                        {
                            SendReport(activity as FragmentActivity, socketManager).execute(listLogFiles)
                        }
                        else
                        {
                            AlertDialog.Builder(activity as Context).apply {
                                setTitle(getString(R.string.title_send_log))
                                setMessage(getString(R.string.no_log_exists))
                                setPositiveButton(getString(R.string.btn_confirm), null)
                            }.show()
                        }
                    })
                    setNegativeButton(getString(R.string.btn_cancel), null)
                }.show()
            }

            "CACHE" -> {
                AlertDialog.Builder(activity as Context).run {
                    setMessage(getString(R.string.confirm_delete_cache))
                    setPositiveButton(getString(R.string.btn_confirm)) { dialog, which ->
                        SQLite(context).setRecord("DROP TABLE IF EXISTS PROPERTY_T", null)
                    }
                    setNegativeButton(getString(R.string.btn_cancel), null)
                }.show()
            }
        }
    }


    private class SendReport(activity:Activity, var socketManager: SocketManager):AsyncTask<Array<File>, Int, String?>() {

        var activityRef:WeakReference<Activity>? = null
        var m_C: Common = Common()
        var m_WDM = WDMAgent()
        var builder:AlertDialog? = null

        init {
            activityRef = WeakReference(activity)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            var activity = activityRef?.get()

            builder = AlertDialog.Builder(activity as Context).apply {
                var view = LayoutInflater.from(activity).inflate(R.layout.progress_line, null)

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

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)

            var activity            = activityRef?.get()
            var nCur                     = if(values[0] != null) values[0]!! else 0
            var nTotal                   = if(values[1] != null) values[1]!! else 0
            var textContents            = String.format(activity!!.getString(R.string.send_progress_contents), nCur+1, nTotal)
            var dialog              = builder as AlertDialog
            var viewProgress = dialog.findViewById<ProgressBar>(R.id.view_progress)

            dialog.findViewById<TextView>(R.id.view_textProgressContents)?.text = textContents

            viewProgress?.apply {
                progress = nCur+1
                if(isIndeterminate) isIndeterminate = false
                max = nTotal
            }

        }
        override fun doInBackground(vararg params: Array<File>?): String? {

            if(!isCancelled)
            {
                var strRes:String? = null
                var listFile = params[0]
                if (listFile != null && listFile.isNotEmpty())
                {
                    val strReportKey = socketManager.getSDocNo()

                    if (!m_C.isBlank(strReportKey)) {
                        for (i in 0 until listFile.size) {
                            val nFile = listFile.get(i)
                            val strDocIRN = WDMAgent().getDocIRN()//m_WDM.getDocIRN("USER", m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)
                            if (!m_C.isBlank(strDocIRN)) {
                                if (socketManager.upload(nFile.absolutePath, strDocIRN, "1", "MOBILE_REPORT_X")) {

                                    var strSuffixFor_UTF_8 = ""

                                    if ("UTF-8".equals(CONNECTION_CHARSET, true) && "MSSQL".equals(CONNECTION_DB, true)) {
                                        strSuffixFor_UTF_8 = "N"
                                    }

                                    val sbQuery = StringBuffer().run {
                                        append("insert into MOBILE_REPORT_T(\"DOC_IRN\", \"CABINET\", \"FOLDER\", \"CO_NO\", \"PART_NO\", ")
                                        append("\"ADD_NO\", \"ADD_NAME\", \"ADD_FILE\", \"REG_USER\", \"REG_TIME\",  \"FILE_SIZE\" ")
                                        append(" )")
                                        append(" values (")
                                        append("'$strDocIRN', ")
                                        append("'" + m_C.getDate("yyyyMMdd") + "', ")
                                        append("'$AGENT_FOLDER', ")
                                        append("'0000', ")
                                        append("'00000000', ")
                                        append("'$strReportKey', ")
                                        append("$strSuffixFor_UTF_8'" + nFile.name.substring(0, nFile.name.lastIndexOf("")) + "', ")
                                        append("$strSuffixFor_UTF_8'" + nFile.name + "', ")
                                        append("'A', ")
                                        append("'" + m_C.getDate("yyyyMMddhhmmsss") + "', ")
                                        append("'" + m_C.getFileSize(nFile.absolutePath) + "' ")
                                        append(" ) ")
                                    }

                                    var strReceivedRes = socketManager.setData(sbQuery.toString())

                                    Thread.sleep(3000)

                                    if (strReceivedRes.get("Result").asBoolean) {
                                        strRes = strReportKey

                                        publishProgress(i, listFile.size)
                                     //   onProgressUpdate()
                                    } else {
                                        Logger.WriteLog(this@SendReport.javaClass.name, "sendReport", "Failed to Insert Query", 7)
                                        strRes = null
                                        break
                                    }
                                } else {
                                    Logger.WriteLog(this@SendReport.javaClass.name, "sendReport", "Failed to Upload logfile", 7)
                                    break
                                }
                            } else {
                                Logger.WriteLog(this@SendReport.javaClass.name, "sendReport", "Failed to generate DocIRN", 7)
                                break
                            }
                        }
                    }
                }
                return strRes
            }
            else
            {
                socketManager.destroy()
                return null
            }
        }

        override fun onCancelled(result: String?) {
            super.onCancelled(result)
            socketManager.destroy()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            builder?.dismiss()
            socketManager.destroy()
            var activity = activityRef?.get()
            var alert = AlertDialog.Builder(activity as Context)
            alert.setTitle(activity.getString(R.string.title_send_log))
            alert.setPositiveButton(activity.getString(R.string.btn_confirm), null)

            if(m_C.isNetworkConnected(activity))
            {
                //consider protocol property
                if("SOCKET".equals(CONNECTION_PROTOCOL, true))
                {

                    if(!m_C.isBlank(result))
                    {
                        var strMsg = String.format(activity.getString(R.string.success_send_log_contents), result)
                        alert.setMessage(strMsg)
                    }
                    else
                    {
                        alert.setTitle(activity.getString(R.string.failed_send_title))
                        alert.setMessage(activity.getString(R.string.failed_send_log_contents))
                    }
                }
            }
            else
            {
                alert.setTitle(activity.getString(R.string.failed_send_title))
                alert.setMessage(activity.getString(R.string.alert_network_disabled_message))
            }

            alert.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

      //  if(buttonView?.isPressed!!) {
            when (buttonView?.tag.toString().toUpperCase()) {
                "OPTION_PASSWORD" -> {
                    if(isChecked) {
                        Intent(activity, PINActivity::class.java).run {
                            this.putExtra("MODE", "CHANGE")
                            activity?.startActivityForResult(this, MainActivity.RESULT_PIN_ENABLE)
                        }
                    }
                    else
                    {
                        Intent(activity, PINActivity::class.java).run {
                            this.putExtra("MODE", "VERIFY")
                            activity?.startActivityForResult(this, MainActivity.RESULT_PIN_DISABLE)
                        }
                    }

//                    g_SysInfo.bPINEnabled = isChecked


//                    if(isChecked) {
//                        Intent(activity, PINActivity::class.java).run {
//                            this.putExtra("MODE", "CHANGE")
//                            activity?.startActivityForResult(this, MainActivity.RESULT_APP_LOGGED)
//                        }
//                    }
                }
                "OPTION_CELLULAR" -> {
                    g_SysInfo.bCellular = isChecked
                }
            }
       // }
    }

}