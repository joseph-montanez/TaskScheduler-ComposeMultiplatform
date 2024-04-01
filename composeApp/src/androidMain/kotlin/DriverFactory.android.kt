import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.shabb.Database
import com.shabb.taskscheduler.TaskSchedulerApp

actual class DriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(Database.Schema, TaskSchedulerApp.applicationContext, "test.db")
    }

    companion object {
        lateinit var context: Context

        fun initialize(context: Context) {
            DriverFactory.context = context
        }
    }
}
