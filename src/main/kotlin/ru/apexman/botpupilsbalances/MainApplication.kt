package ru.apexman.botpupilsbalances

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.util.*

@EnableJpaAuditing
@ConfigurationPropertiesScan
@ComponentScan(value = ["ru.apexman.botpupilsbalances", "org.telegram.telegrambots"])
@EnableJpaRepositories(basePackages = ["ru.apexman.botpupilsbalances.repository"])
@SpringBootApplication
class MainApplication {
    private val logger = LoggerFactory.getLogger(MainApplication::class.java)

    @PostConstruct
    fun postConstruct() {
        // Setting Spring Boot SetTimeZone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        logger.info("Spring boot application running in UTC timezone: " + Date())
    }

    @Bean
    fun mapper(): ObjectMapper? {
        return jacksonObjectMapper().registerModule(JavaTimeModule())
    }
}

fun main(args: Array<String>) {
    runApplication<MainApplication>(*args)
}

