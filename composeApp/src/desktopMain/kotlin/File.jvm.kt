import java.io.File
import java.io.IOException
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

actual var fileChooser: FileChooser? = null

actual fun initializeFileChooser(activity: Any) {
    fileChooser = FileChooserImpl()
}


class FileChooserImpl : FileChooser {

    override fun chooseFileSaveLocation(onResult: (String?) -> Unit) {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Select File Location for JSON Export"
            fileSelectionMode = JFileChooser.FILES_ONLY
            fileFilter = FileNameExtensionFilter("JSON Files", "json")
            isAcceptAllFileFilterUsed = false
        }

        SwingUtilities.invokeLater {
            val userSelection = fileChooser.showSaveDialog(null)
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                onResult(fileChooser.selectedFile.absolutePath)
            } else {
                onResult(null)
            }
        }
    }

    override fun saveToFile(content: String, filePath: String, onComplete: (Boolean) -> Unit) {
        try {
            File(filePath).writeText(content)
            onComplete(true)
        } catch (e: IOException) {
            println("Error saving file: ${e.message}")
            onComplete(false)
        }
    }

    override fun pickFile(onResult: (String?) -> Unit) {
        SwingUtilities.invokeLater {
            val fileChooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.FILES_ONLY
            }
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                onResult(fileChooser.selectedFile.absolutePath)
            } else {
                onResult(null)
            }
        }
    }

    override fun readTextFromFile(filePath: String, onResult: (String?) -> Unit) {
        try {
            val content = File(filePath).readText()
            onResult(content)
        } catch (e: Exception) {
            println("Error reading file: ${e.message}")
            onResult(null)
        }
    }
}