package hr.algebra.myapplication.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import hr.algebra.myapplication.R
import hr.algebra.myapplication.databinding.DialogCaseDetailsBinding
import hr.algebra.myapplication.models.CaseProfile

class CaseDetailsDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogCaseDetailsBinding.inflate(layoutInflater)
        val case = requireArguments().let {
            CaseProfile(
                id = it.getInt(ARG_ID),
                userName = it.getString(ARG_USER) ?: "",
                vehicleInfo = it.getString(ARG_VEHICLE) ?: "",
                latitude = it.getDouble(ARG_LAT),
                longitude = it.getDouble(ARG_LON),
                status = it.getString(ARG_STATUS) ?: "",
                assignedEmployeeName = it.getString(ARG_ASSIGNED),
                createdAt = it.getString(ARG_CREATED) ?: "",
            )
        }

        binding.tvId.text = case.id.toString()
        binding.tvStatus.text = case.status
        binding.tvUser.text = case.userName
        binding.tvVehicle.text = case.vehicleInfo
        binding.tvAssigned.text =
            case.assignedEmployeeName ?: getString(R.string.case_value_unassigned)
        binding.tvLocation.text = getString(
            R.string.case_location_format,
            case.latitude,
            case.longitude,
        )
        binding.tvCreated.text = case.createdAt

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.case_details_title)
            .setView(binding.root)
            .setPositiveButton(R.string.action_close, null)
            .create()
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_USER = "user"
        private const val ARG_VEHICLE = "vehicle"
        private const val ARG_LAT = "lat"
        private const val ARG_LON = "lon"
        private const val ARG_STATUS = "status"
        private const val ARG_ASSIGNED = "assigned"
        private const val ARG_CREATED = "created"

        fun newInstance(case: CaseProfile): CaseDetailsDialog = CaseDetailsDialog().apply {
            arguments = Bundle().apply {
                putInt(ARG_ID, case.id)
                putString(ARG_USER, case.userName)
                putString(ARG_VEHICLE, case.vehicleInfo)
                putDouble(ARG_LAT, case.latitude)
                putDouble(ARG_LON, case.longitude)
                putString(ARG_STATUS, case.status)
                putString(ARG_ASSIGNED, case.assignedEmployeeName)
                putString(ARG_CREATED, case.createdAt)
            }
        }
    }
}
