package screens

import DriverFactory
import FileChooser
import LocalFileChooser
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.shabb.sqldelight.task.TaskQueries
import com.shabb.sqldelight.task.Tasks
import createDatabase
import formatLocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import libs.TasksSerializer
import libs.crontabToWords
import org.jetbrains.compose.ui.tooling.preview.Preview
import parseToLocalDateTime
class TaskManagerScreen : Screen {

    @Composable
    override fun Content() {
        TaskManagerContent()
    }
}

@Composable
fun TaskManagerContent(
    previewTasks: List<Tasks>? = null, // Add this parameter
) {
    var selectedTask by remember { mutableStateOf<Tasks?>(null) }
    val tasks = remember { mutableStateListOf<Tasks>().apply { previewTasks?.let { addAll(it) } } }
    val scrollState = rememberScrollState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }
    val fileChooser = LocalFileChooser.current
    val navigator = LocalNavigator.current

    if (previewTasks == null) {
        val driverFactory = DriverFactory()
        val database = createDatabase(driverFactory)
        val taskQueries: TaskQueries = database.taskQueries

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val taskList = taskQueries.selectByDueDate().executeAsList()
                tasks.clear()
                taskList.forEach { dbTask ->
                    tasks.add(dbTask)
                }
            }
        }
    }


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteTask(selectedTask!!, tasks)
                        showDialog = false
                        selectedTask = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Task Scheduler") },
                actions = {
                    IconButton(onClick = { navigator?.push(TaskEntryScreen()) }) {
                        Icon(Icons.Filled.Add, contentDescription = "New Task")
                    }
                    IconButton(onClick = { if(fileChooser != null) importTasks(fileChooser,tasks)}) {
                        Icon(Icons.Filled.Upload, contentDescription = "Import")
                    }
                    IconButton(onClick = { if(fileChooser != null) exportTasks(fileChooser, tasks.toList())}) {
                        Icon(Icons.Filled.Download, contentDescription = "Export")
                    }
                    IconButton(onClick = {
                        if (selectedTask != null) {
                            showDialog = true
                        } else {
                            errorMessage = "Please select a task to delete."
                        }
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = {
                        if (selectedTask != null) {
                            navigator?.push(TaskEntryScreen(taskToEdit = selectedTask))
                        } else {
                            errorMessage = "Please select a task to edit."
                        }
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Task")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (errorMessage != null) {
            LaunchedEffect(errorMessage) {
                snackbarHostState.showSnackbar(
                    message = errorMessage ?: "",
                    duration = SnackbarDuration.Short
                )
                errorMessage = null
            }
        }

        BoxWithConstraints {
            val maxWidth = maxWidth

            Box(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(0.dp)) {
                    val row1 = 5f
                    val row2 = 8f
                    val row3 = 16f
                    val row4 = 16f
                    val row5 = 16f
                    val cellpadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colors.surface)
                            .padding(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.weight(row1),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 0.dp),
                                text = " ",
                                color = MaterialTheme.colors.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier.weight(row2),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                modifier = Modifier.padding(cellpadding),
                                text = "Name",
                                color = MaterialTheme.colors.onSurface
                            )
                        }
                        if (maxWidth > 500.dp) {
                            Box(
                                modifier = Modifier.weight(row3),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    modifier = Modifier.padding(cellpadding),
                                    text = "Description",
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        }
                        if (maxWidth > 360.dp) {
                            Box(
                                modifier = Modifier.weight(row4),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    modifier = Modifier.padding(cellpadding),
                                    text = "Schedule",
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        }
                        if (maxWidth > 500.dp) {
                            Box(
                                modifier = Modifier.weight(row5),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    modifier = Modifier.padding(cellpadding),
                                    text = "Command",
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        }
                    }

                    if (tasks.isEmpty()) {
                        EmptyTasksMessage(onAddTaskClicked = { navigator?.push(TaskEntryScreen()) })
                    } else {
                        tasks.forEach { task ->
                            val hp = 0.dp
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Max)
                                    .padding(vertical = 0.dp)
                                    .clickable { selectedTask = if (selectedTask == task) null else task }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(row1)
                                        .padding(0.dp)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    RadioButton(
                                        selected = selectedTask == task,
                                        onClick = { selectedTask = if (selectedTask == task) null else task }
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(row2)
                                        .padding(cellpadding)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Spacer(modifier = Modifier.height((-2).dp))
                                    Text(
                                        modifier = Modifier,
                                        text = task.title,
                                        maxLines = 1,
                                    )
                                }
                                if (maxWidth > 500.dp) {
                                    Box(
                                        modifier = Modifier
                                            .weight(row3)
                                            .padding(cellpadding)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = task.description.orEmpty()
                                        )
                                    }
                                }
                                if (maxWidth > 360.dp) {
                                    Box(
                                        modifier = Modifier.weight(row4)
                                            .padding(cellpadding)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(cellpadding),
                                            text = "${crontabToWords(task)}\n${
                                                formatLocalDateTime(
                                                    parseToLocalDateTime(
                                                        task.dueDate.orEmpty()
                                                    )
                                                )
                                            }"
                                        )
                                    }
                                }
                                if (maxWidth > 500.dp) {
                                    Box(
                                        modifier = Modifier.weight(row5)
                                            .padding(cellpadding)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(cellpadding),
                                            text = task.command
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTasksMessage(onAddTaskClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("No tasks found!", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddTaskClicked) {
            Text("Create a Task")
        }
    }
}

fun deleteTask(selectedTask: Tasks, tasks: SnapshotStateList<Tasks>) {
    val driverFactory = DriverFactory()
    val database = createDatabase(driverFactory)
    val taskQueries = database.taskQueries;
    taskQueries.delete(selectedTask.id)

    val taskList = taskQueries.selectByDueDate().executeAsList()

    tasks.clear()
    taskList.forEach { dbTask ->
        tasks.add(dbTask)
    }
}

fun exportTasks(fileChooser: FileChooser, tasks: List<Tasks>) {
    val json = Json { serializersModule = SerializersModule { contextual(Tasks::class, TasksSerializer) } }
    val jsonString = json.encodeToString(ListSerializer(TasksSerializer), tasks)

    println(jsonString)
    // Invoke file chooser and handle the result asynchronously
    fileChooser.chooseFileSaveLocation { filePath ->
        if (filePath != null) {
            println("Saving JSON to: $filePath")
            // Save the jsonString to the selected filePath
            fileChooser.saveToFile(jsonString, filePath) { success ->
                if (success) {
                    println("File was saved successfully.")
                } else {
                    println("Failed to save the file.")
                }
            }
        } else {
            println("File selection was canceled.")
        }
    }
}

fun importTasks(fileChooser: FileChooser, tasks: SnapshotStateList<Tasks>) {
    fileChooser.pickFile { filePath ->
        if (filePath != null) {
            // Assuming you have implemented readTextFromFile to read file content
            fileChooser.readTextFromFile(filePath) { fileContent ->
                if (fileContent != null) {
                    val json =
                        Json { serializersModule = SerializersModule { contextual(Tasks::class, TasksSerializer) } }
                    val tasksFromJson = json.decodeFromString(ListSerializer(TasksSerializer), fileContent)


                    val driverFactory = DriverFactory()
                    val database = createDatabase(driverFactory)
                    val taskQueries = database.taskQueries;
                    println("\nDeserialized:")
                    tasksFromJson.forEach { task: Tasks ->
                        println(task)

                        val existing = taskQueries.selectById(task.id).executeAsList()
                        if (existing.size > 0) {
                            taskQueries.update(
                                id = task.id,
                                title = task.title,
                                description = task.description,
                                entryMode = task.entryMode,
                                runType = task.runType,
                                command = task.command,
                                startDate = task.startDate,
                                endDate = task.endDate,
                                dueDate = task.dueDate,
                                priority = task.priority,
                                status = task.status,
                                minute = task.minute,
                                hour = task.hour,
                                dayOfMonth = task.dayOfMonth,
                                month = task.month,
                                dayOfWeek = task.dayOfWeek,
                            )
                        } else {
                            taskQueries.insert(
                                id = task.id,
                                title = task.title,
                                description = task.description,
                                entryMode = task.entryMode,
                                runType = task.runType,
                                command = task.command,
                                startDate = task.startDate,
                                endDate = task.endDate,
                                dueDate = task.dueDate,
                                priority = task.priority,
                                status = task.status,
                                minute = task.minute,
                                hour = task.hour,
                                dayOfMonth = task.dayOfMonth,
                                month = task.month,
                                dayOfWeek = task.dayOfWeek,
                            )
                        }
                    }


                    tasks.clear()
                    taskQueries.selectByDueDate().executeAsList().forEach { dbTask ->
                        tasks.add(dbTask)
                    }
                } else {
                    println("Failed to read file content.")
                }
            }
        } else {
            println("File selection was canceled.")
        }
    }
}

@Preview
@Composable
fun TaskManagerScreenPreview() {
    TaskManagerContent(
        previewTasks = listOf(
            Tasks(
                id = "1",
                title = "Sample Task 1",
                description = "This is a sample task description",
                startDate = "2023-03-14T10:00:00",
                endDate = "2023-03-14T10:00:00",
                dueDate = "2023-03-14T10:00:00",
                priority = 1,
                status = "Pending",
                entryMode = 2,
                runType = "command",
                command = "echo Hello World",
                minute = "*/5",
                hour = "*",
                dayOfMonth = "*",
                month = "*",
                dayOfWeek = "*",
                createdAt = "2023-03-01T12:00:00",
                updatedAt = "2023-03-01T12:00:00",
            ),
        )
    )
}