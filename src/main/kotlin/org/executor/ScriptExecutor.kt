package org.executor

import java.io.*
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

class ScriptExecutor(val scriptFile: File) {

    fun runKotlinScript(code: String): Process {
        val scriptFileWriter = FileWriter(scriptFile)
        scriptFileWriter.write(code)
        scriptFileWriter.close()

        Logger.getLogger("ScriptExecutor").log(LogRecord(Level.INFO, "Executing script $scriptFile"))

        return ProcessBuilder("/bin/bash", "-c", "kotlinc -script ${scriptFile.absolutePath}")
            .redirectErrorStream(true)
            .start()
    }
}