import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.BufferedReader

actual var fileChooser: FileChooser? = null

actual fun initializeFileChooser(activity: Any) {
    if (activity is ComponentActivity) {
        fileChooser = FileChooserImpl(activity).also {
            it.setupResultHandlers()
        }
    } else {
        throw IllegalArgumentException("Activity must be a ComponentActivity")
    }
}


class FileChooserImpl(
    private val activity: ComponentActivity
) : FileChooser {
    private var onFileSaved: ((String?) -> Unit)? = null
    private var onFilePicked: ((String?) -> Unit)? = null

    private val createDocumentLauncher = activity.registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        onFileSaved?.invoke(uri?.toString())
    }

    private val pickFileLauncher = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        onFilePicked?.invoke(uri?.toString())
    }

    override fun chooseFileSaveLocation(onResult: (String?) -> Unit) {
        onFileSaved = onResult
        createDocumentLauncher.launch("exported_tasks.json")
    }


    override fun saveToFile(
        content: String,
        filePath: String,
        onComplete: (Boolean) -> Unit
    ) {
        try {
            val uri = Uri.parse(filePath)
            activity.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
                onComplete(true)
            } ?: run {
                println("Failed to open output stream")
                onComplete(false)
            }
        } catch (e: Exception) {
            println("Error saving file: ${e.message}")
            onComplete(false)
        }
    }

    override fun pickFile(onResult: (String?) -> Unit) {
        onFilePicked = onResult
        pickFileLauncher.launch(arrayOf("application/json", "text/json", "text/plain")) // Adjust MIME type as needed
    }

    override fun readTextFromFile(filePath: String, onResult: (String?) -> Unit) {
        try {
            val uri: Uri = Uri.parse(filePath)
            activity.contentResolver.openInputStream(uri)?.use { inputStream ->
                val content = inputStream.bufferedReader().use(BufferedReader::readText)
                onResult(content)
            } ?: run {
                println("Failed to open input stream")
                onResult(null)
            }
        } catch (e: Exception) {
            println("Error reading file: ${e.message}")
            onResult(null)
        }
    }

    fun setupResultHandlers() {
        val createDocumentResultHandler: (Uri?) -> Unit = { uri ->
            uri?.let {
                onFileSaved?.invoke(it.toString())
            } ?: run {
                onFileSaved?.invoke(null)
            }
        }

        val pickFileResultHandler: (Uri?) -> Unit = { uri ->
            uri?.let {
                onFilePicked?.invoke(it.toString())
            } ?: run {
                onFilePicked?.invoke(null)
            }
        }
    }
}