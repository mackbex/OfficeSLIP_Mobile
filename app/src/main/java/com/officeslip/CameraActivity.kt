package com.officeslip

import android.os.*
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.cameraview.AspectRatio
import com.google.android.cameraview.CameraView
import com.officeslip.Agent.WDMAgent
import com.officeslip.Util.Common
import com.officeslip.Subclass.AspectRatioFragment
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.activity_camera.*


class CameraActivity : AppCompatActivity(), AspectRatioFragment.Listener {

    var m_C: Common = Common()
    var m_WDM: WDMAgent = WDMAgent()


    companion object {
        val TAG = "CameraActivity"
        val FRAGMENT_DIALOG = "DIALOG"
        val FLASH_OPTIONS = intArrayOf(
                CameraView.FLASH_AUTO,
                CameraView.FLASH_OFF,
                CameraView.FLASH_ON
        )

        val FLASH_ICONS = intArrayOf(
                R.drawable.ic_flash_auto,
                R.drawable.ic_flash_off,
                R.drawable.ic_flash_on
        )

        val FLASH_TITLES = intArrayOf(
                R.string.flash_auto,
                R.string.flash_off,
                R.string.flash_on
        )
    }

    var mCurrentFlash:Int               = CameraView.FLASH_AUTO
    var mBackgroundHandler:Handler?     = null
    private val mOnClickListener = View.OnClickListener { v ->
    when (v.id) {
        R.id.view_floatingTakePicture -> {
            if(view_camera != null)
              //  System.gc()

                view_camera.takePicture()


          }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if(view_camera != null) {
            view_camera.addCallback(mCallback)
        }
        view_floatingTakePicture.setOnClickListener(mOnClickListener)
        setSupportActionBar(view_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onResume() {
        super.onResume()
        if(view_camera != null) {
            view_camera.start()
        }
    }

    override fun onPause() {
        if(view_camera != null) {
            view_camera.stop()
        }
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler?.getLooper()?.quitSafely()
            } else {
                mBackgroundHandler?.getLooper()?.quit()
            }
            mBackgroundHandler = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_camera, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.aspect_ratio -> {
                if(view_camera != null) {
                    val fragmentManager = supportFragmentManager
                    if (fragmentManager.findFragmentByTag(FRAGMENT_DIALOG) == null) {
                        val ratios = view_camera.getSupportedAspectRatios()
                        val currentRatio = view_camera.getAspectRatio()
                        AspectRatioFragment.newInstance(ratios, currentRatio)
                                .show(fragmentManager, FRAGMENT_DIALOG)
                    }
                }
                return true
            }
            R.id.switch_flash -> {
                if(view_camera != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.size
                    item.setTitle(FLASH_TITLES[mCurrentFlash])
                    item.setIcon(FLASH_ICONS[mCurrentFlash])
                    view_camera.setFlash(FLASH_OPTIONS[mCurrentFlash])
                }
                return true
            }
            R.id.switch_camera -> {
                if(view_camera != null) {
                    val facing = view_camera.facing
                    view_camera.facing = if (facing == CameraView.FACING_FRONT)
                        CameraView.FACING_BACK
                    else
                        CameraView.FACING_FRONT
                }
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onAspectRatioSelected(ratio: AspectRatio) {
        if(view_camera != null)
        view_camera.setAspectRatio(ratio)
    }


    private fun getBackgroundHandler(): Handler {
        if (mBackgroundHandler == null) {
            val thread = HandlerThread("background")
            thread.start()
            mBackgroundHandler = Handler(thread.looper)
        }
        return mBackgroundHandler!!
    }

    private val mCallback = object : CameraView.Callback() {

        override fun onCameraOpened(cameraView: CameraView) {
          //  Log.d(TAG, "onCameraOpened")

        }

        override fun onCameraClosed(cameraView: CameraView) {
         //   Log.d(TAG, "onCameraClosed")
        }

        override fun onPictureTaken(cameraView: CameraView, data: ByteArray) {
          //  Log.d(TAG, "onPictureTaken " + data.size)

            view_textInProgress.visibility = View.VISIBLE

//            Toast.makeText(cameraView.context, R.string.picture_taken, Toast.LENGTH_SHORT)
//                    .show()
           // this@CameraActivity.finish()
            getBackgroundHandler().post(Runnable {

                cameraView.stop()

//                var strDocIRN = m_WDM.getDocIRN(g_UserInfo.strUserID,  m_C.getDeviceIP(), CONNECTION_PRD_PORT, AGENT_SERVERKEY)
//                var objOriginalInfo= m_C.saveOriginal(strDocIRN + ".jpg", data)
//                var strThumbPath    = m_C.saveThumb(strDocIRN + ".jpg", objOriginalInfo)
//
//                var objRes = JsonObject()
//                objRes.addProperty("OriginalPath", objOriginalInfo.get("Path").asString)
//                objRes.addProperty("ImageSize", objOriginalInfo.get("FileSize").asInt)
//                objRes.addProperty("Width", objOriginalInfo.get("Width").asInt)
//                objRes.addProperty("Height", objOriginalInfo.get("Height").asInt)
//           //     objRes.addProperty("DocIRN", strDocIRN)
//
//                objRes.addProperty("ThumbPath",strThumbPath)
//                intent.putExtra("Image",objRes.toString())
//                this@CameraActivity.run {
//                    setResult(RESULT_OK, intent)
//                    finish()
//                }
            })
        }
    }




//    public boolean createImageThumb(String loadfile, String savefile, int rate) throws IOException
//    {
//        ByteArrayOutputStream  bos 			= null;
//        OutputStream os							= null;
//        File out										= new File(savefile);
//        boolean isOk 								= false;
//
//        try
//        {
//            Bitmap photo 	= BitmapFactory.decodeFile(loadfile);
//            int imgSize		= (photo.getWidth() > photo.getWeight()) ? photo.getWidth() : photo.getWeight();
//            int width   	= photo.getWidth();
//            int weight  	= photo.getWeight();
//            while(imgSize > 500)
//            {
//                width       = (int) (width * 0.9);
//                weight      = (int) (weight * 0.9);
//                imgSize     = (width > weight) ? width : weight;
//            }
//
//            photo 			= Bitmap.createScaledBitmap(photo, width, weight, true);
//            bos 				= new ByteArrayOutputStream();
//
//            photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//
//            out.createNewFile();
//
//            os = new FileOutputStream(out);
//            os.write(bos.toByteArray());
//            isOk = true;
//        }
//        catch(IOException e)
//        {
//            WriteLog.WriteException("wdmMakeThumbnail - CreateImageThumb ", e,5);
//        }
//        catch(Exception e)
//        {
//            WriteLog.WriteException("wdmMakeThumbnail - CreateImageThumb ", e,5);
//        }
//        finally
//        {
//            try
//            {
//                if(bos != null)		bos.close();
//                if(os != null)		os.close();
//            }
//            catch(IOException e)
//            {
//                WriteLog.WriteException("wdmMakeThumbnail - CreateImageThumb ", e,5);
//            }
//        }
//        return isOk;
//    }
}