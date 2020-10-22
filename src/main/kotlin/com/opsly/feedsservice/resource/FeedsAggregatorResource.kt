package com.opsly.feedsservice.resource

import com.fasterxml.jackson.databind.JsonNode
import com.opsly.feedsservice.service.FeedsAggregatorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class FeedsAggregatorResource(@Autowired val service: FeedsAggregatorService) {

    @GetMapping("/")
    fun get(): Mono<MutableMap<String, JsonNode>> = service.fetch()

}