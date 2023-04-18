package ru.apexman.botpupilsbalances.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import ru.apexman.botpupilsbalances.entity.user.Student
import ru.apexman.botpupilsbalances.entity.userdetails.AlarmDetails
import java.time.LocalDateTime

interface AlarmDetailsRepository : JpaRepository<AlarmDetails, Long> {

    fun findFirstByStudentOrderByCreatedAtDesc(student: Student): AlarmDetails?

    @Transactional
    @Modifying
    @Query("""
        update AlarmDetails ad
        set ad.disabledAt = ?3,
            ad.disabledBy = ?2
        where ad.student = ?1
            and ad.disabledAt is null
        """
    )
    fun disableActiveStudentAlarmDetails(student: Student, modifiedBy: String, now: LocalDateTime)

}