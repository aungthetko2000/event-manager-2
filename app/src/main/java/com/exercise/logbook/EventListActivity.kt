package com.exercise.logbook

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
    private lateinit var dbHelper: EventDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        dbHelper = EventDatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewEvents)
        fabAddEvent = findViewById(R.id.fabAddEvent)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAddEvent.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
    }

    private fun loadEvents() {
        val eventList = dbHelper.getAllEvents()
        val adapter = EventAdapter(
            events = eventList,
            onEditClick = { event ->
                val intent = Intent(this, AddEventActivity::class.java).apply {
                    putExtra("EXTRA_EVENT", event)
                }
                startActivity(intent)
            },
            onDeleteClick = { event ->
                dbHelper.deleteEvent(event.id)
                loadEvents()
                Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter
    }
}