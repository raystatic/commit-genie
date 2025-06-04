package com.github.raystatic.commitgenie

import java.io.File

object GitUtils {
    fun getGitDiff(gitRoot: String): String {
        return try {
            val unstaged = ProcessBuilder("git", "diff")
                .directory(File(gitRoot))
                .redirectErrorStream(true)
                .start()
                .inputStream.bufferedReader().readText()

            val staged = ProcessBuilder("git", "diff", "--cached")
                .directory(File(gitRoot))
                .redirectErrorStream(true)
                .start()
                .inputStream.bufferedReader().readText()

            staged + "\n" + unstaged
        } catch (e: Exception) {
            e.printStackTrace()
            println("Exception while fetching git diff: ${e.message}")
            ""
        }
    }
}

