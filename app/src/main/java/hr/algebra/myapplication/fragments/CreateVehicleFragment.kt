package hr.algebra.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import hr.algebra.myapplication.api.RetrofitClient
import hr.algebra.myapplication.databinding.FragmentCreateVehicleBinding
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.VehicleProfile
import kotlinx.coroutines.launch

class CreateVehicleFragment(vehicle: VehicleProfile) : Fragment() {
    private var _binding: FragmentCreateVehicleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateVehicleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveVehicle.setOnClickListener {
            val brand = binding.etBrand.text.toString()
            val model = binding.etModel.text.toString()
            val vin = binding.etVin.text.toString()
            val plate = binding.etPlate.text.toString()
            val date = binding.etDate.text.toString()

            if (brand.isBlank() || model.isBlank() || vin.isBlank() || plate.isBlank() || date.isBlank()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newVehicle = VehicleProfile(
                id = 0,
                brand = brand,
                model = model,
                vin = vin,
                registrationPlate = plate,
                firstRegistrationDate = date,
                createdAt = ""
            )

            viewLifecycleOwner.lifecycleScope.launch {
                val result = RetrofitClient.userManager?.addVehicle(newVehicle)
                if (result is ApiResult.Success) {
                    Toast.makeText(context, "Vehicle saved", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else if (result is ApiResult.Error) {
                    Toast.makeText(context, "Failed to save vehicle: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}