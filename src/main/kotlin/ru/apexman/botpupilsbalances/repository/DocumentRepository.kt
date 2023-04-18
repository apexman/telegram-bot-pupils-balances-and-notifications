package ru.apexman.botpupilsbalances.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.apexman.botpupilsbalances.entity.Document

interface DocumentRepository : JpaRepository<Document, Long> {

    fun findByDocumentHash(contentHashCode: Int): Document?

}
