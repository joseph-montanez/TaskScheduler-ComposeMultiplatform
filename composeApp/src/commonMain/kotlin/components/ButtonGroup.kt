package components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GenericSelectableButtonGroup(
    options: List<String>,
    isSelected: (Int) -> Boolean, // Function to determine if a button is selected
    onSelected: (Int) -> Unit
) {
    val primaryColor = MaterialTheme.colors.primary
    val onPrimaryColor = MaterialTheme.colors.onPrimary
    val surfaceColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.BackgroundOpacity)
    val onSurfaceColor = MaterialTheme.colors.onSurface

    
    Row(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                options.size - 1 -> RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                else -> RoundedCornerShape(0.dp)
            }
            Button(
                onClick = { onSelected(index) },
                modifier = Modifier.weight(1f),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (isSelected(index)) primaryColor else surfaceColor,
                    contentColor = if (isSelected(index)) onPrimaryColor else onSurfaceColor,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected(index)) onPrimaryColor else onSurfaceColor
                )
            }
        }
    }
}

@Composable
fun SingleSelectableButtonGroup(
    options: List<String>,
    selectedOption: Int,
    onSelected: (Int) -> Unit
) {
    GenericSelectableButtonGroup(
        options = options,
        isSelected = { it == selectedOption },
        onSelected = onSelected
    )
}

@Composable
fun MultiSelectableButtonGroup(
    options: List<String>,
    selectedOptions: List<Int>,
    onSelected: (Int) -> Unit
) {
    GenericSelectableButtonGroup(
        options = options,
        isSelected = { it in selectedOptions },
        onSelected = onSelected
    )
}

@Preview
@Composable
fun SelectableButtonGroupPreview() {
    var selectedOption by remember { mutableStateOf(1) } // Remember the selected option
    val buttonLabels = listOf("Option 1", "Option 2", "Option 3") // Define the button labels

    // Call the SelectableButtonGroup composable function
    SingleSelectableButtonGroup(
        options = buttonLabels,
        selectedOption = selectedOption,
        onSelected = { index ->
            selectedOption = index // Update the state when a button is clicked
        }
    )
}

@Preview
@Composable
fun MySelectableButtonGroupScreen() {
    val options = listOf("S", "M", "T", "W", "T", "F", "S")
    var selectedOptions by remember { mutableStateOf(listOf<Int>(2, 5)) }

    val DarkColorPalette = darkColors(
        primary = Color(0xFF00ff76),
        primaryVariant = Color(0xFF00cb65),
        secondary = Color(0xFF6200EE),
        background = Color(0xFF121212), // Common dark theme background
        surface = Color(0xFF1E1E1E), // Slightly lighter than background for elevation effect
        onPrimary = Color.Black, // Assuming primary is light enough for black text
        onSecondary = Color.Black, // Assuming secondary is light enough for black text
        onBackground = Color.White, // For general background text
        onSurface = Color.White, // For text on surfaces like cards, ensuring readability
        onError = Color.White // Typically white for contrast with the error color
    )

    MaterialTheme(
        colors = DarkColorPalette,
        typography = Typography(),
        shapes = Shapes(),
    ) {
        MultiSelectableButtonGroup(
            options = options,
            selectedOptions = selectedOptions,
            onSelected = { index ->
                selectedOptions = if (index in selectedOptions) {
                    selectedOptions - index // Remove the index if it's already selected
                } else {
                    selectedOptions + index // Add the index if it's not selected
                }
            }
        )
    }
    
}