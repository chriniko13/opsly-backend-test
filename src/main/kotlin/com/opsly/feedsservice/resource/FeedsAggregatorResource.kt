package com.opsly.feedsservice.resource

import com.fasterxml.jackson.databind.JsonNode
import com.opsly.feedsservice.service.FeedsAggregatorService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class FeedsAggregatorResource(@Autowired val service: FeedsAggregatorService) {

    private val log: Logger = LoggerFactory.getLogger(FeedsAggregatorService::class.java)

    @GetMapping("/")
    fun get(): Mono<MutableMap<String, JsonNode>> {
        val startTime = System.currentTimeMillis()
        try {
            return service.fetch()
        } finally {
            log.trace("incoming request...total time in ms: ${System.currentTimeMillis() - startTime}")
        }
    }

}