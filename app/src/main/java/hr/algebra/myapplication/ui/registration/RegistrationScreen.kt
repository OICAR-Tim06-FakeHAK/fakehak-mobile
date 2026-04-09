package hr.algebra.myapplication.ui.registration

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
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel,
) {
    val form by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Create account",
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = form.firstName,
            onValueChange = viewModel::onFirstNameChange,
            label = { Text("First name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        OutlinedTextField(
            value = form.lastName,
            onValueChange = viewModel::onLastNameChange,
            label = { Text("Last name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        OutlinedTextField(
            value = form.phoneNumber,
            onValueChange = viewModel::onPhoneNumberChange,
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            ),
        )

        OutlinedTextField(
            value = form.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
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

        val isLoading = uiState is RegistrationUiState.Loading

        Button(
            onClick = viewModel::register,
            enabled = !isLoading && form.isValid(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(18.dp),
                )
                Spacer(modifier = Modifier.height(0.dp))
                Text(" Registering…")
            } else {
                Text("Register")
            }
        }

        Text(
            text = "Backend: http://10.0.2.2:8080",
            style = MaterialTheme.typography.bodySmall,
        )
    }

    when (val state = uiState) {
        is RegistrationUiState.Success -> {
            AlertDialog(
                onDismissRequest = viewModel::dismissMessage,
                title = { Text("Success") },
                text = { Text("Your account has been created.") },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissMessage) {
                        Text("OK")
                    }
                },
            )
        }

        is RegistrationUiState.Error -> {
            AlertDialog(
                onDismissRequest = viewModel::dismissMessage,
                title = { Text("Registration failed") },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissMessage) {
                        Text("OK")
                    }
                },
            )
        }

        else -> Unit
    }
}
