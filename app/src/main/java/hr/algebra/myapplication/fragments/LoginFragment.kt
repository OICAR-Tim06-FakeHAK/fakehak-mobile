package hr.algebra.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import hr.algebra.myapplication.HostActivity
import hr.algebra.myapplication.databinding.FragmentLoginBinding
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.LoginRequest
import hr.algebra.myapplication.repository.AuthRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var authRepository: AuthRepository

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

            viewLifecycleOwner.lifecycleScope.launch {
                when (val result = authRepository.login(LoginRequest(username, password))) {
                    is ApiResult.Success -> (requireActivity() as HostActivity).loadHomeFragment()
                    is ApiResult.Error -> Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    is ApiResult.Loading -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
