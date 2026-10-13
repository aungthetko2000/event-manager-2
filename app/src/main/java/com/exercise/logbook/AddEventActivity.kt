package com.exercise.logbook

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.exercise.logbook.database.EventDatabaseHelper
import com.exercise.logbook.model.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEventActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etDate: EditText
    private lateinit var etLocation: EditText
    private lateinit var etOrganiser: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSave: Button

    private lateinit var dbHelper: EventDatabaseHelper
    private var editingEventId: Long = -1L // -1 means new event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        dbHelper = EventDatabaseHelper(this)

        etName = findViewById(R.id.etEventName)
        etDate = findViewById(R.id.etEventDate)
        etLocation = findViewById(R.id.etEventLocation)
        etOrganiser = findViewById(R.id.etEventOrganiser)
        spinnerType = findViewById(R.id.spinnerEventType)
        spinnerStatus = findViewById(R.id.spinnerEventStatus)
        btnSave = findViewById(R.id.btnSaveEvent)

        // Populate Spinners
        val eventTypes = arrayOf("Conference", "Workshop", "Seminar", "Social", "Other")
        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, eventTypes)

        val eventStatuses = arrayOf("Upcoming", "Completed", "Cancelled")
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, eventStatuses)

        // Check if we passed an event to edit
        val eventToEdit = intent.getSerializableExtra("EXTRA_EVENT") as? Event
        if (eventToEdit != null) {
            editingEventId = eventToEdit.id
            etName.setText(eventToEdit.name)
            etDate.setText(eventToEdit.date)
            etLocation.setText(eventToEdit.location)
            etOrganiser.setText(eventToEdit.organiser)

            // Set spinner selections
            spinnerType.setSelection(eventTypes.indexOf(eventToEdit.type))
            spinnerStatus.setSelection(eventStatuses.indexOf(eventToEdit.status))
            btnSave.text = "Update Event"
        }

        etDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnSave.setOnClickListener {
            validateAndSaveEvent()
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

    private fun validateAndSaveEvent() {
        if (!validateInputs()) return

        val name = etName.text.toString().trim()
        val dateStr = etDate.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val organiser = etOrganiser.text.toString().trim()
        val type = spinnerType.selectedItem.toString()
        val status = spinnerStatus.selectedItem.toString()

        if (editingEventId == -1L) {
            // Insert new record
            val newEvent = Event(name = name, date = dateStr, location = location, organiser = organiser, type = type, status = status)
            dbHelper.insertEvent(newEvent)
            Toast.makeText(this, "Event added to Database!", Toast.LENGTH_SHORT).show()
        } else {
            // Update existing record
            val updatedEvent = Event(id = editingEventId, name = name, date = dateStr, location = location, organiser = organiser, type = type, status = status)
            dbHelper.updateEvent(updatedEvent)
            Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val name = etName.text.toString().trim()
        val dateStr = etDate.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val organiser = etOrganiser.text.toString().trim()

        if (name.isEmpty()) { etName.error = "Event name is required"; isValid = false }
        if (dateStr.isEmpty()) { etDate.error = "Date is required"; isValid = false }
        if (location.isEmpty()) { etLocation.error = "Location is required"; isValid = false }
        if (organiser.isEmpty()) { etOrganiser.error = "Organiser is required"; isValid = false }

        if (!isValid) { etName.requestFocus(); return false }

        if (!isValidDateFormat(dateStr)) {
            etDate.error = "Invalid date format. Use YYYY-MM-DD"
            etDate.requestFocus()
            return false
        }

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false
            val selectedDate = dateFormat.parse(dateStr)
            val todayCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            if (selectedDate != null && selectedDate.before(todayCalendar.time)) {
                etDate.error = "Events cannot be scheduled in the past!"
                etDate.requestFocus()
                return false
            }
        } catch (e: Exception) {
            etDate.error = "Invalid date format. Use YYYY-MM-DD"
            etDate.requestFocus()
            return false
        }

        return true
    }

    private fun isValidDateFormat(dateStr: String): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.isLenient = false
        return try { format.parse(dateStr) != null } catch (e: Exception) { false }
    }
}