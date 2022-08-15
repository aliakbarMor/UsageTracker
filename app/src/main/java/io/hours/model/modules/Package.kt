package io.hours.model.modules

import android.graphics.drawable.Drawable

data class Package(
    val packageName: String,
) {
    lateinit var appName: String
    lateinit var icon: Drawable
}
