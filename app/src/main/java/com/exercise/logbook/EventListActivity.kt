package com.exercise.logbook

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exercise.logbook.adapter.EventAdapter
import com.exercise.logbook.database.EventDatabaseHelper
import com.exercise.logbook.model.Event
import com.exercise.logbook.utils.CloudSyncHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EventListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddEvent: FloatingActionButton
    private lateinit var etSearchEvents: EditText
    private lateinit var tvSyncStats: TextView
    private lateinit var btnSyncNow: Button
    private lateinit var dbHelper: EventDatabaseHelper

    private lateinit var syncProgressBar: com.google.android.material.progressindicator.LinearProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        syncProgressBar = findViewById(R.id.syncProgressBar)

        dbHelper = EventDatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewEvents)
        fabAddEvent = findViewById(R.id.fabAddEvent)
        etSearchEvents = findViewById(R.id.etSearchEvents)
        tvSyncStats = findViewById(R.id.tvSyncStats)
        btnSyncNow = findViewById(R.id.btnSyncNow)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAddEvent.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }

        val btnAdvancedSearch = findViewById<Button>(R.id.btnAdvancedSearch)
        btnAdvancedSearch.setOnClickListener {
            showAdvancedSearchDialog()
        }

        val phpEndpointUrl = "http://192.168.1.37:8000/upload_events.php"

        val syncAction = {
            val allEvents = dbHelper.getUnsyncedEvents()
            if (allEvents.isEmpty()) {
                Toast.makeText(this, "No events available to upload.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Checking network and syncing...", Toast.LENGTH_SHORT).show()

                CloudSyncHelper.uploadAllEvents(this, allEvents, phpEndpointUrl) { success, message ->
                    if (success) {
                        val eventIds = allEvents.map { it.id }
                        dbHelper.markEventsAsSynced(eventIds)
                    }
                    runOnUiThread {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        updateSyncHeader()
                        loadEvents()
                    }
                }
            }
        }

        btnSyncNow.setOnClickListener { syncAction() }

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
        updateSyncHeader()
        val currentQuery = etSearchEvents.text.toString().trim()
        loadEvents(currentQuery)
    }

    private fun updateSyncHeader() {
        val stats = dbHelper.getSyncStats()
        val total = stats.first
        val synced = stats.second
        val pending = stats.third

        tvSyncStats.text = "Total: $total | Synced: $synced | Pending: $pending"

        val syncPercentage = if (total > 0) {
            ((synced.toFloat() / total.toFloat()) * 100).toInt()
        } else {
            0
        }

        syncProgressBar.progress = syncPercentage
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
                dbHelper.deleteEvent(event.id)
                updateSyncHeader()
                loadEvents(etSearchEvents.text.toString().trim())
                Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter
    }

    private fun showAdvancedSearchDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Advanced Event Search")

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputKeyword = EditText(this).apply { hint = "Keyword (Name / Description)" }
        val inputDate = EditText(this).apply { hint = "Date (e.g., 2026-11-10)" }
        val inputOrganiser = EditText(this).apply { hint = "Organiser name" }

        layout.addView(inputKeyword)
        layout.addView(inputDate)
        layout.addView(inputOrganiser)
        builder.setView(layout)

        builder.setPositiveButton("Search") { _, _ ->
            val keyword = inputKeyword.text.toString().trim()
            val date = inputDate.text.toString().trim()
            val organiser = inputOrganiser.text.toString().trim()

            val filteredEvents = dbHelper.searchEventsAdvanced(keyword, date, organiser)
            loadEventsCustomList(filteredEvents)
            Toast.makeText(this, "Found ${filteredEvents.size} events", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun loadEventsCustomList(eventList: List<Event>) {
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
                dbHelper.deleteEvent(event.id)
                updateSyncHeader()
                loadEventsCustomList(dbHelper.searchEventsAdvanced("", "", ""))
                Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter
    }
}