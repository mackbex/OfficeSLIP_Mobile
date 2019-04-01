package com.officeslip.Socket

import com.officeslip.*
import com.officeslip.Util.Logger
import org.jdom2.input.SAXBuilder
import org.xml.sax.InputSource
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import android.system.Os.socket
import java.util.*


class SocketConnection
{
    companion object {
        const val QUERY = 3001
        const val UPLOAD = 3002
        const val DOWNLOAD = 3003
    }
    private val socket:Socket = Socket()


    fun destroy() {
        socket?.close()
    }
    fun run(vararg params: Any?): Any? {

        var receiveParams: SocketParams? = null
        var strResMsg:Any? = null
        if(params[0] is SocketParams)
        {
            receiveParams = params[0] as SocketParams
        }
        else if(params[0] is String)
        {
            receiveParams = SocketParams(QUERY, params[0] as String)
        }
        else
        {
            Logger.WriteLog(this.javaClass.name,"doInBackground", "received parameter is invalid.", 7)
            return strResMsg
        }

        var IP = CONNECTION_PRD_IP
        var PORT = CONNECTION_PRD_PORT
        if(g_SysInfo.ServerMode == MODE_DEV)
        {
            IP = CONNECTION_DEV_IP
            PORT = CONNECTION_DEV_PORT
        }

        when(receiveParams?.nType)
        {
            QUERY -> {
                //  var strInputMsg:String? = null
                (receiveParams.streamValue as String)?.run {

                    try {

                        socket.connect(InetSocketAddress(IP, PORT), CONNECTION_TIMEOUT)

                        if (socket.isConnected) {

                            //socket?.use {
                            var bMsg = this.toByteArray(kotlin.text.charset(CONNECTION_CHARSET))
                            bMsg?.run {
                                socket.getOutputStream()?.use {
                                    it.write(bMsg)
                                    it.flush()
                                    Scanner(InputStreamReader(socket.getInputStream(), CONNECTION_CHARSET))?.use {
                                        it.useDelimiter("\\A")
                                        strResMsg = if(it.hasNext()) it.next() else ""
                                        strResMsg = (strResMsg as String)?.substring(14, (strResMsg as String)!!.length)
                                    }
                                }
                            }

                            //Write socket returns to logfile.
                            Logger.WriteLog(this@SocketConnection.javaClass.name,"doInBackground", (strResMsg as String)!!, 3)
                        }
                        //if socket not connected
                        else {
                            strResMsg = null
                            Logger.WriteLog(this@SocketConnection.javaClass.name,"doInBackground", "Socket Connection error.", 3)
                        }
                    } catch (e: Exception) {
                        strResMsg = null
                        Logger.WriteException(this@SocketConnection.javaClass.name,"doInBackground", e, 9)
                    } finally {
                        socket.close()
                    }
                }
            }
            UPLOAD ->{
                try
                {
                    var byteArrStreamValue = receiveParams.streamValue as ByteArray
                   // var fileItem = receiveParams.fileItem


                    byteArrStreamValue?.run {
                        socket.connect(InetSocketAddress(IP, PORT), CONNECTION_TIMEOUT)
                        if(socket.isConnected)
                        {
                            //Write to socket
                            BufferedOutputStream(socket.getOutputStream())?.use {

                                var socketOutputSream = it

                                socketOutputSream.write(byteArrStreamValue)

                                socketOutputSream.write(receiveParams.fileItem as ByteArray)
                                socketOutputSream.flush()

//                                BufferedInputStream(FileInputStream(fileItem?.absolutePath))?.use {
//                                    var ch = 0
//                                    while({ ch = it.read(); ch }() > -1)
//                                    {
//                                        socketOutputSream.write(ch)
//                                    }
//
//                                    socketOutputSream.flush()
//                                }

                                //Receive output res from socket
                                BufferedInputStream(socket.getInputStream())?.use {

                                    var byteRes = ByteArray(2)
                                    it.read(byteRes)
                                    var strRes = String(byteRes)

                                    if(strRes[1] != 'T')
                                    {
                                        strResMsg = "F"
                                    }
                                    else
                                    {
                                        strResMsg = "T"
                                    }
                                }
                            }
                        }
                    }
                }
                catch (e: Exception)
                {
                    strResMsg = null
                    Logger.WriteException(this@SocketConnection.javaClass.name,"doInBackground", e, 9)
                }
                finally
                {
                    socket.close()
                }
            }
            DOWNLOAD -> {
                try
                {

                    var byteArrStreamValue = receiveParams.streamValue as ByteArray

                    byteArrStreamValue?.run {
                        socket.connect(InetSocketAddress(IP, PORT), CONNECTION_TIMEOUT)
                        if(socket.isConnected)
                        {
                            //Write to socket
                            BufferedOutputStream(socket.getOutputStream())?.use {

                                var socketOutputSream = it
                                socketOutputSream.write(byteArrStreamValue)
                                socketOutputSream.flush()

                                //Receive output res from socket
                                BufferedInputStream(socket.getInputStream())?.use {

                                    //Receive result flag
                                    var byteRes = ByteArray(2)
                                    it.read(byteRes)
                                    var strRes = String(byteRes)

                                    if(strRes[1] != 'T')
                                    {
                                        strResMsg = "F"
                                    }
                                    else
                                    {
                                        strResMsg = "T"
                                    }

                                    if("F" == strResMsg)
                                    {
                                        return null
                                    }
                                    //Receive BufferLength
                                    byteRes = ByteArray(12)
                                    it.read(byteRes)

                                    //Receive ByteArray
                                    var byteLength 	= String(byteRes).toInt()
                                    byteRes = ByteArray(byteLength)
                                    it.read(byteRes)

                                    var content = String(byteRes)


                                    StringReader(content)?.run {
                                        SAXBuilder().build(InputSource(this))?.run {
                                            var elRoot = rootElement

                                            var lFileSize = elRoot.getAttribute("FileSize").longValue
                                            var strFileName = elRoot.getChild("Row").getChildText("FILENAME")

                                            Logger.WriteLog(this@SocketConnection.javaClass.name,"FileName : ", lFileSize.toString(), 3)
                                            Logger.WriteLog(this@SocketConnection.javaClass.name,"FileSize : ", strFileName, 3)

                                            strResMsg = ByteArray(lFileSize.toInt())
                                            var bTemp = ByteArray(1024)
                                            var nReadLength = 0
                                            var nPos = 0

                                            while({ nReadLength = it.read(bTemp); nReadLength }() != -1)
                                            {
                                                System.arraycopy(bTemp, 0, strResMsg, nPos, nReadLength )
                                                nPos += nReadLength
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch (e: Exception)
                {
                    strResMsg = null
                    Logger.WriteException(this@SocketConnection.javaClass.name,"doInBackground", e, 9)
                }
                finally
                {
                    socket.close()
                }
            }
            else -> {
                Logger.WriteLog(this.javaClass.name,"doInBackground", "invalid operation", 7)
                return null
            }

        }



        return strResMsg
    }

}

class SocketParams {
    var nType:Int               = 0
    var streamValue:Any?        = null
    var fileItem:ByteArray?     = null

    constructor(nType:Int, streamValue:Any?, fileItem:ByteArray? = null)
    {
        this.nType          = nType
        this.streamValue    = streamValue
        this.fileItem       = fileItem

    }
}