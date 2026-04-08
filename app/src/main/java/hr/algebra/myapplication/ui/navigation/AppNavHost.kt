package hr.algebra.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import hr.algebra.myapplication.ui.home.HomeScreen
import hr.algebra.myapplication.ui.login.LoginScreen
import hr.algebra.myapplication.ui.login.LoginViewModel
import hr.algebra.myapplication.ui.registration.RegistrationScreen
import hr.algebra.myapplication.ui.registration.RegistrationViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {

    val registrationViewModel = remember { RegistrationViewModel() }
    val loginViewModel = remember { LoginViewModel() }


    val navigateToLogin by registrationViewModel.navigateToLogin.collectAsState()
    LaunchedEffect(navigateToLogin) {
        if (navigateToLogin) {
            registrationViewModel.onNavigatedToLogin()
            navController.navigate(Routes.Login) {
                popUpTo(Routes.Register) { inclusive = true }
            }
        }
    }

    // Observe one-time navigation event from login.
    val navigateToHome by loginViewModel.navigateToHome.collectAsState()
    LaunchedEffect(navigateToHome) {
        if (navigateToHome) {
            loginViewModel.onNavigatedToHome()
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Login) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Register,
        modifier = modifier,
    ) {
        composable(Routes.Register) {
            RegistrationScreen(viewModel = registrationViewModel)
        }
        composable(Routes.Login) {
            LoginScreen(viewModel = loginViewModel)
        }
        composable(Routes.Home) {
            HomeScreen(
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                }
            )
        }
    }
}
