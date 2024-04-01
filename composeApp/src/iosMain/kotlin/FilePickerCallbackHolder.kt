object FilePickerCallbackHolder {
    private var onFilePicked: ((String?) -> Unit)? = null

    fun registerCallback(callback: (String?) -> Unit) {
        onFilePicked = callback
    }

    fun onFileSelected(filePath: String?) {
        onFilePicked?.invoke(filePath)
    }
}