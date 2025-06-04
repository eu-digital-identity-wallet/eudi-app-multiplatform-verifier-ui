package eu.europa.ec.euidi.verifier.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.preferences.PreferencesController
import eu.europa.ec.euidi.verifier.preferences.PreferencesKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    private val preferencesController: PreferencesController
) : ViewModel() {

    private val _uiState = MutableStateFlow("Welcome to Home Screen")
    val uiState: StateFlow<String> = _uiState.asStateFlow()

    fun updateUiState(newMessage: String) {
        _uiState.value = newMessage
    }

    fun saveToPrefs(value: String) {
        viewModelScope.launch {
            preferencesController.save(PreferencesKey.FOO, value)
        }
    }

    fun retrieveFromPrefs() {
        viewModelScope.launch {
            val value = preferencesController.retrieve<String>(PreferencesKey.FOO).firstOrNull().orEmpty()
            updateUiState(value)
        }
    }
}