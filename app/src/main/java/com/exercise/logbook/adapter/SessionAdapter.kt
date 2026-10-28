package com.exercise.logbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.exercise.logbook.R
import com.exercise.logbook.model.Session

class SessionAdapter(
    private val sessions: List<Session>,
    private val onEditClick: (Session) -> Unit,
    private val onDeleteClick: (Session) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSessionName: TextView = itemView.findViewById(R.id.tvSessionName)
        val tvSessionTimeRoom: TextView = itemView.findViewById(R.id.tvSessionTimeRoom)
        val tvSessionSpeaker: TextView = itemView.findViewById(R.id.tvSessionSpeaker)
        val tvSessionType: TextView = itemView.findViewById(R.id.tvSessionType)
        val ivSettings: ImageView = itemView.findViewById(R.id.ivSessionSettings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.tvSessionName.text = session.name
        holder.tvSessionTimeRoom.text = "${session.time} | Room: ${if (session.room.isEmpty()) "TBD" else session.room}"
        holder.tvSessionSpeaker.text = "Speaker: ${if (session.speaker.isEmpty()) "N/A" else session.speaker}"
        holder.tvSessionType.text = session.type

        holder.ivSettings.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add(0, 1, 0, "Edit Session")
            popup.menu.add(0, 2, 1, "Delete Session")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> { onEditClick(session); true }
                    2 -> { onDeleteClick(session); true }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = sessions.size
}