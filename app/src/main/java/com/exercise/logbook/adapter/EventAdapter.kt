package com.exercise.logbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.exercise.logbook.R
import com.exercise.logbook.model.Event

class EventAdapter(
    private val events: List<Event>,
    private val onItemClick: (Event) -> Unit,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEventName: TextView = itemView.findViewById(R.id.tvEventName)
        val tvEventType: TextView = itemView.findViewById(R.id.tvEventType)
        val tvEventDateLocation: TextView = itemView.findViewById(R.id.tvEventDateLocation)
        val tvEventOrganiser: TextView = itemView.findViewById(R.id.tvEventOrganiser)
        val tvEventStatus: TextView = itemView.findViewById(R.id.tvEventStatus)
        val ivSettings: ImageView = itemView.findViewById(R.id.ivSettings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.tvEventName.text = event.name
        holder.tvEventType.text = event.type
        holder.tvEventDateLocation.text = "${event.date} | ${event.location}"
        holder.tvEventOrganiser.text = "Organiser: ${event.organiser}"
        holder.tvEventStatus.text = event.status

        holder.itemView.setOnClickListener {
            onItemClick(event)
        }

        holder.ivSettings.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add(0, 1, 0, "Edit")
            popup.menu.add(0, 2, 1, "Delete")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        onEditClick(event)
                        true
                    }
                    2 -> {
                        onDeleteClick(event)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = events.size
}