package com.example.data

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Moshi structures for request/response
    data class ContentPart(val text: String)
    data class Content(val parts: List<ContentPart>)
    data class SystemInstruction(val parts: List<ContentPart>)
    data class GenerateContentRequest(
        val contents: List<Content>,
        val systemInstruction: SystemInstruction? = null
    )

    data class Candidate(val content: Content?)
    data class GenerateContentResponse(val candidates: List<Candidate>?)

    suspend fun generateSupportResponse(prompt: String, chatHistory: List<ChatMessage>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Hi there! I am your AI Support bot. I'm currently running in demo mode since your Gemini API key is not fully configured, but I can tell you that LEARN & EARN has amazing packages (Mini: ₹399, Pro: ₹699, Premium: ₹999, Premium Plus: ₹1299) to help you learn and build multiple income streams!"
        }

        // Map chat history to Content objects
        val contents = chatHistory.map { msg ->
            Content(parts = listOf(ContentPart(msg.message)))
        } + Content(parts = listOf(ContentPart(prompt)))

        val sysInstruction = SystemInstruction(
            parts = listOf(
                ContentPart(
                    "You are the official AI Support Chatbot of 'LEARN & EARN'.\n" +
                    "LEARN & EARN is an educational and affiliate platform with the tagline: 'Learn Skills. Build Income. Grow Your Future.'\n" +
                    "Key Offerings:\n" +
                    "- Mini Package (₹399): Basic learning, beginner course, limited earnings.\n" +
                    "- Pro Package (₹699): Advanced learning, better income, premium video courses.\n" +
                    "- Premium Package (₹999): Premium content, high income, priority support.\n" +
                    "- Premium Plus (₹1299): VIP Access, maximum earnings, personal support.\n" +
                    "Categories of courses: Digital Marketing, Instagram Growth, Facebook Ads, Affiliate Marketing, Canva Design, CapCut Editing, AI Marketing, YouTube Growth.\n" +
                    "Minimum Withdrawal: ₹200. Support Whatsapp: +917384091833.\n" +
                    "Help the user with any inquiries about earnings, courses, packages, referral system, daily bonuses, and how to maximize their income. Keep your tone highly professional, encouraging, brief, and business-focused."
                )
            )
        )

        val requestPayload = GenerateContentRequest(
            contents = contents,
            systemInstruction = sysInstruction
        )

        val requestAdapter = moshi.adapter(GenerateContentRequest::class.java)
        val responseAdapter = moshi.adapter(GenerateContentResponse::class.java)

        val jsonRequest = requestAdapter.toJson(requestPayload)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toRequestBody(mediaType)

        val url = "$BASE_URL?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyString = response.body?.string() ?: ""
                val resObj = responseAdapter.fromJson(bodyString)
                val aiReply = resObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                aiReply ?: "I'm sorry, I couldn't generate a response right now. Please try again or contact support at +917384091833."
            } else {
                Log.e(TAG, "Request failed: ${response.code} ${response.message}")
                "Our AI support bot is currently experiencing heavy traffic. Please feel free to reach our official Support team via WhatsApp by clicking the icon at the bottom right!"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            "No internet connection. Please check your network and try again."
        }
    }
}
