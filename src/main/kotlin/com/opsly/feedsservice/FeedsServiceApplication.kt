package com.opsly.feedsservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FeedsServiceApplication

fun main(args: Array<String>) {
    runApplication<FeedsServiceApplication>(*args)
}
