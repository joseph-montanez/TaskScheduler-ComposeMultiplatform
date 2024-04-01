package libs

import com.shabb.sqldelight.task.Tasks
import kotlin.test.Test
import kotlin.test.assertEquals

class CrontabWordsTest {

    @Test
    fun testCrontabToWordsAugust() {
        val task = Tasks(
            id = "1",
            title = "Test Task",
            description = "Test Description",
            dueDate = null,
            priority = null,
            status = null,
            command = "",
            minute = "5",
            hour = "0",
            dayOfMonth = "*",
            month = "8",
            dayOfWeek = "*",
            created_at = null
        )

        val expected = "At 00:05 in August"
        val actual = crontabToWords(task)
        assertEquals(expected, actual, "The crontab to words conversion did not match the expected output.")
    }
    

    @Test
    fun testCrontabToWordsSlash() {
        val task = Tasks(
            id = "1",
            title = "Test Task",
            description = "Test Description",
            dueDate = null,
            priority = null,
            status = null,
            command = "",
            minute = "*/5",
            hour = "0",
            dayOfMonth = "*",
            month = "8",
            dayOfWeek = "*",
            created_at = null
        )

        val expected = "At every 5th minute past hour 0 in August"
        val actual = crontabToWords(task)
        assertEquals(expected, actual, "The crontab to words conversion did not match the expected output.")
    }
    
    @Test
    fun testCrontabToWordsDayMonth() {
        val task = Tasks(
            id = "1",
            title = "Test Task",
            description = "Test Description",
            dueDate = null,
            priority = null,
            status = null,
            command = "",
            minute = "*/5",
            hour = "12",
            dayOfMonth = "2",
            month = "8",
            dayOfWeek = "*",
            created_at = null
        )

        val expected = "At every 5th minute past hour 12 on day-of-month 2 in August"
        val actual = crontabToWords(task)
        assertEquals(expected, actual, "The crontab to words conversion did not match the expected output.")
    }

    @Test
    fun testCrontabToWordsMonth() {
        val task = Tasks(
            id = "1",
            title = "Test Task",
            description = "Test Description",
            dueDate = null,
            priority = null,
            status = null,
            command = "",
            minute = "*/2",
            hour = "19",
            dayOfMonth = "*",
            month = "*",
            dayOfWeek = "*",
            created_at = null
        )

        val expected = "At every 2nd minute past hour 19"
        val actual = crontabToWords(task)
        assertEquals(expected, actual, "The crontab to words conversion did not match the expected output.")
    }

    @Test
    fun testCrontabToWordsEveryDayMonth() {
        val task = Tasks(
            id = "1",
            title = "Test Task",
            description = "Test Description",
            dueDate = null,
            priority = null,
            status = null,
            command = "",
            minute = "*",
            hour = "*",
            dayOfMonth = "15",
            month = "4",
            dayOfWeek = "*",
            created_at = null
        )

        val expected = "At every minute on day-of-month 15 in April"
        val actual = crontabToWords(task)
        assertEquals(expected, actual, "The crontab to words conversion did not match the expected output.")
    }

    @Test
    fun testCrontabToWordsEveryDayEveryMonth() {
        val task = Tasks(
            id = "1",
            title = "Test Task",
            description = "Test Description",
            dueDate = null,
            priority = null,
            status = null,
            command = "",
            minute = "*/5",
            hour = "*/5",
            dayOfMonth = "*",
            month = "*",
            dayOfWeek = "*",
            created_at = null
        )

        val expected = "At every 5th minute past every 5th hour"
        val actual = crontabToWords(task)
        assertEquals(expected, actual, "The crontab to words conversion did not match the expected output.")
    }
    
    @Test
    fun testCrontabToWordsEveryDayOfWeek() {
        val task = Tasks(
            id = "1",
            title = "Test Task",
            description = "Test Description",
            dueDate = null,
            priority = null,
            status = null,
            command = "",
            minute = "0",
            hour = "22",
            dayOfMonth = "*",
            month = "*",
            dayOfWeek = "1-5",
            created_at = null
        )

        val expected = "At 22:00 on every day-of-week from Monday through Friday"
        val actual = crontabToWords(task)
        assertEquals(expected, actual, "The crontab to words conversion did not match the expected output.")
    }
}