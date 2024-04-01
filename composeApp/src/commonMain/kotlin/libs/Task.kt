package libs

import com.shabb.sqldelight.task.Tasks
import kotlinx.datetime.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

data class CommandResult(
    val output: String,
    val errorOutput: String,
    val exitValue: Int
)

data class CronTime(
    val minute: Int,
    val hour: Int,
    val dayOfMonth: Int,
    val month: Int,
    val dayOfWeek: Int
)

data class TaskFormData(
    var id: String? = null,
    var title: String = "",
    var description: String = "",
    var startDate: LocalDateTime? = null,
    var endDate: LocalDateTime? = null,
    var dueDate: LocalDateTime? = null,
    var priority: String = "",
    var status: String = "",
    var runType: String = "",
    var command: String = "",
    var entryMode: Long = 0L,
    var minute: String = "",
    var hour: String = "",
    var dayOfMonth: String = "",
    var month: String = "",
    var dayOfWeek: String = ""
)

fun createTask(
    id: String = "",
    title: String = "",
    description: String? = null,
    startDate: String? = null,
    endDate: String? = null,
    dueDate: String? = null,
    priority: Long? = null,
    status: String? = null,
    runType: String? = null,
    command: String = "",
    entryMode: Long = 0,
    minute: String? = null,
    hour: String? = null,
    dayOfMonth: String? = null,
    month: String? = null,
    dayOfWeek: String? = null,
    createdAt: String? = null,
    updatedAt: String? = null
): Tasks {
    return Tasks(
        id = id,
        title = title,
        description = description,
        startDate = startDate,
        endDate = endDate,
        dueDate = dueDate,
        priority = priority,
        entryMode = entryMode,
        status = status,
        runType = runType,
        command = command,
        minute = minute,
        hour = hour,
        dayOfMonth = dayOfMonth,
        month = month,
        dayOfWeek = dayOfWeek,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Tasks::class)
object TasksSerializer : KSerializer<Tasks> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Tasks", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Tasks) {
        if (encoder !is JsonEncoder) throw SerializationException("This serializer can only be used with JSON")
        val json = buildJsonObject {
            put("id", value.id)
            put("title", value.title)
            put("description", value.description)
            put("dueDate", value.dueDate)
            put("priority", value.priority)
            put("status", value.status)
            put("command", value.command)
            put("runType", value.runType)
            put("entryMode", value.entryMode)
            put("minute", value.minute)
            put("hour", value.hour)
            put("dayOfMonth", value.dayOfMonth)
            put("month", value.month)
            put("dayOfWeek", value.dayOfWeek)
            put("createdAt", value.createdAt)
        }
        encoder.encodeJsonElement(json)
    }

    override fun deserialize(decoder: Decoder): Tasks {
        if (decoder !is JsonDecoder) throw SerializationException("This deserializer can only be used with JSON")
        val jsonObject = decoder.decodeJsonElement() as JsonObject
        return Tasks(
            id = jsonObject["id"]!!.jsonPrimitive.content,
            title = jsonObject["title"]!!.jsonPrimitive.content,
            description = jsonObject["description"]?.jsonPrimitive?.contentOrNull,
            startDate = jsonObject["startDate"]?.jsonPrimitive?.contentOrNull,
            endDate = jsonObject["endDate"]?.jsonPrimitive?.contentOrNull,
            dueDate = jsonObject["dueDate"]?.jsonPrimitive?.contentOrNull,
            priority = jsonObject["priority"]?.jsonPrimitive?.longOrNull,
            status = jsonObject["status"]?.jsonPrimitive?.contentOrNull,
            runType = jsonObject["runType"]!!.jsonPrimitive.content,
            command = jsonObject["command"]!!.jsonPrimitive.content,
            entryMode = jsonObject["entryMode"]!!.jsonPrimitive.long,
            minute = jsonObject["minute"]?.jsonPrimitive?.contentOrNull,
            hour = jsonObject["hour"]?.jsonPrimitive?.contentOrNull,
            dayOfMonth = jsonObject["dayOfMonth"]?.jsonPrimitive?.contentOrNull,
            month = jsonObject["month"]?.jsonPrimitive?.contentOrNull,
            dayOfWeek = jsonObject["dayOfWeek"]?.jsonPrimitive?.contentOrNull,
            createdAt = jsonObject["createdAt"]?.jsonPrimitive?.contentOrNull,
            updatedAt = jsonObject["updatedAt"]?.jsonPrimitive?.contentOrNull
        )
    }
}

fun getOrdinalSuffix(number: Int): String {
    return when {
        number % 10 == 1 && number % 100 != 11 -> "st"
        number % 10 == 2 && number % 100 != 12 -> "nd"
        number % 10 == 3 && number % 100 != 13 -> "rd"
        else -> "th"
    }
}

fun crontabToWords(task: Tasks): String {
    val monthMap = mapOf(
        "1" to "January", "2" to "February", "3" to "March",
        "4" to "April", "5" to "May", "6" to "June",
        "7" to "July", "8" to "August", "9" to "September",
        "10" to "October", "11" to "November", "12" to "December"
    )

    val dayOfWeekMap = mapOf(
        "0" to "Sunday", "1" to "Monday", "2" to "Tuesday",
        "3" to "Wednesday", "4" to "Thursday", "5" to "Friday", "6" to "Saturday"
    )

    return buildString {
        if (task.minute?.startsWith("*/") == true) {
            val minutePattern = task.minute.removePrefix("*/").toInt()
            val minuteSuffix = getOrdinalSuffix(minutePattern)
            append("At every $minutePattern$minuteSuffix minute")

            if (task.hour?.startsWith("*/") == true) {
                val hourPattern = task.hour.removePrefix("*/").toInt()
                val hourSuffix = getOrdinalSuffix(hourPattern)
                append(" past every $hourPattern$hourSuffix hour")
            } else if (!task.hour.isNullOrEmpty()) {
                append(" past hour ${task.hour}")
            }
        } else if (!task.minute.isNullOrEmpty() && !task.hour.isNullOrEmpty()) {
            if (task.minute == "*") {
                append("At every minute")
            } else {
                append("At ${task.hour.padStart(2, '0')}:${task.minute.padStart(2, '0')}")
            }
        }

        if (!task.dayOfMonth.isNullOrEmpty() && task.dayOfMonth != "*") {
            append(" on day-of-month ${task.dayOfMonth}")
        }

        if (!task.month.isNullOrEmpty() && task.month != "*") {
            append(" in ${monthMap[task.month]}")
        }

        if (!task.dayOfWeek.isNullOrEmpty() && task.dayOfWeek != "*") {
            val dayOfWeekParts = task.dayOfWeek.split("-")
            if (dayOfWeekParts.size == 2) {
                // Assuming the range is always valid and in the correct order
                val startDay = dayOfWeekMap[dayOfWeekParts[0]]
                val endDay = dayOfWeekMap[dayOfWeekParts[1]]
                if (startDay != null && endDay != null) {
                    append(" on every day-of-week from $startDay through $endDay")
                }
            } else {
                append(" on ${dayOfWeekMap[task.dayOfWeek]}")
            }
        }
    }.trim()
}

fun convertToTasks(formData: TaskFormData): Tasks {
    val priorityAsLong = formData.priority.toLongOrNull()

    val dueDateString = formData.dueDate.toString()
    val startDateString = formData.startDate.toString()
    val endDateString = formData.endDate.toString()

    val createdAt = Clock.System.now().toString()
    val updatedAt = Clock.System.now().toString()
    
    return Tasks(
        id = formData.id ?: "",
        title = formData.title,
        description = formData.description.ifEmpty { null },
        dueDate = dueDateString,
        priority = priorityAsLong,
        status = formData.status.ifEmpty { null },
        runType = formData.runType,
        command = formData.command,
        entryMode = formData.entryMode,
        minute = formData.minute.ifEmpty { null },
        hour = formData.hour.ifEmpty { null },
        dayOfMonth = formData.dayOfMonth.ifEmpty { null },
        month = formData.month.ifEmpty { null },
        dayOfWeek = formData.dayOfWeek.ifEmpty { null },
        startDate = startDateString,
        endDate = endDateString,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun convertToTaskFormData(tasks: Tasks): TaskFormData {
    return TaskFormData(
        id = tasks.id,
        title = tasks.title,
        description = tasks.description ?: "",
        startDate = try { tasks.startDate?.toLocalDateTime() } catch (e: Exception) { null },
        endDate = try { tasks.endDate?.toLocalDateTime() } catch (e: Exception) { null },
        dueDate = try { tasks.dueDate?.toLocalDateTime() } catch (e: Exception) { null },
        priority = tasks.priority?.toString() ?: "",
        status = tasks.status ?: "",
        command = tasks.command,
        minute = tasks.minute ?: "",
        hour = tasks.hour ?: "",
        dayOfMonth = tasks.dayOfMonth ?: "",
        month = tasks.month ?: "",
        dayOfWeek = tasks.dayOfWeek ?: "",
    )
}