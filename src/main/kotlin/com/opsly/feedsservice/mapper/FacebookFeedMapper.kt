package com.opsly.feedsservice.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FacebookFeedMapper(@Autowired private val objectMapper: ObjectMapper) : FeedMapper {

    override fun process(input: JsonNode): JsonNode {

        val result = objectMapper.createArrayNode()

        if (input is ArrayNode) {

            input.fold(result) { acc, elem ->

                if (elem.isObject && elem.has("status")) {
                    val t = elem.findValue("status").asText()
                    acc.add(t)
                }
                acc
            }
        }

        return result

    }

}