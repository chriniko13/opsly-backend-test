package com.opsly.feedsservice.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.opsly.feedsservice.dto.InstagramFeed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class InstagramFeedProvider(@Autowired private val webClient: WebClient,
                            @Autowired private val objectMapper: ObjectMapper,
                            @Autowired private val cache: FeedProviderCache,
                            @Value("\${instagram-feed-provider.retries}") private val retries: Long,
                            @Value("\${instagram-feed-provider.url}") private val url: String,
                            @Value("\${instagram-feed-provider.timeoutMs}") private val timeoutMs: Long,
                            @Value("\${instagram-feed-provider.cached}") private val cached: Boolean)

    : FeedProvider<List<InstagramFeed>>(webClient, objectMapper, cache) {

    private val log: Logger = LoggerFactory.getLogger(InstagramFeedProvider::class.java)

    override fun consume(): Mono<List<InstagramFeed>> {
        return getRequest(url, retries, timeoutMs, cached)
                .onErrorResume { error ->

                    log.error("error occurred, message: ${error.message}")

                    cache.getLatestEntry<List<InstagramFeed>>(feedName())
                            .orElseGet { listOf() }
                            .toMono()

                }
    }

    override fun feedName(): String = "instagram"

}