package com.opsly.feedsservice.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.opsly.feedsservice.dto.TwitterFeed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class TwitterFeedProvider(
        @Autowired private val webClient: WebClient,
        @Autowired private val objectMapper: ObjectMapper,
        @Autowired private val c: FeedProviderCache,
        @Value("\${twitter-feed-provider.retries}") private val retries: Long,
        @Value("\${twitter-feed-provider.url}") private val url: String,
        @Value("\${twitter-feed-provider.timeoutMs}") private val timeoutMs: Long,
        @Value("\${twitter-feed-provider.cached}") private val cached: Boolean)

    : FeedProvider<List<TwitterFeed>>(webClient, objectMapper, c) {

    private val log: Logger = LoggerFactory.getLogger(TwitterFeedProvider::class.java)

    override fun consume(): Mono<List<TwitterFeed>> {
        return getRequest(url, retries, timeoutMs, cached)
                .onErrorResume { error ->
                    log.warn("error occurred, message: ${error.message}")
                    recoverFromCacheOrEmpty()
                }
    }

    override fun feedName(): String = "twitter"

}
