package com.shabb.taskscheduler

import App
import DriverFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import createDatabase
import fileChooser
import initializeFileChooser
import kotlinx.coroutines.launch
import startTaskScheduler


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DriverFactory.initialize(applicationContext)
        val database = createDatabase(DriverFactory())

        initializeFileChooser(this)

        setContent {
            App(fileChooser = fileChooser)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                startTaskScheduler(database, this)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(fileChooser = null)
}