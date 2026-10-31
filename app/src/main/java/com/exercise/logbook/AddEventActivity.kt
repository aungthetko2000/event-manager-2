package com.exercise.logbook

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.exercise.logbook.database.EventDatabaseHelper
import com.exercise.logbook.model.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import com.google.android.gms.location.LocationServices

class AddEventActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etDate: EditText
    private lateinit var etLocation: EditText
    private lateinit var etOrganiser: EditText

    private lateinit var etDescription: EditText

    private lateinit var etContactEmail: EditText

    private lateinit var etContactPhone: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSave: Button

    private lateinit var locationEditText: EditText
    private val locationPermissionRequestCode = 1001

    private lateinit var dbHelper: EventDatabaseHelper
    private var editingEventId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        dbHelper = EventDatabaseHelper(this)

        etName = findViewById(R.id.etEventName)
        etDate = findViewById(R.id.etEventDate)
        etLocation = findViewById(R.id.etEventLocation)
        etOrganiser = findViewById(R.id.etEventOrganiser)
        etDescription = findViewById(R.id.etEventDescription)
        etContactEmail = findViewById(R.id.etContactEmail)
        etContactPhone = findViewById(R.id.etContactPhone)
        spinnerType = findViewById(R.id.spinnerEventType)
        spinnerStatus = findViewById(R.id.spinnerEventStatus)
        btnSave = findViewById(R.id.btnSaveEvent)

        val eventTypes = arrayOf("Conference", "Workshop", "Seminar", "Social", "Other")

        val typeAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            eventTypes
        )
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        val eventStatuses = arrayOf("Upcoming", "Completed", "Cancelled")

        val statusAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            eventStatuses
        )
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        val eventToEdit = intent.getSerializableExtra("EXTRA_EVENT") as? Event
        if (eventToEdit != null) {
            editingEventId = eventToEdit.id
            etName.setText(eventToEdit.name)
            etDate.setText(eventToEdit.date)
            etLocation.setText(eventToEdit.location)
            etOrganiser.setText(eventToEdit.organiser)
            etDescription.setText(eventToEdit.description)
            etContactEmail.setText(eventToEdit.contactEmail)
            etContactPhone.setText(eventToEdit.contactPhone)

            spinnerType.setSelection(eventTypes.indexOf(eventToEdit.type))
            spinnerStatus.setSelection(eventStatuses.indexOf(eventToEdit.status))
            btnSave.text = "Review & Update Event"
        }

        etDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnSave.setOnClickListener {
            validateAndShowConfirmation()
        }
    }

    private fun fetchCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionRequestCode
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val addressString = "${address.getAddressLine(0)}"
                        locationEditText.setText(addressString)
                    } else {
                        locationEditText.setText("$latitude, $longitude")
                    }
                } catch (e: Exception) {
                    locationEditText.setText("$latitude, $longitude")
                }
            } else {
                locationEditText.setText("GPS location unavailable")
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    Locale.getDefault(),
                    "%d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
                etDate.setText(formattedDate)
                etDate.error = null
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun validateAndShowConfirmation() {
        val name = etName.text.toString().trim()
        val dateStr = etDate.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val organiser = etOrganiser.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val email = etContactEmail.text.toString().trim()
        val phone = etContactPhone.text.toString().trim()
        val type = spinnerType.selectedItem.toString()
        val status = spinnerStatus.selectedItem.toString()

        if (name.isEmpty()) { etName.error = "Event name is required"; etName.requestFocus(); return }
        if (dateStr.isEmpty()) { etDate.error = "Date is required"; etDate.requestFocus(); return }
        if (location.isEmpty()) { etLocation.error = "Location is required"; etLocation.requestFocus(); return }
        if (organiser.isEmpty()) { etOrganiser.error = "Organiser is required"; etOrganiser.requestFocus(); return }

        if (!isValidDateFormat(dateStr)) {
            etDate.error = "Invalid format. Use DD/MM/YYYY"
            etDate.requestFocus()
            return
        }

        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etContactEmail.error = "Invalid email format"
            etContactEmail.requestFocus()
            return
        }

        if (phone.isNotEmpty() && !Patterns.PHONE.matcher(phone).matches()) {
            etContactPhone.error = "Invalid phone format"
            etContactPhone.requestFocus()
            return
        }

        if (dbHelper.isEventDuplicate(name, dateStr, editingEventId)) {
            etName.error = "An event with this name and date already exists!"
            etName.requestFocus()
            Toast.makeText(this, "Duplicate event detected!", Toast.LENGTH_SHORT).show()
            return
        }

        val confirmationMessage = buildString {
            append("Name: $name\n")
            append("Date: $dateStr\n")
            append("Location: $location\n")
            append("Organiser: $organiser\n")
            append("Type: $type | Status: $status\n")
            if (description.isNotEmpty()) append("Description: $description\n")
            if (email.isNotEmpty()) append("Email: $email\n")
            if (phone.isNotEmpty()) append("Phone: $phone\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Event Details")
            .setMessage(confirmationMessage)
            .setPositiveButton("Confirm & Save") { _, _ ->
                saveEventToDatabase(name, dateStr, location, organiser, type, status, description, email, phone)
            }
            .setNegativeButton("Review & Edit", null)
            .show()
    }

    private fun saveEventToDatabase(
        name: String, date: String, location: String, organiser: String,
        type: String, status: String, description: String, email: String, phone: String
    ) {
        val event = Event(
            id = if (editingEventId == -1L) 0 else editingEventId,
            name = name,
            date = date,
            location = location,
            organiser = organiser,
            type = type,
            status = status,
            description = description,
            contactEmail = email,
            contactPhone = phone
        )

        if (editingEventId == -1L) {
            dbHelper.insertEvent(event)
            Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.updateEvent(event)
            Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun isValidDateFormat(dateStr: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.isLenient = false
        return try { format.parse(dateStr) != null } catch (e: Exception) { false }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}