package com.opsly.feedsservice.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.opsly.feedsservice.dto.FacebookFeed
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class FacebookFeedProvider(@Autowired private val webClient: WebClient,
                           @Autowired private val objectMapper: ObjectMapper,
                           @Value("\${facebook-feed-provider.retries}") private val retries: String,
                           @Value("\${facebook-feed-provider.url}") private val url: String,
                           @Value("\${facebook-feed-provider.timeoutMs}") private val timeoutMs: Long)

    : FeedProvider<List<FacebookFeed>>(webClient, objectMapper) {

    override fun consume(): Mono<List<FacebookFeed>> {
        return getRequest(url, retries.toLong(), timeoutMs)
                .map { response -> response.bodyToMono(typeReference<List<FacebookFeed>>()) }
                .flatMap { it }
                .subscribeOn(Schedulers.elastic())
    }

    override fun feedName(): String = "facebook"

}
