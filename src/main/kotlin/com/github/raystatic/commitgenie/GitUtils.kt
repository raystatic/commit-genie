package com.github.raystatic.commitgenie

import java.io.File

object GitUtils {
    fun getGitDiff(gitRoot: String): String {
        return try {
            val process = ProcessBuilder("git", "diff")
                .directory(File(gitRoot))
                .redirectErrorStream(true)
                .start()
            process.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            ""
        }
    }
}