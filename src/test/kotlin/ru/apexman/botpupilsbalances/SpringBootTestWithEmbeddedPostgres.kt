package ru.apexman.botpupilsbalances

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.springframework.boot.test.context.SpringBootTest

@AutoConfigureEmbeddedDatabase(
    type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES,
    refresh = AutoConfigureEmbeddedDatabase.RefreshMode.BEFORE_EACH_TEST_METHOD
)
@SpringBootTest(classes = [TestConfig::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
annotation class SpringBootTestWithEmbeddedPostgres
