package com.officeslip.Util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.officeslip.SQLITE_DB_DELETE_TABLE
import com.officeslip.SQLITE_DB_NAME
import com.officeslip.SQLITE_DB_VERSION
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SQLite : SQLiteOpenHelper
{
    private val m_sqlLiteDB:SQLiteDatabase
    private val m_context:Context
    private val m_C = Common()

    constructor(context: Context) : super(context, SQLITE_DB_NAME, null, SQLITE_DB_VERSION)
    {
        this.m_context = context
        this.m_sqlLiteDB = this.writableDatabase
    }

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, nOldVersion: Int, nNewVersion: Int) { //db!!.execSQL(SQLITE_DB_DELETE_TABLE)
        db!!.execSQL(SQLITE_DB_DELETE_TABLE)
    }
    fun closeDB() {
        this.m_sqlLiteDB.close()
    }

    fun openDB(strQuery:String):Boolean {

        var bRes = false
        try {
            this.m_sqlLiteDB.execSQL(strQuery)
            bRes = true
        }
        catch (e:Exception)
        {
            Logger.WriteException(this.javaClass.name, "openDB", e, 7)
        }

        return bRes
    }

    fun instantDrop(strQuery:String):Boolean {
        var isSuccess:Boolean = false

        this.m_sqlLiteDB?.run {
            try {
                this.beginTransaction()
                execSQL(strQuery, null)
                isSuccess = true
                this.setTransactionSuccessful()

            } catch (e: Exception) {
                Logger.WriteException(this@SQLite.javaClass.name, "SetRecord", e, 7)
                isSuccess = false
            }
            finally {
                m_sqlLiteDB.endTransaction()
                m_sqlLiteDB.close()
            }
        }
        return isSuccess
    }

    fun instantSetRecord(strTable:String, strQuery: String, strArgs: Array<String>):Boolean {

        var isSuccess:Boolean = false

        if(openDB(strTable)) this.m_sqlLiteDB?.run {
            try {
                this.beginTransaction()
                execSQL(strQuery, strArgs)
                isSuccess = true
                this.setTransactionSuccessful()

            } catch (e: Exception) {
                Logger.WriteException(this@SQLite.javaClass.name, "SetRecord", e, 7)
                isSuccess = false
            }
            finally {
                m_sqlLiteDB.endTransaction()
                m_sqlLiteDB.close()
            }
        }
        return isSuccess
    }

    fun setRecord(strQuery: String, strArgs: Array<String>?):Boolean {

        var isSuccess:Boolean = false

        this.m_sqlLiteDB?.run {
            try {
                this.beginTransaction()
                if(strArgs == null || strArgs.size <= 0)
                {
                    execSQL(strQuery)
                }
                else
                {
                    execSQL(strQuery, strArgs)
                }

                isSuccess = true
                this.setTransactionSuccessful()
            }
            catch (e: Exception) {
                Logger.WriteException(this@SQLite.javaClass.name, "SetRecord", e, 7)
                isSuccess = false
            }
            finally {
                m_sqlLiteDB.endTransaction()
                m_sqlLiteDB.close()
            }
        }
        return isSuccess
    }

    fun instantGetRecord(strTable:String, strQuery:String, strArgs:Array<String>?):List<Any>? {

        var arRes:MutableList<Any>? = null//ArrayList()

        if(openDB(strTable)) this.m_sqlLiteDB?.run {

            rawQuery(strQuery, strArgs)?.use {
                arRes = ArrayList()
                it.moveToFirst()
                while (!it.isAfterLast) {
                    var mapRes: HashMap<String, String> = HashMap()

                    for (i in 0 until it.columnCount) {
                        var strCol = it.getColumnName(i)
                        var strVal = it.getString(i)
                        mapRes.put(strCol, strVal)
                    }
                    arRes?.add(mapRes)

                    it.moveToNext()
                }
            }
            this.close()
        }


        return arRes
    }

    // Remove all data stored in SQLite
    fun removeAll():Boolean {
        var bRes = false

        try {
            val c = m_sqlLiteDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ", null)
            c.moveToFirst()
            val tables = ArrayList<String>()
            while (c.moveToNext()) {
                tables.add(c.getString(0))
            }

            for (table in tables) {
                val dropQuery = "DROP TABLE IF EXISTS $table"
                m_sqlLiteDB.execSQL(dropQuery)
            }

            bRes = true
        }
        catch (e : Exception)
        {
            Logger.WriteException(this@SQLite.javaClass.name, "resetAll", e, 7)
        }
        finally {
            m_sqlLiteDB.close()
        }

        return bRes
    }

    fun getRecord(strQuery:String, strArgs:Array<String>):List<Any>? {

        var arRes:MutableList<Any> = ArrayList()
      // var mapRes:HashMap<String, String> = HashMap()

        this.m_sqlLiteDB?.run {
            rawQuery(strQuery, strArgs)?.let {

                it.moveToFirst()
                while(!it.isAfterLast)
                {
                    var mapRes:HashMap<String, String> = HashMap()

                    for(i in 0 until it.columnCount)
                    {
                        var strCol = it.getColumnName(i)
                        var strVal = it.getString(i)
                        mapRes.put(strCol, strVal)
                    }
                    arRes.add(mapRes)

                    it.moveToNext()
                }
            }
        }

        return arRes
    }


}