package com.opsly.feedsservice.mapper

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

interface FeedMapper {

    fun process(input: JsonNode): JsonNode
}