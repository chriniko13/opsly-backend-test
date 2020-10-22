package com.opsly.feedsservice.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.opsly.feedsservice.client.FeedProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class FeedsAggregatorService(@Autowired val feedProviders: List<FeedProvider<*>>) {


    fun fetch(): Mono<MutableMap<String, JsonNode>> {
        return Flux.fromIterable(feedProviders)
                .map { feedProvider -> feedProvider.consumeAsJson().map { Pair(feedProvider.feedName(), it) } }
                .flatMap { it }
                .onErrorResume { _ -> Mono.just(Pair("sample", ObjectMapper().createArrayNode())) }
                .reduce(mutableMapOf(),
                        { acc, record ->
                            acc[record.first] = record.second
                            return@reduce acc
                        }
                )
    }
}