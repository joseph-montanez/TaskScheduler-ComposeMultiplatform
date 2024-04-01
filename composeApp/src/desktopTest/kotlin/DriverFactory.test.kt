import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.shabb.Database

// Test-specific DriverFactory
class TestDriverFactory {
    fun createDriver(): SqlDriver {
        // Use an in-memory SQLite database for tests
        val driver: SqlDriver = JdbcSqliteDriver(
            url = "jdbc:sqlite::memory:",
            schema = Database.Schema,
            migrateEmptySchema = true
        )
        Database.Schema.create(driver)
        return driver
    }
}