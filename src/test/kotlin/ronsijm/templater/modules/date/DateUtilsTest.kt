package ronsijm.templater.modules.date

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Standalone tests for DateUtils that don't require IntelliJ Platform
 */
class DateUtilsTest {
    
    @Test
    fun `test convertMomentFormat with YYYY`() {
        val result = DateUtils.convertMomentFormat("YYYY")
        assertEquals("yyyy", result)
        println("YYYY -> $result")
    }
    
    @Test
    fun `test convertMomentFormat with YYYY-MM-DD`() {
        val result = DateUtils.convertMomentFormat("YYYY-MM-DD")
        assertEquals("yyyy-MM-dd", result)
        println("YYYY-MM-DD -> $result")
    }
    
    @Test
    fun `test convertMomentFormat with HH mm ss`() {
        val result = DateUtils.convertMomentFormat("HH:mm:ss")
        assertEquals("HH:mm:ss", result)
        println("HH:mm:ss -> $result")
    }
    
    @Test
    fun `test convertMomentFormat with 12-hour time`() {
        val result = DateUtils.convertMomentFormat("h:mm:ss a")
        assertEquals("h:mm:ss a", result)
        println("h:mm:ss a -> $result")
    }
    
    @Test
    fun `test convertMomentFormat with full datetime`() {
        val result = DateUtils.convertMomentFormat("YYYY-MM-DD HH:mm:ss")
        assertEquals("yyyy-MM-dd HH:mm:ss", result)
        println("YYYY-MM-DD HH:mm:ss -> $result")
    }
    
    @Test
    fun `test convertMomentFormat with weekday`() {
        val result = DateUtils.convertMomentFormat("dddd, MMMM Do YYYY")
        assertEquals("EEEE, MMMM d yyyy", result)
        println("dddd, MMMM Do YYYY -> $result")
    }
    
    @Test
    fun `test formatDate with simple format`() {
        val date = LocalDate.of(2024, 1, 15)
        val result = DateUtils.formatDate(date, "yyyy-MM-dd")
        assertEquals("2024-01-15", result)
        println("formatDate(2024-01-15, 'yyyy-MM-dd') = $result")
    }
    
    @Test
    fun `test formatDate with Moment format`() {
        val date = LocalDate.of(2024, 1, 15)
        val result = DateUtils.formatDate(date, "YYYY-MM-DD")
        assertEquals("2024-01-15", result)
        println("formatDate(2024-01-15, 'YYYY-MM-DD') = $result")
    }
    
    @Test
    fun `test formatDateTime with simple format`() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45)
        val result = DateUtils.formatDateTime(dateTime, "yyyy-MM-dd HH:mm:ss")
        assertEquals("2024-01-15 14:30:45", result)
        println("formatDateTime(2024-01-15 14:30:45, 'yyyy-MM-dd HH:mm:ss') = $result")
    }
    
    @Test
    fun `test formatDateTime with Moment format`() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45)
        val result = DateUtils.formatDateTime(dateTime, "YYYY-MM-DD HH:mm:ss")
        assertEquals("2024-01-15 14:30:45", result)
        println("formatDateTime(2024-01-15 14:30:45, 'YYYY-MM-DD HH:mm:ss') = $result")
    }
    
    @Test
    fun `test formatDateTime with 12-hour time`() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45)
        val result = DateUtils.formatDateTime(dateTime, "h:mm:ss a")
        assertEquals("2:30:45 PM", result)
        println("formatDateTime(2024-01-15 14:30:45, 'h:mm:ss a') = $result")
    }
    
    @Test
    fun `test formatDateTime with Moment 12-hour time`() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45)
        val result = DateUtils.formatDateTime(dateTime, "h:mm:ss A")
        assertEquals("2:30:45 PM", result)
        println("formatDateTime(2024-01-15 14:30:45, 'h:mm:ss A') = $result")
    }
    
    @Test
    fun `test applyDateTimeOffset with days`() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45)
        val result = DateUtils.applyDateTimeOffset(dateTime, "1")
        assertEquals(LocalDateTime.of(2024, 1, 16, 14, 30, 45), result)
        println("applyDateTimeOffset(2024-01-15 14:30:45, '1') = $result")
    }
    
    @Test
    fun `test applyDateTimeOffset with negative days`() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45)
        val result = DateUtils.applyDateTimeOffset(dateTime, "-1")
        assertEquals(LocalDateTime.of(2024, 1, 14, 14, 30, 45), result)
        println("applyDateTimeOffset(2024-01-15 14:30:45, '-1') = $result")
    }
    
    @Test
    fun `test parseDateTime with simple format`() {
        val result = DateUtils.parseDateTime("2024-01-15 14:30:45", "yyyy-MM-dd HH:mm:ss")
        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 30, 45), result)
        println("parseDateTime('2024-01-15 14:30:45', 'yyyy-MM-dd HH:mm:ss') = $result")
    }
    
    @Test
    fun `test parseDateTime with Moment format`() {
        val result = DateUtils.parseDateTime("2024-01-15 14:30:45", "YYYY-MM-DD HH:mm:ss")
        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 30, 45), result)
        println("parseDateTime('2024-01-15 14:30:45', 'YYYY-MM-DD HH:mm:ss') = $result")
    }
}

