package com.officeslip.Listener

import java.io.InputStream

interface OnNavigationListener {
    fun onAddNavigation(asset: InputStream)
    fun onToggleNavigation()
}