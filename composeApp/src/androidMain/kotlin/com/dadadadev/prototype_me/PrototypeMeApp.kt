package com.dadadadev.prototype_me

import android.app.Application
import com.dadadadev.prototype_me.di.boardModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PrototypeMeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PrototypeMeApp)
            modules(boardModule)
        }
    }
}
