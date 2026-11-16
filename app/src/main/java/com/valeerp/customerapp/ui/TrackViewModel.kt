package com.valeerp.customerapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.valeerp.customerapp.BuildConfig
import com.valeerp.customerapp.dispatch.DispatchTrackApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TrackUiState(
    val apiBase: String = BuildConfig.DEFAULT_API_BASE,
    val tripCode: String = "",
    val polling: Boolean = false,
    val status: String = "",
    val label: String = "",
    val lastLat: Double? = null,
    val lastLng: Double? = null,
    val lastLocatedAt: String? = null,
    val message: String = "Pega el código DSP-… que te dio el transportista.",
)

class TrackViewModel(app: Application) : AndroidViewModel(app) {
    private val api = DispatchTrackApi(BuildConfig.DEFAULT_API_BASE)
    private var pollJob: Job? = null

    private val _state = MutableStateFlow(TrackUiState())
    val state: StateFlow<TrackUiState> = _state

    fun onApiBaseChange(value: String) {
        _state.update { it.copy(apiBase = value) }
        api.updateBaseUrl(value)
    }

    fun onTripCodeChange(value: String) {
        _state.update { it.copy(tripCode = value.trim()) }
    }

    fun startPolling() {
        val code = _state.value.tripCode
        if (code.isBlank()) {
            _state.update { it.copy(message = "Indica un código DSP-…") }
            return
        }
        stopPolling()
        api.updateBaseUrl(_state.value.apiBase)
        _state.update { it.copy(polling = true, message = "Consultando…") }
        pollJob = viewModelScope.launch(Dispatchers.IO) {
            var intervalMs = 12_000L
            while (isActive) {
                runCatching { api.getTrip(code) }
                    .onSuccess { trip ->
                        intervalMs = (trip.pollIntervalSeconds.coerceIn(5, 60) * 1000L)
                        _state.update {
                            it.copy(
                                status = trip.status,
                                label = trip.label,
                                lastLat = trip.lastLatitude,
                                lastLng = trip.lastLongitude,
                                lastLocatedAt = trip.lastLocatedAt,
                                message = "Actualizado (poll ${trip.pollIntervalSeconds}s)",
                            )
                        }
                        if (trip.status == "ENTREGADO" || trip.status == "ANULADO") {
                            _state.update { it.copy(polling = false, message = "Despacho cerrado: ${trip.status}") }
                            return@launch
                        }
                    }
                    .onFailure { err ->
                        _state.update { it.copy(message = err.message ?: "Error de red") }
                    }
                delay(intervalMs)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
        _state.update { it.copy(polling = false) }
    }

    fun refreshOnce() {
        val code = _state.value.tripCode
        if (code.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                api.updateBaseUrl(_state.value.apiBase)
                api.getTrip(code)
            }.onSuccess { trip ->
                _state.update {
                    it.copy(
                        status = trip.status,
                        label = trip.label,
                        lastLat = trip.lastLatitude,
                        lastLng = trip.lastLongitude,
                        lastLocatedAt = trip.lastLocatedAt,
                        message = "Lectura puntual OK",
                    )
                }
            }.onFailure { err ->
                _state.update { it.copy(message = err.message ?: "Error") }
            }
        }
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }
}
