package hr.algebra.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import hr.algebra.myapplication.R
import hr.algebra.myapplication.adapters.CasesAdapter
import hr.algebra.myapplication.databinding.FragmentCasesBinding
import hr.algebra.myapplication.dialogs.CaseDetailsDialog
import hr.algebra.myapplication.managers.UserManager
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.CaseProfile
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CasesFragment : Fragment() {
    private var _binding: FragmentCasesBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var userManager: UserManager

    private lateinit var adapter: CasesAdapter
    private var allCases: List<CaseProfile> = emptyList()
    private var activeOnly: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCasesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CasesAdapter(emptyList()) { case ->
            CaseDetailsDialog.newInstance(case).show(parentFragmentManager, "CaseDetailsDialog")
        }
        binding.rvCases.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCases.adapter = adapter

        binding.casesFilterGroup.check(R.id.btnFilterAll)
        binding.casesFilterGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            activeOnly = checkedId == R.id.btnFilterActive
            applyFilter()
        }

        loadCases()
    }

    private fun loadCases() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = userManager.getCases()) {
                is ApiResult.Success -> {
                    allCases = result.data
                    applyFilter()
                }
                is ApiResult.Error -> {
                    Toast.makeText(
                        context,
                        getString(R.string.cases_load_failed, result.message ?: ""),
                        Toast.LENGTH_SHORT
                    ).show()
                    allCases = emptyList()
                    applyFilter()
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    private fun applyFilter() {
        val filtered = if (activeOnly) {
            allCases.filter { it.status.equals("ACTIVE", ignoreCase = true) }
        } else {
            allCases
        }
        adapter.updateData(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
