package com.github.raystatic.commitgenie

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.ChangeListManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class CommitMessageAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val gitRoot = ProjectLevelVcsManager.getInstance(project).allVcsRoots.firstOrNull()?.path?.path ?: run {
            Messages.showErrorDialog(project, "Could not determine Git root.", "CommitGenius")
            return
        }

        val changes = ChangeListManager.getInstance(project).allChanges
        if (changes.isEmpty()) {
            Messages.showInfoMessage(project, "No staged changes found.", "CommitGenius")
            return
        }

        val diffSummary = getGitDiff(gitRoot)

        if (diffSummary.isEmpty()) {
            Messages.showInfoMessage(project, "No staged changes found.", "CommitGenius")
            return
        }

        println("diffSummary: $diffSummary")


//        val apiKey = System.getenv("OPENAI_API_KEY")
        val apiKey = "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"
        if (apiKey.isNullOrEmpty()) {
            Messages.showErrorDialog(project, "Missing OPENAI_API_KEY environment variable.", "CommitGenius")
            return
        }

        val client = OkHttpClient()

//        val json ="""
//            {
//              "model": "gpt-3.5-turbo",
//              "messages": [
//                {
//                  "role": "system",
//                  "content": "You're a helpful assistant that writes clear, professional Git commit messages based on code diffs."
//                },
//                {
//                  "role": "user",
//                  "content": "Generate a commit message for the following git diff:\n\n$diffSummary"
//                }
//              ]
//            }
//        """.trimIndent()

        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", "You're a helpful assistant that writes clear, professional Git commit messages based on code diffs."))
            put(JSONObject().put("role", "user").put("content", "Generate a commit message for the following git diff:\n\n$diffSummary"))
        }

        val json = JSONObject()
            .put("model", "gpt-3.5-turbo")
            .put("messages", messages)
            .toString()

        println("json: $json")

        val body = json.toRequestBody(contentType = "application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        println("diffSummary: $request")
        println("body: $body")
        println("body2: ${request.body}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog("Failed to fetch message: ${e.localizedMessage}", "Commit Genius")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody)
                        errorJson.optJSONObject("error")?.optString("message") ?: "Unknown error from API."
                    } catch (e: Exception) {
                        "Error parsing error response: ${e.localizedMessage}"
                    }

                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog("Error in generating commit message ${response.code}: $errorMessage", "Commit Genius")
                    }
                    return
                }

                try {


                    val jsonObject = JSONObject(responseBody)

                    // Check for 'error' key even in a 200 OK response
                    if (jsonObject.has("error")) {
                        val errorMsg = jsonObject.getJSONObject("error").optString("message", "Unknown error")
                        throw JSONException(errorMsg)
                    }


                    // Check if "choices" is present and is an array with at least one element
                    if (!jsonObject.has("choices") || jsonObject.getJSONArray("choices").length() == 0) {
                        throw JSONException("Missing or empty 'choices' array in response")
                    }

                    val firstChoice = jsonObject.getJSONArray("choices").getJSONObject(0)

                    // Check if "message" and "content" exist
                    if (!firstChoice.has("message")) {
                        throw JSONException("Missing 'message' in first choice")
                    }

                    val messageObj = firstChoice.getJSONObject("message")

                    if (!messageObj.has("content")) {
                        throw JSONException("Missing 'content' in message object")
                    }

                    val commitMessage = messageObj.getString("content")

                    ApplicationManager.getApplication().invokeLater {
                        Messages.showInfoMessage(commitMessage, "Generated Commit Message")
                    }

                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog("Error in generating commit message: ${e.localizedMessage}", "Commit Genius")
                    }
                }
            }

        })
    }

    fun getGitDiff(gitRoot: String): String {
        val process = ProcessBuilder("git", "diff")
            .directory(java.io.File(gitRoot)) // set the working directory
            .redirectErrorStream(true)
            .start()

        return process.inputStream.bufferedReader().readText()
    }


    fun isGitRepo(): Boolean {
        val process = ProcessBuilder("git", "rev-parse", "--is-inside-work-tree")
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText().trim()
        return output == "true"
    }

}