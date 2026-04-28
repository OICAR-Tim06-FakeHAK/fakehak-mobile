package hr.algebra.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import hr.algebra.myapplication.api.RetrofitClient
import hr.algebra.myapplication.databinding.FragmentVehiclesBinding
import hr.algebra.myapplication.adapters.VehiclesAdapter
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.R
import kotlinx.coroutines.launch

class VehiclesFragment : Fragment() {
    private var _binding: FragmentVehiclesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: VehiclesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehiclesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VehiclesAdapter(
            vehicles = emptyList(),
            onUpdateClick = { vehicle ->
                // Example for updating, using a mock changed model to represent user input
                val updatedVehicle = vehicle.copy(model = vehicle.model + " Updated")

                lifecycleScope.launch {
                    val result = RetrofitClient.userManager?.updateVehicle(vehicle.id, updatedVehicle)
                    if (result is ApiResult.Error) {
                        Toast.makeText(context, "Failed to update: ${result.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Vehicle Successfully Updated", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDeleteClick = { vehicle ->
                lifecycleScope.launch {
                    val result = RetrofitClient.userManager?.deleteVehicle(vehicle.id) // delete returns ApiResult<Unit>
                    if (result is ApiResult.Error) {
                        Toast.makeText(context, "Failed to delete: ${result.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        binding.rvVehicles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVehicles.adapter = adapter

        // Listen for user updates automatically modifying vehicles display
        viewLifecycleOwner.lifecycleScope.launch {
            RetrofitClient.userManager?.userFlow?.collect { profile ->
                profile?.let {
                    adapter.updateData(it.vehicles)
                }
            }
        }

        binding.btnAddVehicle.setOnClickListener {
            lifecycleScope.launch {
                RetrofitClient.userManager?.load()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
