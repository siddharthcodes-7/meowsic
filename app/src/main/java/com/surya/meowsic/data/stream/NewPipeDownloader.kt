package com.surya.meomusic.data.stream

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Request as OkHttpRequest
import java.util.concurrent.TimeUnit

class NewPipeDownloader : Downloader() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    override fun execute(request: Request): Response {

        val builder = OkHttpRequest.Builder()
            .url(request.url())
            .method(
                request.httpMethod(),
                request.dataToSend()?.let {
                    RequestBody.create(null, it)
                }
            )

        request.headers().forEach { (key, values) ->
            values.forEach { value ->
                builder.addHeader(key, value)
            }
        }

        val response = client.newCall(builder.build()).execute()

        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            response.body?.string(),
            response.request.url.toString()
        )
    }
}