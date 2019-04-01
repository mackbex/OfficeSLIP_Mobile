package com.officeslip.View.EditSlip

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.JsonObject
import com.sgenc.officeslip.*
import com.officeslip.Util.Common
import com.officeslip.Util.SQLite
import kotlinx.android.synthetic.main.activity_draw_setting.*
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.InputStreamReader
import javax.xml.parsers.DocumentBuilderFactory
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.officeslip.*


class DrawSettingActivity:AppCompatActivity(), View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener,TextWatcher, View.OnFocusChangeListener {

    private var m_C = Common()
    private var m_strSettingType:String? = null
    private var m_strMode:String? = null

    private var listColors = mutableListOf<List<String>>()
    private var m_objProperty = JsonObject()


    init {
       listColors = mutableListOf(
               listOf("#fd8b83", "#e884f9", "#b38bfc", "#8da0fc", "#84d8fd", "#89fffe")
               ,listOf("#fc1f49", "#d326f6", "#6532fb", "#1eb1fc", "#1eb1fc", "#28e5fd")
               ,listOf("#d30915", "#a926fb", "#6221e6", "#1893e7", "#1893e7", "#1eb8d2")
               ,listOf("#bbf5cb", "#fffe94", "#fed085", "#fd9e83", "#bcaaa4", "#bdbdbd")
               ,listOf("#25e47a", "#fee834", "#fd9126", "#fc3f1d", "#785549", "#616161")
               ,listOf("#1ec659", "#fed530", "#fd6d21", "#db2e17", "#4d342f", "#212121")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_draw_setting)

        m_strMode = intent.getStringExtra("MODE")

        m_strSettingType = intent.getStringExtra("TYPE")

//        if(m_objProperty == null)
//        {
//            AlertDialog.Builder(this@DrawSettingActivity).apply {
//                //setTitle(strMsg)
//                setMessage(getString(R.string.failed_load_data))
//                setPositiveButton("OK") { dialog, which ->
//                    finish()
//                }
//            }.show()
//        }

        setToolbar()
        getCurDrawValues()
        loadXML(g_SysInfo.strDisplayLang+"/view/DRAW_SETTING.xml")

        setupViewUI()
    }

    //initiate toolbar
    private  fun setToolbar() {

        //run only when current fragment is this class.
        setSupportActionBar(view_toolbarDrawSetting)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        view_btnCheck.setOnClickListener(this@DrawSettingActivity)
        view_btnClose.setOnClickListener(this@DrawSettingActivity)

    }

    private fun getCurDrawValues() {

        if(m_strMode == "MODIFY")
        {
            intent.getStringExtra("PROPERTY")?.let {
                m_objProperty = JsonParser().parse(it) as JsonObject

            }
        }
        else
        {
            SQLite(this@DrawSettingActivity).instantGetRecord(SQLITE_CREATE_PROPERTY_T, SQLITE_GET_PROPERTY, arrayOf(m_strSettingType!!))?.forEach{
                var mapProperty = it as HashMap<String, String>

                val gson = GsonBuilder().create()
                val json = gson.toJson(mapProperty)

                json?.run {
                    m_objProperty = JsonParser().parse(json).asJsonObject
                }
            }
        }
    }

    private fun setupViewUI() {

        //setRemoveDataListener(this as OriginalViewAcitivty.RemoveUploadData)

        view_layoutUICell?.run {


            //PEN WEIGHT
            this.findViewWithTag<ConstraintLayout>("PEN_WEIGHT")?.apply {

                var weight = if(m_objProperty.get("WEIGHT") == null || m_objProperty.get("WEIGHT").isJsonNull) PEN_WEIGHT_DEFAULT else m_objProperty.get("WEIGHT").asFloat

                var viewText = findViewById<TextView>(R.id.view_textCellCur)
                viewText.text = StringBuffer().apply {
                    append(weight!!)
                }

                var viewSeekbar = findViewById<SeekBar>(R.id.view_sbContent)
                viewSeekbar.progress = weight.toInt()
            }


            //PEN COLOR
            this.findViewWithTag<ConstraintLayout>("PEN_LINECOLOR")?.apply {

                var strBackColor = if(m_objProperty.get("BackColor") == null || m_objProperty.get("BackColor").isJsonNull) PEN_BACKGROUND_DEFAULT else m_objProperty.get("BackColor").asString

                //Color HEX text
                var viewColorText = findViewById<TextView>(R.id.view_textCellContents)
                viewColorText.text = strBackColor

                //Color circle
                var viewColorCirCle = findViewById<View>(R.id.view_curColor)?.apply {

                    var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                    drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strBackColor), PorterDuff.Mode.MULTIPLY)

                    background = drawableBG
                }
            }


            //RECT SWITCH
            this.findViewWithTag<ConstraintLayout>("RECT_BACKFLAG")?.apply {

                var backFlag = if(m_objProperty.get("BackFlag") == null || m_objProperty.get("BackFlag").isJsonNull) RECT_BG_ENABLE_DEFAULT else m_objProperty.get("BackFlag").asString

                findViewWithTag<SwitchCompat>("RECT_BACKFLAG_SWITCH")?.let {
                    it.isChecked = "1" == backFlag
                }
            }

            //RECT WEIGHT
            this.findViewWithTag<ConstraintLayout>("RECT_WEIGHT")?.apply {

                var weight = if(m_objProperty.get("WEIGHT") == null || m_objProperty.get("WEIGHT").isJsonNull) RECT_WEIGHT_DEFAULT else m_objProperty.get("WEIGHT").asFloat

                var viewText = findViewById<TextView>(R.id.view_textCellCur)
                viewText.text = StringBuffer().apply {
                    append(weight!!)
                }

                var viewSeekbar = findViewById<SeekBar>(R.id.view_sbContent)
                viewSeekbar.progress = weight.toInt()!!
            }

            //RECT LINE COLOR
            this.findViewWithTag<ConstraintLayout>("RECT_LINECOLOR")?.apply {

                var strBackColor = if(m_objProperty.get("LineColor") == null || m_objProperty.get("LineColor").isJsonNull) RECT_LINE_DEFAULT else m_objProperty.get("LineColor").asString

                //Color HEX text
                var viewColorText = findViewById<TextView>(R.id.view_textCellContents)
                viewColorText.text = strBackColor

                //Color circle
                var viewColorCirCle = findViewById<View>(R.id.view_curColor)?.apply {

                    var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                    drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strBackColor), PorterDuff.Mode.MULTIPLY)

                    background = drawableBG
                }
            }

            //RECT BACK COLOR
            this.findViewWithTag<ConstraintLayout>("RECT_BACKCOLOR")?.apply {

                var strBackColor = if(m_objProperty.get("BackColor") == null || m_objProperty.get("BackColor").isJsonNull) RECT_BACKGROUND_DEFAULT else m_objProperty.get("BackColor").asString

                //Color HEX text
                var viewColorText = findViewById<TextView>(R.id.view_textCellContents)
                viewColorText.text = strBackColor

                //Color circle
                var viewColorCirCle = findViewById<View>(R.id.view_curColor)?.apply {

                    var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                    drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strBackColor), PorterDuff.Mode.MULTIPLY)

                    background = drawableBG
                }
            }

            //CIRCLE SWITCH
            this.findViewWithTag<ConstraintLayout>("CIRCLE_BACKFLAG")?.apply {

                var backFlag = if(m_objProperty.get("BackFlag") == null || m_objProperty.get("BackFlag").isJsonNull) CIRCLE_BG_ENABLE_DEFAULT else m_objProperty.get("BackFlag").asString

                findViewWithTag<SwitchCompat>("CIRCLE_BACKFLAG_SWITCH")?.let {
                    it.isChecked = "1" == backFlag
                }
            }

            //CIRCLE WEIGHT
            this.findViewWithTag<ConstraintLayout>("CIRCLET_WEIGHT")?.apply {

                var weight = if(m_objProperty.get("WEIGHT") == null || m_objProperty.get("WEIGHT").isJsonNull) CIRCLE_WEIGHT_DEFAULT else m_objProperty.get("WEIGHT").asFloat

                var viewText = findViewById<TextView>(R.id.view_textCellCur)
                viewText.text = StringBuffer().apply {
                    append(weight!!)
                }

                var viewSeekbar = findViewById<SeekBar>(R.id.view_sbContent)
                viewSeekbar.progress = weight.toInt()!!
            }

            //CIRCLE LINE COLOR
            this.findViewWithTag<ConstraintLayout>("CIRCLE_LINECOLOR")?.apply {

                var strBackColor = if(m_objProperty.get("LineColor") == null || m_objProperty.get("LineColor").isJsonNull) CIRCLE_LINE_DEFAULT else m_objProperty.get("LineColor").asString

                //Color HEX text
                var viewColorText = findViewById<TextView>(R.id.view_textCellContents)
                viewColorText.text = strBackColor

                //Color circle
                var viewColorCirCle = findViewById<View>(R.id.view_curColor)?.apply {

                    var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                    drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strBackColor), PorterDuff.Mode.MULTIPLY)

                    background = drawableBG
                }
            }

            //CIRCLE BACK COLOR
            this.findViewWithTag<ConstraintLayout>("CIRCLE_BACKCOLOR")?.apply {

                var strBackColor = if(m_objProperty.get("BackColor") == null || m_objProperty.get("BackColor").isJsonNull) CIRCLE_BACKGROUND_DEFAULT else m_objProperty.get("BackColor").asString

                //Color HEX text
                var viewColorText = findViewById<TextView>(R.id.view_textCellContents)
                viewColorText.text = strBackColor

                //Color circle
                var viewColorCirCle = findViewById<View>(R.id.view_curColor)?.apply {

                    var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                    drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strBackColor), PorterDuff.Mode.MULTIPLY)

                    background = drawableBG
                }
            }

            //MEMO SWITCH
            this.findViewWithTag<ConstraintLayout>("MEMO_BOLD")?.apply {

                var boldFlag = if(m_objProperty.get("Bold") == null || m_objProperty.get("Bold").isJsonNull) MEMO_BOLD_DEFAULT else m_objProperty.get("Bold").asString

                findViewWithTag<SwitchCompat>("MEMO_BOLD_SWITCH")?.let {
                    it.isChecked = "1" == boldFlag
                }
            }

            //MEMO SWITCH
            this.findViewWithTag<ConstraintLayout>("MEMO_ITALIC")?.apply {

                var italicFlag = if(m_objProperty.get("Italic") == null || m_objProperty.get("Italic").isJsonNull) MEMO_ITALIC_DEFAULT else m_objProperty.get("Italic").asString

                findViewWithTag<SwitchCompat>("MEMO_ITALIC_SWITCH")?.let {
                    it.isChecked = "1" == italicFlag
                }
            }

            //MEMO FONT Size
            this.findViewWithTag<ConstraintLayout>("MEMO_TEXT_SIZE")?.apply {

                var weight = if(m_objProperty.get("TEXT_SIZE") == null || m_objProperty.get("TEXT_SIZE").isJsonNull) MEMO_FONTSIZE_DEFAULT else m_objProperty.get("TEXT_SIZE").asInt

                var viewText = findViewById<TextView>(R.id.view_textCellCur)
                viewText.text = StringBuffer().apply {
                    append(weight!!)
                }

                var viewSeekbar = findViewById<SeekBar>(R.id.view_sbContent)
                viewSeekbar.progress = weight!!
            }

            //MEMO Alpha Size
            this.findViewWithTag<ConstraintLayout>("MEMO_ALPHA")?.apply {

                var weight = if(m_objProperty.get("ALPHA") == null || m_objProperty.get("ALPHA").isJsonNull) MEMO_ALPHA_DEFAULT else m_objProperty.get("ALPHA").asFloat

                var viewText = findViewById<TextView>(R.id.view_textCellCur)
                viewText.text = StringBuffer().apply {
                    append(weight!!)
                }

                var viewSeekbar = findViewById<SeekBar>(R.id.view_sbContent)
                viewSeekbar.progress = (weight * 10).toInt()!!
            }

            //MEMO LINE COLOR
            this.findViewWithTag<ConstraintLayout>("MEMO_LINECOLOR")?.apply {

                var strBackColor = if(m_objProperty.get("LineColor") == null || m_objProperty.get("LineColor").isJsonNull) MEMO_LINE_DEFAULT else m_objProperty.get("LineColor").asString

                //Color HEX text
                var viewColorText = findViewById<TextView>(R.id.view_textCellContents)
                viewColorText.text = strBackColor

                //Color circle
                var viewColorCirCle = findViewById<View>(R.id.view_curColor)?.apply {

                    var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                    drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strBackColor), PorterDuff.Mode.MULTIPLY)

                    background = drawableBG
                }
            }

            //MEMO BACK COLOR
            this.findViewWithTag<ConstraintLayout>("MEMO_BACKCOLOR")?.apply {

                var strBackColor = if(m_objProperty.get("BackColor") == null || m_objProperty.get("BackColor").isJsonNull) MEMO_BACKGROUND_DEFAULT else m_objProperty.get("BackColor").asString

                //Color HEX text
                var viewColorText = findViewById<TextView>(R.id.view_textCellContents)
                viewColorText.text = strBackColor

                //Color circle
                var viewColorCirCle = findViewById<View>(R.id.view_curColor)?.apply {

                    var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                    drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strBackColor), PorterDuff.Mode.MULTIPLY)

                    background = drawableBG
                }
            }

            //MEMO TEXT COLOR
            this.findViewWithTag<ConstraintLayout>("MEMO_TEXTCOLOR")?.apply {

                var strBackColor = if(m_objProperty.get("TextColor") == null || m_objProperty.get("TextColor").isJsonNull) MEMO_FOREGROUND_DEFAULT else m_objProperty.get("TextColor").asString

                //Color HEX text
                var viewColorText = findViewById<TextView>(R.id.view_textCellContents)
                viewColorText.text = strBackColor

                //Color circle
                var viewColorCirCle = findViewById<View>(R.id.view_curColor)?.apply {

                    var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                    drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strBackColor), PorterDuff.Mode.MULTIPLY)

                    background = drawableBG
                }
            }

            //MEMO TEXT
            this.findViewWithTag<ConstraintLayout>("MEMO_TEXT_TITLE")?.apply {

                var strText = if(m_objProperty.get("TEXT_TITLE") == null || m_objProperty.get("TEXT_TITLE").isJsonNull) getString(R.string.new_memo) else m_objProperty.get("TEXT_TITLE").asString

                //Color HEX text
                var viewColorText = findViewById<EditText>(R.id.view_editCellContents)
                viewColorText.setText(strText, TextView.BufferType.EDITABLE)
            }




        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

        when(seekBar?.tag)
        {
            "PEN_WEIGHT_SEEKBAR", "RECT_WEIGHT_SEEKBAR", "CIRCLE_WEIGHT_SEEKBAR", "MEMO_WEIGHT_SEEKBAR" -> {

                //Set seekbar change event with min
                view_layoutUICell.findViewWithTag<ConstraintLayout>(m_strSettingType+"_WEIGHT")?.apply {

                    var strMinVal:String? = findViewById<TextView>(R.id.view_textCellMin)?.text?.toString()

                    var minVal = 0
                    if(!m_C.isBlank(strMinVal))
                    {
                        minVal = strMinVal!!.toInt()
                    }

                    if (progress <= minVal) seekBar.progress = minVal
                    else seekBar.progress = progress
                    findViewById<TextView>(R.id.view_textCellCur)?.text = seekBar.progress.toString()
                    //Add value to propertyMap
                    m_objProperty.addProperty("WEIGHT", seekBar.progress)
                }
            }
            "MEMO_ALPHA_SEEKBAR" -> {

                //Set seekbar change event with min
                view_layoutUICell.findViewWithTag<ConstraintLayout>(m_strSettingType+"_ALPHA")?.apply {

                    var strMinVal:String? = findViewById<TextView>(R.id.view_textCellMin)?.text?.toString()

                    var minVal = 0
                    if(!m_C.isBlank(strMinVal))
                    {
                        minVal = (strMinVal!!.toFloat() * 10).toInt()
                    }

                    if (progress <= minVal) seekBar.progress = minVal
                    else seekBar.progress = progress
                    findViewById<TextView>(R.id.view_textCellCur)?.text = String.format("%.1f",seekBar.progress * 0.1f)
                    //Add value to propertyMap
                    m_objProperty.addProperty("ALPHA", seekBar.progress / 10f)
                }
            }

            "MEMO_TEXT_SIZE_SEEKBAR" -> {

                //Set seekbar change event with min
                view_layoutUICell.findViewWithTag<ConstraintLayout>(m_strSettingType+"_TEXT_SIZE")?.apply {

                    var strMinVal:String? = findViewById<TextView>(R.id.view_textCellMin)?.text?.toString()

                    var minVal = 0
                    if(!m_C.isBlank(strMinVal))
                    {
                        minVal = strMinVal!!.toInt()
                    }

                    if (progress <= minVal) seekBar.progress = minVal
                    else seekBar.progress = progress
                    findViewById<TextView>(R.id.view_textCellCur)?.text = seekBar.progress.toString()
                    //Add value to propertyMap
                    m_objProperty.addProperty("TEXT_SIZE", seekBar.progress)
                }
            }
        }

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        when(buttonView?.tag)
        {
            "RECT_BACKFLAG_SWITCH", "CIRCLE_BACKFLAG_SWITCH"-> {
                m_objProperty.addProperty("BackFlag", if(isChecked) "1" else "0")
            }
            "MEMO_BOLD_SWITCH" -> {
                m_objProperty.addProperty("Bold", if(isChecked) "1" else "0")
            }
            "MEMO_ITALIC_SWITCH" -> {
                m_objProperty.addProperty("Italic", if(isChecked) "1" else "0")
            }
        }
    }

    override fun onClick(v: View?) {

        var strParamsArray:Array<String?>? = null
        var bSqliteRes = false



        //If it's from setting

            when (v?.tag) {

                "RECT_BACKCOLOR", "RECT_LINECOLOR", "CIRCLE_BACKCOLOR", "CIRCLE_LINECOLOR", "MEMO_BACKCOLOR", "MEMO_TEXTCOLOR", "MEMO_LINECOLOR" -> {
                    v.findViewWithTag<LinearLayout>("COLORPICKER")?.apply {
                        visibility = if(visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }
                }

                "PROPERTY_CLOSE" -> {
                    this.finish()
                }

                "PROPERTY_CONFIRM" -> {

                    var backColor:String? = null
                    var LineColor:String? = null
                    var fontColor:String? = null

                    when(m_strSettingType)
                    {
                        "PEN" -> {
                            backColor = view_layoutUICell.findViewWithTag<ConstraintLayout>("PEN_LINECOLOR").findViewById<TextView>(R.id.view_textCellContents).text.toString()
                        }
                        "RECT" -> {
                            backColor = view_layoutUICell.findViewWithTag<ConstraintLayout>("RECT_BACKCOLOR").findViewById<TextView>(R.id.view_textCellContents).text.toString()
                            LineColor = view_layoutUICell.findViewWithTag<ConstraintLayout>("RECT_LINECOLOR").findViewById<TextView>(R.id.view_textCellContents).text.toString()

                        }
                        "CIRCLE" -> {
                            backColor = view_layoutUICell.findViewWithTag<ConstraintLayout>("CIRCLE_BACKCOLOR").findViewById<TextView>(R.id.view_textCellContents).text.toString()
                            LineColor = view_layoutUICell.findViewWithTag<ConstraintLayout>("CIRCLE_LINECOLOR").findViewById<TextView>(R.id.view_textCellContents).text.toString()
                        }
                        "MEMO" -> {
                            backColor = view_layoutUICell.findViewWithTag<ConstraintLayout>("MEMO_BACKCOLOR").findViewById<TextView>(R.id.view_textCellContents).text.toString()
                            LineColor = view_layoutUICell.findViewWithTag<ConstraintLayout>("MEMO_LINECOLOR").findViewById<TextView>(R.id.view_textCellContents).text.toString()
                            fontColor = view_layoutUICell.findViewWithTag<ConstraintLayout>("MEMO_TEXTCOLOR").findViewById<TextView>(R.id.view_textCellContents).text.toString()

                        }
                    }

                    if (m_strMode?.toUpperCase() == "SETTING") {

                        when (m_strSettingType) {

                            "PEN" -> {

                                var weight = if(m_objProperty.get("WEIGHT") == null || m_objProperty.get("WEIGHT").isJsonNull) PEN_WEIGHT_DEFAULT else m_objProperty.get("WEIGHT").asInt

                                strParamsArray = arrayOf(
                                        "PEN"                                           // MODE
                                        , ""+weight           // WEIGHT
                                        , ""                                             // ALPHA
                                        , ""                                            // LineColor
                                        , ""+backColor                                 // BackColor
                                        , ""                                             // TextColor
                                        , ""                                             // TEXT_TITLE
                                        , ""                                             // TEXT_SIZE
                                        , ""                                             // Italic
                                        , ""                                             // Bold
                                        , ""                                              // BackFlag
                                )
                            }
                            "RECT" -> {

                                var weight = if(m_objProperty.get("WEIGHT") == null || m_objProperty.get("WEIGHT").isJsonNull) RECT_WEIGHT_DEFAULT else m_objProperty.get("WEIGHT").asInt
                                var backFlag = if(m_objProperty.get("BackFlag") == null || m_objProperty.get("BackFlag").isJsonNull) RECT_BG_ENABLE_DEFAULT else m_objProperty.get("BackFlag").asString

                                strParamsArray = arrayOf(
                                        "RECT"                                           // MODE
                                        , ""+weight           // WEIGHT
                                        , ""                                             // ALPHA
                                        , ""+LineColor                                   // LineColor
                                        , ""+backColor                                   // BackColor
                                        , ""                                             // TextColor
                                        , ""                                             // TEXT_TITLE
                                        , ""                                             // TEXT_SIZE
                                        , ""                                             // Italic
                                        , ""                                             // Bold
                                        , backFlag         // BackFlag
                                )
                            }
                            "CIRCLE" -> {

                                var weight = if(m_objProperty.get("WEIGHT") == null || m_objProperty.get("WEIGHT").isJsonNull) CIRCLE_WEIGHT_DEFAULT else m_objProperty.get("WEIGHT").asInt
                                var backFlag = if(m_objProperty.get("BackFlag") == null || m_objProperty.get("BackFlag").isJsonNull) CIRCLE_BG_ENABLE_DEFAULT else m_objProperty.get("BackFlag").asString


                                strParamsArray = arrayOf(
                                        "CIRCLE"                                           // MODE
                                        , ""+weight           // WEIGHT
                                        , ""                                             // ALPHA
                                        , ""+LineColor                                   // LineColor
                                        , ""+backColor                                   // BackColor
                                        , ""                                             // TextColor
                                        , ""                                             // TEXT_TITLE
                                        , ""                                             // TEXT_SIZE
                                        , ""                                             // Italic
                                        , ""                                             // Bold
                                        , backFlag         // BackFlag
                                )
                            }
                            "MEMO" -> {

                                var text = if(m_objProperty.get("TEXT_TITLE") == null || m_objProperty.get("TEXT_TITLE").isJsonNull) getString(R.string.new_memo) else m_objProperty.get("TEXT_TITLE").asString
                                var fontSize = if(m_objProperty.get("TEXT_SIZE") == null || m_objProperty.get("TEXT_SIZE").isJsonNull) MEMO_FONTSIZE_DEFAULT else m_objProperty.get("TEXT_SIZE").asInt
                                var italic = if(m_objProperty.get("Italic") == null || m_objProperty.get("Italic").isJsonNull) MEMO_ITALIC_DEFAULT else m_objProperty.get("Italic").asString
                                var bold = if(m_objProperty.get("Bold") == null || m_objProperty.get("Bold").isJsonNull) MEMO_BOLD_DEFAULT else m_objProperty.get("Bold").asString
                                var alpha = if(m_objProperty.get("ALPHA") == null || m_objProperty.get("ALPHA").isJsonNull) MEMO_ALPHA_DEFAULT else m_objProperty.get("ALPHA").asFloat


                                strParamsArray = arrayOf(
                                        "MEMO"                                           // MODE
                                        , ""                                             // WEIGHT
                                        , ""+alpha                                       // ALPHA
                                        , ""+LineColor                                   // LineColor
                                        , ""+backColor                                   // BackColor
                                        , ""+fontColor                                   // TextColor
                                        , ""+text   // TEXT_TITLE
                                        , ""+fontSize     // TEXT_SIZE
                                        , ""+italic         // Italic
                                        , ""+bold          // Bold
                                        , ""                                             // BackFlag
                                )
                            }
                        }


                        //Save settings
                        if (strParamsArray != null) {
                            bSqliteRes = SQLite(this@DrawSettingActivity).instantSetRecord(SQLITE_CREATE_PROPERTY_T, SQLITE_INSERT_PROPERTY_T, strParamsArray as Array<String>)
                        }

                        if (bSqliteRes) {
                            this@DrawSettingActivity.run {
                                setResult(RESULT_OK, intent)
                                finish()
                            }
                        } else {
                            AlertDialog.Builder(this@DrawSettingActivity).run {
                                setMessage(getString(R.string.failed_save_property))
                                setPositiveButton(getString(R.string.btn_confirm), null)
                            }.show()
                        }
                    }
                    //If it's modify
                    else {
                        m_objProperty.addProperty("BackColor", backColor)
                        m_objProperty.addProperty("LineColor", LineColor)
                        m_objProperty.addProperty("TextColor", fontColor)

                        this@DrawSettingActivity.run {
                            intent.putExtra("PROPERTY", m_objProperty.toString())
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    }
                }
            }
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        m_objProperty.addProperty("TEXT_TITLE", s?.toString())
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if(v is EditText) {
            var imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v?.windowToken, 0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Save current upload info to bundle
        outState?.putString("TYPE",m_strSettingType)
    }

    private fun loadXML(strAssetPath:String):Boolean {
        var bRes = false
        assets.open(strAssetPath)?.run {

            view_layoutUICell?.removeAllViews()

            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(InputStreamReader(this, "UTF-8")))?.run {

                var mainElement: Element? = this.documentElement


                if (!m_strSettingType.isNullOrBlank()) {
                    mainElement = this.getElementsByTagName(m_strSettingType).item(0) as Element
                }

                //Add title
                val viewSection = LayoutInflater.from(this@DrawSettingActivity).inflate(R.layout.maincell_title, null)
                viewSection.findViewById<TextView>(R.id.view_textCellTitle).text = ""
                view_layoutUICell.addView(viewSection)

                mainElement?.getElementsByTagName("CELL")?.run {
                    bRes = true
                    for (i in 0 until this.length) {

                        var eleColumn = this.item(i) as Element
                        var strColumnTitle = eleColumn.getAttribute("TITLE")

                        //pass cur node if its enable is 0
                        var strEnable = eleColumn.getAttribute("ENABLE")
                        if ("0" == strEnable) continue

                        var viewColumn: View? = null
                        //  var strColumnType = eleColumn.getAttribute("TYPE")
                        when (eleColumn.getAttribute("TYPE").toUpperCase()) {

                            "SLIDER" -> {
                                viewColumn = LayoutInflater.from(this@DrawSettingActivity).inflate(R.layout.maincell_slider, null)
                                viewColumn?.tag = m_strSettingType + "_"+ eleColumn.getAttribute("ID")?.toUpperCase()

                                //setTextValue
                                viewColumn?.findViewById<TextView>(R.id.view_textCellMin)?.let {
                                    it.text = eleColumn.getAttribute("MIN")
                                }
                                viewColumn?.findViewById<TextView>(R.id.view_textCellMax)?.let {
                                    it.text = eleColumn.getAttribute("MAX")
                                }

                                viewColumn?.findViewById<TextView>(R.id.view_textCellCur)?.let {
                                    it.tag = m_strSettingType +"_"+ eleColumn.getAttribute("ID")?.toUpperCase() + "_CUR_VAL"
                                }

                                //Set seebar value
                                viewColumn?.findViewById<SeekBar>(R.id.view_sbContent)?.let {
                                    if(eleColumn.getAttribute("ID")?.toUpperCase() == "ALPHA" )
                                    {
                                        it.max = eleColumn.getAttribute("MAX").toInt() * 10
                                    }
                                    else {
                                        it.max = eleColumn.getAttribute("MAX").toInt()
                                    }
                                    it.tag = m_strSettingType +"_"+ eleColumn.getAttribute("ID")?.toUpperCase() + "_SEEKBAR"
                                    it.setOnSeekBarChangeListener(this@DrawSettingActivity)
                                }

                            }

                            "SWITCH" -> {
                                viewColumn = LayoutInflater.from(this@DrawSettingActivity).inflate(R.layout.maincell_switch, null)
                                viewColumn?.tag = m_strSettingType + "_"+ eleColumn.getAttribute("ID")?.toUpperCase()

                                viewColumn.findViewById<TextView>(R.id.view_textCellTitle).let {
                                    it.text = eleColumn.getAttribute("TITLE")
                                }

                                viewColumn.findViewById<SwitchCompat>(R.id.view_switchCellContents).let {

                                    var strSwitchDefault = eleColumn.getAttribute("DATA")
                                    //if("0".equals(strSwitchDefault)) it.isChecked = false else true

                                    it.tag = m_strSettingType + "_" +eleColumn.getAttribute("ID")?.toUpperCase() + "_SWITCH"
                                    it.setOnCheckedChangeListener(this@DrawSettingActivity)
                                }
                            }

                            "COLOR2" -> {
                                viewColumn = LayoutInflater.from(this@DrawSettingActivity).inflate(R.layout.maincell_color, null)
                                viewColumn?.tag = m_strSettingType + "_"+ eleColumn.getAttribute("ID")?.toUpperCase()
                                viewColumn?.setOnClickListener(this@DrawSettingActivity)

                                //Set color picker
                                viewColumn?.findViewById<LinearLayout>(R.id.view_layoutColorPicker)?.let {

                                    it.tag = "COLORPICKER"

                                    //Draw row
                                    for(nRow in 0 until listColors.size)
                                    {
                                        var layout = LinearLayout(this@DrawSettingActivity)?.apply {
                                            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                                            gravity = Gravity.CENTER
                                        }

                                        //Draw column
                                        for(strItem in listColors[nRow])
                                        {
                                            var colorView = View(this@DrawSettingActivity)?.apply {
                                                var params = LinearLayout.LayoutParams(COLOR_PICKER_CIRCLE_SIZE.px, COLOR_PICKER_CIRCLE_SIZE.px)
                                                params.setMargins(7.px,7.px,7.px,7.px)

                                                layoutParams = params

                                                var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                                                drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strItem), PorterDuff.Mode.MULTIPLY)

                                                background = drawableBG

                                                //Set onclick
                                                setOnClickListener {

                                                    //Set color circle
                                                    viewColumn?.findViewById<View>(R.id.view_curColor)?.let {
                                                        var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                                                        drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strItem), PorterDuff.Mode.MULTIPLY)

                                                        it.background = drawableBG
                                                    }

                                                    viewColumn?.findViewById<TextView>(R.id.view_textCellContents)?.let {
                                                        it.text = strItem.toUpperCase()
                                                    }

                                                    //Add value to propertyMap
                                                    //m_objProperty.addProperty("BackColor", strItem.toUpperCase())
                                                }
                                            }

                                            layout.addView(colorView)
                                        }

                                        it.visibility = View.GONE

                                        if(nRow >= listColors.size - 1)
                                            layout.setPadding(0,0,0,10.px)
                                        it.addView(layout)
                                    }
                                }

                            }

                            "TEXTFIELD" -> {
                                viewColumn = LayoutInflater.from(this@DrawSettingActivity).inflate(R.layout.maincell_memofield, null)
                                viewColumn?.tag = m_strSettingType + "_"+ eleColumn.getAttribute("ID")?.toUpperCase()

                                viewColumn?.findViewById<EditText>(R.id.view_editCellContents)?.let {

                                    it.tag = m_strSettingType + "_"+ eleColumn.getAttribute("ID")?.toUpperCase() + "_TEXTFIELD"
                                    it.addTextChangedListener(this@DrawSettingActivity)
                                    it.setOnFocusChangeListener(this@DrawSettingActivity)
                                }
                            }

                            "COLOR" -> {
                                viewColumn = LayoutInflater.from(this@DrawSettingActivity).inflate(R.layout.maincell_color, null)
                                viewColumn?.tag = m_strSettingType + "_"+ eleColumn.getAttribute("ID")?.toUpperCase()

                                //Set color picker
                                viewColumn?.findViewById<LinearLayout>(R.id.view_layoutColorPicker)?.let {

                                    //Draw row
                                    for(nRow in 0 until listColors.size)
                                    {
                                        var layout = LinearLayout(this@DrawSettingActivity)?.apply {
                                            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                                            gravity = Gravity.CENTER
                                        }

                                        //Draw column
                                        for(strItem in listColors[nRow])
                                        {
                                            var colorView = View(this@DrawSettingActivity)?.apply {
                                                var params = LinearLayout.LayoutParams(COLOR_PICKER_CIRCLE_SIZE.px, COLOR_PICKER_CIRCLE_SIZE.px)
                                                params.setMargins(7.px,7.px,7.px,7.px)

                                                layoutParams = params

                                                var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                                                drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strItem), PorterDuff.Mode.MULTIPLY)

                                                background = drawableBG

                                                //Set onclick
                                                setOnClickListener {

                                                    //Set color circle
                                                    viewColumn.findViewById<View>(R.id.view_curColor).let {
                                                        var drawableBG = ContextCompat.getDrawable(this@DrawSettingActivity, R.drawable.bg_filled_circle)
                                                        drawableBG?.colorFilter = PorterDuffColorFilter(Color.parseColor(strItem), PorterDuff.Mode.MULTIPLY)

                                                        it.background = drawableBG
                                                    }

                                                    viewColumn?.findViewById<TextView>(R.id.view_textCellContents)?.let {
                                                        it.text = strItem.toUpperCase()
                                                    }

                                                    //Add value to propertyMap
                                                    //m_objProperty.addProperty("BackColor", strItem.toUpperCase())
                                                }
                                            }

                                            layout.addView(colorView)
                                        }

                                        if(nRow >= listColors.size - 1)
                                            layout.setPadding(0,0,0,10.px)
                                        it.addView(layout)
                                    }
                                }
                            }
                        }

                        viewColumn?.findViewById<TextView>(R.id.view_textCellTitle)?.text = strColumnTitle

                        viewColumn?.run {
                            view_layoutUICell.apply {
                                this.addView(viewColumn)
                            }
                        }
                    }
                }
            }
        }
        return bRes
    }
}