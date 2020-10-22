package com.opsly.feedsservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.util.concurrent.TimeUnit


@Configuration
class AppConfig {

    @Bean
    @Primary
    fun webClient(
            @Value("\${read.timeout}") readTimeout: Long,
            @Value("\${connect.timeout}") connectTimeout: Int): WebClient {

        // create reactor netty HTTP client
        val httpClient = HttpClient.create()
                .tcpConfiguration { tcpClient ->
                    tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                            .doOnConnected { conn ->
                                conn.addHandlerLast(ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                            }
                };

        // create a client http connector using above http client
        val connector = ReactorClientHttpConnector(httpClient)

        // use this configured http connector to build the web client
        return WebClient.builder().clientConnector(connector).build()
    }


    @Bean
    @Primary
    fun objectMapper(): ObjectMapper = ObjectMapper()

}