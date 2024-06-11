package com.mrsajal.di

import io.ktor.server.application.*
import org.koin.core.Koin
import org.koin.ktor.plugin.Koin


fun Application.configureDI(){
    install(Koin){
        modules(appModule)
    }
}