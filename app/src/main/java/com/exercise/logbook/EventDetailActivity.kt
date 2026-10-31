package com.exercise.logbook

import android.content.Intent
import android.location.Geocoder
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

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

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

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

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

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        var eventLatitude = 13.7563
        var eventLongitude = 100.5018

        val locationStr = currentEvent.location
        if (locationStr.isNotEmpty()) {
            try {
                if (locationStr.contains(",") && locationStr.split(",").size == 2) {
                    val parts = locationStr.split(",")
                    eventLatitude = parts[0].trim().toDouble()
                    eventLongitude = parts[1].trim().toDouble()
                } else {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocationName(locationStr, 1)
                    if (!addresses.isNullOrEmpty()) {
                        eventLatitude = addresses[0].latitude
                        eventLongitude = addresses[0].longitude
                    }
                }
            } catch (e: Exception) {

            }
        }

        val eventPoint = GeoPoint(eventLatitude, eventLongitude)

        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(eventPoint)

        val marker = Marker(mapView).apply {
            position = eventPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = currentEvent.name
            snippet = currentEvent.location
        }
        mapView.overlays.add(marker)
        mapView.invalidate()

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