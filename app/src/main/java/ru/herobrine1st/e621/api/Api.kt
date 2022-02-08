package ru.herobrine1st.e621.api

import android.util.Log
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.herobrine1st.e621.BuildConfig
import ru.herobrine1st.e621.api.model.Post
import ru.herobrine1st.e621.api.model.PostsEndpoint

object Api {
    private const val TAG = "API"
    private val okHttpClient = OkHttpClient()
    private var credentials: String? = null
    private var login: String? = null
    private val objectMapper = jacksonObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    private fun updateCredentialsInternal(login: String?, apiKey: String?) {
        this.login = login
        credentials = if (login != null && apiKey != null) Credentials.basic(login, apiKey)
        else null
    }

    /**
     * Tries to login with provided credentials and saves them if succeeded
     * @param login Login
     * @param apiKey Api key
     * @return True if credentials valid
     */
    fun checkCredentials(login: String, apiKey: String): Boolean {
        val req = Request.Builder()
            .url(
                HttpUrl.Builder()
                    .scheme("https")
                    .host(BuildConfig.API_URL)
                    .addPathSegments("users/$login.json")
                    .build()
            )
            .header("Authorization", Credentials.basic(login, apiKey))
            .addHeader("Accept", "application/json")
            .build()
        okHttpClient.newCall(req).execute().use {
            return if (it.isSuccessful) {
                updateCredentialsInternal(login, apiKey)
                true
            } else {
                false
            }
        }
    }

    fun logout() {
        updateCredentialsInternal(null, null)
    }

    private fun requestBuilder(): Request.Builder {
        return Request.Builder()
            .apply { credentials?.let { header("Authorization", it) } }
            .addHeader("Accept", "application/json")
            .header("User-Agent", BuildConfig.USER_AGENT)
    }

    fun getPosts(tags: String, page: Int = 1, limit: Int? = null): List<Post> {
        val req = requestBuilder()
            .url(
                HttpUrl.Builder().apply {
                    scheme("https")
                    host(BuildConfig.API_URL)
                    addPathSegments("posts.json")
                    addEncodedQueryParameter("tags", tags)
                    limit?.let { addQueryParameter("limit", it.toString()) }
                    addQueryParameter("page", page.toString())
                }.build().also { Log.d(TAG, it.toString()) }
            )
            .build()
        okHttpClient.newCall(req).execute().use {
            it.body!!.use { body ->
                return objectMapper.readValue<PostsEndpoint>(body.charStream()).posts
            }
        }
    }

    fun getBlacklistedTags(): List<String> {
        val req = requestBuilder()
            .url(
                HttpUrl.Builder()
                    .scheme("https")
                    .host(BuildConfig.API_URL)
                    .addPathSegments("users/$login.json")
                    .build()
            )
            .build()
        okHttpClient.newCall(req).execute().use {
            it.body!!.use { body ->
                return objectMapper.readValue<ObjectNode>(body.charStream())
                    .get("blacklistedTags").asText()
                    .split("\n")
            }
        }
    }
}