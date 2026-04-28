package hr.algebra.fakehak_mobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hr.algebra.myapplication.R
import hr.algebra.myapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSubFragment(ReportFragment())

        binding.btnNavReport.setOnClickListener {
            loadSubFragment(ReportFragment())
        }

        binding.btnNavVehicles.setOnClickListener {
            loadSubFragment(VehiclesFragment())
        }

        binding.btnNavUser.setOnClickListener {
            loadSubFragment(UserFragment())
        }
    }

    private fun loadSubFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.home_content_container, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
