package com.pixo.ai.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixo.ai.data.AiImageRepository
import com.pixo.ai.data.Session
import com.pixo.ai.data.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PixoUiState(
    val session: Session = Session(),
    val selectedBitmap: Bitmap? = null,
    val editedBitmap: Bitmap? = null,
    val prompt: String = "",
    val loading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class PixoViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val aiImageRepository: AiImageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PixoUiState())
    val state: StateFlow<PixoUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.session.collect { session ->
                _state.value = _state.value.copy(session = session)
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(message = "Enter email and password")
            return
        }
        viewModelScope.launch { sessionRepository.login(email.trim()) }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _state.value = _state.value.copy(
                message = "Use an email and a password with at least 6 characters"
            )
            return
        }
        viewModelScope.launch { sessionRepository.login(email.trim()) }
    }

    fun continueAsGuest() {
        viewModelScope.launch { sessionRepository.guest() }
    }

    fun logout() {
        viewModelScope.launch { sessionRepository.logout() }
    }

    fun setImage(bitmap: Bitmap) {
        _state.value = _state.value.copy(
            selectedBitmap = bitmap,
            editedBitmap = null,
            message = null
        )
    }

    fun setPrompt(value: String) {
        _state.value = _state.value.copy(prompt = value)
    }

    fun editImage() {
        val source = _state.value.selectedBitmap ?: return
        val prompt = _state.value.prompt.trim()

        if (prompt.isBlank()) {
            _state.value = _state.value.copy(message = "Tell Pixo what you want to change")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, message = null)
            runCatching {
                aiImageRepository.edit(source, prompt)
            }.onSuccess { result ->
                _state.value = _state.value.copy(
                    editedBitmap = result,
                    loading = false,
                    message = "AI edit complete"
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    loading = false,
                    message = it.message ?: "AI edit failed"
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
