package com.rideclick.app

object TextRequestParser {
    private val money = Regex("""(?i)(?:د\.?أ|دينار|JOD)\s*([0-9]+(?:[.,][0-9]+)?)|([0-9]+(?:[.,][0-9]+)?)\s*(?:د\.?أ|دينار|JOD)""")
    // Match a complete integer and minute unit, excluding decimal tails and Arabic currency.
    private val minutes = Regex(
        """(?<![\p{L}\p{N}.,])([0-9]+)\s*(?:دقيقة|دقائق|د(?!\s*[.أ])|minutes?|mins?)(?![\p{L}\p{N}])""",
        RegexOption.IGNORE_CASE
    )
    private val km = Regex("""([0-9]+(?:[.,][0-9]+)?)\s*(?:كم|km)""", RegexOption.IGNORE_CASE)

    fun parse(text: String, platform: Platform): RideRequest? {
        val price = money.find(text)?.let {
            (it.groups[1]?.value ?: it.groups[2]?.value)?.replace(',', '.')?.toDoubleOrNull()
        } ?: return null
        val eta = minutes.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: return null
        val distance = km.find(text)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull() ?: return null
        return RideRequest(platform, price, eta, distance)
    }
}
