package com.opsly.feedsservice

import com.opsly.feedsservice.config.AppConfig
import io.netty.handler.timeout.ReadTimeoutException
import org.junit.Test
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.LongAdder

/*
    Important Note: make sure you have your service/application up and running.
 */
internal class SmallTortureTest {

    @Test
    fun produceTraffic() {

        val webClient = AppConfig().webClient(3000, 3500)

        val clients = 200

        val pool = Executors.newFixedThreadPool(100)

        val fs = LinkedList<CompletableFuture<ClientResponse>>()

        for (i in 1 until clients) {
            val f: CompletableFuture<ClientResponse> = webClient.get()
                    .uri("http://localhost:8080")
                    .exchange()
                    .subscribeOn(Schedulers.fromExecutor(pool))
                    .toFuture()
            fs += f
        }

        val longAdder = LongAdder()

        println("waiting clients to finish...")
        for(f in fs) {
            try {
                f.join()
            } catch (e: ReadTimeoutException) {
                println("not served")
                longAdder.increment()
            }
        }
        println("clients finished...not served: ${longAdder.sum()}")


        pool.shutdown()

    }

}