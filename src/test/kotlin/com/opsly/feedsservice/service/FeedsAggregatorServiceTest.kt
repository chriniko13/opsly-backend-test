package com.opsly.feedsservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.opsly.feedsservice.client.FeedProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

internal class FeedsAggregatorServiceTest {


    lateinit var aggregatorService: FeedsAggregatorService;

    lateinit var mockProvider1: FeedProvider<*>
    lateinit var mockProvider2: FeedProvider<*>

    @BeforeEach
    fun setup() {
        mockProvider1 = mock()
        mockProvider2 = mock()

        aggregatorService = FeedsAggregatorService(listOf(mockProvider1, mockProvider2))
    }

    @Test
    fun fetch() {

        // given
        val mapper = ObjectMapper()
        whenever(mockProvider1.consumeAsJson())
                .thenReturn(Mono.just(mapper.createArrayNode().add(1).add(2).add(3)))
        whenever(mockProvider1.feedName()).thenReturn("mock-1")

        whenever(mockProvider2.consumeAsJson())
                .thenReturn(Mono.just(mapper.createObjectNode().put("name", "chriniko").put("age", 28)))
        whenever(mockProvider2.feedName()).thenReturn("mock-2")


        // when
        val result = aggregatorService.fetch().block()

        // then
        assertEquals("{\"mock-1\":[1,2,3],\"mock-2\":{\"name\":\"chriniko\",\"age\":28}}", mapper.writeValueAsString(result))


    }
}