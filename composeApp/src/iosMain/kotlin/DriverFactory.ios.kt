import androidx.compose.ui.window.ComposeUIViewController
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.shabb.Database

actual class DriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(Database.Schema, "database.db")
    }
}