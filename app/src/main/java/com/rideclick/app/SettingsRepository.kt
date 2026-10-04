package com.rideclick.app

import android.content.Context

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("rideclick", Context.MODE_PRIVATE)

    fun loadRules(): Rules = Rules(
        prefs.getFloat("minPrice", 4f).toDouble(),
        prefs.getInt("maxEta", 5),
        prefs.getFloat("maxDistance", 3f).toDouble(),
        prefs.getFloat("minPricePerKm", 1f).toDouble()
    )

    fun saveRules(r: Rules) {
        prefs.edit()
            .putFloat("minPrice", r.minPrice.toFloat())
            .putInt("maxEta", r.maxEta)
            .putFloat("maxDistance", r.maxDistance.toFloat())
            .putFloat("minPricePerKm", r.minPricePerKm.toFloat())
            .apply()
    }
}
