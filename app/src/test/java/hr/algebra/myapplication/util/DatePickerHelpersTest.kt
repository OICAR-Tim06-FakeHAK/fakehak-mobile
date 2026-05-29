package hr.algebra.myapplication.util

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Test

class DatePickerHelpersTest {

    @Test fun `parses YYYY-MM-DD into UTC start-of-day millis`() {
        val expected = LocalDate.of(2024, 1, 15)
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()

        assertThat("2024-01-15".toUtcMillisOrNull()).isEqualTo(expected)
    }

    @Test fun `returns null on malformed input`() {
        assertThat("not-a-date".toUtcMillisOrNull()).isNull()
        assertThat("2024/01/15".toUtcMillisOrNull()).isNull()
        assertThat("".toUtcMillisOrNull()).isNull()
    }

    @Test fun `formats epoch millis as ISO local date in UTC`() {
        val millis = LocalDate.of(2024, 1, 15)
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()

        assertThat(millis.toIsoLocalDateString()).isEqualTo("2024-01-15")
    }

    @Test fun `round trips through millis without drift`() {
        val original = "2026-05-28"
        val millis = original.toUtcMillisOrNull()!!
        assertThat(millis.toIsoLocalDateString()).isEqualTo(original)
    }
}
