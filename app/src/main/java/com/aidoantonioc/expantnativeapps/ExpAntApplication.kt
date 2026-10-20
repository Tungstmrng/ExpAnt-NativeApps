package com.aidoantonioc.expantnativeapps

import android.app.Application
import com.aidoantonioc.expantnativeapps.data.AppContainer

class ExpAntApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}