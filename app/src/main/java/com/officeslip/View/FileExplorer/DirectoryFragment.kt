package com.officeslip.View.FileExplorer

import android.app.AlertDialog
import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.StateSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.officeslip.FILE_EXPLORER_LIMIT
import com.officeslip.Listener.ViewHolderClickListener
import com.sgenc.officeslip.R
import com.officeslip.Util.Common
import com.officeslip.Util.Logger
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Exception
import java.util.*

class DirectoryFragment: Fragment(), ClickItem {

    private var fragmentView:View? = null
    private lateinit var recyclerView:RecyclerView
    private lateinit var view_textEmpty:TextView
    private var m_C = Common()
    private var items:ArrayList<ListItem> = ArrayList<ListItem>()
    private var history:ArrayList<HistoryEntry> = ArrayList<HistoryEntry>()
    private var strTitle:String = ""
    private var delegate: DocumentSelectActivityDelegate? = null
    private var currentDir:File? = null
    private var receiverRegistered = false
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var receiver = object:BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var runnable = Runnable {
                try {
                    if(currentDir == null)
                    {
                        listRoots()
                    }
                    else
                    {
                        listFiles(currentDir)
                    }
                }
                catch (e:Exception)
                {
                    Logger.WriteException(this@DirectoryFragment::javaClass.name, "onReceive",e,7)
                }
            }

            if (intent?.action ==  Intent.ACTION_MEDIA_UNMOUNTED) {
                recyclerView.postDelayed(runnable, 1000)
            } else {
                runnable.run()
            }
        }
    }

    class ListItem {
        internal var icon: Int = 0
        internal var title: String = ""
        internal var subtitle = ""
        internal var ext = ""
        internal var file: File? = null
        internal var category:String = ""
    }

    private class HistoryEntry {
        internal var scrollItem:Int = 0
        internal var scrollOffset:Int = 0
        internal var dir: File? = null
        internal var title:String = ""
    }

    interface  DocumentSelectActivityDelegate {

        fun didSelectFiles(activity: DirectoryFragment, files:ArrayList<String>)

        fun startDocumentSelectActivity()

        fun updateToolBarName(name:String)

        fun chooseFile(file:File)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {

        if(!receiverRegistered)
        {
            receiverRegistered = true
            var filter = IntentFilter()
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
            filter.addAction(Intent.ACTION_MEDIA_CHECKING)
            filter.addAction(Intent.ACTION_MEDIA_EJECT)
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED)
            filter.addAction(Intent.ACTION_MEDIA_NOFS)
            filter.addAction(Intent.ACTION_MEDIA_REMOVED)
            filter.addAction(Intent.ACTION_MEDIA_SHARED)
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE)
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            filter.addDataScheme("file")
            activity!!.registerReceiver(receiver, filter)

        }

        //Init fragment ja.view
        if(fragmentView == null)
        {
            fragmentView = inflater?.inflate(R.layout.fragment_file_explorer_layout, container, false)
            viewManager = LinearLayoutManager(activity)
            viewAdapter = ExplorerAdapter(activity, items)

            recyclerView = (fragmentView as ConstraintLayout).findViewById(R.id.view_recyclerExplorer)
            view_textEmpty = (fragmentView as ConstraintLayout).findViewById(R.id.view_textEmpty)

            //       view_recyclerExplorer = fragmentView.
            recyclerView.apply {

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter

                (adapter as ExplorerAdapter).setItemClickListener(this@DirectoryFragment as ClickItem)

            }

            listRoots()
        }

        else
        {
            var parent = fragmentView?.parent as ViewGroup

            parent?.apply {
                removeView(fragmentView)
            }
        }

        return fragmentView!!
    }




    //Set delegate
    fun setDelegate(delegate: DocumentSelectActivityDelegate)
    {
        this.delegate = delegate
    }

    //When back pressed.
    fun onBackPressed():Boolean {
        if(history.size > 0)
        {
            var historyEntry = history.removeAt(history.size - 1)
            strTitle = historyEntry.title
            //Set toolbar title
            updateName(strTitle)
            if(historyEntry.dir != null)
            {
                listFiles(historyEntry.dir)
            }
            else
            {
                listRoots()
            }
            recyclerView?.smoothScrollToPosition(historyEntry.scrollItem)
            return false
        }
        return true
    }

    private fun updateName(title:String)
    {
        delegate?.apply {
            updateToolBarName(title)
        }
    }

    private fun chooseFile(file:File)
    {
        delegate?.apply {
            chooseFile(file)
        }
    }

    fun onFragmentDestroy() {
        try {
            if(receiverRegistered)
            {
                activity.unregisterReceiver(receiver)
            }
        }
        catch (e:Exception)
        {
            Logger.WriteException(this@DirectoryFragment::javaClass.name, "onDestroyReceiver",e,7)
        }
    }



    private fun listFiles(dir:File?):Boolean {
        dir?.apply {
            if (!dir.canRead()) {
                if (
                        dir.absolutePath.startsWith(Environment.getExternalStorageDirectory().toString())
                        || dir.absolutePath.startsWith("/sdcard")
                        || dir.absolutePath.startsWith("/mnt/sdcard")
                ) {
                    var state = Environment.getExternalStorageState()

                    if (state != Environment.MEDIA_MOUNTED
                            && state != Environment.MEDIA_MOUNTED_READ_ONLY) {
                        currentDir = dir
                        items.clear()

                        if (state == Environment.MEDIA_SHARED) {
                            view_textEmpty.text = "USB Active"
                        } else {
                            view_textEmpty.text = "No Mounted"
                        }

                        clearDrawableAnimation(recyclerView)

                        viewAdapter.notifyDataSetChanged()
                        return true
                    }
                }
                showErrorBox("Access Error.")
                return false
            }
        }

        view_textEmpty.text = "No Files."
        var files:Array<File>? = null
        try
        {
            files = dir?.listFiles()
        }
        catch (e:Exception)
        {
            showErrorBox(e.localizedMessage)
            Logger.WriteException(this@DirectoryFragment::javaClass.name, "listFiles",e,7)
            return false
        }

        if(files == null)
        {
            showErrorBox("Unknown Error.")
            return false
        }

        currentDir = dir
        items.clear()

        Arrays.sort(files) { lhs:File, rhs:File->
            if(lhs.isDirectory != rhs.isDirectory)
            {
                return@sort if(lhs.isDirectory) -1 else 1
            }
            return@sort lhs.name.compareTo(rhs.name, true)
        }

        for (file in files) {
            if (file.name.startsWith(".")) {
                continue
            }
            if (file.name.endsWith(".jpg", true)
                    || file.name.endsWith(".png", true)
                    || file.name.endsWith(".gif", true)
                    || file.name.endsWith(".jpeg", true))
            {
                continue
            }

            val item = ListItem()
            item.title = file.name
            item.file = file
            if (file.isDirectory) {
                item.icon = R.drawable.ic_directory
                item.subtitle = "Folder"
                item.category = "folder"
            } else {
                var fname = file.name.toLowerCase()
                val sp = fname.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                item.ext = if (sp.size > 1) sp[sp.size - 1] else "?"
                item.subtitle = m_C.convertToFileSize(file.length())
                item.category = "file"
            }
            items.add(item)
        }
        val item = ListItem()
        item.title = "Back"
        item.subtitle = ""
        item.icon = R.drawable.ic_external_storage
        item.file = null
        item.category = "Back"
        items.add(0, item)
        clearDrawableAnimation(recyclerView)
        // scrolling = true;
        recyclerView.adapter.notifyDataSetChanged()
        return true
    }

    private fun clearDrawableAnimation(view:View)
    {
        if (Build.VERSION.SDK_INT < 21 || view == null) {
            return
        }
        var drawable: Drawable? = null
        if (view is ListView) {
            drawable = view.selector
            if (drawable != null) {
                drawable.state = StateSet.NOTHING
            }
        } else {
            drawable = view.background
            if (drawable != null) {
                drawable.state = StateSet.NOTHING
                drawable.jumpToCurrentState()
            }
        }
    }

    fun showErrorBox(error: String) {
        if (activity == null) {
            return
        }
        AlertDialog.Builder(activity)
                .setTitle(activity!!.getString(R.string.app_name))
                .setMessage(error).setPositiveButton(getString(R.string.btn_confirm), null).show()
    }

    private fun listRoots() {
        currentDir = null
        items.clear()
        val extStorage = Environment.getExternalStorageDirectory()
                .absolutePath
        val ext = ListItem()
        if (Build.VERSION.SDK_INT < 9 || Environment.isExternalStorageRemovable()) {
            ext.title = "SdCard"
        } else {
            ext.title = getString(R.string.file_explorer_internal_storage)
        }
        if (Build.VERSION.SDK_INT < 9 || Environment.isExternalStorageRemovable())
        {
            ext.icon = R.drawable.ic_external_storage
            ext.category = "external"
        }
        else
        {
            ext.icon = R.drawable.ic_storage
            ext.category = "root"
        }
        ext.subtitle = getRootSubtitle(extStorage)
        ext.file = Environment.getExternalStorageDirectory()

        items.add(ext)
        try {
            val reader = BufferedReader(FileReader("/proc/mounts"))
            var line: String? = null
            val aliases = HashMap<String, ArrayList<String>>()
            val result = ArrayList<String>()
            var extDevice: String? = null
            while ({line = reader.readLine(); line}() != null) {
                if (!line!!.contains("/mnt")
                        && !line!!.contains("/storage")
                        && !line!!.contains("/sdcard")
                        || line!!.contains("asec")
                        || line!!.contains("tmpfs")
                        || line!!.contains("none")) {
                    continue
                }
                val info = line!!.split(" ")
                if (!aliases.containsKey(info[0]))
                {
                    aliases[info[0]] = ArrayList()
                }
                aliases[info[0]]!!.add(info[1])
                if (info[1] == extStorage) {
                    extDevice = info[0]
                }
                result.add(info[1])
            }
            reader.close()
            if (extDevice != null) {
                result.removeAll(aliases[extDevice] as ArrayList<String>)
                for (path in result) {
                    try {
                        val item = ListItem()
                        if (path.toLowerCase().contains("sd")) {
                            ext.title = "SdCard"
                        } else {
                            ext.title = "ExternalStorage"
                        }
                        item.icon = R.drawable.ic_external_storage
                        item.subtitle = getRootSubtitle(path)
                        item.file = File(path)
                        item.subtitle = "root"
                        items.add(item)
                    } catch (e: Exception) {
                        Log.e("tmessages", e.toString())
                    }

                }
            }
        } catch (e: Exception) {
            Log.e("tmessages", e.toString())
        }

//        val fs = ListItem()
//        fs.title = "/"
//        fs.subtitle = "SystemRoot"
//        fs.icon = R.drawable.ic_directory
//        fs.file = File("/")
//        items.add(fs)

        recyclerView.adapter.notifyDataSetChanged()
    }

    private fun getRootSubtitle(path:String):String
    {
        var stat = StatFs(path)
        var total = stat.blockCountLong * stat.blockSizeLong
        var free = stat.availableBlocksLong * stat.blockSizeLong

        if(total <= 0)
        {
            return ""
        }

        return "Free " + m_C.convertToFileSize(free) + " of " + m_C.convertToFileSize(total)
    }


    override fun clickItem(item: ListItem) {
        var file = item.file

        if(file == null)
        {
            var historyEntry = history.removeAt(history.size - 1)
            strTitle = historyEntry.title
            updateName(strTitle)
            if(historyEntry.dir != null)
            {
                listFiles(historyEntry.dir)
            }
            else
            {
                listRoots()
            }

            recyclerView.smoothScrollToPosition(historyEntry.scrollItem)
        }
        else if(file.isDirectory)
        {
            var strCurPathName = file.absolutePath
            strCurPathName = strCurPathName.substring(strCurPathName.lastIndexOf('/')+1, strCurPathName.length)
            strTitle = strCurPathName
            var historyEntry = HistoryEntry()
            historyEntry.scrollItem = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            historyEntry.scrollOffset = recyclerView.getChildAt(0).top
            historyEntry.dir = currentDir
            historyEntry.title = strTitle
            updateName(strTitle)

            if(!listFiles(file))
            {
                return
            }
            history.add(historyEntry)
            strTitle = item.title
            recyclerView.scrollToPosition(0)
        }
        else
        {
            if(!file.canRead())
            {
                showErrorBox("Access Error.")
                return
            }

            if(file.length() > FILE_EXPLORER_LIMIT)
            {
                showErrorBox("File upload limit excess.")
                return
            }

            chooseFile(file)
        }
    }

    //Create list adapter
    private class ExplorerAdapter(val context: Context, var items:ArrayList<ListItem>):RecyclerView.Adapter<ExplorerAdapter.ViewHolder>(), ViewHolderClickListener
    {
        var mInflater = LayoutInflater.from(context)

        private var callBackClick: ClickItem? = null


        fun setItemClickListener(listener: ClickItem) {
            this.callBackClick = listener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = mInflater.inflate(R.layout.recycle_file_exeplorer_row, parent, false)
            return ViewHolder(view, this)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun getItem(nIdx:Int): ListItem {
            return items[nIdx]
        }

        override fun getItemViewType(position: Int): Int {
            return if(items.get(position).subtitle.isNotEmpty()) 0 else 1
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var item = items.get(position)

            holder.view_textFileName.text = item.title
            holder.view_textDescription.text = item.subtitle


            when(item.category.toUpperCase())
            {
                "FOLDER" -> {
                    holder.view_textExplorerThumb.visibility = View.GONE
                    holder.view_imageExplorerThumb.visibility = View.VISIBLE
                    holder.view_imageExplorerThumb.setImageResource(R.drawable.ic_directory)
                }
                "BACK" -> {
                    holder.view_textExplorerThumb.visibility = View.GONE
                    holder.view_imageExplorerThumb.visibility = View.VISIBLE
                    holder.view_imageExplorerThumb.setImageResource(R.drawable.ic_directory)
                }
                "ROOT" -> {
                    holder.view_textExplorerThumb.visibility = View.GONE
                    holder.view_imageExplorerThumb.visibility = View.VISIBLE
                    holder.view_imageExplorerThumb.setImageResource(R.drawable.ic_storage)
                }
                "EXTERNAL" -> {
                    holder.view_textExplorerThumb.visibility = View.GONE
                    holder.view_imageExplorerThumb.visibility = View.VISIBLE
                    holder.view_imageExplorerThumb.setImageResource(R.drawable.ic_external_storage)
                }
                else -> {
//                     item.file?.apply {
//                        var strMimeExt = MimeTypeMap.getFileExtensionFromUrl(absolutePath)
//                        if (strMimeExt != null) {
//                            var type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toLowerCase()
//
//                            if (type.contains("jpg", true)
//                                    || type.contains("png", true)
//                                    || type.contains("gif", true)
//                                    || type.contains("jpeg", true))
//                            {
//                                var nThumbWidth = holder.view_imageExplorerThumb.measuredWidth
//                                var nThumbHeight = holder.view_imageExplorerThumb.measuredHeight
//                                try
//                                {
//                                    holder.view_textExplorerThumb.visibility = View.GONE
//                                    holder.view_imageExplorerThumb.visibility = View.VISIBLE
//                                    var bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(absolutePath), nThumbWidth, nThumbHeight);
//                                    holder.view_imageExplorerThumb.setImageBitmap(bitmap)
//                                }
//                                catch(e :Exception)
//                                {
//                                    Logger.WriteException(this::javaClass.name, "onBindViewHolder",e,7)
//                                    holder.view_textExplorerThumb.visibility = View.VISIBLE
//                                    holder.view_imageExplorerThumb.visibility = View.GONE
//                                    holder.view_textExplorerThumb.text = item.ext
//                                }
//                            }
//                            else
//                            {
//                                holder.view_textExplorerThumb.visibility = View.VISIBLE
//                                holder.view_imageExplorerThumb.visibility = View.GONE
//                                holder.view_textExplorerThumb.text = item.ext
//                            }
//                        }
//                        else
//                        {
                    holder.view_textExplorerThumb.visibility = View.VISIBLE
                    holder.view_imageExplorerThumb.visibility = View.GONE
                    holder.view_textExplorerThumb.text = item.ext
//                        }
//                    }
                }
            }

            if(item.icon != 0)
            {
                //holder.view_imageExplorerThumb.setImageBitmap()
            }
            else
            {
                holder.view_imageExplorerThumb.visibility = View.GONE
            }
        }

        override fun onTap(index: Int) {
            callBackClick?.clickItem(items[index])
        }

        override fun onLongTap(index: Int) {
            callBackClick?.clickItem(items[index])
        }


        class ViewHolder(itemView:View, val listener: ViewHolderClickListener) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener,View.OnTouchListener {
            var view_imageExplorerThumb:ImageView
            var view_textFileName: TextView
            var view_textDescription:TextView
            var view_textExplorerThumb:TextView
            var view_layoutFileExplorerRow: ConstraintLayout

            init {
                view_imageExplorerThumb   = itemView.findViewById(R.id.view_imageExplorerThumb)
                view_textFileName   = itemView.findViewById(R.id.view_textFileName)
                view_textDescription       = itemView.findViewById(R.id.view_textDescription)
                view_layoutFileExplorerRow             = itemView.findViewById(R.id.view_layoutFileExplorerRow)
                view_textExplorerThumb  = itemView.findViewById(R.id.view_textExplorerThumb)

                view_layoutFileExplorerRow.setOnClickListener(this)
                //   view_layoutFileExplorerRow.setOnLongClickListener(this)
                view_layoutFileExplorerRow.setOnTouchListener(this)
            }

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when(event?.action)
                {
                    MotionEvent.ACTION_DOWN -> {
                        v?.setBackgroundColor(ContextCompat.getColor(v.context, R.color.colorHovered))
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v?.setBackgroundColor(Color.TRANSPARENT)

                    }
                }
                return false
            }

            override fun onClick(v: View?) {
                listener.onTap(adapterPosition)
            }

            override fun onLongClick(v: View?): Boolean {
                //     listener.onLongTap(adapterPosition)

                return true
            }
        }

        override fun onRemove(index: Int) {}

    }
}

interface ClickItem {
    fun clickItem(item: DirectoryFragment.ListItem)
}
