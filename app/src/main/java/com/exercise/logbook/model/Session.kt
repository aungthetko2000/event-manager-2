package com.exercise.logbook.model

import java.io.Serializable

data class Session(
    val id: Long = 0,
    val eventId: Long,
    val name: String,
    val time: String,
    val speaker: String = "",
    val room: String = "",
    val type: String = "Presentation",
    val description: String = ""
): Serializable