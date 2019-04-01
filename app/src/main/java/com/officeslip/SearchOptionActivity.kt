package com.officeslip

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.officeslip.Util.Common
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.activity_search_slip.*
import java.text.SimpleDateFormat
import java.util.*
import android.support.constraint.ConstraintSet
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import com.sgenc.officeslip.R


class SearchOptionActivity : View.OnClickListener, AppCompatActivity(), CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, TextView.OnEditorActionListener, View.OnFocusChangeListener, TextWatcher {


    private val m_C: Common = Common()
    private var m_objSearchOptions = JsonObject()
    private var nCurCategoryStepId = SEGMENT_STEP_ALL
    private var nCurCategoryTaskId = SEGMENT_TASK_COMMON
    private var m_startDate:Calendar? = null
    private var m_endDate:Calendar? = null

    companion object {
        //To do : Need to be initalized with specific code
        const val SEGMENT_STEP_ALL = 0
        const val SEGMENT_STEP_UNUSE = 1
        const val SEGMENT_STEP_USE = 2
        const val SEGMENT_TASK_COMMON = 0
        const val SEGMENT_TASK_CO = 1
        const val SEGMENT_TASK_HR = 2
        const val SEGMENT_TASK_SAP = 3
        const val SEGMENT_TASK_ADDFILE = 4
        const val SEGMENT_TASK_STATS = 5
        const val RESULT_SDOC_TYPE = 5010

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_slip)

        intent.getStringExtra("SEARCH_UI_SECTION")?.apply {
            when(this.toUpperCase()) {
                "SLIP" -> {
                    nCurCategoryTaskId = 0
                }
                "ADDFILE" -> {
                    nCurCategoryTaskId = 2
                }
                "STATS" -> {
                    nCurCategoryTaskId = 1
                }
            }
        }

        setToolbar()
        var strColumnName = m_C.getNodeNameAtIndex(this@SearchOptionActivity, g_SysInfo.strDisplayLang+"/view/VIEW_SEARCH.xml", nCurCategoryTaskId)
        m_C.loadUIXML(this@SearchOptionActivity, view_layoutUICell, this@SearchOptionActivity,  g_SysInfo.strDisplayLang+"/view/VIEW_SEARCH.xml", strColumnName)
        setupViewUI()
    }

    fun setToolbar() {
        (this as AppCompatActivity)?.run {
            setSupportActionBar(view_toolbarSearchSlip)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        view_btnClose.setOnClickListener(this)
        view_btnCheck.setOnClickListener(this)
    }

    private fun setupViewUI() {
        view_layoutUICell?.run {
            this.findViewWithTag<SegmentedGroup>("USED")?.apply{
                setOnCheckedChangeListener(null)
                (getChildAt(nCurCategoryStepId) as RadioButton)?.isChecked = true
                m_objSearchOptions.addProperty("USED_CATEGORY", nCurCategoryStepId)
                setOnCheckedChangeListener(this@SearchOptionActivity)
            }

            this.findViewWithTag<SegmentedGroup>("WORK")?.apply{
                setOnCheckedChangeListener(null)
                (getChildAt(nCurCategoryTaskId) as RadioButton)?.isChecked = true
                m_objSearchOptions.addProperty("WORK_CATEGORY", nCurCategoryTaskId)
                setOnCheckedChangeListener(this@SearchOptionActivity)
            }

            this.findViewWithTag<EditText>("SDOC_NAME")?.let {
                m_objSearchOptions.get("SDOC_NAME")?.apply {
                    it.setText(this.asString, TextView.BufferType.EDITABLE)
                }
            }

            this.findViewWithTag<EditText>("ADD_FILE")?.let {
                m_objSearchOptions.get("ADD_FILE")?.apply {
                    it.setText(this.asString, TextView.BufferType.EDITABLE)
                }
            }

            this.findViewWithTag<ConstraintLayout>("SDOC_TYPE")?.apply{

                m_objSearchOptions.get("SDOC_TYPE")?.apply {
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

            this.findViewWithTag<ConstraintLayout>("SEARCH")?.apply{

                findViewById<TextView>(R.id.view_textCellTitle)?.let{
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(this)
                    constraintSet.connect(it.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
                    constraintSet.applyTo(this)
                    it.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    it.setTypeface(null, Typeface.BOLD)

                }

                findViewById<ImageView>(R.id.view_imageCellContents)?.let{
                    it.visibility = View.GONE
                }
            }

            this.findViewWithTag<ConstraintLayout>("CABINET_START")?.apply{

                var viewText = findViewById<TextView>(R.id.view_textCellContents)

                viewText.text?.apply {

                    var dateVal = this.toString()
                    var nStart = dateVal.substring(indexOf(',')+1, dateVal.length)?.let { it.toInt() }

                    val calendar = Calendar.getInstance() // Get Calendar Instance
                    calendar.time = Date()
                    calendar.add(Calendar.DATE, nStart)
                    var strDisplayDate = SimpleDateFormat(String.format(getString(R.string.date_format_yyyymmdd), "yyyy", "MM", "dd")).format(Date(calendar.timeInMillis))

                    viewText.text = strDisplayDate

                    m_startDate = calendar
                }
            }

            this.findViewWithTag<ConstraintLayout>("CABINET_END")?.apply{

                var viewText = findViewById<TextView>(R.id.view_textCellContents)

                viewText.text?.apply {

                    val calendar = Calendar.getInstance() // Get Calendar Instance
                    calendar.time = Date()
                    var strDisplayDate = SimpleDateFormat(String.format(getString(R.string.date_format_yyyymmdd), "yyyy", "MM", "dd")).format(Date(calendar.timeInMillis))

                    viewText.text = strDisplayDate

                    m_endDate = calendar
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

            this.findViewWithTag<ConstraintLayout>("CO_NO")?.apply{

                var viewText = findViewById<TextView>(R.id.view_textCellContents)
                viewText.text = StringBuffer().apply {
                    append(g_UserInfo.strCoName)
                    append("(")
                    append(g_UserInfo.strCoID)
                    append(")")
                }
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            //Result of SDocType Select
            RESULT_SDOC_TYPE -> {
                if (resultCode == RESULT_OK) {

                    var listSelected: JsonArray? = null
                    data?.extras?.get("listSelected")?.apply {
                        listSelected = JsonParser().parse(this as String)?.asJsonArray

                        var strName: String? = null
                        var strCode: String? = null

                        listSelected?.get(0)?.apply {
                            val objItem = this as JsonObject
                            strName = objItem.get("name").asString
                            strCode = objItem.get("code").asString
                            m_objSearchOptions.add("SDOC_TYPE", objItem)
                        }

                        view_layoutUICell?.findViewWithTag<ConstraintLayout>("SDOC_TYPE")?.let {
                            it.findViewById<TextView>(R.id.view_textCellContents)?.apply {
                                text = StringBuffer().apply {
                                    append(strName)
                                    if(strCode != "-1") {
                                        append(" (")
                                        append(strCode)
                                        append(")")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onClick(v: View?) {
        when(v?.tag.toString().toUpperCase()) {
            "SDOC_TYPE" -> {
                Intent(this@SearchOptionActivity, SDocTypeActivity::class.java).run {
                    putExtra("SelectedItem", m_objSearchOptions.get("SDOC_TYPE")?.toString())
                    putExtra("Mode", "SEARCH")
                    startActivityForResult(this, RESULT_SDOC_TYPE)
                }
            }

            "CABINET_START" ->{

                m_startDate?.run {

                    var datePickListener = DatePickerDialog.OnDateSetListener{
                        view, year, month, dayOfMonth ->

                        this.set(year,month,dayOfMonth)

                        view_layoutUICell?.findViewWithTag<ConstraintLayout>("CABINET_START")?.apply{

                            findViewById<TextView>(R.id.view_textCellContents)?.apply {
                                this.text = SimpleDateFormat(String.format(getString(R.string.date_format_yyyymmdd), "yyyy", "MM", "dd")).format(Date(m_startDate!!.timeInMillis))
                            }
                        }
                    }

                    DatePickerDialog(this@SearchOptionActivity, AlertDialog.THEME_HOLO_LIGHT,datePickListener,0,0,0)?.let {
                        it.updateDate(this.get(Calendar.YEAR), this.get(Calendar.MONTH), this.get(Calendar.DATE))
                        it.show()
                    }
                }
            }

            "CABINET_END" ->{
                m_endDate?.run {

                    var datePickListener = DatePickerDialog.OnDateSetListener{
                        view, year, month, dayOfMonth ->

                        this.set(year,month,dayOfMonth)

                        view_layoutUICell?.findViewWithTag<ConstraintLayout>("CABINET_END")?.apply{

                            findViewById<TextView>(R.id.view_textCellContents)?.apply {
                                this.text = SimpleDateFormat(String.format(getString(R.string.date_format_yyyymmdd), "yyyy", "MM", "dd")).format(Date(m_endDate!!.timeInMillis))
                            }
                        }
                    }

                    DatePickerDialog(this@SearchOptionActivity, AlertDialog.THEME_HOLO_LIGHT,datePickListener,0,0,0)?.let {
                        it.updateDate(this.get(Calendar.YEAR), this.get(Calendar.MONTH), this.get(Calendar.DATE))
                        it.show()
                    }
                }
            }
            "CLOSE" ->{
                finish()
            }

            "SEARCH"-> {
//                m_objSearchOptions.addProperty("SDOC_STEP", nCurCategoryStepId)
//                m_objSearchOptions.addProperty("TASK", nCurCategoryTaskId)

                var strStartDate    = SimpleDateFormat("yyyyMMdd").format(Date(m_startDate!!.timeInMillis))
                var strEndDate      = SimpleDateFormat("yyyyMMdd").format(Date(m_endDate!!.timeInMillis))

                m_objSearchOptions.addProperty("CABINET_START", strStartDate)
                m_objSearchOptions.addProperty("CABINET_END", strEndDate)

                intent.putExtra("SEARCH_OPTION",m_objSearchOptions.toString())
                this@SearchOptionActivity.run {
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.tag.toString().toUpperCase()) {

        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (group?.tag.toString().toUpperCase()) {
            "USED" -> {
                nCurCategoryStepId = checkedId
                m_objSearchOptions.addProperty("USED_CATEGORY", nCurCategoryStepId)
            }

            "WORK" -> {
                nCurCategoryTaskId = checkedId
                m_objSearchOptions.addProperty("WORK_CATEGORY", nCurCategoryTaskId)
                var strColumnName = m_C.getNodeNameAtIndex(this@SearchOptionActivity, g_SysInfo.strDisplayLang+"/view/VIEW_SEARCH.xml", nCurCategoryTaskId)
                m_C.loadUIXML(this@SearchOptionActivity, view_layoutUICell, this@SearchOptionActivity,  g_SysInfo.strDisplayLang+"/view/VIEW_SEARCH.xml", strColumnName)
                setupViewUI()
            }
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {

        if (actionId == EditorInfo.IME_ACTION_DONE) {
            var imm = this@SearchOptionActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v?.windowToken, 0)
            v?.clearFocus()
        }
        return false
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if(v is EditText) {
            var imm = this@SearchOptionActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v?.windowToken, 0)
        }
    }

    override fun afterTextChanged(s: Editable?) {

        view_layoutUICell?.run {

            if(s?.hashCode() == findViewWithTag<EditText>("SDOC_NAME")?.text?.hashCode())
            {
                m_objSearchOptions.addProperty("SDOC_NAME", s.toString())
            }
            else  if(s?.hashCode() == findViewWithTag<EditText>("ADD_FILE")?.text?.hashCode())
            {
                m_objSearchOptions.addProperty("ADD_FILE", s.toString())
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
}