package components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DatePickerRow(
    initialDate: LocalDateTime?,
    onDateChanged: (LocalDateTime?) -> Unit
) {
    val timezone = TimeZone.currentSystemDefault()
    val currentDate = Clock.System.now().toLocalDateTime(timezone).date

    var selectedYear by remember { mutableStateOf(initialDate?.year) }
    var selectedMonth by remember { mutableStateOf(initialDate?.monthNumber) }
    var selectedDay by remember { mutableStateOf(initialDate?.dayOfMonth) }

    val daysInMonth = if (selectedYear != null && selectedMonth != null) {
        LocalDate(year = selectedYear!!, monthNumber = selectedMonth!!, dayOfMonth = 1).daysInMonth
    } else {
        31
    }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun adjustDayOfMonth() {
        if (selectedYear != null && selectedMonth != null) {
            val maxDays = LocalDate(year = selectedYear!!, monthNumber = selectedMonth!!, dayOfMonth = 1).daysInMonth

            selectedDay?.let {
                if (it > maxDays) {
                    selectedDay = maxDays
                }
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth()) {

        val monthItems = monthNames.map { DropdownItem.StringItem(it) }
        DropdownSelector(
            label = "Month",
            items = monthItems,
            selectedItem = selectedMonth?.let { DropdownItem.StringItem(monthNames[it - 1]) },
            onItemSelect = { item ->
                if (item is DropdownItem.StringItem) {
                    selectedMonth = monthNames.indexOf(item.string) + 1
                }
            },
            modifier = Modifier.weight(1.15f)
        )


        Spacer(modifier = Modifier.width(6.dp))

        val dayItems = (1..daysInMonth).map { DropdownItem.NumberItem(it) }
        DropdownSelector(
            label = "Day",
            items = dayItems,
            selectedItem = selectedMonth?.let { DropdownItem.StringItem(monthNames[it - 1]) },
            onItemSelect = { item ->
                if (item is DropdownItem.NumberItem) {
                    selectedDay = item.number
                }
                // Rest of the logic remains the same
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        val yearItems = (currentYear..currentYear + 100).map { DropdownItem.NumberItem(it) }
        DropdownSelector(
            label = "Year",
            items = yearItems,
            selectedItem = selectedYear?.let { DropdownItem.NumberItem(it) },
            onItemSelect = { item ->
                if (item is DropdownItem.NumberItem) {
                    selectedYear = item.number
                    adjustDayOfMonth()
                    onDateChanged(
                        LocalDateTime(
                            year = selectedYear ?: currentYear,
                            monthNumber = selectedMonth ?: currentDate.monthNumber,
                            dayOfMonth = selectedDay ?: currentDate.dayOfMonth,
                            0, 0, 0
                        )
                    )
                }
            },
            modifier = Modifier.weight(1f) // Adjust weight as needed
        )
    }

    LaunchedEffect(selectedYear, selectedMonth, selectedDay) {
        // Only call onDateChanged with a non-null LocalDateTime if all components are non-null
        if (selectedYear != null && selectedMonth != null && selectedDay != null) {
            onDateChanged(
                LocalDateTime(
                    year = selectedYear!!,
                    monthNumber = selectedMonth!!,
                    dayOfMonth = selectedDay!!,
                    0, 0, 0
                )
            )
        } else {
            onDateChanged(null)
        }
    }
}

sealed class DropdownItem {
    data class NumberItem(val number: Int) : DropdownItem() {
        override fun toString(): String = number.toString()
    }
    data class StringItem(val string: String) : DropdownItem() {
        override fun toString(): String = string
    }
}

@Composable
fun DropdownSelector(
    label: String,
    items: List<DropdownItem>,
    selectedItem: DropdownItem?,
    onItemSelect: (DropdownItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextField(
            value = selectedItem?.toString() ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            singleLine = true, 
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown",
                    modifier = Modifier.clickable { expanded = true }
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onItemSelect(item)
                        expanded = false
                    }
                ) {
                    Text(text = item.toString())
                }
            }
        }
    }
}

// Extension property to get the number of days in a month for a LocalDate
private val LocalDate.daysInMonth: Int
    get() = when (this.monthNumber) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (this.isLeapYear) 29 else 28
        else -> throw IllegalStateException("Invalid month")
    }

// Extension property to check if a year is a leap year
private val LocalDate.isLeapYear: Boolean
    get() = (this.year % 4 == 0 && this.year % 100 != 0) || (this.year % 400 == 0)

@Preview
@Composable
fun PreviewDatePicker() {
    Box {
        DatePickerRow(
            initialDate = LocalDateTime(2022, 3, 14, 0, 0, 0),
            onDateChanged = { newDate ->
                println("Selected date: $newDate")
            }
        )
    }
}