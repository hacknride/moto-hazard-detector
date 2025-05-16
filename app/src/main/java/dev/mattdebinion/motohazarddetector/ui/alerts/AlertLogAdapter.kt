package dev.mattdebinion.motohazarddetector.ui.alerts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.mattdebinion.motohazarddetector.R
import dev.mattdebinion.motohazarddetector.ui.alerts.data.HazardAlert
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * AlertLogAdapter is an adapter for the alert log recycler view.
 */
class AlertLogAdapter(private var alerts: List<HazardAlert>) : RecyclerView.Adapter<AlertLogAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view.findViewById(R.id.alert_container) // Make sure this exists in your layout
        val title: TextView = view.findViewById(R.id.alert_title)
        val timestamp: TextView = view.findViewById(R.id.alert_timestamp)
        val description: TextView = view.findViewById(R.id.alert_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.alert_log_generic, parent, false)
        return AlertViewHolder(view)
    }

    override fun getItemCount() = alerts.size

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        val context = holder.itemView.context

        // Format timestamp
        val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val outputFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy, hh:mm:ss a")
        holder.timestamp.text = try {
            val parsed = LocalDateTime.parse(alert.timestamp, inputFormat)
            "Time: ${parsed.format(outputFormat)}"
        } catch (e: Exception) {
            "Time: ${alert.timestamp}" // fallback
        }

        holder.title.text = alert.title
        holder.description.text = alert.description

        // Common background for all alerts
        holder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.alert_background))

        when (alert.severity) {
            0 -> { // Info
                holder.title.setTextColor(ContextCompat.getColor(context, R.color.alert_text))
                holder.title.setBackgroundColor(ContextCompat.getColor(context, R.color.alert_info))
                holder.timestamp.setTextColor(ContextCompat.getColor(context, R.color.alert_timestamp))
                holder.description.setTextColor(ContextCompat.getColor(context, R.color.alert_advisory))
            }
            1 -> { // Advisory
                holder.title.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.title.setBackgroundColor(ContextCompat.getColor(context, R.color.alert_advisory))
                holder.timestamp.setTextColor(ContextCompat.getColor(context, R.color.alert_timestamp))
                holder.description.setTextColor(ContextCompat.getColor(context, R.color.alert_advisory))
            }
            2 -> { // Warning
                holder.title.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.title.setBackgroundColor(ContextCompat.getColor(context, R.color.alert_warning))
                holder.timestamp.setTextColor(ContextCompat.getColor(context, R.color.alert_timestamp))
                holder.description.setTextColor(ContextCompat.getColor(context, R.color.alert_warning))
            }
            else -> { // Default fallback (info alert)
                holder.title.setTextColor(ContextCompat.getColor(context, R.color.alert_text))
                holder.title.setBackgroundColor(ContextCompat.getColor(context, R.color.alert_info))
                holder.timestamp.setTextColor(ContextCompat.getColor(context, R.color.alert_timestamp))
                holder.description.setTextColor(ContextCompat.getColor(context, R.color.alert_advisory))
            }
        }
    }

    fun updateAlerts(newAlerts: List<HazardAlert>) {
        alerts = newAlerts
        notifyDataSetChanged()
    }
}