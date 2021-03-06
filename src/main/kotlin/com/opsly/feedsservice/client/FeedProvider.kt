package com.opsly.feedsservice.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import java.time.Duration

abstract class FeedProvider<R: Any>(
        private val webClient: WebClient,
        private val mapper: ObjectMapper,
        val cache: FeedProviderCache) {

    protected fun getRequest(url: String, retries: Long, timeoutMs: Long, cached: Boolean): Mono<R> {
        var f: Mono<R> = webClient
                .get()
                .uri(url)
                .exchange()
                .timeout(Duration.ofMillis(timeoutMs))
                .retry(retries)
                .map { it.bodyToMono(object : ParameterizedTypeReference<R>() {}) }
                .flatMap { it }

        if (cached) {
            f = f.doOnNext { cacheResult(it as Any) }
        }

        return f.subscribeOn(Schedulers.boundedElastic())
    }

    protected inline fun <reified R> recoverFromCacheOrEmpty(): Mono<List<R>> {
        return cache
                .getLatestEntry<List<R>>(feedName())
                .orElseGet { emptyList() }
                .toMono()
    }

    private fun cacheResult(v: Any) = cache.add(feedName(), v)

    private fun jsonNode(it: R?) = mapper.readTree(mapper.writeValueAsBytes(it))

    // --- public ---
    abstract fun consume(): Mono<R>

    fun consumeAsJson(): Mono<JsonNode> {
        return consume()
                .publishOn(Schedulers.parallel())
                .map { jsonNode(it) }
    }

    abstract fun feedName(): String

}