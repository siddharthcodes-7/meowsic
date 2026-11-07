package com.surya.meowsic.data.stream

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class NewPipeDownloader : Downloader() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    @Throws(IOException::class)
    override fun execute(request: Request): Response {

        val headersBuilder = okhttp3.Headers.Builder()

        request.headers().forEach { (name, values) ->
            values.forEach { value ->
                headersBuilder.add(name, value)
            }
        }

        val requestBody = request.dataToSend()?.toRequestBody(
            "application/octet-stream".toMediaTypeOrNull()
        )

        val okhttpRequest = okhttp3.Request.Builder()
            .url(request.url())
            .headers(headersBuilder.build())
            .method(
                request.httpMethod(),
                if (request.httpMethod() == "GET" ||
                    request.httpMethod() == "HEAD"
                ) null else requestBody
            )
            .build()

        client.newCall(okhttpRequest).execute().use { response ->

            val responseBody = response.body?.string() ?: ""
            val latestUrl = response.request.url.toString()

            return Response(
                response.code,
                response.message,
                response.headers.toMultimap(),
                responseBody,
                latestUrl
            )
        }
    }
}