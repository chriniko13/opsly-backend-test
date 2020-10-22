package com.opsly.feedsservice.client

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class FeedProviderCache {

    val cache: MutableMap<String, TreeSet<CacheEntry>> = ConcurrentHashMap()

    fun add(feedName: String, entry: Any): Any {

        val cacheEntry = CacheEntry(Instant.now(), entry)

        cache.compute(feedName) { _, v ->
            if (v == null) {
                val ts = TreeSet<CacheEntry> { c1, c2 -> c1.instant.compareTo(c2.instant) }
                ts += cacheEntry
                ts
            } else {
                if (v.size > 1000) {
                    v.clear()
                }

                v.add(cacheEntry)
                v
            }
        }

        return entry
    }

    final inline fun <reified T> getLatestEntry(feedName: String): Optional<T> =
            Optional.ofNullable(cache[feedName]).map { it.last() }.map { it.entry as T }

}

data class CacheEntry(val instant: Instant, val entry: Any)