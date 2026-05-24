package hr.algebra.myapplication

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp
import hr.algebra.myapplication.data.AuthPreferences
import javax.inject.Inject

@HiltAndroidApp
class HrApp : Application() {

    @Inject lateinit var authPreferences: AuthPreferences

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(authPreferences.getNightMode())
    }
}
