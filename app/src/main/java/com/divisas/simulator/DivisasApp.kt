package com.divisas.simulator

import android.app.Application

class DivisasApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: DivisasApp
            private set
    }
}
