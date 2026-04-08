package hr.algebra.myapplication.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
) {
    val form by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Surface {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineSmall,
            )

            OutlinedTextField(
                value = form.identifier,
                onValueChange = viewModel::onIdentifierChange,
                label = { Text("Email or username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
            )

            OutlinedTextField(
                value = form.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            val isLoading = uiState is LoginUiState.Loading

            Button(
                onClick = viewModel::login,
                enabled = !isLoading && form.isValid(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(18.dp),
                    )
                    Text(" Logging in…")
                } else {
                    Text("Login")
                }
            }

            Text(
                text = "Backend: http://10.0.2.2:8080",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        when (val state = uiState) {
            is LoginUiState.Error -> {
                AlertDialog(
                    onDismissRequest = viewModel::dismissMessage,
                    title = { Text("Login failed") },
                    text = { Text(state.message) },
                    confirmButton = {
                        TextButton(onClick = viewModel::dismissMessage) { Text("OK") }
                    },
                )
            }

            else -> Unit
        }
    }
}
