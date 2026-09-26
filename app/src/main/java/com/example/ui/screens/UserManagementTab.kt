package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.data.UserRole
import com.example.ui.IndicatorsViewModel
import com.example.ui.theme.CorporateBlueDark
import com.example.ui.theme.CorporateBlueLight
import com.example.ui.theme.CorporateBluePrimary
import com.example.ui.theme.CorporateBorder
import com.example.ui.theme.CorporateTealSecondary
import com.example.ui.theme.CorporateTextPrimary
import com.example.ui.theme.CorporateTextSecondary
import com.example.ui.theme.StatusDanger

@Composable
fun UserManagementTab(
    users: List<User>,
    viewModel: IndicatorsViewModel,
    modifier: Modifier = Modifier
) {
    var showAddUserDialog by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CorporateBlueLight.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CorporateBluePrimary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Gestão de Usuários e Técnicos",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CorporateBlueDark
                                )
                            )
                            Text(
                                text = "Cadastre novos técnicos e gerencie os e-mails cadastrados para redefinição segura de senha.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CorporateTextSecondary
                                )
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Usuários Cadastrados (${users.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CorporateTextPrimary
                        )
                    )

                    Button(
                        onClick = { showAddUserDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CorporateBluePrimary),
                        modifier = Modifier.testTag("add_user_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Novo Técnico", fontSize = 13.sp)
                    }
                }
            }

            items(users, key = { it.id }) { user ->
                UserCard(
                    user = user,
                    onResetPassword = {
                        viewModel.resetUserPassword(user) { msg ->
                            notificationMessage = msg
                        }
                    },
                    onDelete = {
                        if (user.role != UserRole.ADMIN) {
                            viewModel.deleteUser(user)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Diálogo para Adicionar Técnico
        if (showAddUserDialog) {
            AddTechnicianDialog(
                onDismiss = { showAddUserDialog = false },
                onConfirm = { nome, login, email, pass ->
                    viewModel.addTechnician(nome, login, email, pass) { success, msg ->
                        notificationMessage = msg
                        if (success) {
                            showAddUserDialog = false
                        }
                    }
                }
            )
        }

        // Diálogo com Mensagem de Confirmação
        notificationMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { notificationMessage = null },
                title = { Text("Aviso do Sistema") },
                text = { Text(msg) },
                confirmButton = {
                    Button(onClick = { notificationMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onResetPassword: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CorporateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (user.role == UserRole.ADMIN) CorporateBluePrimary else CorporateTealSecondary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (user.role == UserRole.ADMIN) Icons.Default.Shield else Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CorporateTextPrimary
                            )
                        )
                        Text(
                            text = "Login da Planilha: ${user.login}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CorporateBluePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (user.role == UserRole.ADMIN) CorporateBlueLight else CorporateTealSecondary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (user.role == UserRole.ADMIN) "ADMIN" else "TÉCNICO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (user.role == UserRole.ADMIN) CorporateBluePrimary else CorporateTealSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = CorporateTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "E-mail cadastrado (Recuperação): ${user.email}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CorporateTextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onResetPassword,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("reset_password_${user.login}")
                ) {
                    Icon(imageVector = Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Redefinir Senha", fontSize = 12.sp)
                }

                if (user.role != UserRole.ADMIN) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_user_${user.login}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remover Usuário",
                            tint = StatusDanger
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddTechnicianDialog(
    onDismiss: () -> Unit,
    onConfirm: (nome: String, login: String, email: String, pass: String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("123456") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cadastrar Novo Técnico",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "O login cadastrado aqui deve coincidir com o login que constará nas planilhas Excel para permitir o vínculo automático dos dados.",
                    style = MaterialTheme.typography.bodySmall.copy(color = CorporateTextSecondary)
                )

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome Completo") },
                    placeholder = { Text("Ex: Roberto Gomes") },
                    modifier = Modifier.fillMaxWidth().testTag("new_user_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it },
                    label = { Text("Login da Planilha (Identificador)") },
                    placeholder = { Text("Ex: tec.gomes") },
                    modifier = Modifier.fillMaxWidth().testTag("new_user_login"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail Cadastrado pelo ADM") },
                    placeholder = { Text("Ex: roberto.gomes@empresa.com.br") },
                    modifier = Modifier.fillMaxWidth().testTag("new_user_email"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Senha Inicial") },
                    modifier = Modifier.fillMaxWidth().testTag("new_user_password"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nome, login, email, pass) },
                colors = ButtonDefaults.buttonColors(containerColor = CorporateBluePrimary),
                modifier = Modifier.testTag("confirm_add_user")
            ) {
                Text("Salvar Técnico")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
