package com.exercise.logbook.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.exercise.logbook.model.Event

class EventDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "events.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_EVENTS = "events"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DATE = "date"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_ORGANISER = "organiser"
        const val COLUMN_TYPE = "type"
        const val COLUMN_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_EVENTS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, "
                + "$COLUMN_DATE TEXT, "
                + "$COLUMN_LOCATION TEXT, "
                + "$COLUMN_ORGANISER TEXT, "
                + "$COLUMN_TYPE TEXT, "
                + "$COLUMN_STATUS TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        onCreate(db)
    }

    fun insertEvent(event: Event): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, event.name)
            put(COLUMN_DATE, event.date)
            put(COLUMN_LOCATION, event.location)
            put(COLUMN_ORGANISER, event.organiser)
            put(COLUMN_TYPE, event.type)
            put(COLUMN_STATUS, event.status)
        }
        val id = db.insert(TABLE_EVENTS, null, values)
        db.close()
        return id
    }

    fun getAllEvents(): List<Event> {
        val eventList = mutableListOf<Event>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EVENTS ORDER BY $COLUMN_DATE ASC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION))
                val organiser = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANISER))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
                val status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))

                eventList.add(Event(id, name, date, location, organiser, type, status))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return eventList
    }

    fun updateEvent(event: Event): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, event.name)
            put(COLUMN_DATE, event.date)
            put(COLUMN_LOCATION, event.location)
            put(COLUMN_ORGANISER, event.organiser)
            put(COLUMN_TYPE, event.type)
            put(COLUMN_STATUS, event.status)
        }
        val rows = db.update(TABLE_EVENTS, values, "$COLUMN_ID = ?", arrayOf(event.id.toString()))
        db.close()
        return rows
    }

    fun deleteEvent(id: Long): Int {
        val db = this.writableDatabase
        val rows = db.delete(TABLE_EVENTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }
}