package ru.apexman.botpupilsbalances.service.notification

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.core.publisher.Mono

@Configuration
class TelegramWebClientConfiguration {
    private val logger = LoggerFactory.getLogger(TelegramWebClientConfiguration::class.java)

    @Bean
    fun telegramWebClient(telegramProperties: TelegramProperties): WebClient {
        val factory = DefaultUriBuilderFactory(telegramProperties.telegramApiUrl)
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
        return WebClient
            .builder()
            .baseUrl(telegramProperties.telegramApiUrl)
            .uriBuilderFactory(factory)
            .filters { exchangeFilterFunctions ->
                exchangeFilterFunctions.add(requestLog())
                exchangeFilterFunctions.add(responseLog())
            }
            .build()
    }

    fun requestLog(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofRequestProcessor {
            logger.trace("Requesting ${it.method()}: ${it.url()}")
            Mono.just(it)
        }

    fun responseLog(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofResponseProcessor {
            logger.trace("Got response with status code: ${it.statusCode()}")
            Mono.just(it)
        }
}
