package com.opsly.feedsservice.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.opsly.feedsservice.client.FeedProvider
import com.opsly.feedsservice.mapper.FeedMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class FeedsAggregatorService(
        @Autowired private val feedProviders: List<FeedProvider<*>>,
        @Autowired private val feedMappers: Map<String, FeedMapper>,
        @Autowired private val objectMapper: ObjectMapper,
        @Value("\${feed-aggregator.apply-mapping}") private val applyMapping: Boolean) {

    fun fetch(): Mono<MutableMap<String, JsonNode>> {

        if (!applyMapping) {

            return Flux.fromIterable(feedProviders)
                    .map { it.consumeAsJson().map { json -> Pair(it.feedName(), json) } }
                    .flatMap { it }
                    .reduce(mutableMapOf(),
                            { acc, record ->
                                acc[record.first] = record.second
                                acc
                            }
                    )
        } else {

            return Flux.fromIterable(feedProviders)
                    .map {
                        it.consume()
                                .publishOn(Schedulers.parallel())
                                .map { feeds -> Pair(it.feedName(), feeds) }
                    }
                    .flatMap { it }
                    .map { result ->
                        val feedName = result.first
                        val feedResult = objectMapper.writeValueAsString(result.second)

                        val feedMapper = (feedMappers["${feedName}FeedMapper"]
                                ?: error("system error, feed not registered to system"))
                        Pair(feedName, feedMapper.process(objectMapper.readTree(feedResult)))
                    }
                    .reduce(mutableMapOf(),
                            { acc, record ->
                                val feed = record.first
                                acc[feed] = record.second
                                acc
                            }
                    )

        }
    }
}