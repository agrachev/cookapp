package ru.agrachev.cookapp.core

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.agrachev.cookapp.di.appModule

class CookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CookApp)
            modules(appModule)
        }
    }
}
