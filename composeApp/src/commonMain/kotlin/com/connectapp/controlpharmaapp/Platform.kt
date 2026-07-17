package com.connectapp.controlpharmaapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform