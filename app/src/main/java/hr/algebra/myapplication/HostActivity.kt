package hr.algebra.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import hr.algebra.myapplication.databinding.ActivityHostBinding
import hr.algebra.myapplication.fragments.HomeFragment
import hr.algebra.myapplication.fragments.LoginFragment
import hr.algebra.myapplication.fragments.RegisterFragment
import hr.algebra.myapplication.managers.AppEvent
import hr.algebra.myapplication.managers.AppEventBus
import hr.algebra.myapplication.repository.AuthRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHostBinding

    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            AppEventBus.events.collect { event ->
                if (event is AppEvent.Logout) {
                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    loadLoginFragment()
                }
            }
        }

        if (savedInstanceState == null) {
            if (authRepository.hasPersistedSession()) {
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
            .replace(R.id.fragment_container, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }

    fun loadHomeFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }
}
