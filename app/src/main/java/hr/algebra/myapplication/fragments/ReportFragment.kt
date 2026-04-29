package hr.algebra.myapplication.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import hr.algebra.myapplication.api.RetrofitClient
import hr.algebra.myapplication.databinding.FragmentReportBinding
import hr.algebra.myapplication.dialogs.ReportIncidentDialog
import hr.algebra.myapplication.models.ApiResult
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showMap()

        binding.btnReportIncident.setOnClickListener {
            showReportDialog()
        }
    }

    private fun showMap() {
        Configuration.getInstance().userAgentValue = requireContext().packageName

        val map = binding.map
        map.setMultiTouchControls(true)

        val controller = map.controller
        controller.setZoom(5.0) // start zoomed out

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        val locationOverlay = MyLocationNewOverlay(
            GpsMyLocationProvider(requireContext()),
            map
        )

        locationOverlay.enableMyLocation()

        locationOverlay.runOnFirstFix {
            requireActivity().runOnUiThread {
                val myLocation = locationOverlay.myLocation
                if (myLocation != null) {
                    currentLatitude = myLocation.latitude
                    currentLongitude = myLocation.longitude
                    controller.setZoom(15.0) // zoom in
                    controller.setCenter(myLocation)
                }
            }
        }

        map.overlays.add(locationOverlay)
    }
    private fun showReportDialog() {

        val dialog = ReportIncidentDialog(
            vehicles =
                RetrofitClient.userManager?.userFlow?.value?.vehicles ?: emptyList(),
            currentLat = currentLatitude,
            currentLon = currentLongitude
        ) { request ->
            Log.d("REPORT", "Sending request: $request")

            viewLifecycleOwner.lifecycleScope.launch {
                val res = RetrofitClient.userManager?.createReport(request)
                if (res is ApiResult.Success) {
                    Toast.makeText(context, "Report filed successfully", Toast.LENGTH_SHORT).show()
                } else if (res is ApiResult.Error) {
                    Toast.makeText(context, "Report failed: ${res.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show(parentFragmentManager, "ReportIncidentDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
