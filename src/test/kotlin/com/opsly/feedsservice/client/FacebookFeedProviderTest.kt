package com.opsly.feedsservice.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport


internal class FacebookFeedProviderTest {


    companion object {
        lateinit var feedProvider: FacebookFeedProvider

        lateinit var mockBackEnd: MockWebServer

        lateinit var feedProviderCache: FeedProviderCache

        lateinit var mapper: ObjectMapper

        @BeforeAll
        @JvmStatic
        internal fun setUp() {
            mockBackEnd = MockWebServer()
            mockBackEnd.start()
        }

        @AfterAll
        @JvmStatic
        internal fun tearDown() {
            LockSupport.parkNanos(Duration.ofMillis(2000).toNanos())
            mockBackEnd.shutdown()
        }
    }


    @BeforeEach
    fun setup() {
        mapper = ObjectMapper()
        feedProviderCache = mock()
    }

    @Test
    fun consumeWorksAsExpected() {

        // given
        feedProvider = FacebookFeedProvider(
                WebClient.create(),
                mapper,
                feedProviderCache,
                2,
                "http://localhost:${mockBackEnd.port}/facebook",
                1000,
                true
        )

        val integrationResponse = "[{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"},{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]"

        mockBackEnd.enqueue(MockResponse()
                .setBody(integrationResponse)
                .setBodyDelay(400, TimeUnit.MILLISECONDS)
                .addHeader("Content-Type", "application/json"))

        // when
        val mono = feedProvider.consumeAsJson()

        // then
        StepVerifier.create(mono)
                .expectNextMatches { mapper.writeValueAsString(it) == integrationResponse }
                .verifyComplete()

        verify(feedProviderCache).add(any(), any())
    }

    @Test
    fun consumeMalformedWorksAsExpected() {

        // given
        feedProvider = FacebookFeedProvider(
                WebClient.create(),
                mapper,
                feedProviderCache,
                2,
                "http://localhost:${mockBackEnd.port}/facebook",
                1000,
                true
        )

        val integrationResponse = "how robust you are?"

        mockBackEnd.enqueue(MockResponse()
                .apply {
                    status = "200"
                    setBodyDelay(300, TimeUnit.MILLISECONDS)
                    setBody(integrationResponse)
                    addHeader("Content-Type", "application/json")
                }
        )

        // when
        val mono = feedProvider.consumeAsJson()


        // then
        StepVerifier.create(mono)
                .expectNextMatches { mapper.writeValueAsString(it) == "[]" }
                .verifyComplete()

        verify(feedProviderCache, times(0)).add(any(), any())
    }

    @Test
    fun consumeTimeoutWorksAsExpected() {
        // given
        feedProvider = FacebookFeedProvider(
                WebClient.create(),
                mapper,
                feedProviderCache,
                1,
                "http://localhost:${mockBackEnd.port}/facebook",
                200, // Note: time is money...
                true
        )

        val integrationResponse = "[{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"},{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]"

        mockBackEnd.enqueue(MockResponse()
                .apply {
                    setBodyDelay(7, TimeUnit.SECONDS)
                    setBody(integrationResponse)
                    addHeader("Content-Type", "application/json")
                }
        )

        // when
        val mono = feedProvider.consumeAsJson()


        // then
        StepVerifier.create(mono)
                .expectNextMatches { mapper.writeValueAsString(it) == "[]" }
                .verifyComplete()

        verify(feedProviderCache, times(0)).add(any(), any())
    }


    @Test
    fun consumeErrorStatusCodeResponseWorksAsExpected() {
        // given
        feedProvider = FacebookFeedProvider(
                WebClient.create(),
                mapper,
                feedProviderCache,
                4,
                "http://localhost:${mockBackEnd.port}/facebook",
                200, // Note: time is money...
                true
        )

        val integrationResponse = "[{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"},{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]"

        mockBackEnd.enqueue(MockResponse()
                .apply {
                    status = "500"
                    setBodyDelay(7, TimeUnit.SECONDS)
                    setBody(integrationResponse)
                    addHeader("Content-Type", "application/json")
                }
        )

        // when
        val mono = feedProvider.consumeAsJson()


        // then
        StepVerifier.create(mono)
                .expectNextMatches { mapper.writeValueAsString(it) == "[]" }
                .verifyComplete()

        verify(feedProviderCache, times(0)).add(any(), any())
    }
}