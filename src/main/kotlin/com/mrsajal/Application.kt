package com.mrsajal

import com.mrsajal.di.configureDI
import com.mrsajal.plugins.configureRouting
import com.mrsajal.plugins.configureSecurity
import com.mrsajal.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}


fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureDI()
    configureSecurity()
    configureRouting()
}
