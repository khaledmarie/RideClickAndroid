package com.rideclick.app

interface PlatformAdapter {
    val platform: Platform
    fun extractRequest(rootText: String): RideRequest?
}

/**
 * UI selectors are intentionally not hard-coded yet.
 * They must be verified against real, current app screens on a test device.
 */
class JeenyAdapter : PlatformAdapter {
    override val platform = Platform.JEENY
    override fun extractRequest(rootText: String) =
        TextRequestParser.parse(rootText, platform)
}

class PetraRideAdapter : PlatformAdapter {
    override val platform = Platform.PETRA_RIDE
    override fun extractRequest(rootText: String) =
        TextRequestParser.parse(rootText, platform)
}
