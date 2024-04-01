import libs.CommandResult
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

actual fun runTaskCommand(command: String): CommandResult {
    try {
        val process = Runtime.getRuntime().exec(command)

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

        val output = reader.readText()
        val errorOutput = errorReader.readText()

        val exitVal = process.waitFor()
        if (exitVal == 0) {
            println("Success!")
            println(output)
        } else {
            println("Error!")
            println(errorOutput)
        }

        return CommandResult(output, errorOutput, exitVal)
    } catch (e: IOException) {
        e.printStackTrace()
        return CommandResult("", e.message ?: "IOException occurred", -1)
    } catch (e: InterruptedException) {
        e.printStackTrace()
        return CommandResult("", e.message ?: "InterruptedException occurred", -1)
    }
}