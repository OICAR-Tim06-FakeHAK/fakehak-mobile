package hr.algebra.myapplication.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import hr.algebra.myapplication.databinding.DialogUpdateVehicleBinding
import hr.algebra.myapplication.models.VehicleProfile

class UpdateVehicleDialog(
    private val vehicle: VehicleProfile,
    private val onUpdate: (VehicleProfile) -> Unit
) : DialogFragment() {

    private var _binding: DialogUpdateVehicleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogUpdateVehicleBinding.inflate(layoutInflater)

        binding.etBrand.setText(vehicle.brand)
        binding.etModel.setText(vehicle.model)
        binding.etVin.setText(vehicle.vin)
        binding.etPlate.setText(vehicle.registrationPlate)
        binding.etDate.setText(vehicle.firstRegistrationDate)

        return AlertDialog.Builder(requireContext())
            .setTitle("Update Vehicle")
            .setView(binding.root)
            .setPositiveButton("Update") { _, _ ->
                val brand = binding.etBrand.text.toString()
                val model = binding.etModel.text.toString()
                val vin = binding.etVin.text.toString()
                val plate = binding.etPlate.text.toString()
                val date = binding.etDate.text.toString()

                if (brand.isBlank() || model.isBlank() || vin.isBlank() || plate.isBlank() || date.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedVehicle = vehicle.copy(
                        brand = brand,
                        model = model,
                        vin = vin,
                        registrationPlate = plate,
                        firstRegistrationDate = date
                    )
                    onUpdate(updatedVehicle)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
