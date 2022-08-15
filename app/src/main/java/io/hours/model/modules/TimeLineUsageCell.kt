package io.hours.model.modules

data class TimeLineUsageCell(
    var pack: Package,
    var launchTime: Long,
    val exitTime: Long,
    var duration: Long
)
