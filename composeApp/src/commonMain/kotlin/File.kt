interface FileChooser {
    fun chooseFileSaveLocation(onResult: (String?) -> Unit)
    fun saveToFile(content: String, filePath: String, onComplete: (Boolean) -> Unit)
    fun pickFile(onResult: (String?) -> Unit)
    fun readTextFromFile(filePath: String, onResult: (String?) -> Unit)
}

expect var fileChooser: FileChooser?
expect fun initializeFileChooser(activity: Any)
