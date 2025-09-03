package com.markrogers.journal.net
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface OpenAiService { @Headers("Content-Type: application/json") @POST("v1/chat/completions") suspend fun chat(@Body body: OpenAiRequest): OpenAiResponse }
data class OpenAiRequest(val model: String = "gpt-4o-mini", val messages: List<Message>, val temperature: Double = 0.2)
data class Message(val role: String, val content: String)
data class OpenAiResponse(val choices: List<Choice>) { data class Choice(val message: Message) }
fun openAiRetrofit(apiKey: String): OpenAiService {
    val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    val auth = Interceptor { chain -> chain.proceed(chain.request().newBuilder().addHeader("Authorization", "Bearer " + apiKey).build()) }
    val client = OkHttpClient.Builder().addInterceptor(auth).addInterceptor(log).build()
    return Retrofit.Builder().baseUrl("https://api.openai.com/").client(client).addConverterFactory(GsonConverterFactory.create()).build().create(OpenAiService::class.java)
}
interface GeminiService { @Headers("Content-Type: application/json") @POST("v1beta/models/gemini-pro:generateContent") suspend fun generate(@Query("key") key: String, @Body body: GeminiRequest): GeminiResponse }
data class GeminiRequest(val contents: List<GeminiContent>)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiPart(val text: String)
data class GeminiResponse(@SerializedName("candidates") val candidates: List<Candidate>) { data class Candidate(@SerializedName("content") val content: GeminiContent?) }
fun geminiRetrofit(): GeminiService = Retrofit.Builder().baseUrl("https://generativelanguage.googleapis.com/").addConverterFactory(GsonConverterFactory.create()).build().create(GeminiService::class.java)
