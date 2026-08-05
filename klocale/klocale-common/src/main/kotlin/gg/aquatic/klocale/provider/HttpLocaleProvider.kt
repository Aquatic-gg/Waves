package gg.aquatic.klocale.provider

import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.LocaleSerializer
import gg.aquatic.klocale.message.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class HttpLocaleProvider<M : Message<M>>(
    val url: String,
    val serializer: LocaleSerializer<Any, M>,
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
) : LocaleProvider<M> {

    override suspend fun fetch(): Map<String, Map<String, M>> = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()

        val response = try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Exception) {
            return@withContext emptyMap()
        }

        if (response.statusCode() != 200) {
            return@withContext emptyMap()
        }

        val body = response.body() ?: return@withContext emptyMap()
        return@withContext serializer.parse(body) ?: emptyMap()
    }
}