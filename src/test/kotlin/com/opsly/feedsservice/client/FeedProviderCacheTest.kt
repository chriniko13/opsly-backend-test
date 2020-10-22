package com.opsly.feedsservice.client

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.locks.LockSupport

internal class FeedProviderCacheTest {

    @Test
    fun getLatestEntryWorksAsExpected() {

        // given
        val cache = FeedProviderCache()

        cache.add("test-feed", 1)
        LockSupport.parkNanos(Duration.ofMillis(100).toNanos())
        cache.add("test-feed", 2)
        LockSupport.parkNanos(Duration.ofMillis(200).toNanos())
        cache.add("test-feed", 3)
        LockSupport.parkNanos(Duration.ofMillis(300).toNanos())
        cache.add("test-feed", 4)
        LockSupport.parkNanos(Duration.ofMillis(400).toNanos())

        // when
        val result = cache.getLatestEntry<Int>("test-feed")

        // then
        assertTrue(result.isPresent)
        assertEquals(4, result.get())


    }
}