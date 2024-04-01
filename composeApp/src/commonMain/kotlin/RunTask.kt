import com.benasher44.uuid.uuid4
import com.shabb.Database
import com.shabb.sqldelight.task.TaskQueries
import com.shabb.sqldelight.task.TaskRunQueries
import com.shabb.sqldelight.task.Tasks
import kotlinx.coroutines.*
import kotlinx.datetime.*
import libs.CommandResult
import libs.CronTime

expect fun runTaskCommand(command: String): CommandResult

@OptIn(DelicateCoroutinesApi::class)
fun startTaskScheduler(database: Database, scope: CoroutineScope) {
    val taskQueries: TaskQueries = database.taskQueries

    scope.launch(Dispatchers.IO) { // Use an appropriate scope in a real application
        while (isActive) { // Keep running until the coroutine is cancelled
            val tasksToRun = taskQueries.selectByDueDate().executeAsList()

            val timezone = TimeZone.currentSystemDefault()
            val localDateTime = Clock.System.now().toLocalDateTime(timezone)
            tasksToRun.forEach { task: Tasks ->
                val taskDueDateTime = task.dueDate?.let { parseToLocalDateTime(it) }
                if (taskDueDateTime == null || localDateTime >= taskDueDateTime) {
                    if (shouldTaskRun(task, localDateTime)) {
                        println("Running - ${task.id} - ${task.command}")
                        println(localDateTime.toString())
                        val result = runTaskCommand(task.command)

                        val taskRunQueries: TaskRunQueries = database.taskRunQueries

                        val runId = uuid4().toString()
                        val runTimestamp =
                            Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds()).toString()
                        val status = if (result.exitValue == 0) "success" else "failure"

                        val combinedOutput = "Output:\n${result.output}\nError Output:\n${result.errorOutput}"

                        taskRunQueries.transaction {
                            taskRunQueries.insert(
                                run_id = runId,
                                task_id = task.id,
                                run_timestamp = runTimestamp,
                                output = combinedOutput,
                                status = status
                            )
                        }

                        // Only calculate and update dueDate if it's due or passed
                        val nextDate = getNextRunTime(task, localDateTime, TimeZone.currentSystemDefault())
                        if (nextDate != null) {
                            taskQueries.updateDueDate(nextDate.toString(), task.id)
                        }
                    }
                }
            }

            delay(60_000) // Wait for one minute before running the loop again
        }
    }
}

fun getCurrentTime(): CronTime {
    val current = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return CronTime(
        minute = current.minute,
        hour = current.hour,
        dayOfMonth = current.dayOfMonth,
        month = current.monthNumber,
        dayOfWeek = current.dayOfWeek.ordinal % 7 // Adjust if needed for your dayOfWeek numbering
    )
}

fun shouldTaskRun(task: Tasks, currentTime: LocalDateTime): Boolean {
    fun matchCronField(taskValue: String?, currentTimeValue: Int, maxValue: Int): Boolean {
        if (taskValue == null) return false // Consider how you want to handle nulls; this is a simplistic approach
        if (taskValue == "*") return true
        if (taskValue.toIntOrNull() == currentTimeValue) return true

        // Handle the "*/n" pattern
        val split = taskValue.split("/")
        if (split.size == 2 && split[0] == "*") {
            val interval = split[1].toIntOrNull() ?: return false
            return currentTimeValue % interval == 0
        }

        return false
    }

    val minuteMatch = matchCronField(task.minute, currentTime.minute, 59)
    val hourMatch = matchCronField(task.hour, currentTime.hour, 23)
    val dayOfMonthMatch = matchCronField(task.dayOfMonth, currentTime.dayOfMonth, 31)
    val monthMatch = matchCronField(task.month, currentTime.monthNumber, 12)
    val dayOfWeekMatch = matchCronField(task.dayOfWeek, (currentTime.dayOfWeek.ordinal + 1) % 7, 6)

    return minuteMatch && hourMatch && dayOfMonthMatch && monthMatch && dayOfWeekMatch
}

fun getNextRunTime(task: Tasks, currentDateTime: LocalDateTime, timeZone: TimeZone): LocalDateTime? {
    var nextTimeInstant = currentDateTime.toInstant(timeZone)
    var nextTime: LocalDateTime

    repeat(50400) { // Max attempts: 5 weeks * 7 days * 24 hours * 60 minutes = 50400 minutes
        nextTimeInstant = nextTimeInstant.plus(1, DateTimeUnit.MINUTE, timeZone)
        nextTime = nextTimeInstant.toLocalDateTime(timeZone)

        if (shouldTaskRun(task, nextTime)) {
            return nextTime
        }
    }

    return null
}

fun parseToLocalDateTime(dueDateString: String): LocalDateTime? {
    return try {
        // Assuming ISO-8601 format for simplicity; adjust as needed
        LocalDateTime.parse(dueDateString)
    } catch (e: Exception) {
        null // Handle parsing error or log as needed
    }
}

fun formatLocalDateTime(localDateTime: LocalDateTime?): String? {
    localDateTime ?: return null

    // Custom formatting function since kotlinx-datetime doesn't support direct formatting
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val hour = if (localDateTime.hour % 12 == 0) 12 else localDateTime.hour % 12
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val ampm = if (localDateTime.hour < 12) "AM" else "PM"

    return "$month/$day $hour:$minute $ampm"
}
