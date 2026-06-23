package com.memorias.app.domain.model.enums

enum class SessionSize(val count: Int) {
    QUICK(5),
    NORMAL(10),
    EXTENDED(20),
    UNLIMITED(Int.MAX_VALUE),
    ;
    val isUnlimited: Boolean get() = this == UNLIMITED
}