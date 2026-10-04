package com.rideclick.app

object RuleEngine {
    fun evaluate(request: RideRequest, rules: Rules, enabled: Set<Platform>): Evaluation {
        val reasons = mutableListOf<String>()
        if (request.platform !in enabled) reasons += "المنصة غير مفعّلة"
        if (request.price < rules.minPrice) reasons += "السعر أقل من الحد الأدنى"
        if (request.eta > rules.maxEta) reasons += "وقت الوصول أعلى من الحد"
        if (request.distance > rules.maxDistance) reasons += "المسافة أكبر من الحد"
        if (request.pricePerKm < rules.minPricePerKm) reasons += "السعر لكل كيلومتر أقل من الحد"
        return Evaluation(reasons.isEmpty(), reasons, request)
    }
}
