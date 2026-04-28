package hr.algebra.myapplication.managers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.algebra.myapplication.models.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

sealed class AppEvent {
    object Logout : AppEvent()
    data class UserUpdated(val profile: UserProfile) : AppEvent()
}

object AppEventBus {

    private val _events = MutableSharedFlow<AppEvent>()

    /** Observe all app-wide events. Collect this in your ViewModel or Activity. */
    val events = _events.asSharedFlow()

    suspend fun publish(event: AppEvent) {
        _events.emit(event)
    }
}

// ─── ViewModel Extensions ─────────────────────────────────────────────────────

fun ViewModel.onLogout(action: suspend () -> Unit) {
    viewModelScope.launch {
        AppEventBus.events
            .filterIsInstance<AppEvent.Logout>()
            .collect { action() }
    }
}

fun ViewModel.onUserUpdated(action: suspend (UserProfile) -> Unit) {
    viewModelScope.launch {
        AppEventBus.events
            .filterIsInstance<AppEvent.UserUpdated>()
            .collect { action(it.profile) }
    }
}

fun ViewModel.onAppEvent(
    onLogout: (suspend () -> Unit)? = null,
    onUserUpdated: (suspend (UserProfile) -> Unit)? = null
) {
    viewModelScope.launch {
        AppEventBus.events.collect { event ->
            when (event) {
                is AppEvent.Logout -> onLogout?.invoke()
                is AppEvent.UserUpdated -> onUserUpdated?.invoke(event.profile)
            }
        }
    }
}