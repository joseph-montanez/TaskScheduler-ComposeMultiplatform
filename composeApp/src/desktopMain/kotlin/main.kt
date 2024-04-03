import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.SystemTray
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import java.awt.Toolkit
import java.io.File
import java.net.URI
import kotlin.system.exitProcess

var isWindowVisible = mutableStateOf(true)


fun uriToFilePath(uri: URI): String {
    // Convert the URI to a File, then get the absolute path
    return File(uri).absolutePath
}

fun getTrayIconImagePath(iconPath: String): String {
    // Attempt to load the resource as a URL from the classpath
    val url = Thread.currentThread().contextClassLoader.getResource(iconPath)
    // If the URL is not null, return its external form
    if (url != null) {
        return url.toExternalForm()
    } else {
        throw IllegalArgumentException("Resource not found: $iconPath")
    }
}

fun integrateSystemTray() {
    val tray = SystemTray.get()

    if (tray == null) {
        println("System tray not supported!")
        return
    }

    //-- Enable Template Image to have the system tray icon color change based on appearance mode.
    val osName = System.getProperty("os.name").lowercase()
    val isMacOS = osName.contains("mac")
    if (isMacOS) {
        System.setProperty("apple.awt.enableTemplateImages", "true");
    }

    val dpi = Toolkit.getDefaultToolkit().screenResolution
    val iconPath = when {
        dpi <= 96 -> "icons/icon-24x24.png"
        dpi <= 120 -> "icons/icon-32x32.png"
        dpi <= 144 -> "icons/icon-64x64.png"
        else -> "icons/icon-64x64.png"
    }
    val iconURI = URI(getTrayIconImagePath(iconPath))
    val filePath = uriToFilePath(iconURI)
    tray.setImage(filePath)

    tray.menu.add(MenuItem("Open") {
        showWindow()
    }).setShortcut('o');
    tray.menu.add(MenuItem("Close") {
        exitProcess(0)
    }).setShortcut('q');
}

fun showWindow() {
    isWindowVisible.value = true
}

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    integrateSystemTray()

    val database = createDatabase(DriverFactory())
    startTaskScheduler(database, GlobalScope) // Start background task scheduler

    application(exitProcessOnExit = false) {
        val windowVisible = remember { isWindowVisible }
        if (windowVisible.value) {
            Window(onCloseRequest = {
                isWindowVisible.value = false
            }, title = "Task Scheduler") {
                App(fileChooser = FileChooserImpl())
            }
        }
    }
}