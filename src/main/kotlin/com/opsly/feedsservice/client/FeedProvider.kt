package com.opsly.feedsservice.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Duration

abstract class FeedProvider<R>(
        private val webClient: WebClient,
        private val mapper: ObjectMapper,
        val cache: FeedProviderCache) {

    protected fun getRequest(url: String, retries: Long, timeoutMs: Long, cached: Boolean): Mono<R> {
        val f: Mono<R> = webClient
                .get()
                .uri(url)
                .exchange()
                .timeout(Duration.ofMillis(timeoutMs))
                .retry(retries)
                .map { it.bodyToMono(object : ParameterizedTypeReference<R>() {}) }
                .flatMap { it }

        return if (cached) {
            f.doOnNext { cacheResult(it as Any) }
        } else f
    }

    protected inline fun <reified R> recoverFromCacheOrEmpty(): Mono<List<R>> {
        return cache.getLatestEntry<List<R>>(feedName())
                .orElseGet { emptyList() }
                .toMono()
    }

    protected abstract fun consume(): Mono<R>

    private fun cacheResult(v: Any) = cache.add(feedName(), v)

    private fun jsonNode(it: R?) = mapper.readTree(mapper.writeValueAsBytes(it))

    // --- public ---
    fun consumeAsJson(): Mono<JsonNode> = consume().map { jsonNode(it) }

    abstract fun feedName(): String

}