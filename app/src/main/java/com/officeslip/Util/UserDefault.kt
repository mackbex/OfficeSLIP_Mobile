package com.officeslip.Util

import android.app.Activity
import android.content.Context
import com.officeslip.APP_BUNDLE_ID


class UserDefault(context: Context) {

    private val context:Context

    init {
        this.context = context
    }

    fun removeAll() {
        val sharedPref = context.getSharedPreferences(APP_BUNDLE_ID, Activity.MODE_PRIVATE)
        with(sharedPref.edit())
        {
            clear()
            commit()
        }
    }
    fun setString(value:String, key:String) {

        val sharedPrep = context.getSharedPreferences(APP_BUNDLE_ID, Activity.MODE_PRIVATE) ?: return

        with(sharedPrep.edit())
        {
            putString(key, value)
            commit()
        }
    }

    fun getString(key:String): String {
        val sharedPref = context.getSharedPreferences(APP_BUNDLE_ID, Activity.MODE_PRIVATE)
        with(sharedPref)
        {
            return getString(key, "")
        }
    }

//    + (BOOL) setObject:(id)object forkey:(NSString *)key;
//    + (BOOL) setStringEncrypt:(NSString*)object forkey:(NSString *)key;
//    + (BOOL) setInteger:(int)object forkey:(NSString *)key;
//    + (BOOL) setDouble:(double)object forkey:(NSString *)key;
//    + (BOOL) setFloat:(float)object forkey:(NSString *)key;
//    + (BOOL) setBool:(BOOL)object forkey:(NSString *)key;
}