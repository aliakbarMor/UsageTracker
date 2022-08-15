package io.hours.model.modules


data class TimelineCell(
    val hour: String,
    val usage: Long,
    val pack: ArrayList<Package>,
    val apps: List<TimeLineUsageCell>,
)