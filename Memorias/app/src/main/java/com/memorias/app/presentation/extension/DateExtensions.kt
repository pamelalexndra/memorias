package com.memorias.app.presentation.extension

fun Int.yearsAgoLabel(): String = when (this) {
    0 -> "hoy"
    1 -> "hace 1 año"
    else -> "hace $this años"
}
