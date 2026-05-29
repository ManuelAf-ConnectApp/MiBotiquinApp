package com.connectapp.cleanarch

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform