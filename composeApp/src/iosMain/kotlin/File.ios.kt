import filePicker.FilePickerBridge
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.getOriginalKotlinClass
import kotlinx.cinterop.useContents
import kotlinx.cinterop.ObjCClass
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*

fun getCurrentWindowScene(): UIWindowScene? {
    val scenes = UIApplication.sharedApplication.connectedScenes
    for (scene in scenes) {
        if (scene is UIWindowScene) {
            return scene
        }
    }
    return null
}

fun getRootViewController(): UIViewController? {
    // Obtaining the current window scene
    val windowScene = getCurrentWindowScene()
    // Iterating through the windows of the window scene to find the key window
    val keyWindow: UIWindow? = windowScene?.windows?.firstOrNull { window ->
        window is UIWindow && window.isKeyWindow()
    } as UIWindow?
    // Returning the root view controller of the key window
    return keyWindow?.rootViewController
}

@OptIn(kotlinx.cinterop.BetaInteropApi::class)
fun createTempFile(content: String, fileName: String): String? {
    val tempDirectory = NSTemporaryDirectory()
    val fileURL = "$tempDirectory/$fileName"

    val data: NSData? = NSString.create(string = content).dataUsingEncoding(NSUTF8StringEncoding)

    return data?.let {
        // Write the NSData to file
        if (it.writeToFile(fileURL, atomically = true)) {
            fileURL // Return the file URL if successful
        } else {
            println("Failed to write to temp file")
            null // Return null if the write operation failed
        }
    } ?: run {
        println("Failed to convert string to NSData")
        null
    }
}

@OptIn(ExperimentalForeignApi::class)
fun shareFileFromUrl(fileUrl: String, viewController: UIViewController) {
    val activityItems = listOf(NSURL(fileURLWithPath = fileUrl))
    val activityViewController = UIActivityViewController(activityItems = activityItems, applicationActivities = null)

    // For iPad, setting the presentation style and source view
    val popover = activityViewController.popoverPresentationController
    popover?.sourceView = viewController.view

    viewController.view.bounds.useContents {
        popover?.sourceRect = CGRectMake(size.width / 2.0, size.height / 2.0, 0.0, 0.0)
    }

    viewController.presentViewController(activityViewController, animated = true, completion = null)
}


@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
class FileChooserImpl : FileChooser {
    // on iOS a user cannot select a file
    override fun chooseFileSaveLocation(onResult: (String?) -> Unit) {
        onResult("data.json")
    }

    override fun saveToFile(content: String, filePath: String, onComplete: (Boolean) -> Unit) {
        val tempFile = createTempFile(content, filePath)
        if (tempFile != null) {
            val rootViewController = getRootViewController()
            rootViewController?.let {
                shareFileFromUrl(tempFile, rootViewController)
            }
            onComplete(true)
        } else {
            onComplete(false)
        }
    }

    override fun pickFile(onResult: (String?) -> Unit) {
        FilePickerCallbackHolder.registerCallback { filePath ->
            println("File selected: $filePath")
            onResult(filePath)
        }

        FilePickerBridge.pickFileWithViewController { filePath ->
            println("filepath: $filePath")
            onResult(filePath)
        }
    }

    override fun readTextFromFile(filePath: String, onResult: (String?) -> Unit) {
        FilePickerBridge.readTextFromFileWithFilePath(filePath) { data ->
            println(data)
            onResult(data)
        }
    }
}

actual var fileChooser: FileChooser? = FileChooserImpl()

actual fun initializeFileChooser(activity: Any) {
}