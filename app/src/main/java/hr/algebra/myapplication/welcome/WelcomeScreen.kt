package hr.algebra.myapplication.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("Register")
            }
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("Login")
            }
        }
    }
}
