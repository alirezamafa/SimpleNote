package simplenote.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import simplenote.AuthRepository
import simplenote.UserInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserInfo?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }


    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            user = repo.fetchUserInfo()
        } catch (e: Exception) {
            error = e.localizedMessage ?: e.message ?: "Unknown error"
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {

                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("<", fontSize = 28.sp)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text("Error: $error")
            } else {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            val label = (user?.email ?: user?.username ?: "U").firstOrNull()?.uppercase() ?: "U"
                            Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(user?.username ?: "-", style = MaterialTheme.typography.titleMedium)
                        Text(user?.email ?: "-", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("APP SETTINGS", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))


                ListItem(
                    headlineContent = { Text("Change Password") },
                    trailingContent = { Text(">", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("change_password") }.background(MaterialTheme.colorScheme.surface)
                )

                Divider()


                ListItem(
                    headlineContent = { Text("Log Out", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutConfirm = true }
                )

                if (showLogoutConfirm) {
                    AlertDialog(
                        onDismissRequest = { showLogoutConfirm = false },
                        title = { Text("Log Out") },
                        text = { Text("Are you sure you want to log out from the application?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutConfirm = false

                                repo.clearTokens()
                                navController.navigate("login") {
                                    popUpTo("getting_started") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }) { Text("Yes") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }
    val scope = rememberCoroutineScope()
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var showOldPass by remember { mutableStateOf(false) }
    var showNewPass by remember { mutableStateOf(false) }
    var showConfirmPass by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("<", fontSize = 28.sp) }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center-align content
        ) {
            Text(
                text = "Please input your current password first",
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center // Center-align text
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = oldPass,
                onValueChange = { oldPass = it },
                label = { Text("Current Password") },
                placeholder = { Text("e.g., password123") }, // Added placeholder
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showOldPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showOldPass = !showOldPass }) {
                        Icon(
                            imageVector = if (showOldPass) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showOldPass) "Hide current password" else "Show current password"
                        )
                    }
                }
            )
            Spacer(Modifier.height(12.dp))

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = { Text("New Password") },
                placeholder = { Text("e.g., newpassword123") }, // Added placeholder
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showNewPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNewPass = !showNewPass }) {
                        Icon(
                            imageVector = if (showNewPass) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showNewPass) "Hide new password" else "Show new password"
                        )
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it },
                label = { Text("Retype New Password") },
                placeholder = { Text("e.g., newpassword123") }, // Added placeholder
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showConfirmPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPass = !showConfirmPass }) {
                        Icon(
                            imageVector = if (showConfirmPass) Icons.Filled.VisibilityOff else Icons.Filled.VisibilityOff,
                            contentDescription = if (showConfirmPass) "Hide confirm password" else "Show confirm password"
                        )
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    msg = null
                    if (oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
                        msg = "Please fill all fields"
                        return@Button
                    }
                    if (newPass != confirmPass) {
                        msg = "New password and confirm do not match"
                        return@Button
                    }
                    loading = true
                    scope.launch {
                        try {
                            val resp = repo.changePassword(oldPass, newPass)
                            msg = resp.message ?: "Password changed"
                            oldPass = ""; newPass = ""; confirmPass = ""
                        } catch (e: Exception) {
                            msg = e.localizedMessage ?: e.message ?: "Failed to change password"
                        } finally { loading = false }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Submitting...")
                } else {
                    Text("Submit New Password")
                }
            }

            msg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, textAlign = TextAlign.Center) // Center-align message
            }
        }
    }
}