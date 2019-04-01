package com.officeslip

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import com.officeslip.Util.Common
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.acitivity_pin.*

class PINActivity:FragmentActivity(), PinLockListener{


    private var CHANGE_PASSWORD_ENTER_NEW = 1002
    private var CHANGE_PASSWORD_CURRENT_VERIFY = 1001
    private var m_nCurStatus = CHANGE_PASSWORD_ENTER_NEW
    private val m_C: Common = Common()
    private var m_strNewPassword:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.acitivity_pin)

        initView()


        //        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
//        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
//
//        mPinLockView.attachIndicatorDots(mIndicatorDots);
//        mPinLockView.setPinLockListener(mPinLockListener);
//        //mPinLockView.setCustomKeySet(new int[]{2, 3, 1, 5, 9, 6, 7, 0, 8, 4});
//        //mPinLockView.enableLayoutShuffling();
//
//        mPinLockView.setPinLength(4);
//        mPinLockView.setTextColor(ContextCompat.getColor(this, R.color.white));
//
//        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);

    }

    private fun initView() {

        view_pinlock.attachIndicatorDots(view_indicatorDots)
        view_pinlock.setPinLockListener(this@PINActivity)

        view_pinlock.pinLength = 4

        view_indicatorDots.indicatorType = IndicatorDots.IndicatorType.FILL

        when(intent.getStringExtra("MODE").toUpperCase())
        {
            "VERIFY" -> {
                view_textPinTitle.text = getString(R.string.changepassword_verify_title)
                view_textPinSubTitle.text = getString(R.string.changepassword_sub_verify)
            }
            "CHANGE" -> {
                if(m_C.isBlank(g_SysInfo.strLockPasswordValue))
                {
                    m_nCurStatus = CHANGE_PASSWORD_ENTER_NEW
                    view_textPinTitle.text = getString(R.string.changepassword_new_title)
                    view_textPinSubTitle.text = getString(R.string.changepassword_sub_new)
                }
                else
                {
                    m_nCurStatus = CHANGE_PASSWORD_CURRENT_VERIFY
                    view_textPinTitle.text = getString(R.string.changepassword_verify_title)
                    view_textPinSubTitle.text = getString(R.string.changepassword_sub_verify)
                }
            }
        }

    }

    private fun verifyNewEnteredPassword(strPassword: String?) {
        if(m_C.isBlank(m_strNewPassword)) {
            m_strNewPassword = strPassword
            view_pinlock.resetPinLockView()
            view_textPinSubTitle.text = getString(R.string.changepassword_sub_again)
        }
        else
        {
            g_SysInfo.strLockPasswordValue = strPassword!!
            setResult(RESULT_OK)
            this@PINActivity.finish()
        }
    }

    //check PIN with App stored password.
    private fun verifyCurrentPassword(strPassword:String?):Boolean {
        var bRes = false
        if(g_SysInfo.strLockPasswordValue == strPassword)
        {
            view_pinlock.resetPinLockView()
            bRes = true
            view_textPinTitle.text = getString(R.string.changepassword_change_title)
        }
        else
        {
            view_textPinContents.text = getString(R.string.changepassword_contents_incorrect)
        }

        return bRes
    }

    override fun onEmpty() {
        view_textPinContents.text = ""
    }

    override fun onComplete(pin: String?) {

        view_pinlock.resetPinLockView()

        when(intent.getStringExtra("MODE").toUpperCase())
        {
            "VERIFY" -> {
                if(g_SysInfo.strLockPasswordValue == pin) {
                    setResult(RESULT_OK)
                    this@PINActivity.finish()
                }
                else {
                    view_textPinContents.text = getString(R.string.changepassword_contents_incorrect)
                }
            }
            "CHANGE" -> {
                when(m_nCurStatus)
                {
                    CHANGE_PASSWORD_ENTER_NEW -> {
                        verifyNewEnteredPassword(pin)
                    }
                    CHANGE_PASSWORD_CURRENT_VERIFY -> {
                        m_nCurStatus = CHANGE_PASSWORD_CURRENT_VERIFY
                        if(verifyCurrentPassword(pin))
                            m_nCurStatus = CHANGE_PASSWORD_ENTER_NEW
                    }
                }
            }
        }

    }

    override fun onPinChange(pinLength: Int, intermediatePin: String?) {

        if(pinLength < 4)
        {
            view_textPinContents.text = ""
        }
    }

}