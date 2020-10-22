package com.opsly.feedsservice.error

sealed class ServiceError
object FeedProviderTimeoutError : Error()
object FeedProviderMalformedResponseError : Error()
object FeedProviderNotReachableError : Error()