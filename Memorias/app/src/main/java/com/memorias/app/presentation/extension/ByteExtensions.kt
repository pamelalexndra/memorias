package com.memorias.app.presentation.extension

fun Long.formatBytes(): String = when {
    this >= 1_073_741_824L -> "%.2f GB".format(this / 1_073_741_824f)
    this >= 1_048_576L     -> "%.1f MB".format(this / 1_048_576f)
    this >= 1_024L         -> "${this / 1_024} KB"
    else                   -> "$this B"
}