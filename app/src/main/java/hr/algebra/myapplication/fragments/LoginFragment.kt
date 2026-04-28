package hr.algebra.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import hr.algebra.myapplication.HostActivity
import hr.algebra.myapplication.api.RetrofitClient
import hr.algebra.myapplication.databinding.FragmentLoginBinding
import hr.algebra.myapplication.managers.TokenManager
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.LoginRequest
import hr.algebra.myapplication.repository.UserRepository
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGoToRegister.setOnClickListener {
            (requireActivity() as HostActivity).loadRegisterFragment()
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(username, password)
            val userRepository = UserRepository(RetrofitClient.apiService)

            viewLifecycleOwner.lifecycleScope.launch {
                val result = userRepository.login(loginRequest)
                if (result is ApiResult.Success) {
                    val token = result.data.token
                    val tokenManager = TokenManager(requireContext())
                    tokenManager.saveToken(token)

                    // trigger user loading and then redirect
                    val loadResult = RetrofitClient.userManager?.load()
                    if (loadResult is ApiResult.Success) {
                        (requireActivity() as HostActivity).loadHomeFragment()
                    } else {
                        Toast.makeText(context, "Failed to load user profile", Toast.LENGTH_SHORT).show()
                    }
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
