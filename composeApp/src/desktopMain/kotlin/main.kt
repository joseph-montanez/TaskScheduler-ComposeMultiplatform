import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    val database = createDatabase(DriverFactory())
    startTaskScheduler(database, GlobalScope) // Start background task scheduler
    
    application {
        Window(onCloseRequest = ::exitApplication, title = "Task Scheduler") {
            App(fileChooser = FileChooserImpl())
        }
    }
}