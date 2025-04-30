package dev.mattdebinion.motohazardetector.data

/**
 * HazardAlert is a data class representing a hazard alert.
 *
 * @param title The title of the alert
 * @param timestamp The timestamp of the alert
 * @param severity The severity of the alert where 0 is informational, 1 is warning, and 2 is alert
 * @param description The description of the alert
 */
data class HazardAlert(
    val title: String,
    val timestamp: String,
    val severity: Int,
    val description: String
)
