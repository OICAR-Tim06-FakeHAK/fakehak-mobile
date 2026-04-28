package hr.algebra.fakehak_mobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import hr.algebra.myapplication.HostActivity
import hr.algebra.myapplication.api.RetrofitClient
import hr.algebra.myapplication.databinding.FragmentRegisterBinding
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.RegisterRequest
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGoToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnRegister.setOnClickListener {
            val req = RegisterRequest(
                firstName = binding.etFirstName.text.toString().trim(),
                lastName = binding.etLastName.text.toString().trim(),
                phoneNumber = binding.etPhoneNumber.text.toString().trim(),
                email = binding.etEmail.text.toString().trim(),
                password = binding.etPassword.text.toString().trim()
            )

            if (req.firstName.isBlank() || req.lastName.isBlank() || req.email.isBlank() || req.password.isBlank()) {
                Toast.makeText(context, "Please fill the required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userRepository = hr.algebra.myapplication.repository.UserRepository(RetrofitClient.apiService)

            viewLifecycleOwner.lifecycleScope.launch {
                val result = userRepository.register(req)

                if (result is ApiResult.Success) {
                    Toast.makeText(context, "Registration successful. Please login.", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                } else if (result is ApiResult.Error) {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
