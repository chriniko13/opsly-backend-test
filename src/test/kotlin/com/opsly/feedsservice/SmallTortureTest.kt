package com.opsly.feedsservice

import com.opsly.feedsservice.config.AppConfig
import io.netty.handler.timeout.ReadTimeoutException
import org.junit.Test
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.LongAdder

/*
    Important Note: make sure you have your service/application up and running.
              In the future we could write a more sophisticated benchmark test with JMH.
 */

fun main(args: Array<String>) {
    SmallTortureTest().produceTraffic()
}

internal class SmallTortureTest {

    fun produceTraffic() {

        val webClient = AppConfig().webClient(3000, 3500)

        val clients = 500

        val pool = Executors.newFixedThreadPool(100)

        val fs = LinkedList<CompletableFuture<ClientResponse>>()

        for (i in 1 until clients) {
            val f: CompletableFuture<ClientResponse> = webClient.get()
                    .uri("http://localhost:3000")
                    .exchange()
                    .subscribeOn(Schedulers.fromExecutor(pool))
                    .toFuture()
            fs += f
        }

        val notServerCounter = LongAdder()
        val generalErrorsCounter = LongAdder()

        println("waiting clients to finish...")
        for(f in fs) {
            try {
                f.join()
            } catch (e: CompletionException) {

                if (e.cause is ReadTimeoutException) {
                    println("not served")
                    notServerCounter.increment()
                } else {
                    println("error occurred: ${e.cause?.message}")
                    generalErrorsCounter.increment()
                }

            }
        }
        println(" ### clients finished...not served: ${notServerCounter.sum()}, general errors: ${generalErrorsCounter.sum()} ### ")


        pool.shutdown()

    }

}