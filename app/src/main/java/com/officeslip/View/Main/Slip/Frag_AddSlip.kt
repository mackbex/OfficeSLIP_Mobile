package com.officeslip.View.Main.Slip

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.officeslip.*
import com.sgenc.officeslip.*
import com.officeslip.Adapter.AddThumbAdapter
import com.officeslip.Agent.WDMAgent
import com.officeslip.Operation.UploadDocument
import com.officeslip.Util.Common
import com.officeslip.View.EditSlip.EditSlipActivity
import com.officeslip.View.FileExplorer.FileExplorerActivity
import com.officeslip.Subclass.OverlayButtonDialog
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.fragment_main_addslip.*
import java.io.File
import java.lang.ref.WeakReference


class Frag_AddSlip:Fragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, TextView.OnEditorActionListener, View.OnFocusChangeListener, TextWatcher, AddThumbAdapter.ChangeSlipMode, OverlayButtonDialog.OnClickButton, AddThumbAdapter.ViewOriginal
{
    private val m_C: Common = Common()
    private var nCurSegmentId = 0
    private var m_objUploadInfo = JsonObject()
    private var nCurUploadSegmentId = SEGMENT_BTN_IMAGE
   // private var m_fileCapturedImagePath:Uri? = null
    private var m_fileCapturedFilePath:String? = null
    private var view_cardUpload:RecyclerView? = null
    private var tempDialogBuilder:AlertDialog? = null
    companion object {
        /**
         * Segment button tag
         */

        const val SEGMENT_BTN_IMAGE = 100001
        const val SEGMENT_BTN_FILE = 100002
        const val RESULT_SDOC_TYPE = 4001
        const val RESULT_CAMERA = 4002
        const val RESULT_GALLERY = 4003
        const val RESULT_VIEW_ORIGINAL = 4004
        const val RESULT_ADD_SLIP = 4005
        const val RESULT_ADD_FILE = 4006

//        const val VERIFY_NO_SDOC_NAME = 4011
//        const val VERIFY_NO_SDOC_TYPE = 4012
//        const val VERIFY_NO_SDOC_ITEM = 4013
//        const val VERIFY_SUCCESS = 4010
     }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.fragment_main_addslip, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Restore upload info when screen rotation.
        savedInstanceState?.apply {
            getString("UploadInfo")?.apply {
                m_objUploadInfo = JsonParser().parse(this) as JsonObject
            }
        }
        setToolbar()

        var strColumnName = m_C.getNodeNameAtIndex(activity as Activity, g_SysInfo.strDisplayLang+"/view/VIEW_ADDSLIP.xml", nCurSegmentId)
        m_C.loadUIXML(activity as Activity, view_layoutUICell, this as Frag_AddSlip,  g_SysInfo.strDisplayLang+"/view/VIEW_ADDSLIP.xml", strColumnName)
        setupViewUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Save current upload info to bundle
        outState?.putString("UploadInfo",m_objUploadInfo.toString())

    }

    private fun setupViewUI() {


        //setRemoveDataListener(this as OriginalViewAcitivty.RemoveUploadData)

        view_layoutUICell?.run {

            this.findViewWithTag<SegmentedGroup>("TASK")?.apply{
                setOnCheckedChangeListener(null)
                (getChildAt(nCurSegmentId) as RadioButton)?.isChecked = true
                setOnCheckedChangeListener(this@Frag_AddSlip)
            }

            this.findViewWithTag<SegmentedGroup>("SLIP")?.apply{
                setOnCheckedChangeListener(null)
                findViewById<RadioButton>(nCurUploadSegmentId)?.isChecked = true
                setOnCheckedChangeListener(this@Frag_AddSlip)
            }

            this.findViewWithTag<ConstraintLayout>("PART_NO")?.apply{

                var viewText = findViewById<TextView>(R.id.view_textCellContents)
                viewText.text = StringBuffer().apply {
                    append(g_UserInfo.strPartName)
                    append("(")
                    append(g_UserInfo.strPartID)
                    append(")")
                }
            }

            this.findViewWithTag<ConstraintLayout>("REG_USER")?.apply{

                var viewText = findViewById<TextView>(R.id.view_textCellContents)
                viewText.text = StringBuffer().apply {
                    append(g_UserInfo.strUserName)
                    append("(")
                    append(g_UserInfo.strUserID)
                    append(")")
                }
            }

            this.findViewWithTag<ConstraintLayout>("SDOC_TYPE")?.apply{

                m_objUploadInfo.get("SDOC_TYPE")?.apply {
                    var objItem = this as JsonObject

                    var viewText = findViewById<TextView>(R.id.view_textCellContents)
                    viewText.text = StringBuffer().apply {

                        append(objItem.get("name").asString)
                        append(" (")
                        append(objItem.get("code").asString)
                        append(")")
                    }
                }
            }

            this.findViewWithTag<RecyclerView>("UPLOAD_ITEM")?.apply {
                view_cardUpload = this

                m_objUploadInfo.add("UPLOAD_IMAGE",JsonArray())
                m_objUploadInfo.add("UPLOAD_FILE",JsonArray())

                var arObjImage = m_objUploadInfo.get("UPLOAD_IMAGE")
                (adapter as AddThumbAdapter).setItem(arObjImage.asJsonArray,"IMAGE")
            }

        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if(isVisibleToUser) {
        //    setToolbar()



            toolbar_title?.apply {
                if(g_SysInfo.ServerMode == MODE_PRD)
                {
                    text = getString(R.string.main_tab_add_slip)
                }
                else
                {
                    text = getString(R.string.main_tab_add_slip) + " (DEV)"
                }
            }
        }
    }



    //initiate toolbar
    private  fun setToolbar() {
        //run only when current fragment is this class.
        (activity as AppCompatActivity)?.run {
            setSupportActionBar(view_toolbarAddSlip)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            setHasOptionsMenu(false)
        }

        view_btnNavi.setOnClickListener(activity as MainActivity)
        view_btnSubmit.setOnClickListener(this)
       // activity.suppo(toolbar);
       // inflater.inflate(R.menu.meeting_tab_menu,menu);
//        var curFrag = activity?.view_pagerMain?.run {
//            (adapter as MainViewPageAdapter).getItem(this.currentItem)
//        }
//
//        if(curFrag?.javaClass == this@Frag_AddSlip.javaClass) {
//            setHasOptionsMenu(true)
//            activity?.view_pagerMain?.currentItem
//            (activity as AppCompatActivity).supportActionBar?.let {
//                it.show()
//                it.title = getString(R.string.main_tab_add_slip)
//            }
//        }
    }

    fun verifyEntries():Boolean {
        var strMsg:String? = null//VERIFY_SUCCESS
        var bRes = true
        if(m_objUploadInfo.get("SDOC_NAME") == null || m_C.isBlank(m_objUploadInfo.get("SDOC_NAME").asString))
        {
            bRes = false
            strMsg = getString(R.string.verify_enter_sdocname)
            view_layoutUICell.findViewWithTag<ConstraintLayout>("CELL_SDOC_NAME").isPressed = true
        }
        else if(m_objUploadInfo.get("SDOC_TYPE") == null || m_objUploadInfo.get("SDOC_TYPE").asJsonObject.size() <= 0)
        {
            bRes = false
            strMsg = getString(R.string.verify_select_sdoctype)
            view_layoutUICell.findViewWithTag<ConstraintLayout>("SDOC_TYPE").isPressed = true
        }
        else if(
                (m_objUploadInfo.get("UPLOAD_IMAGE") == null || m_objUploadInfo.get("UPLOAD_IMAGE").asJsonArray.size() <= 0)
                &&
                (m_objUploadInfo.get("UPLOAD_FILE") == null || m_objUploadInfo.get("UPLOAD_FILE").asJsonArray.size() <= 0)
        )
        {
            bRes = false
            strMsg = getString(R.string.verify_select_slpiitem)

        }


        if(!m_C.isBlank(strMsg)) {
            AlertDialog.Builder(activity as Context).apply {
                setTitle(getString(R.string.error))
                setMessage(strMsg)

                setPositiveButton("OK", null)

            }.show()
        }
        return bRes
    }

    override fun viewOriginal(objItem: JsonObject, nIdx:Int) {

        Intent(activity, OriginalViewAcitivty::class.java).run {

            var obj = JsonObject()
            obj.addProperty("CUR_IDX", nIdx)
            obj.addProperty("CATEGORY", "ADD")
            obj.addProperty("SDOC_NAME", getString(R.string.slip))
            obj.add("SLIP_LIST", getOriginalViewList())
            putExtra("SLIP_DATA", obj.toString())
            startActivityForResult(this, RESULT_VIEW_ORIGINAL)
        }
    }

    //Reconstitute slip original item
    fun getOriginalViewList():JsonArray {
        var objOriginalList = JsonArray()
        m_objUploadInfo?.get("UPLOAD_IMAGE")?.asJsonArray?.let {
            for(i in 0 until it.size())
            {
                var objSlipItem = it.get(i).asJsonObject.deepCopy()
                objSlipItem.addProperty("DOC_NO",1)
                objSlipItem.addProperty("FILE_NAME","${objSlipItem.get("DOC_IRN").asString}.JPG")
                objSlipItem.addProperty("THUMB_PATH","${objSlipItem.get("ThumbPath")}")
                objOriginalList.add(objSlipItem)
            }
        }
        return objOriginalList
    }

    override fun onClick(v: View?)
    {
        when(v?.tag.toString().toUpperCase()) {


            "SUBMIT" -> {
              //  Text
                AlertDialog.Builder(activity as Context).apply {
                    setTitle(getString(R.string.btn_confirm))
                    setMessage(getString(R.string.confirm_submit_slip))

                    setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        if(verifyEntries())
                        {
                            NewDocument(this@Frag_AddSlip, m_objUploadInfo).execute()
                        }
//                        else
//                        {
//                            Logger.WriteLog(this::javaClass.name, object{}.javaClass.enclosingMethod.name, "Failed to uplaod slip - " + )
//                        }
                    })
                    setNegativeButton("Cancel", null)

                }.show()

            }
            "CELL_SDOC_NAME" -> {
                v?.findViewWithTag<EditText>("SDOC_NAME")?.run {
                    requestFocus()
                    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
                }
            }
            "SDOC_TYPE" -> {
                Intent(activity, SDocTypeActivity::class.java).run {
                    
                    putExtra("SelectedItem", m_objUploadInfo.get("SDOC_TYPE")?.toString())
                    startActivityForResult(this, RESULT_SDOC_TYPE)
                }
            }
            "ADD_UPLOADITEM" -> {

                var arObjBtns = JsonObject()

                if(nCurUploadSegmentId == SEGMENT_BTN_IMAGE) {
                    arObjBtns.apply {
                        addProperty("CAMERA", getString(R.string.dialog_uploaditem_camera))
                        addProperty("GALLERY", getString(R.string.dialog_uploaditem_gallery))
//                        addProperty("FILE", getString(R.string.dialog_uploaditem_imagefile))
//                        addProperty("CLOUD", getString(R.string.dialog_uploaditem_cloud))
//                        addProperty("SEARCH", getString(R.string.dialog_uploaditem_searchadd))
                    }
                }

                else if(nCurUploadSegmentId == SEGMENT_BTN_FILE) {
                    arObjBtns.apply {
                        addProperty("FILE", getString(R.string.dialog_uploaditem_file))
//                       addProperty("CLOUD", getString(R.string.dialog_uploaditem_cloud))
//                        addProperty("SEARCH", getString(R.string.dialog_uploaditem_searchadd))
                    }
                }

                OverlayButtonDialog(activity as Context).apply {
                    setButton(getString(R.string.dialog_uploaditem_title),arObjBtns)
                    setOnClickButtonListener(this@Frag_AddSlip)
                }.show()
            }
            "REMOVE_UPLOADITEM" -> {
                view_cardUpload?.apply {
                    var itemAdapter = adapter as AddThumbAdapter


//                    var strCurCategory = "UPLOAD_" + itemAdapter.getCurrentCategory()
//
//                    m_objUploadInfo.get(strCurCategory)?.apply {
//
//                        var arObjItem = asJsonArray
//
//                        for (i in 0 until arObjItem.size()) {
//
//                            if(arObjItem.get(i).asJsonObject.get("CHECKED") != null)
//                            {
//                                itemAdapter.removeItem(i)
//                                itemAdapter.notifyItemRemoved(i)
//                            }
//                        }
//                    }
                    itemAdapter.removeSelectedItems()


//                    var strCurCategory = "UPLOAD_" + itemAdapter.getCurrentCategory()

//                    m_objUploadInfo.get(strCurCategory)?.apply {

//                        var arObjItem = asJsonArray

//                        for(i in 0 until arObjItem.size())
//                        {
//                            if(arObjItem.get(i).asJsonObject.get("CHECKED") != null)
//                            {
//                                itemAdapter.removeItem(i)
//                            }
//
//                        }
                    //}
                }
            }
        }
    }

    fun moveToEditView(objItem:JsonObject, builder:AlertDialog?)
    {
        Intent(activity, EditSlipActivity::class.java).run {

            var obj = JsonObject()
            obj.addProperty("CATEGORY", "NEW")
            obj.addProperty("SDOC_NAME", getString(R.string.slip))
            obj.add("SLIP_DATA", objItem.asJsonObject)
            putExtra("SLIP_ITEM", obj.toString())
            startActivityForResult(this, RESULT_ADD_SLIP)
           // builder?.dismiss()
            tempDialogBuilder= builder
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        tempDialogBuilder?.dismiss()
        tempDialogBuilder = null

        when(requestCode) {

            RESULT_ADD_SLIP -> {
                data?.extras?.get("SLIP_ITEM")?.apply {
                    var objSlip = JsonParser().parse(this as String)?.asJsonObject
                    if (resultCode == RESULT_OK) {
                        if (resultCode == RESULT_OK) {

                             objSlip?.apply {
                                view_cardUpload?.apply {
                                    var itemAdapter = adapter as AddThumbAdapter
                                    itemAdapter.addItem(objSlip)
                                }
                            }
                        }

                    } else {

                        m_C.removeFile(activity as Context, objSlip?.get("OriginalPath")?.asString!!)
                    }
                }
            }

            RESULT_VIEW_ORIGINAL ->{
                if(resultCode == RESULT_OK) {
                    data?.extras?.get("LIST_DATA")?.apply {
                        var listImage = JsonParser().parse(this as String)?.asJsonArray
                        m_objUploadInfo.add("UPLOAD_IMAGE", listImage)

                        view_cardUpload?.apply {

                            var strCategory:String = "IMAGE"
                            when(nCurUploadSegmentId)
                            {
                                SEGMENT_BTN_IMAGE -> {
                                    strCategory = "IMAGE"
                                }
                                SEGMENT_BTN_FILE -> {
                                    strCategory = "FILE"
                                }
                            }

                            var itemAdapter = adapter as AddThumbAdapter
                            itemAdapter.setItem(m_objUploadInfo.getAsJsonArray("UPLOAD_IMAGE"), strCategory)
                        }

                    }
                }
            }
            //Result of SDocType Select
            RESULT_SDOC_TYPE -> {
                if (resultCode == RESULT_OK) {

                    var listSelected:JsonArray? = null
                    data?.extras?.get("listSelected")?.apply {
                        listSelected = JsonParser().parse(this as String)?.asJsonArray

                        var strName:String? = null
                        var strCode:String? = null

                        listSelected?.get(0)?.apply {
                            val objItem = this as JsonObject
                            strName = objItem.get("name").asString
                            strCode = objItem.get("code").asString
                            m_objUploadInfo.add("SDOC_TYPE",objItem)
                        }

                        view_layoutUICell?.findViewWithTag<ConstraintLayout>("SDOC_TYPE")?.let {
                            it.findViewById<TextView>(R.id.view_textCellContents)?.apply {
                                text = StringBuffer().apply {
                                    append(strName)
                                    append(" (")
                                    append(strCode)
                                    append(")")
                                }
                            }
                        }
                    }
                }
            }
            RESULT_ADD_FILE -> {
                if(resultCode == RESULT_OK)
                {
                    data?.extras?.get("FILE_ITEM")?.apply {
                        var strOldFilePath = this as String
                        var strFileName = strOldFilePath.substring(strOldFilePath.lastIndexOf('/')+1, strOldFilePath.length)
                        var strNewPath = UPLOAD_PATH + File.separator + strFileName

                        var path =  File(UPLOAD_PATH)
                        if(!path.exists())
                        {
                            path.mkdirs()
                        }
                        m_C.copyFile(strOldFilePath, strNewPath)

                        var strDocIRN =  WDMAgent().getDocIRN()//WDMAgent().getDocIRN(g_UserInfo.strUserID, m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)

                        var objRes = JsonObject()
                        objRes.addProperty("FILE_NAME", strFileName)
                        objRes.addProperty("FILE_SIZE", m_C.getFileSize(strNewPath,"KB"))
                        objRes.addProperty("FILE_PATH", strNewPath)
                        objRes.addProperty("DOC_IRN", strDocIRN)


                        view_cardUpload?.apply {
                            var itemAdapter = adapter as AddThumbAdapter
                            itemAdapter.addItem(objRes)
                        }
                    }
                }
            }
            //Result of image of camera
            RESULT_CAMERA -> {
               // if (resultCode == RESULT_OK) {
                    //var bitmap = data?.extras?.get("data") as Bitmap
               var imageFile = File(m_fileCapturedFilePath)

                if(imageFile.exists())
                {
                    //Remove temp file when nothing taken
                    if(imageFile.length() <= 0)
                    {
                        m_C.removeFile(activity as Context, m_fileCapturedFilePath!!)
                        return
                    }


                    ProcessOriginalImage(this@Frag_AddSlip).execute(m_fileCapturedFilePath)

//                    var runnable = Runnable {
//                        var objOriginalInfo = m_C.saveOriginal(activity!!, strOriginalName, renamedFile)
//                        var objRes = JsonObject()
//                        objRes.addProperty("OriginalPath", objOriginalInfo.get("Path").asString)
//                        objRes.addProperty("ThumbPath", objOriginalInfo.get("Path").asString)
//                        objRes.addProperty("Width", objOriginalInfo.get("Width").asInt)
//                        objRes.addProperty("Height", objOriginalInfo.get("Height").asInt)
//                        objRes.addProperty("ImageSize", objOriginalInfo.get("FileSize").asInt)
//                        objRes.addProperty("DOC_IRN", strDocIRN)
//
//                        moveToEditView(objRes)
//                    }
//                    Handler().postDelayed(runnable,10)


                  //  var objOriginalInfo = m_C.saveOriginal(activity!!, strOriginalName, renamedFile)


//                    var strThumbPath = m_C.saveThumb(strThumbName, objOriginalInfo)


//                    view_cardUpload?.apply {
//                        var itemAdapter = adapter as AddThumbAdapter
//                        itemAdapter.addItem(objRes)
//                    }
                } else {
                    AlertDialog.Builder(activity as Context).apply {
                        setTitle(getString(R.string.error))
                        setMessage(getString(R.string.error_failed_load_image_from_gallery))

                        setPositiveButton("OK", null)

                    }.show()
                }
            }
               /* else
                {
                    AlertDialog.Builder(activity as Context).apply {
                        setTitle(getString(R.string.error))
                        setMessage(getString(R.string.error_failed_load_image_from_gallery))

                        setPositiveButton("OK", null)

                    }.show()
                }*/

// Get image from camera
//                    var objImage:JsonObject? = null
//                    data?.extras?.get("Image")?.apply {
//
//                        objImage = JsonParser().parse(this as String)?.asJsonObject
//
//                        objImage?.let {
//
//                            view_cardUpload?.apply {
//                                var itemAdapter = adapter as AddThumbAdapter
//                                itemAdapter.addItem(it)
//                            }
//                        }
//                    }

            //}
            //Get images from
            RESULT_GALLERY -> {
                if (resultCode == RESULT_OK) {
                    var strPath = m_C.getRealPathFromUri(data?.data, activity as Context)
                    if (!m_C.isBlank(strPath)) {
                        //    var fImgDegree = m_C.getDeviceRotateAngle(strPath, CameraView.FACING_BACK)
                      //  var bImage = m_C.getBytesFromFile(File(strPath))
                       // if (bImage.isNotEmpty()) {

                        if(File(strPath).length() > 0) {
                            var strDocIRN =  WDMAgent().getDocIRN()//WDMAgent().getDocIRN(g_UserInfo.strUserID, m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)
                            var strOriginalName = strDocIRN + ".JPG"
                            var strThumbName = strDocIRN + ".JPG"

                            var path = File(UPLOAD_PATH)
                            if(!path.exists())
                            {
                                path.mkdirs()
                            }
                            m_C.copyFile(strPath!!, UPLOAD_PATH +File.separator+ strOriginalName)

                            ProcessOriginalImage(this@Frag_AddSlip).execute(UPLOAD_PATH +File.separator+ strOriginalName)


//                            var objOriginalInfo = m_C.saveOriginal(activity!!, strOriginalName, File(UPLOAD_PATH, strOriginalName))
//                      //      var strThumbPath = m_C.saveThumb(strThumbName, objOriginalInfo)
//
//                            var objRes = JsonObject()
//                            objRes.addProperty("OriginalPath", objOriginalInfo.get("Path").asString)
//                            objRes.addProperty("ThumbPath", objOriginalInfo.get("Path").asString)
//                            objRes.addProperty("Width", objOriginalInfo.get("Width").asInt)
//                            objRes.addProperty("Height", objOriginalInfo.get("Height").asInt)
//                            objRes.addProperty("ImageSize", objOriginalInfo.get("FileSize").asInt)
//                            objRes.addProperty("DOC_IRN", strDocIRN)
//
//                            moveToEditView(objRes)

//                            view_cardUpload?.apply {
//                                var itemAdapter = adapter as AddThumbAdapter
//                                itemAdapter.addItem(objRes)
//                            }
                        }
                        else
                        {
                            AlertDialog.Builder(activity as Context).apply {
                                setTitle(getString(R.string.error))
                                setMessage(getString(R.string.error_failed_load_image_from_gallery))

                                setPositiveButton("OK", null)

                            }.show()
                        }
                    } else {
                        AlertDialog.Builder(activity as Context).apply {
                            setTitle(getString(R.string.error))
                            setMessage(getString(R.string.error_failed_load_image_from_gallery))

                            setPositiveButton("OK", null)

                        }.show()
                    }
                }
            }
         }
    }

    //Switch button
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.tag.toString().toUpperCase()) {
            "SHOW_REMOVE" -> {
                if(isChecked) {
                    view_cardUpload?.run {
                        (adapter as AddThumbAdapter).setMode(AddThumbAdapter.MODE_SELECT)
                    }
                    view_layoutUICell?.findViewWithTag<ImageButton>("REMOVE_UPLOADITEM")?.apply { visibility = View.VISIBLE }
                }
                else
                {
                    view_cardUpload?.run {
                        (adapter as AddThumbAdapter).setMode(AddThumbAdapter.MODE_VIEW)
                    }
                    view_layoutUICell?.findViewWithTag<ImageButton>("REMOVE_UPLOADITEM")?.apply { visibility = View.GONE }
                }
            }
            "SDOC_ONE" -> {
                if(isChecked) {
                    m_objUploadInfo.addProperty("SDOC_ONE",true)
                }
                else
                {
                    m_objUploadInfo.addProperty("SDOC_ONE",false)
                }
            }
            "SDOC_AFTER" -> {
                if(isChecked) {
                    m_objUploadInfo.addProperty("SDOC_AFTER",true)
                }
                else
                {
                    m_objUploadInfo.addProperty("SDOC_AFTER",false)
                }
            }
        }
    }

    //Radio button
    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        when (group?.tag.toString().toUpperCase()) {
           "TASK" -> {
                nCurSegmentId = checkedId
                var strSelectedSection: String? = m_C.getNodeNameAtIndex(activity as Activity , g_SysInfo.strDisplayLang + "/view/VIEW_ADDSLIP.xml", checkedId)
                if (!strSelectedSection.isNullOrBlank()) {
                    m_C.loadUIXML(activity as Activity, view_layoutUICell, this as Frag_AddSlip, g_SysInfo.strDisplayLang + "/view/VIEW_ADDSLIP.xml", strSelectedSection)
                    setupViewUI()
                }
            }
            "SLIP" -> {

                nCurUploadSegmentId = checkedId
                var view_radioBtn = group?.findViewById<RadioButton>(nCurUploadSegmentId)


                var strCategory = "IMAGE"
                var nAdapterCategory = AddThumbAdapter.CATEGORY_SLIP

                when(view_radioBtn!!.id)
                {
                    SEGMENT_BTN_IMAGE -> {
                        strCategory = "IMAGE"
                        nAdapterCategory = AddThumbAdapter.CATEGORY_SLIP
                    }
                    SEGMENT_BTN_FILE -> {
                        strCategory = "FILE"
                        nAdapterCategory = AddThumbAdapter.CATEGORY_FILE
                    }
                }

                view_cardUpload?.apply{

                    var arObjItem = m_objUploadInfo.get("UPLOAD_"+strCategory).asJsonArray

                    (adapter as AddThumbAdapter).setCategory(nAdapterCategory)
                   // var existItem = if(m_objUploadInfo.get("UPLOAD_"+strCategory) == null) null else m_objUploadInfo.get("UPLOAD_"+strCategory).asJsonArray
                    (adapter as AddThumbAdapter).setItem(arObjItem, strCategory)

                    view_layoutUICell?.run {
                        findViewWithTag<SwitchCompat>("SHOW_REMOVE")?.run {
                            isChecked = false
                        }
                    }

                }
            }
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            var imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v?.windowToken, 0)
            v?.clearFocus()
        }
        return false
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if(v is EditText) {
            var imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v?.windowToken, 0)
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        m_objUploadInfo.addProperty("SDOC_NAME", s.toString())
    }

    override fun afterTextChanged(s: Editable?) {
        //
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        //
    }

    override fun changeSlipMode(mode:Int) {

        when(mode)
        {
            AddThumbAdapter.MODE_SELECT -> {
                view_layoutUICell?.run {
                    findViewWithTag<SwitchCompat>("SHOW_REMOVE")?.run {
                        isChecked = true
                    }
                }
            }
            AddThumbAdapter.MODE_VIEW -> {
                view_layoutUICell?.run {
                    findViewWithTag<SwitchCompat>("SHOW_REMOVE")?.run {
                        isChecked = false
                    }
                }
            }
        }
    }


    override fun onClickButton(dialog:Dialog, strTag:String) {

        dialog.dismiss()

        when(strTag.toUpperCase())
        {
            "CAMERA" -> {
             //   dialog.dismiss()
                context?.let {
                    it.packageManager?.let {
                        if (it.hasSystemFeature(PackageManager.FEATURE_CAMERA))
                        {

//                            var path =  File(TEMP_PATH)
//                            if(!path.exists())
//                            {
//                                path.mkdirs()
//                            }
//                            var imageFile = File.createTempFile(
//                                    System.currentTimeMillis().toString(),
//                            ".JPG",
//                                    path
//                            )
                            var path =  File(UPLOAD_PATH)
                            if(!path.exists())
                            {
                                path.mkdirs()
                            }
                            var imageFile = File.createTempFile(
                                    "tmp",
                                    ".JPG",
                                    path
                            )

                            m_fileCapturedFilePath = imageFile.absolutePath

                            var fileCapturedImagePath =
                                    FileProvider.getUriForFile(context!!,
                                            BuildConfig.APPLICATION_ID + ".Util.GenericFileProvider",
                                            imageFile
                                    )

                            Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).run {
//                                data =
//                                type = "camera/*"
                                 //   flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                                putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

                                CALL_ANOTHER_APPLICATION = true /*In security reason, set true for another app called from own activity*/
                                    putExtra(MediaStore.EXTRA_OUTPUT, fileCapturedImagePath)

                                var runnable = Runnable {
                                    startActivityForResult(this, RESULT_CAMERA)
                                }
                                Handler().postDelayed(runnable,500)
                            }
                        }
                        else
                        {
                        AlertDialog.Builder(activity as Context).apply {
                                setTitle(getString(R.string.error))
                                setMessage(getString(R.string.error_no_camera_feature))

                                setPositiveButton("OK", null)

                            }.show()
                        }
                    }
                }

//                context?.let {
//                    it.packageManager?.let {
//                        if(it.hasSystemFeature(PackageManager.FEATURE_CAMERA))
//                        {
//                            Intent(activity, CameraActivity::class.java).run {
//                                startActivityForResult(this, RESULT_CAMERA)
//                            }
//                        }
//                        else
//                        {
//                        AlertDialog.Builder(activity as Context).apply {
//                                setTitle(getString(R.string.error))
//                                setMessage(getString(R.string.error_no_camera_feature))
//
//                                setPositiveButton("OK", null)
//
//                            }.show()
//                        }
//                    }
//                }
            }
            "GALLERY"->{
              //  dialog.dismiss()
                Intent(Intent.ACTION_PICK).run{
                    data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    type = "image/*"

                    CALL_ANOTHER_APPLICATION = true /*In security reason, set true for another app called from own activity*/
                    startActivityForResult(this, RESULT_GALLERY)
                }
            }
            "FILE"->{
             //   dialog.dismiss()

                Intent(activity, FileExplorerActivity::class.java).run {

                     startActivityForResult(this, RESULT_ADD_FILE)
                }

            }

            else ->{
                dialog.dismiss()
            }
//                    addProperty("CAMERA", getString(R.string.dialog_uploaditem_camera))
//                addProperty("LIBRARY", getString(R.string.dialog_uploaditem_gallery))
//            addProperty("FILE", getString(R.string.dialog_uploaditem_imagefile))
//                    addProperty("CLOUD", getString(R.string.dialog_uploaditem_cloud))
//                addProperty("SEARCH", getString(R.string.dialog_uploaditem_searchadd))
        }
    }

    fun reset() {

        m_C.removeFolder(activity as Activity, UPLOAD_PATH)
        m_objUploadInfo = JsonObject()
        nCurUploadSegmentId = SEGMENT_BTN_IMAGE

        m_C.removeFolder(this@Frag_AddSlip.context!!, TEMP_PATH)

        var strColumnName = m_C.getNodeNameAtIndex(activity as Activity, g_SysInfo.strDisplayLang+"/view/VIEW_ADDSLIP.xml", nCurSegmentId)
        m_C.loadUIXML(activity as Activity, view_layoutUICell, this as Frag_AddSlip,  g_SysInfo.strDisplayLang+"/view/VIEW_ADDSLIP.xml", strColumnName)
        setupViewUI()
    }

    //ASyncTask for upload slip.
    private class NewDocument(var fragment: Frag_AddSlip, var objDocumentInfo:JsonObject) :AsyncTask<Void, Int, Int>() {

        var activityRef: WeakReference<Activity>? = null
        var builder: AlertDialog? = null
        var m_C = Common()
        var nCurrentStatus = SDocTypeActivity.GET_SDOCTYPE_FAILED

        init {
            activityRef = WeakReference(fragment.activity as Activity)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            var activity = activityRef?.get()
            builder = AlertDialog.Builder(activity as Context).apply {
                var view = LayoutInflater.from(activity).inflate(R.layout.progress_line, null)
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

        override fun doInBackground(vararg params: Void?): Int {

            if(!isCancelled) {

                //Calculate upload process step
                var nProcessStep = 4
                var isSDocOne = if(objDocumentInfo.get("SDOC_ONE") == null) false else objDocumentInfo.get("SDOC_ONE").asBoolean
                var arObjImages = objDocumentInfo.get("UPLOAD_IMAGE").asJsonArray ?: null
                var arObjFiles = objDocumentInfo.get("UPLOAD_FILE").asJsonArray ?: null


                if(arObjFiles == null || arObjFiles.asJsonArray.size() <= 0)
                {
                    nProcessStep = 3
                }

                var activity = activityRef?.get()
                var uploadProc = UploadDocument()

                if(arObjImages?.size()!! > 0)
                {
                    updateProgress(0, activity?.getString(R.string.progress_upload_original), nProcessStep)

                    arObjImages = uploadProc.initItem(activityRef?.get(), arObjImages!!)
                    var strSDocNo:String? = null

                    //Upload original image
                    var objRes = uploadProc.uploadSlip(arObjImages!!, isSDocOne)
                    if(objRes.get("RESULT").asInt != UploadDocument.UPLOAD_SLIP_SUCCESS)
                    {
                        return objRes.get("RESULT").asInt
                    }
                    //Archive SDOC_NO from Slip upload
                    strSDocNo = objRes.get("SDOC_NO").asString

                    updateProgress(1, activity?.getString(R.string.progress_upload_original), nProcessStep)

                    //Upload thumb image
                    objRes = uploadProc.uploadThumb(arObjImages!!, isSDocOne)
                    if(objRes.get("RESULT").asInt != UploadDocument.UPLOAD_SLIP_SUCCESS)
                    {
                        return objRes.get("RESULT").asInt
                    }
                    updateProgress(2, activity?.getString(R.string.progress_upload_thumb), nProcessStep)

                    //Upload Slipdoc
                    objRes = uploadProc.uploadSlipDoc(objDocumentInfo, arObjImages!!, isSDocOne, strSDocNo)
                    if(objRes.get("RESULT").asInt != UploadDocument.UPLOAD_SLIP_SUCCESS)
                    {
                        return objRes.get("RESULT").asInt
                    }
                    updateProgress(3, activity?.getString(R.string.progress_upload_slipdoc), nProcessStep)
                }

                if(arObjFiles?.size()!! > 0)
                {
                //Upload AddFile
                    var objRes = uploadProc.uploadAddfile(arObjFiles, objDocumentInfo)
                    if (objRes.get("RESULT").asInt != UploadDocument.UPLOAD_ADDFILE_SUCCESS) {
                        return objRes.get("RESULT").asInt
                    }
                    updateProgress(4, activity?.getString(R.string.progress_upload_addfile), nProcessStep)
                }
            }
            return UploadDocument.UPLOAD_SLIP_SUCCESS
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)

            var nProgress = values[0]
            builder?.let {
                it.findViewById<ProgressBar>(R.id.view_progress)?.progress = nProgress!!
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            builder?.dismiss()
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)

            builder?.dismiss()

            var activity = activityRef?.get()
            var strMsg = activity?.getString(R.string.upload_success_slip)

            when(result)
            {
                UploadDocument.UPLOAD_SLIP_FAILED_UPLOAD_IMAGE ->{
                    activity?.getString(R.string.upload_failed_upload_slip)?.let {
                        strMsg = it
                    }
                }
                UploadDocument.UPLOAD_SLIP_FAILED_INSERT_SLIP ->{
                    activity?.getString(R.string.upload_failed_upload_slip)?.let {
                        strMsg = it
                    }
                }
                UploadDocument.UPLOAD_SLIP_FAILED_INSERT_THUMB ->{
                    activity?.getString(R.string.upload_failed_upload_thumb)?.let {
                        strMsg = it
                    }
                }
                UploadDocument.UPLOAD_SLIP_FAILED_CREATE_KEY ->{
                    activity?.getString(R.string.upload_failed_upload_slipdoc)?.let {
                        strMsg = it
                    }
                }
                UploadDocument.UPLOAD_SLIP_FAILED_CREATE_XML ->{
                    activity?.getString(R.string.upload_failed_upload_slipdoc)?.let {
                        strMsg = it
                    }
                }
                UploadDocument.UPLOAD_SLIP_FAILED_INSERT_XML ->{
                    activity?.getString(R.string.upload_failed_upload_slipdoc)?.let {
                        strMsg = it
                    }
                }
                UploadDocument.UPLOAD_ADDFILE_FAILED ->{
                    activity?.getString(R.string.upload_failed_upload_addfile)?.let {
                        strMsg = it
                    }
                }
                UploadDocument.UPLOAD_ADDFILE_FAILED_CREATE_KEY ->{
                    activity?.getString(R.string.upload_failed_upload_addfile)?.let {
                        strMsg = it
                    }
                }
            }

            AlertDialog.Builder(activity as Context).apply {
                //setTitle(strMsg)
                setMessage(strMsg)

                setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->

                    if(result == UploadDocument.UPLOAD_SLIP_SUCCESS) {
                        fragment.reset()
                    }
                })

            }.show()
        }

        fun updateProgress(nStatus:Int, strProgressTitle:String?, nMax:Int)
        {
            publishProgress(nStatus)
            var activity = activityRef?.get()
            builder?.let {
                activity?.runOnUiThread {
                    kotlin.run {
                        it.findViewById<TextView>(R.id.view_textProgressTitle)?.text = strProgressTitle
                    //    it.findViewById<TextView>(R.id.view_textProgressContents)?.text = activity?.getString(R.string.send_progress_contents, nStatus, nMax)
                    }
                }

                it.findViewById<ProgressBar>(R.id.view_progress)?.max = nMax
            }
        }
    }

    //ProcessOriginal Bitmap. detect orientation and get Cropping area etc..
    private class ProcessOriginalImage(var fragment: Frag_AddSlip):AsyncTask<String,Void,JsonObject?>() {

        var activityRef: WeakReference<Activity>? = null
        public var builder: AlertDialog? = null
        var m_C = Common()
        var strDocIRN = WDMAgent().getDocIRN()//WDMAgent().getDocIRN(g_UserInfo.strUserID, m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)


        init {
            activityRef = WeakReference(fragment.activity as Activity)
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
                        //   activity.onBackPressed()
                    }

                    dialog.dismiss()
                }
            }.create()
            builder?.show()
        }

        override fun doInBackground(vararg params: String?): JsonObject? {

            var objRes:JsonObject? = null
            if(!isCancelled) {
                var strCapturedImagePath = params[0].toString()
                var strOriginalName = strDocIRN + ".JPG"
                //var strThumbName = strDocIRN + ".JPG"

                var oldFile = File(strCapturedImagePath)
                var renamedFile = File(UPLOAD_PATH, strOriginalName)
                oldFile.renameTo(renamedFile)
//
//
//            Thread().run {
                var activity = activityRef?.get()
                objRes = m_C.saveOriginal(activity!!, strOriginalName, renamedFile)

//
//                moveToEditView(objRes)
//            }
            }
            return objRes
        }

        override fun onPostExecute(result: JsonObject?) {
            super.onPostExecute(result)

            //builder?.dismiss()
            var objOriginalInfo = result

            objOriginalInfo?.run {

                var objRes = JsonObject()
                objRes.addProperty("OriginalPath", objOriginalInfo.get("Path").asString)
                objRes.addProperty("ThumbPath", objOriginalInfo.get("Path").asString)
                objRes.addProperty("Width", objOriginalInfo.get("Width").asInt)
                objRes.addProperty("Height", objOriginalInfo.get("Height").asInt)
                objRes.addProperty("ImageSize", objOriginalInfo.get("FileSize").asInt)
                objRes.addProperty("DOC_IRN", strDocIRN)

                fragment.moveToEditView(objRes,builder)
            }

        }

    }
}