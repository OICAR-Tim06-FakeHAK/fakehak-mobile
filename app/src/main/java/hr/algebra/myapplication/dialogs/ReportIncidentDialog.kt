package hr.algebra.myapplication.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import hr.algebra.myapplication.R
import hr.algebra.myapplication.models.CaseReport
import hr.algebra.myapplication.models.VehicleProfile

class ReportIncidentDialog(
    private val vehicles: List<VehicleProfile>,
    private val currentLat: Double?,
    private val currentLon: Double?,
    private val onSubmit: (CaseReport) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = requireActivity().layoutInflater
            .inflate(R.layout.dialog_report_incident, null)

        val spinner = view.findViewById<Spinner>(R.id.spinnerVehicles)
        val editText = view.findViewById<EditText>(R.id.etDescription)

        // Spinner setup
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            vehicles.map { it.brand + ", " + it.model + " (" + it.registrationPlate + ")" }
        )
        spinner.adapter = adapter

        return AlertDialog.Builder(requireContext())
            .setTitle("Report Incident")
            .setView(view)
            .setPositiveButton("Submit") { _, _ ->

                val lat = currentLat
                val lon = currentLon

                if (lat == null || lon == null) return@setPositiveButton

                val selectedVehicle = vehicles[spinner.selectedItemPosition]

                val request = CaseReport(
                    userId = 1,
                    vehicleId = selectedVehicle.id,
                    latitude = lat,
                    longitude = lon,
                    description = editText.text.toString()
                )

                onSubmit(request)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}