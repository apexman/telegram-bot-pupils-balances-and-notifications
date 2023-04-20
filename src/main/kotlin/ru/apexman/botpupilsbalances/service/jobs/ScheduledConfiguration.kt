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


/**
 * Создание планировщиков
 * Расчет даты-время старта джобы: забирается время из пропертей и интервал выполнения. Дата берется за прошлый день
 * Пример расчета даты-время:
 * - приложение стартует в 21:02
 * - на вход: время 20:53, интервал - 1мин.
 * - джоба считает, что она должна была стартовать вчера в 20:53 с интервалом в 1 минуту
 * - сразу начинается выполнение джобы с интервалом в 1мин
 */
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

    @Bean
    fun balanceDecreaserScheduler(factory: SchedulerFactoryBean, scheduledProperties: ScheduledProperties): Scheduler {
        val job = JobBuilder.newJob().ofType(BalanceDecreaserJobService::class.java)
            .storeDurably()
            .withIdentity("qrtz_balance_decreaser_job_detail")
            .withDescription("Decrease students' balances who not paused")
            .build()
        val triggerDate: Date = parseLocalTime(scheduledProperties.balanceDecreaserStartTime, scheduledProperties.userTimeZone)
        val trigger = TriggerBuilder.newTrigger().forJob(job)
            .withIdentity("qrtz_balance_decreaser_trigger")
            .withDescription("Trigger to decrease students' balances who not paused")
            .startAt(triggerDate)
            .withSchedule(
                simpleSchedule()
                    .repeatForever()
                    .withIntervalInMinutes(scheduledProperties.balanceDecreaserIntervalMinutes)
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