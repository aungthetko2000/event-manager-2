package com.exercise.logbook.session

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.exercise.logbook.R
import com.exercise.logbook.database.EventDatabaseHelper
import com.exercise.logbook.model.Session

class AddSessionActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etTime: EditText
    private lateinit var etSpeaker: EditText
    private lateinit var etRoom: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var btnSave: Button

    private lateinit var dbHelper: EventDatabaseHelper
    private var eventId: Long = -1L
    private var editingSessionId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_session)

        dbHelper = EventDatabaseHelper(this)
        eventId = intent.getLongExtra("EXTRA_EVENT_ID", -1L)

        etName = findViewById(R.id.etSessionName)
        etTime = findViewById(R.id.etSessionTime)
        etSpeaker = findViewById(R.id.etSessionSpeaker)
        etRoom = findViewById(R.id.etSessionRoom)
        etDescription = findViewById(R.id.etSessionDescription)
        spinnerType = findViewById(R.id.spinnerSessionType)
        btnSave = findViewById(R.id.btnSaveSession)

        val sessionTypes = arrayOf("Workshop", "Presentation", "Panel Discussion", "Networking", "Keynote", "Others")
        val typeAdapter = ArrayAdapter(this, R.layout.spinner_item, sessionTypes)
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        val sessionToEdit = intent.getSerializableExtra("EXTRA_SESSION") as? Session
        if (sessionToEdit != null) {
            editingSessionId = sessionToEdit.id
            eventId = sessionToEdit.eventId
            etName.setText(sessionToEdit.name)
            etTime.setText(sessionToEdit.time)
            etSpeaker.setText(sessionToEdit.speaker)
            etRoom.setText(sessionToEdit.room)
            etDescription.setText(sessionToEdit.description)
            spinnerType.setSelection(sessionTypes.indexOf(sessionToEdit.type))
            btnSave.text = "Update Session"
        }

        btnSave.setOnClickListener { saveSession() }
    }

    private fun saveSession() {
        val name = etName.text.toString().trim()
        val time = etTime.text.toString().trim()
        val speaker = etSpeaker.text.toString().trim()
        val room = etRoom.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val type = spinnerType.selectedItem.toString()

        if (name.isEmpty()) { etName.error = "Session name is required"; etName.requestFocus(); return }
        if (time.isEmpty()) { etTime.error = "Session time is required"; etTime.requestFocus(); return }

        val session = Session(
            id = if (editingSessionId == -1L) 0 else editingSessionId,
            eventId = eventId,
            name = name,
            time = time,
            speaker = speaker,
            room = room,
            type = type,
            description = description
        )

        if (editingSessionId == -1L) {
            dbHelper.insertSession(session)
            Toast.makeText(this, "Session added successfully!", Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.updateSession(session)
            Toast.makeText(this, "Session updated successfully!", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}