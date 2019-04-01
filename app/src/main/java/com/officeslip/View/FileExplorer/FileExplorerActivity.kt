package com.officeslip.View.FileExplorer

import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.sgenc.officeslip.R
import kotlinx.android.synthetic.main.activity_file_explorer.*
import java.io.File
import java.util.ArrayList

class FileExplorerActivity:AppCompatActivity(), View.OnClickListener
{
    private var m_directoryFrag = DirectoryFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_explorer)

        setToolbar()
        setupViewUI()
    }

    private  fun setToolbar() {
        //run only when current fragment is this class.
        this.run {
            setSupportActionBar(view_toolbarFileExplorer)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        view_btnBack.setOnClickListener(this)
        view_btnClose.setOnClickListener(this)
    }

    private fun setupViewUI()
    {
        var fragmentTransaction = fragmentManager.beginTransaction()
        m_directoryFrag.setDelegate(object: DirectoryFragment.DocumentSelectActivityDelegate {

            override fun startDocumentSelectActivity() {

            }

            override fun didSelectFiles(activity: DirectoryFragment, files: ArrayList<String>) {
                files?.run {
                    m_directoryFrag.showErrorBox(get(0))
                }
            }

            override fun updateToolBarName(name: String) {
                view_textCurFolder.text = name
            }

            override fun chooseFile(file: File) {

                intent.putExtra("FILE_ITEM",file.absolutePath)
                setResult(RESULT_OK, intent)
                finish()
            }
        })

        fragmentTransaction.add(R.id.fragment_container, m_directoryFrag as Fragment, "" + m_directoryFrag.toString())
        fragmentTransaction.commit()
    }

    override fun onClick(v: View?) {

        when(v?.id)
        {
            view_btnClose.id -> {
                finish()
            }
            view_btnBack.id -> {
                onBackPressed()
            }

        }
    }

    override fun onDestroy() {

        m_directoryFrag.onFragmentDestroy()
        super.onDestroy()
    }

    override fun onBackPressed() {

        if(m_directoryFrag.onBackPressed()) super.onBackPressed()
    }
}