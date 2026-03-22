package com.dadadadev.prototype_me

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dadadadev.prototype_me.di.boardModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(boardModule)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "PrototypeMe",
        ) {
            App()
        }
    }
}
