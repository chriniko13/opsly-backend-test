package com.opsly.feedsservice.service

import com.fasterxml.jackson.databind.JsonNode
import com.opsly.feedsservice.client.FeedProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class FeedsAggregatorService(@Autowired private val feedProviders: List<FeedProvider<*>>) {

    fun fetch(): Mono<MutableMap<String, JsonNode>> {
        return Flux.fromIterable(feedProviders)
                .map { it.consumeAsJson().map { json -> Pair(it.feedName(), json) } }
                .flatMap { it }
                .reduce(mutableMapOf(),
                        { acc, record ->
                            acc[record.first] = record.second
                            acc
                        }
                )
    }
}