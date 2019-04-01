package com.officeslip.Util

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.text.TextWatcher
import android.util.DisplayMetrics
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.widget.*
import com.sgenc.officeslip.R
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.officeslip.*
import com.officeslip.Adapter.AddThumbAdapter
import com.officeslip.View.Main.Slip.Frag_AddSlip.Companion.SEGMENT_BTN_FILE
import com.officeslip.View.Main.Slip.Frag_AddSlip.Companion.SEGMENT_BTN_IMAGE
import info.hoang8f.android.segmented.SegmentedGroup
import org.jdom2.input.SAXBuilder
import org.jdom2.output.XMLOutputter
import org.w3c.dom.Node
import java.io.*
import java.math.BigInteger
import java.net.NetworkInterface
import java.security.MessageDigest
import java.text.SimpleDateFormat


class Common {

    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    fun isBlank(str:String?): Boolean {

        if(str == null || str.replace(Regex("\\p{Z}"), "").isEmpty())
        {
            return true
        }

		return false
    }


    fun getRealPathFromUri(uri:Uri?, context:Context):String? {
        var strResPath:String? = null

        uri?.run {
            var nColumnIndex = 0
            var objData  = arrayOf(MediaStore.Images.Media.DATA)
            var cursor      = context.contentResolver.query(uri, objData, null, null, null)

            try {
                if(cursor.moveToFirst())
                {
                    nColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                }
                strResPath = cursor.getString(nColumnIndex)
            }
            catch (e : Exception) {
                Logger.WriteException(this::class.java.name, object {}.javaClass.enclosingMethod.name, e, 7)
                strResPath = null
            }
            finally {
                cursor.close()
            }
        }
        return strResPath
    }

    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    fun handleSamplingAndRotationBitmap(activity:Activity, selectedImage:Uri):Bitmap? {
        var MAX_HEIGHT = 1024
        var MAX_WIDTH = 1024

        // First decode with inJustDecodeBounds=true to check dimensions
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        activity.contentResolver.openInputStream(selectedImage)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        var img:Bitmap? = null
        activity.contentResolver.openInputStream(selectedImage)?.use {
            img = BitmapFactory.decodeStream(it, null, options)
        }

        img?.let {
            img = ExifUtil().rotateBitmap(activity, selectedImage.path)
            //img = rotateImageIfRequired(activity, it, selectedImage)


        }
        return img
    }

    /**
     * get display width
     * @param activity
     */
    fun getDisplayWidth(activity:Activity?) : Int
    {
        val displaymetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displaymetrics)
        return displaymetrics.widthPixels
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    fun rotateImageIfRequired(activity:Activity, img:Bitmap, selectedImage:Uri):Bitmap {

        activity.contentResolver.openInputStream(selectedImage).use {
            var ei:ExifInterface? = null
            if (Build.VERSION.SDK_INT > 23)
                ei = ExifInterface(it)
            else
                ei = ExifInterface(selectedImage.path)

            var orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)


            var resBitmap:Bitmap? = null
            var nDegree = 0
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    nDegree = 90
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    nDegree = 180
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    nDegree = 270
                }
            }

            val exifInterface = ExifInterface(selectedImage.path)
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,
                    nDegree.toString())
            resBitmap = rotateImage(img, 0)

            exifInterface.saveAttributes()

            return resBitmap
        }
    }

    private fun rotateImage(img:Bitmap, degree:Int):Bitmap {
        var matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        var rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
       // img.recycle()
        return rotatedImg
    }

    fun MD5(str:String):String{

        var strRes =  ""

        try
        {
            var md = MessageDigest.getInstance("MD5")
            strRes =  BigInteger(1, md.digest(str.toByteArray())).toString(16).padStart(32, '0')
        }
        catch(e : Exception){

            Logger.WriteException(Common::javaClass.name, "MD5", e, 7)

            strRes = ""
        }

        return strRes
    }

    fun changeLocale(context:Context, lang:String) {

        val locale: Locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            locale = context.resources.configuration.locales.get(0)
        }
        else
        {
            locale = context.resources.configuration.locale
        }

        if(locale.language.substring(0,2) != g_SysInfo.strDisplayLang)
        {
            var res = context.resources
            var dm = res.displayMetrics
            var conf = res.configuration
            conf.setLocale(Locale(lang.toLowerCase()))
            res.updateConfiguration(conf, dm)
            var activity = context as Activity
        //    activity.recreate()
        }
    }

    /**
     * Parse menu json
     */

    fun parseMenuList(activity:Activity, path:String) : JsonArray? {
        val res =   activity.assets?.open(path)?.use {
            val bytes = ByteArray(it.available())
            it.read(bytes, 0, bytes.size)
            JsonParser().parse(String(bytes))?.asJsonArray
        }

        return res
    }

    fun getSystemLocale(context:Context):String {

        val locale: Locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            locale = context.resources.configuration.locales.get(0)
        }
        else
        {
            locale = context.resources.configuration.locale
        }

        return locale.language.substring(0,2)
    }

    fun setTabBtnTint(view:View, color: Int) {

        (view as ImageView).drawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }


//    fun dpToPixel(activity:Activity, fDP:Float):Float {
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,fDP,activity.resources.displayMetrics)
//    }
//
//    fun pixelToDp(activity: Activity, px:Int):Int {
//        var nRes = px / (activity.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
//        return nRes
//    }

    fun pixel2pt(pixel:Int, dpi:Int = 200):Int {

        return  ( pixel * 720 ) / dpi
    }


    fun pt2pixel( pt:Int, dpi:Int = 200):Float {
        return ( pt * dpi ) / 720f
    }

    fun leftPad(originalString: String, length: Int,
                padCharacter: Char): String {
        val sb = StringBuilder()
        for (i in 0 until length) {
            sb.append(padCharacter)
        }
        val padding = sb.toString()
        return padding.substring(originalString.length) + originalString
    }

    fun getRGB(hex:String):String
    {
        var r = Integer.parseInt(hex.substring(1, 3), 16) // 16 for hex
        var g = Integer.parseInt(hex.substring(3, 5), 16) // 16 for hex
        var b = Integer.parseInt(hex.substring(5, 7), 16) // 16 for hex
        return String.format("%d,%d,%d",r,g,b)
    }


    fun isNetworkConnected(context:Context):Boolean {

        var bRes = false
        if(!g_SysInfo.bCellular)
        {
            if(isWifiConnected(context))
            {
                bRes = true
            }
        }
        else
        {
            bRes = true
        }

        return bRes
    }

    //Get drawn shapes tag
    fun getShapeTag(objShapeInfo:JsonObject):String? {

        var sbTag = StringBuffer()

        var keys = objShapeInfo.keySet()

        //Init shape elements
        var elShapeItem = org.jdom2.Element("Shape").setAttribute("Display","1")
        var elPenItem = org.jdom2.Element("LightPen").setAttribute("Display","1")
        var elMemoItem = org.jdom2.Element("Note").setAttribute("Display","1")
        for(key in keys)
        {
            //var elPage:Element? = null//Element("Page")
            var arShape = objShapeInfo.get(key).asJsonArray
//            var elItem:Element? = null
//            when(key.toUpperCase())
//            {
//                "PEN" -> {
//                    elItem = Element("LightPen")
//                }
//                "RECT" -> {
//                    elItem = Element("Shape")
//                }
//                "CIRCLE" -> {
//                    elItem = Element("Shape")
//                }
//                "MEMO" -> {
//                    elItem = Element("Note")
//                }
//            }

//            elItem!!.setAttribute("Display","1")

            for(item in arShape)
            {
                var shape = item.asJsonObject

                var rect = String.format("%d,%d,%d,%d",
                        pixel2pt(shape.get("leftPoint").asString.toFloat().toInt())
                        , pixel2pt(shape.get("topPoint").asString.toFloat().toInt())
                        , pixel2pt(shape.get("rightPoint").asString.toFloat().toInt())
                        , pixel2pt(shape.get("bottomPoint").asString.toFloat().toInt()))

                var elShape: org.jdom2.Element? = null
                when(key.toUpperCase())
                {
                    "PEN" -> {
                        elShape = org.jdom2.Element("Line").apply {
                            setAttribute("Type", "PEN")
                            setAttribute("Style", "SOLID")
                            setAttribute("Rect", rect)
                            setAttribute("LineWidth", shape.get("weight").toString())
                            setAttribute("LineColor", getRGB(shape.get("bgColor").asString))
                            setAttribute("ID", "PL" + getDate("yyMMddHHmmss"))
                        }
                        elPenItem.addContent(elShape)
                    }
                    "RECT" -> {

                        var bgColor:String? = null
                        if(shape.get("bgColor") == null || shape.get("bgColor").isJsonNull)
                        {
                            bgColor = RECT_BACKGROUND_DEFAULT
                        }
                        else
                        {
                            bgColor = shape.get("bgColor").asString
                        }


                        elShape = org.jdom2.Element("Rectangle").apply {
                            setAttribute("Type", "SHAPE")
                            setAttribute("Style", "RECTANGLE")
                            setAttribute("Rect", rect)
                            setAttribute("LineWidth", shape.get("weight").toString())
                            setAttribute("LineColor", getRGB(shape.get("lineColor").asString))
                            setAttribute("BackColor", getRGB(bgColor!!))
                            setAttribute("Alpha", shape.get("backFlag").asString)
                            setAttribute("ID", "PR" + getDate("yyMMddHHmmsss"))
                        }
                        elShapeItem.addContent(elShape)
                    }
                    "CIRCLE" -> {

                        var bgColor:String? = null
                        if(shape.get("bgColor") == null || shape.get("bgColor").isJsonNull) bgColor = CIRCLE_BACKGROUND_DEFAULT else bgColor = shape.get("bgColor").asString

                        elShape = org.jdom2.Element("Ellipse").apply {
                            setAttribute("Type", "SHAPE")
                            setAttribute("Style", "ELLIPSE")
                            setAttribute("Rect", rect)
                            setAttribute("LineWidth", shape.get("weight").toString())
                            setAttribute("LineColor", getRGB(shape.get("lineColor").asString))
                            setAttribute("BackColor", getRGB(bgColor!!))
                            setAttribute("Alpha", shape.get("backFlag").asString)
                            setAttribute("ID", "PC" + getDate("yyMMddHHmmsss"))
                        }
                        elShapeItem.addContent(elShape)
                    }
                    "MEMO" -> {
                        elShape = org.jdom2.Element("Box").apply {
                            setAttribute("Comment", shape.get("text").asString)
                            setAttribute("LineColor", getRGB(shape.get("lineColor").asString))
                            setAttribute("BackColor", getRGB(shape.get("bgColor").asString))
                            setAttribute("TextColor", getRGB(shape.get("fontColor").asString))
                            setAttribute("Image", "")
                            setAttribute("Alpha", (shape.get("alpha").asFloat * 100f).toString())
                            setAttribute("FontName", "굴림")
                            setAttribute("FontSize", shape.get("fontSize").asString )
                            setAttribute("Italic", shape.get("italic").asString)
                            setAttribute("Bold", shape.get("bold").asString)
                            setAttribute("Rect", rect)
                            setAttribute("ID", "PM" + getDate("yyMMddHHmmsss"))
                        }
                        elMemoItem.addContent(elShape)
                    }
                }
            }
        }

        if(elShapeItem.children.size > 0)
        {
            sbTag.append(XMLOutputter().outputString(elShapeItem))
        }
        if(elPenItem.children.size > 0)
        {
            sbTag.append(XMLOutputter().outputString(elPenItem))
        }
        if(elMemoItem.children.size > 0)
        {
            sbTag.append(XMLOutputter().outputString(elMemoItem))
        }


        var strRes = sbTag.toString()
        if(!isBlank(strRes))
        {
            strRes = strRes.replace("<", "[")
            strRes = strRes.replace(">", "]")
            strRes = strRes.replace("\t",  "")
            strRes = strRes.replace("\n",  "")
            strRes = strRes.replace("&quot;",  "\"")
        }
        return strRes
    }

    fun getHexString(str:String):String {

        var color = Color.rgb(str.split(",")[0].toInt(),str.split(",")[1].toInt(),str.split(",")[2].toInt())
        var hex = Integer.toHexString(color)
        return "#"+hex.substring(2)
    }


    //Parse shape data to JsonObject string
    fun parseShapeXML(strTag:String?):String {

        var strRes = ""
        if(isBlank(strTag)) return strRes

        try {
            var sbShapeXML = StringBuilder().apply{
                append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                append("<Document>")
                append(strTag)
                append("</Document>")
            }
            val builder = SAXBuilder()
            val stream = ByteArrayInputStream(sbShapeXML.toString().toByteArray(charset("UTF-8")))
            val document = builder.build(stream)
            var elItems = document.rootElement.children

            var objItem = JsonObject()
            var arObjRect = JsonArray()
            var arObjCircle = JsonArray()
            var arObjPen = JsonArray()
            var arObjMemo = JsonArray()

            for(elItem in elItems)
            {
                var strType = elItem.name
                for(elShape in elItem.children)
                {
                    var shapeType = elShape.getAttributeValue("Style")
                    var objShape = JsonObject()

                    var strRect = elShape.getAttributeValue("Rect")
                    var splitPos = strRect.split(",")

                    var leftPoint = pt2pixel(splitPos[0].toInt())
                    var topPoint = pt2pixel(splitPos[1].toInt())
                    var rightPoint = pt2pixel(splitPos[2].toInt())
                    var bottomPoint = pt2pixel(splitPos[3].toInt())

                    objShape.addProperty("leftPoint",leftPoint)
                    objShape.addProperty("topPoint",topPoint)
                    objShape.addProperty("rightPoint",rightPoint)
                    objShape.addProperty("bottomPoint",bottomPoint)
                    objShape.addProperty("width",rightPoint - leftPoint)
                    objShape.addProperty("height",bottomPoint - topPoint)

                    when(strType.toUpperCase())
                    {
                        "SHAPE" -> {
                            var bgColor = elShape.getAttributeValue("BackColor")
                            if(!isBlank(bgColor)) bgColor = getHexString(bgColor) //"#"+Integer.toHexString(bgColor.split(",")[0].toInt()) + Integer.toHexString(bgColor.split(",")[1].toInt()) + Integer.toHexString(bgColor.split(",")[0].toInt())

                            var lineColor = elShape.getAttributeValue("LineColor")
                            if(!isBlank(lineColor)) lineColor = getHexString(lineColor)//"#"+Integer.toHexString(bgColor.split(",")[0].toInt()) + Integer.toHexString(bgColor.split(",")[1].toInt()) + Integer.toHexString(bgColor.split(",")[0].toInt())

                            objShape.addProperty("weight", elShape.getAttributeValue("LineWidth"))
                            objShape.addProperty("lineColor", lineColor)
                            objShape.addProperty("bgColor", bgColor)
                          //  objShape.addProperty("alpha", elShape.getAttributeValue("Alpha"))
                            objShape.addProperty("tag", elShape.getAttributeValue("ID"))
                            objShape.addProperty("backFlag", elShape.getAttributeValue("Alpha"))

                            if(shapeType.toUpperCase() == "RECTANGLE")
                            {
                                arObjRect.add(objShape)
                            }
                            else
                            {
                                arObjCircle.add(objShape)
                            }
                        }
                        "LIGHTPEN" -> {
                            var bgColor = elShape.getAttributeValue("LineColor")
                            if(!isBlank(bgColor)) bgColor = getHexString(bgColor)//String.format("#%02x%02x%02x", bgColor.split(",")[0],bgColor.split(",")[1],bgColor.split(",")[2])

                            objShape.addProperty("weight", elShape.getAttributeValue("LineWidth"))
                            objShape.addProperty("bgColor", bgColor)
                            objShape.addProperty("tag", elShape.getAttributeValue("ID"))

                            arObjPen.add(objShape)
                        }
                        "NOTE" -> {
                            var lineColor = elShape.getAttributeValue("LineColor")
                            if(!isBlank(lineColor)) lineColor = getHexString(lineColor)//String.format("#%02x%02x%02x", lineColor.split(",")[0],lineColor.split(",")[1],lineColor.split(",")[2])

                            var bgColor = elShape.getAttributeValue("BackColor")
                            if(!isBlank(bgColor)) bgColor = getHexString(bgColor)//String.format("#%02x%02x%02x", bgColor.split(",")[0],bgColor.split(",")[1],bgColor.split(",")[2])

                            var fontColor = elShape.getAttributeValue("TextColor")
                            if(!isBlank(fontColor)) fontColor = getHexString(fontColor)//String.format("#%02x%02x%02x", fontColor.split(",")[0],fontColor.split(",")[1],fontColor.split(",")[2])

                            objShape.addProperty("lineColor", lineColor)
                            objShape.addProperty("bgColor", bgColor)
                            objShape.addProperty("fontColor", fontColor)
                            objShape.addProperty("text", elShape.getAttributeValue("Comment"))
                            objShape.addProperty("alpha", elShape.getAttributeValue("Alpha").toFloat() / 100f)
                            objShape.addProperty("fontSize", elShape.getAttributeValue("FontSize"))
                            objShape.addProperty("italic", elShape.getAttributeValue("Italic"))
                            objShape.addProperty("bold", elShape.getAttributeValue("Bold"))
                            objShape.addProperty("tag", elShape.getAttributeValue("ID"))

                            arObjMemo.add(objShape)
                        }
                    }
                }
            }
            objItem.add("PEN", arObjPen)
            objItem.add("MEMO", arObjMemo)
            objItem.add("CIRCLE", arObjCircle)
            objItem.add("RECT", arObjRect)

            strRes = objItem.toString()
        } catch (e: Exception) {
            Logger.WriteException(this@Common::javaClass.name, "parseShapeXML", e, 5)
        }

        return strRes
    }

    fun calculateInSampleSize(
            options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw weight and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // weight and width larger than the requested weight and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun decodeSampledBitmapFromResource(strPath:String?, reqWidth:Int, reqHeight:Int): Bitmap? {


        var bRes:Bitmap? = null
        try
        {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(strPath, options)
            //  BitmapFactory.decodeResource(res, resId, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false

            bRes = BitmapFactory.decodeFile(strPath, options)
        }
        catch(e : Exception)
        {
            Logger.WriteException("Common", "decodeSampledBitmapFromResource", e, 7)
            return null
        }

        return  bRes
    }

    fun isMobileConnected(context: Context):Boolean {
        var isConnected = false

        var connetion:ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var cellInfo = connetion.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        if(cellInfo.isConnected)
        {
            isConnected = true
        }

        return isConnected
    }
    fun isWifiConnected(context: Context):Boolean {
        var isConnected = false

        var connetion:ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var wifiInfo = connetion.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        if(wifiInfo.isConnected)
        {
            isConnected = true
        }

        return isConnected
    }

    fun getNodeNameAtIndex(activity: Activity, strXmlPath:String, index:Int):String? {

        var strRes:String? = null
        //Get segment button name by index.
        activity.assets.open(strXmlPath)?.run {

            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(InputStreamReader(this, "UTF-8")))?.run {

                this.documentElement.childNodes?.run {
                    var nNodeIdx = 0
                    for(i in 0 until this.length)
                    {
                        if (item(i).nodeType == Node.ELEMENT_NODE)
                        {
                            if(nNodeIdx == index)
                            {
                                strRes = item(i).nodeName
                                break
                            }
                            else
                            {
                                nNodeIdx++
                            }
                        }
                    }
                }
            }
        }
        return strRes
    }


    fun getDeviceRotateAngle(imagePath:String, nFacing:Int):Float {

        var exifInterface = ExifInterface(imagePath)

        var orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        var fDegree = 0f
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 ->{
                    fDegree = 90f
                }
                ExifInterface.ORIENTATION_ROTATE_180 ->{
                    fDegree = 180f
                }
                ExifInterface.ORIENTATION_ROTATE_270 ->{
                    fDegree = 270f
                }
            }

//        var nRotation = activity.windowManager.defaultDisplay.rotation
//        var fDegree = 0.0f
//
//        when(nRotation) {
//            Surface.ROTATION_0 -> {
//                fDegree = 0.0f
//            }
//            Surface.ROTATION_90 -> {
//                fDegree = 90.0f
//            }
//            Surface.ROTATION_180 -> {
//                fDegree = 180.0f
//            }
//            Surface.ROTATION_270 -> {
//                fDegree = 270.0f
//            }
//        }

//        var fRes:Float = 0.0f
//        if(nFacing == CameraView.FACING_FRONT){
//            fRes = (nRotation + fDegree) % 360.0f
//            fRes = (360.0f - fRes) % 360.0f
//        }else{
//            fRes = (fDegree + 360.0f - nRotation.toFloat()) % 360.0f
//        }

        return fDegree
    }

    /**
	 * 특수문자 변환
	 */
	fun convertSpecialChar(str:String):String
	{
        var strRes = str
        strRes = str.replace("\\n", "&ltbr&gt")
        strRes = str.replace(">", "〉")
        strRes = str.replace("<", "〈")
        strRes = str.replace("'", "''")
        strRes = str.replace("!", "！")
        strRes = str.replace("%", "％")
        strRes = str.replace("&gt", "〉")
        strRes = str.replace("&lt", "〈")
        strRes = str.replace("&amp", "［")
        strRes = str.replace("&ampquot", "］")
        strRes = str.replace("&", "＆")

		return strRes
	}

    /**
	* 증빙번호 채번
	* @param nZeroCnt
	*
	* @param nSlipNO
	*
	* @return strSlipNO
	*/

	fun getSlipNo(nZeroCnt:Int, nSlipNO:Int):String?
	{
		var strSlipNO:String?	=	null
		var strZero:String?	    =	""

		strSlipNO	=	Integer.toString(nSlipNO)

		if(strSlipNO.length < nZeroCnt)
		{
			for(i in 0 until  nZeroCnt-strSlipNO.length)
			{
				strZero	+=	"0"
			}
			strSlipNO	=	strZero + strSlipNO
		}
		return strSlipNO
	}

    fun getSlipTotalSize(arObjImages: JsonArray):Int {
        var nResSize:Int = 0


        for(obj in arObjImages)
        {
            nResSize += obj.asJsonObject.get("ImageSize").asInt
        }

        return nResSize
    }

    fun loadUIXML(activity: Activity, parentView: View?, listener: Any?, strAssetPath:String, strMainNode:String? = null):Boolean {
        var bRes = false
        activity.assets.open(strAssetPath)?.run {

            (parentView as LinearLayout)?.removeAllViews()

            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(InputStreamReader(this, "UTF-8")))?.run {

                var mainElement:Element? = this.documentElement


                if(!strMainNode.isNullOrBlank())
                {
                    mainElement = this.getElementsByTagName(strMainNode).item(0) as Element
                }

                mainElement?.getElementsByTagName("SECTION")?.run {
                    bRes = true
                    for (i in 0 until this.length) {

                        var eleSection = this.item(i) as Element
                        //Display section.
                        var strSectionTitle = eleSection.getAttribute("TITLE")


                        val viewSection = LayoutInflater.from(activity).inflate(R.layout.maincell_title, null)

                        viewSection.findViewById<TextView>(R.id.view_textCellTitle).text = strSectionTitle

                        parentView?.findViewById<LinearLayout>(R.id.view_layoutUICell)?.apply {
                            this.addView(viewSection)
                        }

                        //Hide section is attr has been set
                        eleSection.getAttribute("HIDE_TITLE")?.run {
                            if(this == "1")
                            {
                                viewSection.visibility = View.GONE
                            }
                        }

                        //DisplayColumn
                        eleSection.getElementsByTagName("CELL")?.run {
                            for (i in 0 until this.length) {
                                var eleColumn = this.item(i) as Element
                                var strColumnTitle = eleColumn.getAttribute("TITLE")

                                //pass cur node if its enable is 0
                                var strEnable  = eleColumn.getAttribute("ENABLE")
                                if("0" == strEnable) continue

                                var viewColumn: View? = null
                                //  var strColumnType = eleColumn.getAttribute("TYPE")
                                when (eleColumn.getAttribute("TYPE").toUpperCase()) {
                                    "BUTTON" -> {
                                        var strTag = eleColumn.getAttribute("ID")?.toUpperCase()
                                        viewColumn = LayoutInflater.from(activity).inflate(R.layout.maincell_button, null)
                                        viewColumn?.tag = strTag
                                        viewColumn?.setOnClickListener(listener as View.OnClickListener)

//

                                    }
                                    "SELECT" -> {
                                        var strTag = eleColumn.getAttribute("ID")?.toUpperCase()
                                        viewColumn = LayoutInflater.from(activity).inflate(R.layout.maincell_select, null)
                                        viewColumn?.tag = strTag
                                        viewColumn?.setOnClickListener(listener as View.OnClickListener)

                                        viewColumn?.findViewById<TextView>(R.id.view_textCellContents)?.let {
                                            var strTextContents = eleColumn.getAttribute("PLACEHOLDER")
                                            it.text = strTextContents
                                        }
                                    }
                                    "SWITCH" -> {
                                        viewColumn = LayoutInflater.from(activity).inflate(R.layout.maincell_switch, null)
                                        viewColumn.findViewById<SwitchCompat>(R.id.view_switchCellContents).let {

                                            var strSwitchDefault = eleColumn.getAttribute("DATA")
                                            if("0".equals(strSwitchDefault)) it.isChecked = false else true

                                            it.tag = eleColumn.getAttribute("ID")?.toUpperCase()
                                            it.setOnCheckedChangeListener(listener as CompoundButton.OnCheckedChangeListener)
                                        }

                                    }
                                    "LABEL" -> {
                                        viewColumn = LayoutInflater.from(activity).inflate(R.layout.maincell_label, null)
                                        viewColumn?.tag = eleColumn.getAttribute("ID")?.toUpperCase()
                                        viewColumn.findViewById<TextView>(R.id.view_textCellContents)?.let {
                                            var strTextContents = eleColumn.getAttribute("PLACEHOLDER")
                                            it.text = strTextContents
                                        }
                                    }

                                    "TEXT" -> {
                                        viewColumn = LayoutInflater.from(activity).inflate(R.layout.maincell_text, null)
                                        viewColumn?.tag = "CELL_" + eleColumn.getAttribute("ID")?.toUpperCase()
                                        viewColumn?.setOnClickListener(listener as View.OnClickListener)
                                        viewColumn?.findViewById<TextView>(R.id.view_textCellContents)?.let {
                                            var strTextContents = eleColumn.getAttribute("DATA")
                                            it.text = strTextContents

                                        }

                                        viewColumn?.findViewById<EditText>(R.id.view_editCellContents)?.let {
                                            it.tag = eleColumn.getAttribute("ID")?.toUpperCase()
                                            it.hint = eleColumn.getAttribute("PLACEHOLDER")
                                            it.onFocusChangeListener = listener as View.OnFocusChangeListener
                                            it.setOnEditorActionListener(listener as TextView.OnEditorActionListener)
                                            it.addTextChangedListener(listener as TextWatcher)
                                        }
                                    }

                                    "SEG" -> {
                                        viewColumn = LayoutInflater.from(activity).inflate(R.layout.maincell_segment, null)

                                        viewColumn?.findViewById<SegmentedGroup>(R.id.view_segGroup)?.apply {
                                            tag = eleColumn.getAttribute("ID")?.toUpperCase()

                                            eleColumn.getAttribute("DATA")?.split(";")?.forEachIndexed { i, element ->

                                                var viewSegmentBtn = (LayoutInflater.from(activity).inflate(R.layout.maincell_segment_btn, null) as RadioButton).apply {
                                                    text = element
                                                    id = i
                                                }
                                                addView(viewSegmentBtn)
                                            }

                                            //set segmeneted group UI and add eventhadler
                                            updateBackground()
                                            // (getChildAt(0) as RadioButton).isChecked = true
                                            //tag = eleColumn.getAttribute("TYPE")?.toUpperCase()
                                            setOnCheckedChangeListener(listener as RadioGroup.OnCheckedChangeListener)

                                        }

                                        viewColumn.findViewById<TextView>(R.id.view_textCellContents)?.let {
                                            var strTextContents = eleColumn.getAttribute("DATA")
                                            it.text = strTextContents
                                        }
                                    }
                                    "SLIP" -> {
                                        viewColumn = LayoutInflater.from(activity).inflate(R.layout.maincell_slip, null)

                                        //Add segment buttons (0 : image, 1 : file)
                                        viewColumn?.findViewById<SegmentedGroup>(R.id.view_segGroup)?.apply {
                                            tag = eleColumn.getAttribute("TYPE")?.toUpperCase()

                                            (LayoutInflater.from(activity).inflate(R.layout.maincell_segment_btn, null) as RadioButton).apply {
                                                text = activity?.getString(R.string.segment_image)
                                                id = SEGMENT_BTN_IMAGE

                                                addView(this)
                                            }

                                            (LayoutInflater.from(activity).inflate(R.layout.maincell_segment_btn, null) as RadioButton).apply {
                                                text = activity?.getString(R.string.segment_file)
                                                id = SEGMENT_BTN_FILE

                                                addView(this)
                                            }

                                            //set segmeneted group UI and add eventhadler
                                            updateBackground()
                                            // (getChildAt(0) as RadioButton).isChecked = true
                                            //tag = eleColumn.getAttribute("TYPE")?.toUpperCase()
                                            setOnCheckedChangeListener(listener as RadioGroup.OnCheckedChangeListener)
                                        }

                                        viewColumn?.findViewById<ImageButton>(R.id.view_btnAdd)?.apply{
                                            tag = "ADD_UPLOADITEM"
                                            setOnClickListener(listener as View.OnClickListener)
                                        }

                                        viewColumn?.findViewById<ImageButton>(R.id.view_btnRemove)?.apply{
                                            tag = "REMOVE_UPLOADITEM"
                                            visibility = View.GONE
                                            setOnClickListener(listener as View.OnClickListener)
                                        }

                                        viewColumn?.findViewById<SwitchCompat>(R.id.view_switchShowRemoveBtn)?.apply{
                                            tag = "SHOW_REMOVE"
                                            setOnCheckedChangeListener(listener as CompoundButton.OnCheckedChangeListener)
                                        }

                                        viewColumn?.findViewById<RecyclerView>(R.id.view_recyclerThumb)?.apply {

                                            tag = "UPLOAD_ITEM"

                                            setHasFixedSize(true)

                                            layoutManager = LinearLayoutManager(activity.applicationContext, LinearLayoutManager.HORIZONTAL, false)

                                            adapter = AddThumbAdapter(activity, null, "IMAGE", false)
                                            (adapter as AddThumbAdapter).setChangeSlipModeListener(listener as AddThumbAdapter.ChangeSlipMode)
                                            (adapter as AddThumbAdapter).setViewOriginalListener(listener as AddThumbAdapter.ViewOriginal)

                                        }
                                    }

                                    else -> {

                                    }
                                }

                                viewColumn?.findViewById<TextView>(R.id.view_textCellTitle)?.text = strColumnTitle



                                viewColumn?.run {
                                    (parentView as LinearLayout)?.apply {
                                        this.addView(viewColumn)
                                    }
                                }


//                                //add border
//                                parentView?.findViewById<LinearLayout>(R.id.view_layoutUICell)?.apply {
//                                    this.addView(LayoutInflater.from(activity).inflate(R.layout.view_cell_border, null))
//                                }
                            }
                        }
                    }
//            InputSource(StringReader(it))?.let {
//                resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it).getElementsByTagName("Row")
//            }
                }
            }

        }
        return bRes
    }

    /**Get file Size
     *
     */

    fun getFileSize(strFilePath:String, strUnit:String = "KB"):Long
    {
        var fileSize:Long = 0
        File(strFilePath)?.let {

            when(strUnit.toUpperCase())
            {
                "BYTE" -> fileSize = it.length()
                "KB" -> fileSize = it.length() / 1000
                "MB" -> fileSize = it.length() / 1024 / 1024
                "GB" -> fileSize = it.length() / 1024 / 1024 / 1024
                "TB" -> fileSize = it.length() / 1024 / 1024 / 1024 / 1024
                "PB" -> fileSize = it.length() / 1024 / 1024 / 1024 / 1024 / 1024
                else -> fileSize = it.length() / 1024
        }

        return fileSize
        }
    }

    /** Get file size including float
     *
     */

    fun convertToFileSize(size:Long) : String
    {
        return when {
            size < 1024 -> String.format("%d B", size)
            size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0f)
            size < 1024 * 1024 * 1024 -> String.format("%.1f MB", size.toFloat() / 1024.0f / 1024.0f)
            else -> String.format("%.1f GB", size.toFloat() / 1024.0f / 1024.0f / 1024.0f)
        }
    }

    /**GetToday
     *
     */
    fun getDate(strRegex:String):String
    {
        return SimpleDateFormat(strRegex).format(Date())
    }
    /**
	 * 클라이언트 ip 가져오기 (ipv4)
     * bUserIPv4 =
	 */
	fun getDeviceIP(bUseIPv4:Boolean = true):String
	{
        var strIP = "127.0.0.1"
		var useIPv4 = true

		 try
         {
            var interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
             for(network in interfaces)
             {
                 var inetAddresses = Collections.list(network.inetAddresses)
                 for(inetAddress in inetAddresses)
                 {
                     if(!inetAddress.isLoopbackAddress)
                     {
                         var strAddr = inetAddress.hostAddress
                         var isIPv4 = strAddr.indexOf(':') < 0
                         if(useIPv4)
                         {
                             if(isIPv4) {
                                 strIP = strAddr

                             }
                         }
                         else {
                             if (!isIPv4) {
                                 var delim = strAddr.indexOf('%') // drop ip6 zone suffix
                                 strIP = if(delim < 0)  strAddr.toUpperCase() else strAddr.substring(0, delim).toUpperCase()
                             }
                         }
                     }
                 }
             }
            } catch (e:Exception) {
             Logger.WriteException(this@Common.javaClass.name, "getDeviceIP", e, 7)
            }
		 return strIP
	}

//    fun getFileSize(size: Long): String {
//        if (size <= 0)
//            return "0"
//
//        val units = arrayOf("B", "KB", "MB", "GB", "TB")
//        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
//
//        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
//    }

    fun resizeOriginal(context:Context, file :File):Int
    {
        var bitmap = BitmapFactory.decodeFile(file.absolutePath)//ExifUtil().rotateBitmap(file.absolutePath)
        var imageSize_KB = file.length() / 1024
        var nDefaultQuality = 100

        while (imageSize_KB > UPLOAD_SLIP_SIZE_LIMIT)
        {
            nDefaultQuality -= 5
            FileOutputStream(file).use{
                bitmap.compress(Bitmap.CompressFormat.JPEG, nDefaultQuality, it)
                imageSize_KB = file.length() / 1000
                it.flush()

            }
        }

        return nDefaultQuality
    }

    //Save original image.
    fun saveOriginal(activity:Activity, strImageName:String, strImgFile: File /*data:ByteArray*/):JsonObject {

        var objImageInfo = JsonObject()

        var strFilePath = UPLOAD_PATH
        var fileName    = strImageName

        var nWidth  = 0
        var nHeight = 0

        val path               = File(strFilePath)

        if(!path.exists())
        {
            path.mkdirs()
        }
//        var file = File(path.absolutePath, fileName)
        var imageSize_KB:Long = 0

//        FileOutputStream(file).use {
//            it.write(data)
//          //  imageSize_KB = file.length() / 1000
//            it.flush()
//        }
        var bitmap =  //BitmapFactory.decodeFile(file.absolutePath)//
                        ExifUtil().rotateBitmap(activity, strImgFile.absolutePath)
        //handleSamplingAndRotationBitmap(activity, Uri.fromFile(strImgFile))



//        while (imageSize_KB > UPLOAD_SLIP_SIZE_LIMIT)
//        {
//            nDefaultQuality -= 5
//            FileOutputStream(file).use{
//                bitmap.compress(Bitmap.CompressFormat.JPEG, nDefaultQuality, it)
//                imageSize_KB = file.length() / 1000
//                it.flush()
//
//            }
//        }

        bitmap?.let {

            var nDefaultQuality = 100
            FileOutputStream(File(path.absolutePath, strImageName)).use{
                bitmap.compress(Bitmap.CompressFormat.JPEG, nDefaultQuality, it)
                imageSize_KB = strImgFile.length() / 1000
                it.flush()
            }

            nWidth = it.width
            nHeight = it.height


            objImageInfo.addProperty("FileSize", imageSize_KB)
            objImageInfo.addProperty("Path", strImgFile.absolutePath)
            objImageInfo.addProperty("Width",nWidth)
            objImageInfo.addProperty("Height",nHeight)
            objImageInfo.addProperty("Quality", nDefaultQuality)

            if(!it.isRecycled)
            {
                it.recycle()
            }

            it.recycle()
        }
        return objImageInfo
    }

    //copy File
    fun copyFile(strOldPath:String, strNewPath:String):Boolean {

        var bRes = false
        var fis:FileInputStream? = null
        var fos:FileOutputStream? = null

        try {

            var file = File(strNewPath)
            if(!file.exists())
            {
                file.createNewFile()
            }
            fis = FileInputStream(strOldPath)
            fos = FileOutputStream(strNewPath)

            var byteArr 	= ByteArray(1024)

            var nLength = 0

            while({ nLength = fis?.read(byteArr)!!; nLength}() > -1)
            {
                fos.write(byteArr, 0, nLength)
            }

            bRes = true
        }
        catch (e:Exception)
        {
            Logger.WriteException(this::javaClass.name, "copyFile", e, 7)
        }

        finally
        {
            fis?.close()
            fos?.close()
        }
        return bRes
    }

    //Get camera orientation
    fun getAppOrientation(activity:Activity):Int {
        var rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0

        when(rotation){
            Surface.ROTATION_0 -> {
              degrees = 0
            }
            Surface.ROTATION_90 -> {
              degrees = 90
            }
            Surface.ROTATION_180 -> {
                degrees = 180
            }
            Surface.ROTATION_270 -> {
                degrees = 270
            }

        }

        return degrees
    }

    //Save thumb image.
    fun saveThumb(activity:Activity, strDocIRN:String, strOriginalPath:String, strOriginalQuality:Int, objOriginalInfo:JsonObject):String? {

        var strOriginalPath= strOriginalPath//objOriginalInfo.get("Path").asString
        var strFilePath = UPLOAD_THUMB_PATH
        var fileName    = strDocIRN
        val path               = File(strFilePath)

        var nWidth = objOriginalInfo.get("Width").asInt
        var nHeight = objOriginalInfo.get("Height").asInt
        var nQuality = strOriginalQuality//objOriginalInfo.get("Quality").asInt

//        var imgSize	= if(nWidth > nHeight)  nWidth else nHeight

//        while(imgSize > THUMB_SIZE)
//        {
//            nWidth = (0.9f * nWidth).toInt()
//            nHeight = (0.9f * nHeight).toInt()
//            imgSize	= if(nWidth > nHeight)  nWidth else nHeight
//        }

//        var bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(strOriginalPath), nWidth, nHeight)

        var bitmap =   //BitmapFactory.decodeFile(strOriginalPath)//
         ExifUtil().rotateBitmap(activity, strOriginalPath)//
        //handleSamplingAndRotationBitmap(activity, Uri.fromFile(File(strOriginalPath)))

        var file:File? = null

        bitmap?.run {
          //
            if(!path.exists())
            {
                path.mkdirs()
            }

            file = File(path.absolutePath, fileName)

            FileOutputStream(file).use{
                compress(Bitmap.CompressFormat.JPEG, nQuality / 4, it)

                it.flush()

                if(!isRecycled)
                {
                    recycle()
                }
            }

        }
        return file?.absolutePath
    }

    fun getBytesFromFile(file: File): ByteArray {

        lateinit var bytes:ByteArray
        FileInputStream(file).use {
            var lFileSize = file.length()

            if(lFileSize > Long.MAX_VALUE)
            {

            }
            bytes = ByteArray(file.readBytes().size)

            var offset = 0
            var numRead = 0

            // while (it.read(bytes).let { tmp = it; it != -1 })
            //while( offset < bytes.size && (numRead = it.read(bytes, offset,bytes.size - offset) > -1))
            while(offset < bytes.size && { numRead = it.read(bytes, offset, bytes.size - offset); numRead }() > -1)
            {
                offset += numRead
            }

            if(offset < bytes.size)
            {
                throw IOException("Could not completely read file " + file.name)
            }
        }

        return bytes
    }

    fun removeFolder(context:Context, strFolderPath:String):Boolean {

        var dir = File(strFolderPath)
        if (dir.isDirectory)
        {
            for(file in dir.listFiles())
            {
                removeFolder(context, file.absolutePath)
            }
        }

        if(dir.delete())
        {
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dir)))
        }
        return true
    }

    fun removeFile(context:Context, strFilePath:String?):Boolean {
        if(isBlank(strFilePath)) return true
        var bRes = false
        var file = File(strFilePath)
        bRes = file.delete()
        if(bRes)
        {
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
        }

        Logger.WriteLog(this::javaClass.name, "removeFile", "remove file", 9)
        return bRes
    }

    /**
     * Find child json object by specific key
     */

    fun getChildJsonArray(arObjItem: JsonArray, value:String, key:String, subMenuKey:String) : JsonArray?
    {
        for (item in arObjItem)
        {
            val objItem = item.asJsonObject

            val itemValue = objItem.get(key).asString
            if (itemValue == value)
                return objItem.getAsJsonArray(subMenuKey)

            // Item not returned yet. Search its children by recursive call.
            if (objItem.get(subMenuKey) != null)
            {
                val subresult = getChildJsonArray(objItem.get(subMenuKey).asJsonArray, value, key, subMenuKey)

                // If the item was found in the subchildren, return it.
                if (subresult != null)
                    return subresult
            }
        }
        // Nothing found yet? return null.
        return null
    }


    /**
     *
     * 앱 다운로드
     * @param context
     *
     */
     fun DownloadApp(activity:Activity) {

        var strDesination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator
        var strFileName = "OfficeSLIP.apk"
        strDesination = strDesination + strFileName
        var uri = Uri.parse("file://" + strDesination)

        //Delete update file if exists
        var file = File(strDesination)
        if (file.exists()) file.delete()

        //get url of app on server

        //set downloadmanager
        var downloadRequest = DownloadManager.Request(Uri.parse(APP_UPDATE_URL))
        downloadRequest.setTitle(activity.getString(R.string.app_name))

        //set destination
        downloadRequest.setDestinationUri(uri)

        // get download service and enqueue file
        var manager =  activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var downloadId = manager.enqueue(downloadRequest)

        //set BroadcastReceiver to install app when .apk is downloaded
        var onComplete = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                var install = Intent(Intent.ACTION_VIEW)?.apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    setDataAndType(uri,
                            manager.getMimeTypeForDownloadedFile(downloadId))
                }
                activity.startActivity(install)

                activity.unregisterReceiver(this)
                activity.finish()
            }
        }

        //register receiver for when .apk download is compete
        activity.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}