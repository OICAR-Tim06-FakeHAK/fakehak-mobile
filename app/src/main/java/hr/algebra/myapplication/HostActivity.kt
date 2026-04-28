package hr.algebra.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hr.algebra.fakehak_mobile.fragments.HomeFragment
import hr.algebra.fakehak_mobile.fragments.LoginFragment
import hr.algebra.myapplication.databinding.ActivityHostBinding
import hr.algebra.myapplication.managers.AppEvent
import hr.algebra.myapplication.managers.AppEventBus
import hr.algebra.myapplication.managers.TokenManager
import kotlinx.coroutines.launch

class HostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hr.algebra.myapplication.api.RetrofitClient.init(this)

        binding = ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            AppEventBus.events.collect { event ->
                if (event is AppEvent.Logout) {
                    supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    loadLoginFragment()
                }
            }
        }

        if (savedInstanceState == null) {
            val tokenManager = TokenManager(this)
            if (tokenManager.isTokenValid()) {
                loadHomeFragment()
            } else {
                loadLoginFragment()
            }
        }
    }

    fun loadLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    fun loadRegisterFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hr.algebra.fakehak_mobile.fragments.RegisterFragment())
            .addToBackStack(null)
            .commit()
    }

    fun loadHomeFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }
}
