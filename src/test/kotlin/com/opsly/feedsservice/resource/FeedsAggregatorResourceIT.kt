package com.opsly.feedsservice.resource

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.opsly.feedsservice.client.FacebookFeedProvider
import com.opsly.feedsservice.client.InstagramFeedProvider
import com.opsly.feedsservice.client.TwitterFeedProvider
import org.junit.Rule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.test.util.ReflectionTestUtils


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class FeedsAggregatorResourceIT {

    @LocalServerPort
    lateinit var port: String

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    final val wiremockPort: Int = 8089

    @Rule
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var applicationContext: ApplicationContext

    companion object {

        @BeforeAll
        @JvmStatic
        internal fun setUp() {
            println(">> Setup")
        }

        @AfterAll
        @JvmStatic
        internal fun tearDown() {
            println(">> Tear Down")
        }
    }

    @BeforeEach
    fun setUpEach() {
        wireMockServer = WireMockServer(
                options()
                        .extensions(ResponseTemplateTransformer(false))
                        .port(wiremockPort));

        wireMockServer.start();
    }

    @AfterEach
    fun tearDownEach() {
        wireMockServer.stop()
    }

    @Test
    fun getHappyPathWorksAsExpected() {

        // given
        val facebookFeedProvider = applicationContext.getBean(FacebookFeedProvider::class.java)
        ReflectionTestUtils.setField(facebookFeedProvider, "url", "http://localhost:${wiremockPort}/facebook")

        val twitterFeedProvider = applicationContext.getBean(TwitterFeedProvider::class.java)
        ReflectionTestUtils.setField(twitterFeedProvider, "url", "http://localhost:${wiremockPort}/twitter")

        val instagramFeedProvider = applicationContext.getBean(InstagramFeedProvider::class.java)
        ReflectionTestUtils.setField(instagramFeedProvider, "url", "http://localhost:${wiremockPort}/instagram")


        wireMockServer.stubFor(get(urlPathMatching("/facebook"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"}," +
                                "{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]")))


        wireMockServer.stubFor(get(urlPathMatching("/twitter"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"username\":\"@GuyEndoreKaiser\",\"tweet\":\"If you live to be 100, you should make up some fake reason why, just to mess with people... like claim you ate a pinecone every single day.\"}," +
                                "{\"username\":\"@mikeleffingwell\",\"tweet\":\"STOP TELLING ME YOUR NEWBORN'S WEIGHT AND LENGTH I DON'T KNOW WHAT TO DO WITH THAT INFORMATION.\"}]")))


        wireMockServer.stubFor(get(urlPathMatching("/instagram"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("404 page not found")))


        // when
        val responseEntity = restTemplate.getForEntity<String>("http://localhost:${port}/", String::class.java)

        // then
        assertEquals(HttpStatus.OK, responseEntity.statusCode)

        JSONAssert.assertEquals(

                "{\"instagram\":[]," +
                        "\"facebook\":[" +
                        "{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"}," +
                        "{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]," +
                        "\"twitter\":[{\"username\":\"@GuyEndoreKaiser\",\"tweet\":\"If you live to be 100, you should make up some fake reason why, just to mess with people... like claim you ate a pinecone every single day.\"}," +
                        "{\"username\":\"@mikeleffingwell\",\"tweet\":\"STOP TELLING ME YOUR NEWBORN'S WEIGHT AND LENGTH I DON'T KNOW WHAT TO DO WITH THAT INFORMATION.\"}]}",

                responseEntity.body,

                JSONCompareMode.STRICT
        )

    }


    @Test
    fun getTwitterMalformedWorksAsExpected() {

        // given
        val facebookFeedProvider = applicationContext.getBean(FacebookFeedProvider::class.java)
        ReflectionTestUtils.setField(facebookFeedProvider, "url", "http://localhost:${wiremockPort}/facebook")

        val twitterFeedProvider = applicationContext.getBean(TwitterFeedProvider::class.java)
        ReflectionTestUtils.setField(twitterFeedProvider, "url", "http://localhost:${wiremockPort}/twitter")

        val instagramFeedProvider = applicationContext.getBean(InstagramFeedProvider::class.java)
        ReflectionTestUtils.setField(instagramFeedProvider, "url", "http://localhost:${wiremockPort}/instagram")


        wireMockServer.stubFor(get(urlPathMatching("/facebook"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"}," +
                                "{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]")))


        wireMockServer.stubFor(get(urlPathMatching("/twitter"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("life is short...")))


        wireMockServer.stubFor(get(urlPathMatching("/instagram"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("404 page not found")))


        // when
        val responseEntity = restTemplate.getForEntity<String>("http://localhost:${port}/", String::class.java)

        // then
        assertEquals(HttpStatus.OK, responseEntity.statusCode)

        JSONAssert.assertEquals(

                "{\"instagram\":[]," +
                        "\"facebook\":[" +
                        "{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"}," +
                        "{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]," +
                        "\"twitter\":[]}",

                responseEntity.body,

                JSONCompareMode.STRICT
        )
    }

    @Test
    fun getTwitterTimeoutWorksAsExpected() {

        // given
        val facebookFeedProvider = applicationContext.getBean(FacebookFeedProvider::class.java)
        ReflectionTestUtils.setField(facebookFeedProvider, "url", "http://localhost:${wiremockPort}/facebook")

        val twitterFeedProvider = applicationContext.getBean(TwitterFeedProvider::class.java)
        ReflectionTestUtils.setField(twitterFeedProvider, "url", "http://localhost:${wiremockPort}/twitter")

        val instagramFeedProvider = applicationContext.getBean(InstagramFeedProvider::class.java)
        ReflectionTestUtils.setField(instagramFeedProvider, "url", "http://localhost:${wiremockPort}/instagram")


        wireMockServer.stubFor(get(urlPathMatching("/facebook"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"}," +
                                "{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]")))


        wireMockServer.stubFor(get(urlPathMatching("/twitter"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(7000)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"username\":\"@GuyEndoreKaiser\",\"tweet\":\"If you live to be 100, you should make up some fake reason why, just to mess with people... like claim you ate a pinecone every single day.\"}," +
                                "{\"username\":\"@mikeleffingwell\",\"tweet\":\"STOP TELLING ME YOUR NEWBORN'S WEIGHT AND LENGTH I DON'T KNOW WHAT TO DO WITH THAT INFORMATION.\"}]")))


        wireMockServer.stubFor(get(urlPathMatching("/instagram"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("404 page not found")))


        // when
        val responseEntity = restTemplate.getForEntity<String>("http://localhost:${port}/", String::class.java)

        // then
        assertEquals(HttpStatus.OK, responseEntity.statusCode)

        JSONAssert.assertEquals(

                "{\"instagram\":[]," +
                        "\"facebook\":[" +
                        "{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"}," +
                        "{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]," +
                        "\"twitter\":[]}",

                responseEntity.body,

                JSONCompareMode.STRICT
        )

    }

    @Test
    fun getFirstCallIsSuccessfulAndCachedSecondCallTimeoutBuyCacheToTheRescue() {

        // given
        val facebookFeedProvider = applicationContext.getBean(FacebookFeedProvider::class.java)
        ReflectionTestUtils.setField(facebookFeedProvider, "url", "http://localhost:${wiremockPort}/facebook")

        val twitterFeedProvider = applicationContext.getBean(TwitterFeedProvider::class.java)
        ReflectionTestUtils.setField(twitterFeedProvider, "url", "http://localhost:${wiremockPort}/twitter")

        val instagramFeedProvider = applicationContext.getBean(InstagramFeedProvider::class.java)
        ReflectionTestUtils.setField(instagramFeedProvider, "url", "http://localhost:${wiremockPort}/instagram")


        wireMockServer.stubFor(get(urlPathMatching("/facebook"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"}," +
                                "{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]")))


        wireMockServer.stubFor(get(urlPathMatching("/twitter"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"username\":\"@GuyEndoreKaiser\",\"tweet\":\"If you live to be 100, you should make up some fake reason why, just to mess with people... like claim you ate a pinecone every single day.\"}," +
                                "{\"username\":\"@mikeleffingwell\",\"tweet\":\"STOP TELLING ME YOUR NEWBORN'S WEIGHT AND LENGTH I DON'T KNOW WHAT TO DO WITH THAT INFORMATION.\"}]")))


        wireMockServer.stubFor(get(urlPathMatching("/instagram"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("404 page not found")))

        val responseEntity = restTemplate.getForEntity<String>("http://localhost:${port}/", String::class.java)

        assertEquals(HttpStatus.OK, responseEntity.statusCode)

        JSONAssert.assertEquals(

                "{\"instagram\":[]," +
                        "\"facebook\":[" +
                        "{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"}," +
                        "{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]," +
                        "\"twitter\":[{\"username\":\"@GuyEndoreKaiser\",\"tweet\":\"If you live to be 100, you should make up some fake reason why, just to mess with people... like claim you ate a pinecone every single day.\"}," +
                        "{\"username\":\"@mikeleffingwell\",\"tweet\":\"STOP TELLING ME YOUR NEWBORN'S WEIGHT AND LENGTH I DON'T KNOW WHAT TO DO WITH THAT INFORMATION.\"}]}",

                responseEntity.body,

                JSONCompareMode.STRICT
        )



        // when
        wireMockServer.stubFor(get(urlPathMatching("/facebook"))
                .willReturn(aResponse()
                        .withFixedDelay(7000)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"}," +
                                "{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]")))


        wireMockServer.stubFor(get(urlPathMatching("/twitter"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("down for upgrade")))


        wireMockServer.stubFor(get(urlPathMatching("/instagram"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("404 page not found")))

        val responseEntitySecondCall = restTemplate.getForEntity<String>("http://localhost:${port}/", String::class.java)



        // then
        assertEquals(HttpStatus.OK, responseEntitySecondCall.statusCode)

        JSONAssert.assertEquals(

                "{\"instagram\":[]," +
                        "\"facebook\":[" +
                        "{\"name\":\"Some Friend\",\"status\":\"Here's some photos of my holiday. Look how much more fun I'm having than you are!\"}," +
                        "{\"name\":\"Drama Pig\",\"status\":\"I am in a hospital. I will not tell you anything about why I am here.\"}]," +
                        "\"twitter\":[{\"username\":\"@GuyEndoreKaiser\",\"tweet\":\"If you live to be 100, you should make up some fake reason why, just to mess with people... like claim you ate a pinecone every single day.\"}," +
                        "{\"username\":\"@mikeleffingwell\",\"tweet\":\"STOP TELLING ME YOUR NEWBORN'S WEIGHT AND LENGTH I DON'T KNOW WHAT TO DO WITH THAT INFORMATION.\"}]}",

                responseEntitySecondCall.body,

                JSONCompareMode.STRICT
        )
    }


}