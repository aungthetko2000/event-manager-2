package com.exercise.logbook.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.exercise.logbook.model.Event
import com.exercise.logbook.model.Session

class EventDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "events.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_EVENTS = "events"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DATE = "date"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_ORGANISER = "organiser"
        const val COLUMN_TYPE = "type"
        const val COLUMN_STATUS = "status"

        const val COLUMN_DESCRIPTION = "description"

        const val COLUMN_CONTACT_EMAIL = "contact_email"

        const val COLUMN_CONTACT_PHONE = "contact_phone"


        // Session
        const val TABLE_SESSIONS = "sessions"
        const val COLUMN_SESSION_ID = "session_id"
        const val COLUMN_SESSION_EVENT_ID = "event_id"
        const val COLUMN_SESSION_NAME = "session_name"
        const val COLUMN_SESSION_TIME = "session_time"
        const val COLUMN_SESSION_SPEAKER = "session_speaker"
        const val COLUMN_SESSION_ROOM = "session_room"
        const val COLUMN_SESSION_TYPE = "session_type"
        const val COLUMN_SESSION_DESCRIPTION = "session_description"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_EVENTS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, "
                + "$COLUMN_DATE TEXT, "
                + "$COLUMN_LOCATION TEXT, "
                + "$COLUMN_ORGANISER TEXT, "
                + "$COLUMN_TYPE TEXT, "
                + "$COLUMN_STATUS TEXT, "
                + "$COLUMN_DESCRIPTION TEXT, "
                + "$COLUMN_CONTACT_EMAIL TEXT, "
                + "$COLUMN_CONTACT_PHONE TEXT)")
        db?.execSQL(createTableQuery)

        val createSessionsTableQuery = ("CREATE TABLE $TABLE_SESSIONS ("
                + "$COLUMN_SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_SESSION_EVENT_ID INTEGER, "
                + "$COLUMN_SESSION_NAME TEXT, "
                + "$COLUMN_SESSION_TIME TEXT, "
                + "$COLUMN_SESSION_SPEAKER TEXT, "
                + "$COLUMN_SESSION_ROOM TEXT, "
                + "$COLUMN_SESSION_TYPE TEXT, "
                + "$COLUMN_SESSION_DESCRIPTION TEXT, "
                + "FOREIGN KEY($COLUMN_SESSION_EVENT_ID) REFERENCES $TABLE_EVENTS($COLUMN_ID) ON DELETE CASCADE)")
        db?.execSQL(createSessionsTableQuery)
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        db?.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        onCreate(db)
    }

    fun insertEvent(event: Event): Long {
        val db = this.writableDatabase
        val values = getContentValues(event)
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
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)) ?: ""
                val contactEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_EMAIL)) ?: ""
                val contactPhone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_PHONE)) ?: ""

                eventList.add(Event(id, name, date, location, organiser, type, status, description, contactEmail, contactPhone))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return eventList
    }

    fun updateEvent(event: Event): Int {
        val db = this.writableDatabase
        val values = getContentValues(event)
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

    fun isEventDuplicate(name: String, date: String, excludeId: Long = -1L): Boolean {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_ID FROM $TABLE_EVENTS WHERE LOWER($COLUMN_NAME) = LOWER(?) AND $COLUMN_DATE = ? AND $COLUMN_ID != ?"
        val cursor = db.rawQuery(query, arrayOf(name, date, excludeId.toString()))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    private fun getContentValues(event: Event): ContentValues {
        return ContentValues().apply {
            put(COLUMN_NAME, event.name)
            put(COLUMN_DATE, event.date)
            put(COLUMN_LOCATION, event.location)
            put(COLUMN_ORGANISER, event.organiser)
            put(COLUMN_TYPE, event.type)
            put(COLUMN_STATUS, event.status)
            put(COLUMN_DESCRIPTION, event.description)
            put(COLUMN_CONTACT_EMAIL, event.contactEmail)
            put(COLUMN_CONTACT_PHONE, event.contactPhone)
        }
    }

    fun insertSession(session: Session): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SESSION_EVENT_ID, session.eventId)
            put(COLUMN_SESSION_NAME, session.name)
            put(COLUMN_SESSION_TIME, session.time)
            put(COLUMN_SESSION_SPEAKER, session.speaker)
            put(COLUMN_SESSION_ROOM, session.room)
            put(COLUMN_SESSION_TYPE, session.type)
            put(COLUMN_SESSION_DESCRIPTION, session.description)
        }
        val id = db.insert(TABLE_SESSIONS, null, values)
        db.close()
        return id
    }

    fun getSessionsForEvent(eventId: Long): List<Session> {
        val sessionList = mutableListOf<Session>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_SESSIONS WHERE $COLUMN_SESSION_EVENT_ID = ? ORDER BY $COLUMN_SESSION_ID ASC",
            arrayOf(eventId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SESSION_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_NAME))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_TIME))
                val speaker = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_SPEAKER)) ?: ""
                val room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_ROOM)) ?: ""
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_TYPE)) ?: "Presentation"
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_DESCRIPTION)) ?: ""

                sessionList.add(Session(id, eventId, name, time, speaker, room, type, description))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return sessionList
    }

    fun updateSession(session: Session): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SESSION_NAME, session.name)
            put(COLUMN_SESSION_TIME, session.time)
            put(COLUMN_SESSION_SPEAKER, session.speaker)
            put(COLUMN_SESSION_ROOM, session.room)
            put(COLUMN_SESSION_TYPE, session.type)
            put(COLUMN_SESSION_DESCRIPTION, session.description)
        }
        val rows = db.update(TABLE_SESSIONS, values, "$COLUMN_SESSION_ID = ?", arrayOf(session.id.toString()))
        db.close()
        return rows
    }

    fun deleteSession(sessionId: Long): Int {
        val db = this.writableDatabase
        val rows = db.delete(TABLE_SESSIONS, "$COLUMN_SESSION_ID = ?", arrayOf(sessionId.toString()))
        db.close()
        return rows
    }

    fun searchEvents(query: String): List<Event> {
        val eventList = mutableListOf<Event>()
        val db = this.readableDatabase
        val searchQuery = "%$query%"
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_EVENTS WHERE $COLUMN_NAME LIKE ? OR $COLUMN_DESCRIPTION LIKE ? ORDER BY $COLUMN_DATE ASC",
            arrayOf(searchQuery, searchQuery)
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION))
                val organiser = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANISER))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
                val status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)) ?: ""
                val contactEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_EMAIL)) ?: ""
                val contactPhone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_PHONE)) ?: ""

                eventList.add(Event(id, name, date, location, organiser, type, status, description, contactEmail, contactPhone))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return eventList
    }

    fun searchSessionsForEvent(eventId: Long, query: String): List<Session> {
        val sessionList = mutableListOf<Session>()
        val db = this.readableDatabase
        val searchQuery = "%$query%"
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_SESSIONS WHERE $COLUMN_SESSION_EVENT_ID = ? AND ($COLUMN_SESSION_NAME LIKE ? OR $COLUMN_SESSION_DESCRIPTION LIKE ?) ORDER BY $COLUMN_SESSION_ID ASC",
            arrayOf(eventId.toString(), searchQuery, searchQuery)
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SESSION_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_NAME))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_TIME))
                val speaker = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_SPEAKER)) ?: ""
                val room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_ROOM)) ?: ""
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_TYPE)) ?: "Presentation"
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_DESCRIPTION)) ?: ""

                sessionList.add(Session(id, eventId, name, time, speaker, room, type, description))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return sessionList
    }

    fun getSessionCountForEvent(eventId: Long): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_SESSIONS WHERE $COLUMN_SESSION_EVENT_ID = ?", arrayOf(eventId.toString()))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }
}