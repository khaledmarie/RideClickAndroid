package com.rideclick.app

data class Rules(
    val minPrice: Double = 4.0,
    val maxEta: Int = 5,
    val maxDistance: Double = 3.0,
    val minPricePerKm: Double = 1.0
)

enum class Platform { JEENY, PETRA_RIDE }

data class RideRequest(
    val platform: Platform,
    val price: Double,
    val eta: Int,
    val distance: Double
) {
    val pricePerKm: Double get() = if (distance > 0) price / distance else 0.0
}

data class Evaluation(
    val accepted: Boolean,
    val reasons: List<String>,
    val request: RideRequest
)
