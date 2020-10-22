package com.opsly.feedsservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class AppConfig {

    @Bean
    @Primary
    fun webClient(): WebClient =
            WebClient
                    .builder()
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build()

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper = ObjectMapper()

}