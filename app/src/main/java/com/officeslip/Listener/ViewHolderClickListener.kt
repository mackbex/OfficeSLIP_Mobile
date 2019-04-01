package com.officeslip.Listener

interface ViewHolderClickListener
{
    fun onLongTap(index : Int)
    fun onTap(index : Int)
    fun onRemove(index: Int)
}