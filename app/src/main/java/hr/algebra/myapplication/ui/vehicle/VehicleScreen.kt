package hr.algebra.myapplication.ui.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VehicleScreen(
    viewModel: VehicleViewModel,
    userId: Long,
    token: String
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = uiState.brand,
                onValueChange = viewModel::onBrandChange,
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.model,
                onValueChange = viewModel::onModelChange,
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.vin,
                onValueChange = viewModel::onVinChange,
                label = { Text("VIN") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.registrationPlate,
                onValueChange = viewModel::onRegistrationPlateChange,
                label = { Text("Registration Plate") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.firstRegistrationDate,
                onValueChange = viewModel::onFirstRegistrationDateChange,
                label = { Text("First Registration Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.createVehicle(userId, token) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Save Vehicle")
            }
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            uiState.vehicle?.let {
                Text(
                    text = "Vehicle created: ${it.brand} ${it.model}",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

