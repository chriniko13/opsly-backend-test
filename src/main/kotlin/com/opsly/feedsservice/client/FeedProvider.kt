package com.opsly.feedsservice.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

abstract class FeedProvider<R>(private val webClient: WebClient, private val mapper: ObjectMapper) {

    protected inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

    fun getRequest(url: String, retries: Long, timeoutMs: Long): Mono<ClientResponse> {
        return webClient
                .get()
                .uri(url)
                .exchange()
                .timeout(Duration.ofMillis(timeoutMs))
                .retry(retries)
    }

    abstract fun consume(): Mono<R>

    fun consumeAsJson(): Mono<JsonNode> = consume().map { mapper.readTree(mapper.writeValueAsBytes(it)) }

    abstract fun feedName(): String
}