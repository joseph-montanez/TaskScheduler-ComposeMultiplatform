package screens

import DriverFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.benasher44.uuid.uuid4
import com.shabb.sqldelight.task.TaskQueries
import com.shabb.sqldelight.task.Tasks
import components.DatePickerRow
import components.MultiSelectableButtonGroup
import components.SingleSelectableButtonGroup
import createDatabase
import getNextRunTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import libs.TaskFormData
import libs.convertToTaskFormData
import libs.convertToTasks
import libs.createTask
import org.jetbrains.compose.ui.tooling.preview.Preview
import parseToLocalDateTime


class TaskEntryScreen(val taskToEdit: Tasks? = null) : Screen {

    @Composable
    @Preview
    override fun Content() {
        val initialFormData = convertToTaskFormData(taskToEdit ?: createTask())
        val formData = remember { mutableStateOf(initialFormData) }
        val scrollState = rememberScrollState()
        val selectedOption = remember { mutableStateOf(2) }
        val buttonLabels = listOf("Presets", "Custom", "Manual")
        val navigator = LocalNavigator.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Task Entry") },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            TaskFormUi(
                formData = formData,
                scrollState = scrollState,
                selectedOption = selectedOption,
                buttonLabels = buttonLabels,
                onFormSubmit = { formData: TaskFormData ->
                    handleSubmit(formData)
                    navigator?.pop()
                },
                innerPadding = innerPadding
            )
        }
    }

    private fun handleSubmit(formData: TaskFormData) {
        // Here you will handle the form submission.
        // This could involve validating the data and inserting it into your database.
        println("Form data submitted: $formData")

        val driverFactory = DriverFactory()
        val database = createDatabase(driverFactory)

        val tasks: TaskQueries = database.taskQueries

        val timezone = TimeZone.currentSystemDefault()
        val localDateTime = Clock.System.now().toLocalDateTime(timezone)
        val nextDate = getNextRunTime(convertToTasks(formData), localDateTime, TimeZone.currentSystemDefault())
        if (nextDate != null) {
            formData.dueDate = nextDate
        }

        val priorityValue = try {
            formData.priority.toLong()
        } catch (e: NumberFormatException) {
            0L
        }

        if (formData.id == null || formData.id?.length == 0) {
            println("Form insert")
            tasks.insert(
                id = uuid4().toString(),
                title = formData.title,
                description = formData.description,
                startDate = formData.startDate.toString(),
                endDate = formData.endDate.toString(),
                dueDate = formData.dueDate.toString(),
                entryMode = formData.entryMode,
                runType = formData.runType,
                command = formData.command,
                priority = priorityValue,
                status = formData.status,
                minute = formData.minute,
                hour = formData.hour,
                month = formData.month,
                dayOfMonth = formData.dayOfMonth,
                dayOfWeek = formData.dayOfWeek,
            )
        } else {
            println("Form insert")
            tasks.update(
                id = formData.id!!,
                title = formData.title,
                description = formData.description,
                startDate = formData.startDate.toString(),
                endDate = formData.endDate.toString(),
                dueDate = formData.dueDate.toString(),
                entryMode = formData.entryMode,
                runType = formData.runType,
                command = formData.command,
                priority = priorityValue,
                status = formData.status,
                minute = formData.minute,
                hour = formData.hour,
                month = formData.month,
                dayOfMonth = formData.dayOfMonth,
                dayOfWeek = formData.dayOfWeek
            )
        }
        println("processed")
    }
}

// Define a data class to hold the animation states
data class TransitionState(
    val alpha: Float,
    val translateY: Float
)

@Composable
fun AnimatedContentGroup(
    formData: MutableState<TaskFormData>,
    textFieldWidth: Dp,
    selectedOption: Int,
    modifier: Modifier = Modifier
) {
    val options = listOf(0, 1, 2) // Assuming 3 options for simplicity
    val transitionStates = remember { mutableStateMapOf<Int, TransitionState>() }

    options.forEach { option ->
        val isSelected = option == selectedOption
        val targetAlpha = if (isSelected) 1f else 0f
        val targetTranslationY = if (isSelected) 0f else 30f  // Offset for hidden content

        val alphaState by animateFloatAsState(
            targetValue = targetAlpha,
            animationSpec = tween(500)
        )
        val translationYState by animateFloatAsState(
            targetValue = targetTranslationY,
            animationSpec = tween(500)
        )

        transitionStates[option] = TransitionState(
            alpha = alphaState,
            translateY = translationYState
        )
    }

    Box(modifier = modifier) {
        options.forEach { option ->
            val transition = transitionStates[option]!!
            // Only display the content if it should be visible or is currently animating out
            if (transition.alpha > 0f) {
                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            alpha = transition.alpha,
                            translationY = transition.translateY
                        )
                        .fillMaxWidth()
                ) {
                    when (option) {
                        0 -> OptionZeroContent(formData, textFieldWidth)
                        1 -> FormCustom(formData, textFieldWidth)
                        2 -> FormPro(formData, textFieldWidth)
                    }
                }
            }
        }
    }
}

@Composable
fun FormPro(formData: MutableState<TaskFormData>, textFieldWidth: Dp, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        TextField(
            value = formData.value.minute,
            onValueChange = { formData.value = formData.value.copy(minute = it) },
            label = { Text("Minute (0-59 or *)") },
            modifier = Modifier.width(textFieldWidth)
        )
        TextField(
            value = formData.value.hour,
            onValueChange = { formData.value = formData.value.copy(hour = it) },
            label = { Text("Hour (0-23 or *)") },
            modifier = Modifier.width(textFieldWidth)
        )
        TextField(
            value = formData.value.dayOfMonth,
            onValueChange = { formData.value = formData.value.copy(dayOfMonth = it) },
            label = { Text("Day of Month (1-31 or *)") },
            modifier = Modifier.width(textFieldWidth)
        )
        TextField(
            value = formData.value.month,
            onValueChange = { formData.value = formData.value.copy(month = it) },
            label = { Text("Month (1-12 or *)") },
            modifier = Modifier.width(textFieldWidth)
        )
        TextField(
            value = formData.value.dayOfWeek,
            onValueChange = { formData.value = formData.value.copy(dayOfWeek = it) },
            label = { Text("Day of Week (0-6 for Sunday to Saturday or *)") },
            modifier = Modifier.width(textFieldWidth)
        )

        Spacer(modifier = Modifier.padding(top = 8.dp))
        Text(text = "Start Date", style = MaterialTheme.typography.subtitle1)
        DatePickerRow(
            initialDate = formData.value.startDate,
            onDateChanged = { newDate ->
                formData.value = formData.value.copy(startDate = newDate)
                println("Selected date: $newDate")
            }
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))

        Text(text = "End Date", style = MaterialTheme.typography.subtitle1)
        DatePickerRow(
            initialDate = formData.value.endDate,
            onDateChanged = { newDate ->
                formData.value = formData.value.copy(endDate = newDate)
                println("Selected date: $newDate")
            }
        )
    }
}

@Composable
fun FormCustom(formData: MutableState<TaskFormData>, textFieldWidth: Dp, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            var expanded by remember { mutableStateOf(false) }

            TextField(
                value = "1",
                onValueChange = { },
                label = { Text("Repeat Every") },
                modifier = Modifier.width(textFieldWidth / 2)
            )

            TextField(
                modifier = Modifier.width(textFieldWidth / 2),
                value = "Hours",
                onValueChange = { },
                label = { Text("Interval") },
                trailingIcon = {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                readOnly = true // Make the TextField read-only
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(onClick = { }) { Text("Minutes") }
                DropdownMenuItem(onClick = { }) { Text("Hours") }
                DropdownMenuItem(onClick = { }) { Text("Days") }
                DropdownMenuItem(onClick = { }) { Text("Week") }
                DropdownMenuItem(onClick = { }) { Text("Month") }
            }
        }


        var selectedOptions by remember { mutableStateOf(listOf(2, 3, 5)) }
        MultiSelectableButtonGroup(
            options = listOf("S", "M", "T", "W", "T", "F", "S"),
            selectedOptions = selectedOptions,
            onSelected = { index ->
                selectedOptions = if (index in selectedOptions) {
                    selectedOptions - index // Remove the index if it's already selected
                } else {
                    selectedOptions + index // Add the index if it's not selected
                }
            }
        )


        Text(text = "Start Date", style = MaterialTheme.typography.subtitle1)
        DatePickerRow(
            initialDate = formData.value.startDate,
            onDateChanged = { newDate ->
                formData.value = formData.value.copy(startDate = newDate)
                println("Selected date: $newDate")
            }
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))

        Text(text = "End Date", style = MaterialTheme.typography.subtitle1)
        DatePickerRow(
            initialDate = formData.value.endDate,
            onDateChanged = { newDate ->
                formData.value = formData.value.copy(endDate = newDate)
                println("Selected date: $newDate")
            }
        )
    }
}



@Composable
fun OptionZeroContent(formData: MutableState<TaskFormData>, textFieldWidth: Dp, modifier: Modifier = Modifier) {
    Text("Option 0 Content", modifier = modifier)
}


@Composable
fun TaskFormUi(
    formData: MutableState<TaskFormData>,
    scrollState: ScrollState,
    selectedOption: MutableState<Int>,
    buttonLabels: List<String>,
    onFormSubmit: (TaskFormData) -> Unit,
    innerPadding: PaddingValues
) {
    val navigator = LocalNavigator.current
    val textFieldWidth = 420.dp
    val animationProgress by animateFloatAsState(targetValue = if (selectedOption.value == 2) 1f else 0f)

    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .width(textFieldWidth)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = formData.value.title,
                        onValueChange = { formData.value = formData.value.copy(title = it) },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = formData.value.description,
                        onValueChange = { formData.value = formData.value.copy(description = it) },
                        label = { Text("Description") },
                        modifier = Modifier.width(textFieldWidth)
                    )
                    TextField(
                        value = formData.value.dueDate?.toString() ?: "",
                        onValueChange = { value ->
                            formData.value = formData.value.copy(dueDate = parseToLocalDateTime(value))
                        },
                        label = { Text("Due Date (YYYY-MM-DD HH:MM:SS)") },
                        modifier = Modifier.width(textFieldWidth)
                    )
                    TextField(
                        value = formData.value.priority,
                        onValueChange = { formData.value = formData.value.copy(priority = it) },
                        label = { Text("Priority (1 for high, 2 for medium, 3 for low)") },
                        modifier = Modifier.width(textFieldWidth)
                    )
                    TextField(
                        value = formData.value.status,
                        onValueChange = { formData.value = formData.value.copy(status = it) },
                        label = { Text("Status (e.g., pending, in progress, completed)") },
                        modifier = Modifier.width(textFieldWidth)
                    )
                    TextField(
                        value = formData.value.command,
                        onValueChange = { formData.value = formData.value.copy(command = it) },
                        label = { Text("Command") },
                        modifier = Modifier.width(textFieldWidth)
                    )

                    SingleSelectableButtonGroup(
                        options = buttonLabels,
                        selectedOption = selectedOption.value,
                        onSelected = { index ->
                            selectedOption.value = index
                        }
                    )

                    AnimatedContentGroup(formData, textFieldWidth, selectedOption = selectedOption.value)

                    Spacer(Modifier.padding(top = 24.dp))

                    Button(onClick = {
                        onFormSubmit(formData.value)
                        navigator?.pop()
                    }) {
                        Text("Submit", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun PreviewTaskFormUi() {
    val initialFormData = TaskFormData(
        id = "",
        title = "",
        description = "",
        dueDate = null,
        priority = "",
        status = "",
        command = "",
        minute = "",
        hour = "",
        dayOfMonth = "",
        month = "",
        dayOfWeek = "",
    )
    val formData = remember { mutableStateOf(initialFormData) }
    val scrollState = rememberScrollState()
    val selectedOption = remember { mutableStateOf(1) }
    val buttonLabels = listOf("Preset", "Custom", "Pro")

    TaskFormUi(
        formData = formData,
        scrollState = scrollState,
        selectedOption = selectedOption,
        buttonLabels = buttonLabels,
        onFormSubmit = { },
        innerPadding = PaddingValues(0.dp)
    )
}

@Composable
@Preview
fun PreviewFormCustom() {
    val initialFormData = TaskFormData(
        id = "",
        title = "",
        description = "",
        dueDate = null,
        priority = "",
        status = "",
        command = "",
        minute = "",
        hour = "",
        dayOfMonth = "",
        month = "",
        dayOfWeek = "",
        )
    val formData = remember { mutableStateOf(initialFormData) }
    FormCustom(formData, 300.dp, Modifier.padding(all=2.dp))
}


@Composable
@Preview
fun PreviewFormPro() {
    val initialFormData = TaskFormData(
        id = "",
        title = "",
        description = "",
        dueDate = null,
        priority = "",
        status = "",
        command = "",
        minute = "",
        hour = "",
        dayOfMonth = "",
        month = "",
        dayOfWeek = "",
        )
    val formData = remember { mutableStateOf(initialFormData) }
    FormPro(formData, 300.dp, Modifier.padding(all=2.dp))
}