package com.officeslip.Adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup
import com.google.gson.JsonObject
import com.officeslip.View.Main.Slip.Frag_SearchSlip
import com.officeslip.View.Main.Slip.Frag_Statistics

class MainViewPageAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager), Frag_Statistics.SearchSelectedItemListener {


    companion object {
        const val NONE_EXISTS = -999
    }

    private val m_mapFragments:LinkedHashMap<String, Fragment> = LinkedHashMap()
    private var m_curFragment:Fragment? = null


    fun addFragment(fragment:Fragment, tag:String?) {

        var frag:Fragment? = null

        tag?.run {
            if(m_mapFragments.get(tag) == null)
            {
                m_mapFragments.put(tag, fragment)
                frag = fragment
            }
            else
            {
                frag = m_mapFragments.get(tag)
            }

            //add current fragemnt value before it created.
            frag?.apply {
                val bundle =  Bundle()
                bundle.putString("tag",tag)
                arguments = bundle
            }

            notifyDataSetChanged()
        }

    }

    override fun getCount(): Int {
        return m_mapFragments.size
    }

    override fun getItem(position: Int): Fragment {

        var resFrag:Fragment? = null
        var nIdx = 0
        for ((key, value) in m_mapFragments) {

            if(nIdx == position)
            {
                resFrag = m_mapFragments[key]
                break
            }
            nIdx++
        }

        return resFrag!!
    }

    fun getFragItemPosition(tag:String?):Int {
        var nResIdx = NONE_EXISTS
        var nIdx = 0
        tag?.apply {
            for ((key, value) in m_mapFragments) {

                if(key == tag)
                {
                    nResIdx = nIdx
                    break
                }
                nIdx++
            }
        }
        return nResIdx
    }
    fun getCurrentFragment():Fragment? {
        return m_curFragment
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if(`object` is Fragment) m_curFragment = `object` as Fragment
        super.setPrimaryItem(container, position, `object`)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position)
    }

    //Override for search slip from statistics
    override fun searchSelectedSlip(objItem: JsonObject) {

        for ((key, value) in m_mapFragments) {

            m_mapFragments[key]?.run {
                if(this::class.java == Frag_SearchSlip::class.java)
                {
                    (this as Frag_SearchSlip).searchSelectedSlip(objItem)
                }
            }
        }
    }
}