package ru.apexman.botpupilsbalances.service.googleapi.pages

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ClearValuesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.apexman.botpupilsbalances.dto.GoogleMainPageRowRequest
import ru.apexman.botpupilsbalances.dto.OperationResult
import ru.apexman.botpupilsbalances.repository.StudentRepository
import ru.apexman.botpupilsbalances.service.StudentService
import ru.apexman.botpupilsbalances.service.googleapi.SheetsProperties


@Service
class PushingToMainPageService(
    private val sheetsProperties: SheetsProperties,
    private val sheets: Sheets,
    private val studentRepository: StudentRepository,
    private val studentService: StudentService,
) : AbstractSheetsService() {

    @Synchronized
    fun pushToMainPage(): OperationResult<Long> {
        val errors = mutableListOf<String>()
        clearMainPage()
        pushHeaders()
        pushStudents()
        return OperationResult(true, studentRepository.count(), errors)
    }

    private fun clearMainPage() {
        sheets.spreadsheets().values()
            .clear(sheetsProperties.sheetId, sheetsProperties.mainTableName, ClearValuesRequest())
            .execute()
    }

    private fun pushHeaders() {
        val body = listOf(
            listOf(
                "ID",
                "PUBLIC_ID",
                "NAME",
                "BIRTHDAY",
                "AGE",
                "DATE_ENROLLMENT",
                "CLASS",
                "HOSTEL",
                "DISCONT",
                "PRICE",
                "CURRENCY",
                "BALANCE",
                "PARENT_ID",
                "CHILD_ID",
                "PAUSE",
                "COMMENT",
                "ALARM",
                "ALARM_DETALS",
                "PENALTY",
            )
        )
        appendMainPage(body)
    }

    private fun pushStudents() {
        var isLast = false
        while (!isLast) {
            var currentPageNum = 0
            val studentsPage = studentRepository
                .findAll(PageRequest.of(currentPageNum, rowsCountAtRequest, Sort.by(Sort.Direction.ASC, "id")))
            val googleMainPageRows = studentsPage.content.map { studentService.toGoogleMainPageRowRequest(it) }
            pushToMainPage(googleMainPageRows)
            isLast = studentsPage.isLast
            currentPageNum += 1
        }
    }

    private fun pushToMainPage(googleMainPageRowRequests: List<GoogleMainPageRowRequest>) {
        val body = googleMainPageRowRequests.map {
            listOf(
                "'" + it.googleId,
                "'" + it.publicId,
                it.name,
                it.birthday,
                it.age,
                it.dateEnrollment,
                it.classNum,
                it.hostel,
                it.discount,
                it.price,
                it.currency,
                it.balance,
                it.parentId,
                it.childId,
                it.pause,
                it.comment,
                it.alarm,
                it.alarmDetails,
                it.penalty,
            )
        }
        appendMainPage(body)
    }

    private fun appendMainPage(body: List<List<String>>) {
        val appendBody = ValueRange()
            .setValues(body)
        sheets.spreadsheets().values()
            .append(sheetsProperties.sheetId, "${sheetsProperties.mainTableName}!A1", appendBody)
            .setValueInputOption("USER_ENTERED")
            .setInsertDataOption("INSERT_ROWS")
            .execute()
    }

}