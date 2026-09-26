package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserRole
import com.example.ui.AuthViewModel
import com.example.ui.theme.CorporateBlueDark
import com.example.ui.theme.CorporateBlueLight
import com.example.ui.theme.CorporateBluePrimary
import com.example.ui.theme.CorporateBorder
import com.example.ui.theme.CorporateTealLight
import com.example.ui.theme.CorporateTealSecondary
import com.example.ui.theme.CorporateTextPrimary
import com.example.ui.theme.CorporateTextSecondary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()
    var loginInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = CorporateBluePrimary,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CorporateBlueDark,
                    letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Portal Claro de Performance e Indicadores Técnicos",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CorporateTextSecondary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Acesso ao Sistema",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = CorporateTextPrimary
                        )
                    )
                    Text(
                        text = "Insira suas credenciais corporativas",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CorporateTextSecondary
                        ),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Campo Login
                    OutlinedTextField(
                        value = loginInput,
                        onValueChange = { loginInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_input"),
                        label = { Text("Login do Técnico ou ADM") },
                        placeholder = { Text("Ex: tec.carlos ou admin") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuário",
                                tint = CorporateBluePrimary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Senha
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        label = { Text("Senha") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Senha",
                                tint = CorporateBluePrimary
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.testTag("toggle_password_visibility")
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Ocultar senha" else "Exibir senha",
                                    tint = CorporateTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                authViewModel.login(loginInput, passwordInput)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Botão Esqueci minha senha
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(
                            onClick = { authViewModel.openForgotPasswordDialog() },
                            modifier = Modifier.testTag("forgot_password_button")
                        ) {
                            Text(
                                text = "Esqueci minha senha",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = CorporateBluePrimary
                                )
                            )
                        }
                    }

                    // Mensagem de Erro
                    AnimatedVisibility(visible = uiState.loginError != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = uiState.loginError ?: "",
                                color = StatusDanger,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão Entrar
                    Button(
                        onClick = { authViewModel.login(loginInput, passwordInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_button"),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CorporateBluePrimary
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.surface,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Entrar no Sistema",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Atalhos de Demonstração / Perfis Rápidos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CorporateBlueLight.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CorporateBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Acesso Rápido de Teste (Perfis Disponíveis)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = CorporateBlueDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickLoginChip(
                            label = "👑 ADM Geral",
                            login = "admin",
                            role = "Acesso Total",
                            onClick = {
                                loginInput = "admin"
                                passwordInput = "C.1985.w"
                                authViewModel.quickLoginAs(UserRole.ADMIN, "admin", "C.1985.w")
                            }
                        )

                        QuickLoginChip(
                            label = "🛠️ Tec. Carlos",
                            login = "tec.carlos",
                            role = "Seus Indicadores",
                            onClick = {
                                loginInput = "tec.carlos"
                                passwordInput = "123456"
                                authViewModel.quickLoginAs(UserRole.TECNICO, "tec.carlos", "123456")
                            }
                        )

                        QuickLoginChip(
                            label = "🛠️ Tec. Marcos",
                            login = "tec.silva",
                            role = "Seus Indicadores",
                            onClick = {
                                loginInput = "tec.silva"
                                passwordInput = "123456"
                                authViewModel.quickLoginAs(UserRole.TECNICO, "tec.silva", "123456")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rodapé com garantia de segurança e sincronização offline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CorporateTealSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Autenticação Segura • Suporte Offline com Auto-Sync",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CorporateTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Diálogo "Esqueci minha senha"
        if (uiState.showForgotPasswordDialog) {
            ForgotPasswordDialog(
                uiState = uiState,
                onLoginChange = { authViewModel.setForgotPasswordLoginInput(it) },
                onRequestReset = { authViewModel.requestPasswordReset() },
                onDismiss = { authViewModel.closeForgotPasswordDialog() }
            )
        }
    }
}

@Composable
fun QuickLoginChip(
    label: String,
    login: String,
    role: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CorporateBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CorporateBluePrimary
                )
            )
            Text(
                text = "$login ($role)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CorporateTextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    uiState: com.example.ui.AuthUiState,
    onLoginChange: (String) -> Unit,
    onRequestReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = CorporateBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recuperação de Senha",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CorporateBlueDark
                    )
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Informe seu login cadastrado no sistema. Uma nova senha temporária será gerada e direcionada automaticamente para o e-mail cadastrado pelo administrador.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CorporateTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.forgotPasswordLoginInput,
                    onValueChange = onLoginChange,
                    label = { Text("Login do Técnico ou ADM") },
                    placeholder = { Text("Ex: tec.carlos") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forgot_password_login_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (uiState.forgotPasswordLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = CorporateBluePrimary
                        )
                    }
                }

                uiState.forgotPasswordResult?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = if (result.success) CorporateTealLight else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (result.success) "Envio Realizado com Sucesso!" else "Atenção",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (result.success) StatusSuccess else StatusDanger
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CorporateTextPrimary
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRequestReset,
                enabled = !uiState.forgotPasswordLoading,
                colors = ButtonDefaults.buttonColors(containerColor = CorporateBluePrimary),
                modifier = Modifier.testTag("submit_forgot_password")
            ) {
                Text("Direcionar para o E-mail")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
