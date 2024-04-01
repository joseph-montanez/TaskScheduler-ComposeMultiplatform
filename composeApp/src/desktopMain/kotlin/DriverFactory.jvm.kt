import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.shabb.Database
import java.io.File

fun getAppDocumentsDirectory(appName: String): File {
    val userHome = System.getProperty("user.home")
    val documentsPath = when {
        System.getProperty("os.name").toLowerCase().contains("win") -> "$userHome\\Documents\\$appName"
        System.getProperty("os.name").toLowerCase().contains("mac") -> "$userHome/Documents/$appName"
        else -> "$userHome/$appName" // Fallback for Linux and other OS
    }
    val documentsDir = File(documentsPath)
    if (!documentsDir.exists()) {
        documentsDir.mkdirs() // Create directories if they do not exist
    }
    return documentsDir
}

actual class DriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        val appDocumentsDir = getAppDocumentsDirectory("TaskScheduler")
        val databaseFile = File(appDocumentsDir, "database.db")
        println(databaseFile.absolutePath)
        val driver: SqlDriver = JdbcSqliteDriver(
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
            schema = Database.Schema,
            migrateEmptySchema = true
            
        )
        println("version: ${Database.Schema.version}")
        
        return driver
    }
}