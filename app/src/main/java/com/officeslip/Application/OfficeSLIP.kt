package com.officeslip.Application

import android.app.Application

class OfficeSLIP : Application()
{
    //Add tracker to detect app went to background
    override fun onCreate() {
        super.onCreate()

        var tracker = AppLifecycleTracker()
        registerActivityLifecycleCallbacks(tracker)
    }
}