package hr.algebra.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import hr.algebra.myapplication.ui.case.CaseScreen
import hr.algebra.myapplication.ui.case.CaseViewModel
import hr.algebra.myapplication.ui.home.HomeScreen
import hr.algebra.myapplication.ui.login.LoginScreen
import hr.algebra.myapplication.ui.login.LoginUiState
import hr.algebra.myapplication.ui.login.LoginViewModel
import hr.algebra.myapplication.ui.registration.RegistrationScreen
import hr.algebra.myapplication.ui.registration.RegistrationViewModel
import hr.algebra.myapplication.ui.vehicle.VehicleScreen
import hr.algebra.myapplication.ui.vehicle.VehicleUiState
import hr.algebra.myapplication.ui.vehicle.VehicleViewModel
import hr.algebra.myapplication.welcome.WelcomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {

    val registrationViewModel: RegistrationViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()
    val vehicleViewModel: VehicleViewModel = viewModel()


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
        startDestination = "welcome",
        modifier = modifier,
    )
    {
        composable("welcome") {
            WelcomeScreen(
                onNavigateToRegister = { navController.navigate(Routes.Register) },
                onNavigateToLogin = { navController.navigate(Routes.Login) }
            )
        }
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
                },
                onNavigateToVehicle = {
                    navController.navigate(Routes.Vehicle)
                },
                onNavigateToCase = {
                    navController.navigate(Routes.Case)
                }
            )
        }
        composable(Routes.Vehicle) {
            val loginState by loginViewModel.uiState.collectAsState()
            val loginResult = loginState
            if (loginResult is LoginUiState.Success) {
                VehicleScreen(
                    viewModel = vehicleViewModel,
                    userId = loginResult.user.id,
                    token = loginResult.token
                )
            }
        }
        composable(Routes.Case) {
            val loginState by loginViewModel.uiState.collectAsState()
            val loginResult = loginState
            val vehicleState by vehicleViewModel.uiState.collectAsState()
            val vehicle = vehicleState.vehicle

            if (loginResult is LoginUiState.Success) {
                if (vehicle != null) {
                    CaseScreen(
                        viewModel = viewModel<CaseViewModel>(),
                        userId = loginResult.user.id,
                        vehicleId = vehicle.id,
                        token = loginResult.token
                    )
                } else {
                    // Redirect to Vehicle screen if no vehicle is created yet
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.Vehicle)
                    }
                }
            }
        }
    }
}
