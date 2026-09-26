package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.User
import com.example.data.UserRole
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordLoginInput: String = "",
    val forgotPasswordLoading: Boolean = false,
    val forgotPasswordResult: ForgotPasswordResult? = null
)

data class ForgotPasswordResult(
    val success: Boolean,
    val registeredEmail: String,
    val temporaryPassword: String,
    val message: String
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        userDao = database.userDao(),
        indicatorDao = database.indicatorDao(),
        uploadLogDao = database.uploadLogDao(),
        notificationDao = database.notificationDao()
    )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun login(login: String, pass: String) {
        if (login.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(loginError = "Por favor, informe seu login e senha.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loginError = null)
            val user = repository.authenticate(login, pass)
            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isLoading = false,
                    loginError = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginError = "Login ou senha incorretos. Verifique suas credenciais ou utilize 'Esqueci minha senha'."
                )
            }
        }
    }

    fun quickLoginAs(userRole: UserRole, login: String, pass: String) {
        login(login, pass)
    }

    fun openForgotPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showForgotPasswordDialog = true,
            forgotPasswordLoginInput = "",
            forgotPasswordResult = null
        )
    }

    fun closeForgotPasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showForgotPasswordDialog = false,
            forgotPasswordResult = null
        )
    }

    fun setForgotPasswordLoginInput(input: String) {
        _uiState.value = _uiState.value.copy(forgotPasswordLoginInput = input)
    }

    fun requestPasswordReset() {
        val login = _uiState.value.forgotPasswordLoginInput.trim()
        if (login.isBlank()) {
            _uiState.value = _uiState.value.copy(
                forgotPasswordResult = ForgotPasswordResult(
                    success = false,
                    registeredEmail = "",
                    temporaryPassword = "",
                    message = "Por favor, digite seu login para recuperar a senha."
                )
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(forgotPasswordLoading = true)
            val (success, email, tempPassword) = repository.resetPasswordForLogin(login)
            if (success) {
                // Notificação no sistema
                NotificationHelper.showPasswordResetNotification(
                    getApplication(),
                    login = login,
                    email = email,
                    tempPassword = tempPassword
                )

                _uiState.value = _uiState.value.copy(
                    forgotPasswordLoading = false,
                    forgotPasswordResult = ForgotPasswordResult(
                        success = true,
                        registeredEmail = email,
                        temporaryPassword = tempPassword,
                        message = "Uma nova senha temporária foi gerada e direcionada com sucesso para o e-mail cadastrado pelo administrador:\n\n📧 $email\n\nSenha provisória: $tempPassword"
                    )
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    forgotPasswordLoading = false,
                    forgotPasswordResult = ForgotPasswordResult(
                        success = false,
                        registeredEmail = "",
                        temporaryPassword = "",
                        message = "Login '$login' não foi encontrado. Contate o administrador do sistema para cadastrar seu usuário."
                    )
                )
            }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }
}
