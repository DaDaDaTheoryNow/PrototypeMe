package com.dadadadev.prototype_me

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform