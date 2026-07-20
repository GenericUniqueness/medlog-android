package com.medlog.app

import android.app.Application
import com.medlog.app.di.AppContainer

class MedLogApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
