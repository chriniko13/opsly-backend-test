package com.opsly.feedsservice.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.opsly.feedsservice.dto.InstagramFeed
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class InstagramFeedProvider(@Autowired private val webClient: WebClient,
                            @Autowired private val objectMapper: ObjectMapper,
                            @Value("\${instagram-feed-provider.retries}") private val retries: Long,
                            @Value("\${instagram-feed-provider.url}") private val url: String,
                            @Value("\${instagram-feed-provider.timeoutMs}") private val timeoutMs: Long)

    : FeedProvider<List<InstagramFeed>>(webClient, objectMapper) {

    override fun consume(): Mono<List<InstagramFeed>> {
        return getRequest(url, retries, timeoutMs)
                .map { response -> response.bodyToMono(typeReference<List<InstagramFeed>>()) }
                .flatMap { it }
                .subscribeOn(Schedulers.elastic())
    }

    override fun feedName(): String = "instagram"

}