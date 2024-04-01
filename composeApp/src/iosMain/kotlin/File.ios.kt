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
// Implement any initialization logic needed for the iOS side.
// This could involve storing a reference to a UIViewController or similar.
}

//// iOS Implementation in Swift
//import Foundation
//import UIKit
//
//class FilePickerDelegate: NSObject, UIDocumentPickerDelegate {
//    var onResult: ((String?) -> Void)?
//
//    init(onResult: @escaping ((String?) -> Void)) {
//        self.onResult = onResult
//    }
//
//    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
//        // Assuming you want the path of the selected file
//        let path = urls.first?.path
//                onResult?(path)
//    }
//
//    func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
//        onResult?(nil)
//    }
//}
//
//func chooseFileSaveLocation(onResult: @escaping (String?) -> Void) {
//    let documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: [.content], asCopy: true)
//    let delegate = FilePickerDelegate(onResult: onResult)
//    documentPicker.delegate = delegate
//
//    // Present the document picker from your current view controller
//    // You need to retrieve the current UIViewController to present the document picker
//    // This is a simplified example; in a real app, you would need to handle this more robustly
//    UIApplication.shared.windows.first?.rootViewController?.present(documentPicker, animated: true, completion: nil)
//}
//
////@objc class FileIO: NSObject {
////    @objc static func saveToFile(content: String, filePath: String, onComplete: @escaping (Bool) -> Void) {
////        guard let url = URL(string: filePath) else {
////            onComplete(false)
////            return
////        }
////
////        do {
////            try content.write(to: url, atomically: true, encoding: .utf8)
////onComplete(true)
////        } catch {
////            print("Error saving file: \(error)")
////            onComplete(false)
////        }
////    }
////}
//
//actual fun saveToFile(content: String, filePath: String, onComplete: (Boolean) -> Unit) {
//    val fileIO = FileIO()
//    fileIO.saveToFile(content, filePath) { success ->
//        onComplete(success)
//    }
//}
//
////import UIKit
////import MobileCoreServices
////
////class FilePicker: NSObject, UIDocumentPickerDelegate {
////    var onFilePicked: ((String?) -> Void)?
////
////    func pickFile(viewController: UIViewController) {
////        let documentPicker = UIDocumentPickerViewController(documentTypes: [kUTTypeContent as String], in: .import)
////        documentPicker.delegate = self
////        documentPicker.modalPresentationStyle = .formSheet
////                viewController.present(documentPicker, animated: true, completion: nil)
////    }
////
////    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
////        guard let url = urls.first else {
////            onFilePicked?(nil)
////            return
////        }
////        // You might want to check if the URL is a file URL and perhaps copy it to a local directory
////        onFilePicked?(url.path)
////    }
////
////    func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
////        onFilePicked?(nil)
////    }
////}
////
////// Global instance to retain the delegate
////let filePicker = FilePicker()
////
////@objc func pickFileFromKotlin(onFilePicked: @escaping (String?) -> Void) {
////    // Assuming you have a way to access the current UIViewController
////    if let viewController = UIApplication.shared.keyWindow?.rootViewController {
////        filePicker.onFilePicked = onFilePicked
////        filePicker.pickFile(viewController: viewController)
////    }
////}
//
//// iOSMain source set
//actual fun pickFile(onResult: (String?) -> Unit) {
//    // Call the Swift function here
//    pickFileFromKotlin(onResult)
//}
//
//
////import Foundation
////
////@objc class FileIO: NSObject {
////    @objc static func readTextFromFile(atPath filePath: String, completion: @escaping (String?) -> Void) {
////        let url = URL(fileURLWithPath: filePath)
////        do {
////            let content = try String(contentsOf: url, encoding: .utf8)
////completion(content)
////        } catch {
////            print("Error reading file: \(error)")
////            completion(nil)
////        }
////    }
////}
//
//actual fun readTextFromFile(filePath: String, onResult: (String?) -> Unit) {
//    // Assuming you have a mechanism to call Swift functions directly
//    FileIO.readTextFromFile(atPath: filePath) { content in
//            onResult(content)
//    }
//}
