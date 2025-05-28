package com.github.raystatic.commitgenie

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

object OpenAIClient {

    private val client = OkHttpClient()

    fun buildChatJsonPayload(diff: String): JSONObject {
        val messagesArray = JSONArray()

        val systemMessage = JSONObject()
        systemMessage.put("role", "system")
        systemMessage.put("content", "You are an AI assistant that writes professional Git commit messages based on diffs.\n\n" +
                "Follow the Conventional Commits specification: <type>: <description>.\n" +
                "Types: feat, fix, chore, docs, style, refactor, perf, test.\n" +
                "Use imperative mood and concise description.")

        val userMessage = JSONObject()
        userMessage.put("role", "user")
        userMessage.put("content", """
        Generate a commit message for the following git diff:
        
        ```diff
        $diff
        ```
    """.trimIndent())

        messagesArray.put(systemMessage)
        messagesArray.put(userMessage)

        val json = JSONObject()
        json.put("model", "gpt-3.5-turbo")
        json.put("messages", messagesArray)

        return json
    }


    fun generateCommitMessage(apiKey: String, diff: String, callback: (Result) -> Unit) {
        val json = buildChatJsonPayload(diff)

        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.Failure("Failed to fetch message: ${e.localizedMessage}"))
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    callback(Result.Failure("API Error ${response.code}: ${responseBody}"))
                    return
                }

                try {
                    val jsonObject = JSONObject(responseBody)
                    val choices = jsonObject.getJSONArray("choices")
                    if (choices.length() == 0) {
                        callback(Result.Failure("No choices returned in response."))
                        return
                    }
                    val message = choices.getJSONObject(0).getJSONObject("message").getString("content")
                    callback(Result.Success(message))
                } catch (e: Exception) {
                    callback(Result.Failure("Failed to parse response: ${e.localizedMessage}"))
                }
            }
        })
    }
}