package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.DailyStudyEntry
import com.example.data.SubjectProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getInsights(
        entries: List<DailyStudyEntry>,
        subjects: List<SubjectProgress>
    ): List<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            Log.w(TAG, "Gemini API key is placeholder or missing. Falling back to local rule-based insights.")
            return@withContext getDefaultInsights(entries, subjects)
        }

        val prompt = buildPrompt(entries, subjects)

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini API call failed with code: ${response.code}")
                    return@withContext getDefaultInsights(entries, subjects)
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)

                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                val text = firstPart?.optString("text") ?: ""

                if (text.isNotEmpty()) {
                    return@withContext parseBullets(text)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API, falling back", e)
        }

        return@withContext getDefaultInsights(entries, subjects)
    }

    private fun buildPrompt(entries: List<DailyStudyEntry>, subjects: List<SubjectProgress>): String {
        val entryCount = entries.size
        val totalHours = entries.sumOf { it.studyHours }
        val avgHours = if (entryCount > 0) totalHours / entryCount else 0.0
        val totalPyqs = entries.sumOf { it.pyqsSolved }
        val totalMocks = entries.sumOf { it.mockTestsAttempted }

        val subjectsStr = subjects.joinToString("\n") {
            "- ${it.subjectName}: ${it.completionPercentage}% complete (PYQs: ${it.pyqsDone}, Mocks: ${it.mockTestsDone})"
        }

        return """
            You are an expert AI mentor for GATE Electrical Engineering aspirants.
            Here is the student's current preparation state data:
            - Total recorded days: $entryCount
            - Total study hours: $totalHours (Average: ${"%.1f".format(avgHours)} hours/day)
            - Total PYQs solved: $totalPyqs
            - Total mock tests attempted: $totalMocks

            Subject-wise Progress:
            $subjectsStr

            Please provide exactly 4 short, action-oriented, and highly motivational smart insights/suggestions for this student's GATE EE preparation.
            Keep them very concise (under 15 words each). Do NOT use markdown asterisks or numbering in the points themselves. Just output 4 plain text lines, each starting with a hyphen (-) or dot (•). Make sure some points contain Electrical Engineering specific technical topics (e.g. Power Systems, Machines, Control Systems, Electromagnetic Fields, Power Electronics) based on the progress.
        """.trimIndent()
    }

    private fun parseBullets(text: String): List<String> {
        return text.split("\n")
            .map { it.trim().trimStart('-', '•', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.') }
            .filter { it.isNotEmpty() }
            .take(4)
    }

    private fun getDefaultInsights(entries: List<DailyStudyEntry>, subjects: List<SubjectProgress>): List<String> {
        val list = mutableListOf<String>()
        val totalHours = entries.sumOf { it.studyHours }
        val pyqs = entries.sumOf { it.pyqsSolved }

        if (totalHours < 15.0) {
            list.add("Establish a steady routine of 4-6 hours to build core concepts.")
        } else {
            list.add("Maintaining high study consistency. Keep this active learning pace up!")
        }

        if (pyqs < 100) {
            list.add("Increase practice of core PYQs in Electrical Machines and Power Systems.")
        } else {
            list.add("Excellent progress on solving PYQs. Continue tackling more difficult ones.")
        }

        val lowSubject = subjects.minByOrNull { it.completionPercentage }
        if (lowSubject != null && lowSubject.completionPercentage < 40) {
            list.add("Allocate dedicated hours to cover fundamental concepts in ${lowSubject.subjectName}.")
        } else {
            list.add("Take a full-length 3-hour Mock Test to check timing and mental stamina.")
        }

        list.add("Control Systems and Network Theory are high scoring—ensure perfect revision.")
        return list.take(4)
    }
}
