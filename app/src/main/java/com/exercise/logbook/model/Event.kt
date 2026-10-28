package com.exercise.logbook.model

import java.io.Serializable
data class Event(
    val id: Long = 0,
    val name: String,
    val date: String,
    val location: String,
    val organiser: String,
    val type: String,
    val status: String = "Upcoming",
    val description: String = "",
    val contactEmail: String = "",
    val contactPhone: String = ""
) : Serializable