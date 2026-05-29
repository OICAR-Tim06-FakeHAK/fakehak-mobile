package hr.algebra.myapplication.util

import android.widget.EditText
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

/**
 * Wires [field] so tapping it opens a Material date picker. The picked value is written back
 * as a YYYY-MM-DD string. If the field already contains a valid YYYY-MM-DD value, that date is
 * pre-selected.
 */
fun EditText.bindAsDatePicker(fragmentManager: FragmentManager, tag: String = "datePicker") {
    setOnClickListener {
        val preselect = text?.toString()?.takeIf { it.isNotBlank() }?.toUtcMillisOrNull()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(hint?.toString() ?: "Select date")
            .apply { if (preselect != null) setSelection(preselect) }
            .build()
        picker.addOnPositiveButtonClickListener { utcMillis ->
            setText(utcMillis.toIsoLocalDateString())
        }
        picker.show(fragmentManager, tag)
    }
}

internal fun String.toUtcMillisOrNull(): Long? = try {
    LocalDate.parse(this, ISO_DATE).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
} catch (_: DateTimeParseException) {
    null
}

internal fun Long.toIsoLocalDateString(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate().format(ISO_DATE)
