package hr.algebra.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import hr.algebra.myapplication.R
import hr.algebra.myapplication.data.AuthPreferences
import hr.algebra.myapplication.databinding.FragmentUserBinding
import hr.algebra.myapplication.managers.UserManager
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.UserProfileUpdate
import hr.algebra.myapplication.repository.AuthRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var userManager: UserManager
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var authPreferences: AuthPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            userManager.userFlow.collect { profile ->
                profile?.let {
                    binding.etFirstName.setText(it.firstName)
                    binding.etLastName.setText(it.lastName)
                    binding.etUserEmail.setText(it.email)
                    binding.etUserPhone.setText(it.phoneNumber)
                    binding.tvUserStatus.text = "Status: ${it.accountStatus}"
                }
            }
        }

        binding.btnUpdateUser.setOnClickListener {
            val r = UserProfileUpdate(
                firstName = binding.etFirstName.text.toString().trim(),
                lastName = binding.etLastName.text.toString().trim(),
                phoneNumber = binding.etUserPhone.text.toString().trim(),
                email = binding.etUserEmail.text.toString().trim()
            )

            viewLifecycleOwner.lifecycleScope.launch {
                val res = userManager.update(r)
                if (res is ApiResult.Success) {
                    Toast.makeText(context, "User Updated successfully", Toast.LENGTH_SHORT).show()
                } else if (res is ApiResult.Error) {
                    Toast.makeText(context, "Update failed: ${res.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                authRepository.logout()
            }
        }

        setupThemeToggle()
    }

    private fun setupThemeToggle() {
        val checkedId = when (authPreferences.getNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> R.id.btnThemeLight
            AppCompatDelegate.MODE_NIGHT_YES -> R.id.btnThemeDark
            else -> R.id.btnThemeSystem
        }
        binding.themeToggleGroup.check(checkedId)

        binding.themeToggleGroup.addOnButtonCheckedListener { _, buttonId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (buttonId) {
                R.id.btnThemeLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.btnThemeDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            if (mode == authPreferences.getNightMode()) return@addOnButtonCheckedListener
            authPreferences.saveNightMode(mode)
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
