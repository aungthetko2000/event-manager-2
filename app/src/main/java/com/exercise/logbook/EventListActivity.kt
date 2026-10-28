package com.exercise.logbook

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exercise.logbook.adapter.EventAdapter
import com.exercise.logbook.database.EventDatabaseHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EventListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddEvent: FloatingActionButton
    private lateinit var etSearchEvents: EditText
    private lateinit var dbHelper: EventDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        dbHelper = EventDatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewEvents)
        fabAddEvent = findViewById(R.id.fabAddEvent)
        etSearchEvents = findViewById(R.id.etSearchEvents)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAddEvent.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }

        etSearchEvents.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                loadEvents(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val currentQuery = etSearchEvents.text.toString().trim()
        loadEvents(currentQuery)
    }

    private fun loadEvents(query: String = "") {
        val eventList = if (query.isEmpty()) {
            dbHelper.getAllEvents()
        } else {
            dbHelper.searchEvents(query)
        }

        val adapter = EventAdapter(
            events = eventList,
            onItemClick = { event ->
                val intent = Intent(this, EventDetailActivity::class.java).apply {
                    putExtra("EXTRA_EVENT", event)
                }
                startActivity(intent)
            },
            onEditClick = { event ->
                val intent = Intent(this, AddEventActivity::class.java).apply {
                    putExtra("EXTRA_EVENT", event)
                }
                startActivity(intent)
            },
            onDeleteClick = { event ->
                dbHelper.deleteEvent(event.id) // Cascade delete automatically wipes its sessions!
                loadEvents(etSearchEvents.text.toString().trim())
                Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter
    }
}