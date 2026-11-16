package com.valeerp.customerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valeerp.customerapp.ui.TrackViewModel

class MainActivity : ComponentActivity() {
    private val vm: TrackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val state by vm.state.collectAsState()
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("ValeERP Cliente", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Seguimiento por polling REST (sin push ni webhooks).",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedTextField(
                            value = state.apiBase,
                            onValueChange = vm::onApiBaseChange,
                            label = { Text("API base") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.tripCode,
                            onValueChange = vm::onTripCodeChange,
                            label = { Text("Código DSP-…") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = vm::startPolling,
                                enabled = !state.polling,
                                modifier = Modifier.weight(1f),
                            ) { Text("Seguir") }
                            OutlinedButton(
                                onClick = vm::stopPolling,
                                enabled = state.polling,
                                modifier = Modifier.weight(1f),
                            ) { Text("Parar") }
                        }
                        OutlinedButton(
                            onClick = vm::refreshOnce,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Actualizar una vez") }
                        Text(state.label.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium)
                        Text("Estado: ${state.status.ifBlank { "—" }}")
                        Text("Lat: ${state.lastLat ?: "—"}")
                        Text("Lng: ${state.lastLng ?: "—"}")
                        Text("Última señal: ${state.lastLocatedAt ?: "—"}")
                        Text(state.message, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Mapa OSM/Google queda para una fase posterior; " +
                                "aquí validamos el contrato de ubicación con el ERP.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
