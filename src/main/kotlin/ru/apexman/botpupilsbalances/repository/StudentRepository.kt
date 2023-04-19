package ru.apexman.botpupilsbalances.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.apexman.botpupilsbalances.entity.user.Student

interface StudentRepository : JpaRepository<Student, Long> {

    fun existsByGoogleId(googleId: String): Boolean

    fun existsByPublicId(publicId: String): Boolean

    fun findByGoogleId(googleId: String): Student?

    fun findByPublicId(publicId: String): Student?

    @Query("""
        select s 
        from Student s 
        left join fetch s.alarmDetails ad
        where s.isAlarm = true
    """)
    fun findAllByIsAlarmIsTrueWithActiveAlarmDetails(): List<Student>

    fun findAllByIsPauseIsTrue(): List<Student>

    @Query("""
        select s 
        from Student s 
        left join fetch s.contacts con
        where s.isPause != true
            and s.balance <= ?1
    """)
    fun findAllByIsPauseIsFalseAndBalanceLessThanEqual(balance: Int = 7): List<Student>

}