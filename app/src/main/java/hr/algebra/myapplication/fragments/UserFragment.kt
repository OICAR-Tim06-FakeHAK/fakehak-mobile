package hr.algebra.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import hr.algebra.myapplication.api.RetrofitClient
import hr.algebra.myapplication.databinding.FragmentUserBinding
import hr.algebra.myapplication.managers.TokenManager
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.UserProfileUpdate
import kotlinx.coroutines.launch

class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

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
            RetrofitClient.userManager?.userFlow?.collect { profile ->
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
                val res = RetrofitClient.userManager?.update(r)
                if (res is ApiResult.Success) {
                    Toast.makeText(context, "User Updated successfully", Toast.LENGTH_SHORT).show()
                } else if (res is ApiResult.Error) {
                    Toast.makeText(context, "Update failed: ${res.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                TokenManager(requireContext()).clear()
                RetrofitClient.userManager?.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
