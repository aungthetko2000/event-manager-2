package com.exercise.logbook

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exercise.logbook.adapter.SessionAdapter
import com.exercise.logbook.database.EventDatabaseHelper
import com.exercise.logbook.model.Event
import com.exercise.logbook.session.AddSessionActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EventDetailActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvDateTimeLocation: TextView
    private lateinit var tvOrganiser: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvContact: TextView
    private lateinit var tvSessionCount: TextView
    private lateinit var recyclerViewSessions: RecyclerView
    private lateinit var fabAddSession: FloatingActionButton
    private lateinit var etSearchSessions: EditText

    private lateinit var dbHelper: EventDatabaseHelper
    private lateinit var currentEvent: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        dbHelper = EventDatabaseHelper(this)
        currentEvent = intent.getSerializableExtra("EXTRA_EVENT") as Event

        tvName = findViewById(R.id.tvDetailEventName)
        tvDateTimeLocation = findViewById(R.id.tvDetailDateTimeLocation)
        tvOrganiser = findViewById(R.id.tvDetailOrganiser)
        tvDescription = findViewById(R.id.tvDetailDescription)
        tvContact = findViewById(R.id.tvDetailContact)
        tvSessionCount = findViewById(R.id.tvSessionCount)
        recyclerViewSessions = findViewById(R.id.recyclerViewSessions)
        fabAddSession = findViewById(R.id.fabAddSession)
        etSearchSessions = findViewById(R.id.etSearchSessions)

        recyclerViewSessions.layoutManager = LinearLayoutManager(this)

        populateEventInfo()

        fabAddSession.setOnClickListener {
            val intent = Intent(this, AddSessionActivity::class.java).apply {
                putExtra("EXTRA_EVENT_ID", currentEvent.id)
            }
            startActivity(intent)
        }

        etSearchSessions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                loadSessions(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val currentQuery = etSearchSessions.text.toString().trim()
        loadSessions(currentQuery)
    }

    private fun populateEventInfo() {
        tvName.text = currentEvent.name
        tvDateTimeLocation.text = "${currentEvent.date} | ${currentEvent.location}"
        tvOrganiser.text = "Organiser: ${currentEvent.organiser} (${currentEvent.type} - ${currentEvent.status})"

        if (currentEvent.description.isNotEmpty()) {
            tvDescription.visibility = View.VISIBLE
            tvDescription.text = currentEvent.description
        } else {
            tvDescription.visibility = View.GONE
        }

        val contactInfo = buildString {
            if (currentEvent.contactEmail.isNotEmpty()) append("Email: ${currentEvent.contactEmail}  ")
            if (currentEvent.contactPhone.isNotEmpty()) append("Phone: ${currentEvent.contactPhone}")
        }
        if (contactInfo.isNotEmpty()) {
            tvContact.visibility = View.VISIBLE
            tvContact.text = contactInfo
        } else {
            tvContact.visibility = View.GONE
        }
    }

    private fun loadSessions(query: String = "") {
        val sessions = if (query.isEmpty()) {
            dbHelper.getSessionsForEvent(currentEvent.id)
        } else {
            dbHelper.searchSessionsForEvent(currentEvent.id, query)
        }

        val sessionCount = sessions.size
        tvSessionCount.text = "Total Sessions: $sessionCount"

        val adapter = SessionAdapter(
            sessions = sessions,
            onEditClick = { session ->
                val intent = Intent(this, AddSessionActivity::class.java).apply {
                    putExtra("EXTRA_SESSION", session)
                }
                startActivity(intent)
            },
            onDeleteClick = { session ->
                dbHelper.deleteSession(session.id)
                loadSessions(etSearchSessions.text.toString().trim())
                Toast.makeText(this, "Session deleted", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerViewSessions.adapter = adapter
    }
}