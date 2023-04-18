package ru.apexman.botpupilsbalances

import org.mockito.kotlin.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.function.client.WebClient

@TestConfiguration
class TestConfig {

    @Primary
    @Bean
    fun webClient(): WebClient {
        return mock() as WebClient
    }

}
