package com.dadadadev.prototype_me

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dadadadev.prototype_me.di.erdBoardModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(erdBoardModule)
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
