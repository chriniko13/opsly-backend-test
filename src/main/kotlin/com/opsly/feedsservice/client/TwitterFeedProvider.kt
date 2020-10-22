package com.opsly.feedsservice.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.opsly.feedsservice.dto.TwitterFeed
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class TwitterFeedProvider(
        @Autowired private val webClient: WebClient,
        @Autowired private val objectMapper: ObjectMapper,
        @Value("\${twitter-feed-provider.retries}") private val retries: Long,
        @Value("\${twitter-feed-provider.url}") private val url: String,
        @Value("\${twitter-feed-provider.timeoutMs}") private val timeoutMs: Long)

    : FeedProvider<List<TwitterFeed>>(webClient, objectMapper) {

    override fun consume(): Mono<List<TwitterFeed>> {
        return getRequest(url, retries, timeoutMs)
                .map { response -> response.bodyToMono(typeReference<List<TwitterFeed>>()) }
                .flatMap { it }
                .subscribeOn(Schedulers.elastic())
    }

    override fun feedName(): String = "twitter"

}
