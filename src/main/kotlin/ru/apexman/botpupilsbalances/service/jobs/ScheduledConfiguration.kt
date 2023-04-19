package ru.apexman.botpupilsbalances.service.jobs

import org.quartz.*
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


@Configuration
class ScheduledConfiguration {

    @Bean
    fun informOverdueScheduler(factory: SchedulerFactoryBean, scheduledProperties: ScheduledProperties): Scheduler {
        val job = JobBuilder.newJob().ofType(InformOverdueJobService::class.java)
            .storeDurably()
            .withIdentity("qrtz_inform_overdue_job_detail")
            .withDescription("Sends messages with students, who has overdue payments")
            .build()
        val triggerDate: Date = parseLocalTime(scheduledProperties.informOverdueStartTime, scheduledProperties.userTimeZone)
        val trigger = TriggerBuilder.newTrigger().forJob(job)
            .withIdentity("qrtz_inform_overdue_trigger")
            .withDescription("Trigger to send messages with students, who has overdue payments")
            .startAt(triggerDate)
            .withSchedule(
                simpleSchedule()
                    .repeatForever()
                    .withIntervalInMinutes(scheduledProperties.informOverdueIntervalMinutes)
            )
            .build()

        val scheduler: Scheduler = factory.scheduler
        scheduler.scheduleJob(job, trigger)
        scheduler.start()
        return scheduler
    }

    private fun parseLocalTime(informOverdueStartTime: LocalTime, userTimeZone: TimeZone): Date {
        val instant: Instant = informOverdueStartTime
            .atDate(LocalDate.now(userTimeZone.toZoneId()).minusDays(1))
            .atZone(userTimeZone.toZoneId())
            .toInstant()
        return Date.from(instant);
    }

}