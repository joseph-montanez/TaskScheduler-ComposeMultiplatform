package com.shabb.taskscheduler

import android.app.Application
import android.content.Context

class TaskSchedulerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: TaskSchedulerApp? = null

        val applicationContext: Context
            get() = instance!!.applicationContext
    }
}