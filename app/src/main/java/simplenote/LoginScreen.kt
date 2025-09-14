// Modified LoginScreen.kt
// Changes: Renamed some variables for clarity (e.g., 'repo' to 'authRepo', 'message' to 'errorMessage'), reordered composables (moved GettingStartedScreen and RegisterScreen before LoginScreen)

package simplenote.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.yourorg.simplenote.data.db.DatabaseProvider
import com.yourorg.simplenote.data.guest.GuestDatabaseProvider
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import simplenote.AuthRepository


@Composable
fun GettingStartedScreen(navController: NavHostController) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally // Center-align content
        ) {
            Text(text = "SimpleNote", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center ,color = MaterialTheme.colorScheme.primary )
            Spacer(modifier = Modifier.height(50.dp))
            Button(onClick = { navController.navigate("login") },  modifier = Modifier.fillMaxWidth()) { Text("Sign-Up/In") }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { navController.navigate("notes") }, modifier = Modifier.fillMaxWidth()) { Text("Continue as Guest") }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun parseError(json: String): String {
        return try {
            val obj = JSONObject(json)
            val builder = StringBuilder()
            obj.keys().forEach { key ->
                val arr = obj.getJSONArray(key)
                for (i in 0 until arr.length()) {
                    builder.append("$key: ${arr.getString(i)}\n")
                }
            }
            builder.toString().trim()
        } catch (e: Exception) {
            json
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally // Center-align content
        ) {
            TextButton(onClick = { navController.popBackStack() }) { Text("<",textAlign = TextAlign.Left, fontSize = 28.sp) }
            Text(
                text = "Register",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center // Center-align text
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                placeholder = { Text("e.g., alireza") }, // Added placeholder
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("e.g., alireza@gmail.com") }, // Added placeholder
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                placeholder = { Text("e.g., Alireza") }, // Added placeholder
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                placeholder = { Text("e.g., Mohammadi") }, // Added placeholder
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("e.g., password123") }, // Added placeholder
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff ,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                placeholder = { Text("e.g., password123") }, // Added placeholder
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showConfirmPassword) "Hide confirm password" else "Show confirm password"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match."
                            return@launch
                        }
                        if (username.isBlank() || email.isBlank() || firstName.isBlank() || lastName.isBlank() || password.isBlank()) {
                            errorMessage = "Please fill all fields."
                            return@launch
                        }

                        isLoading = true
                        try {
                            repo.register(username.trim(), password, email.trim(), firstName.trim(), lastName.trim())
                            navController.navigate("login")
                        } catch (e: HttpException) {
                            val body = e.response()?.errorBody()?.string()
                            errorMessage = body?.let { parseError(it) } ?: "Unknown error"
                        } catch (e: Exception) {
                            errorMessage = "Register failed: ${e.localizedMessage ?: e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registering...")
                } else {
                    Text("Register")
                }
            }
        }
    }

    errorMessage?.let { msg ->
        ErrorDialog(
            message = msg,
            onDismiss = { errorMessage = null },
            onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Error", msg)
                clipboard.setPrimaryClip(clip)
            }
        )
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    var userName by remember { mutableStateOf("") }
    var passWord by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally // Center-align content
        ) {
            TextButton(onClick = { navController.navigate("getting_started") }) { Text("<",textAlign = TextAlign.Left, fontSize = 28.sp) }
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center // Center-align text
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Username") },
                placeholder = { Text("e.g., alireza") }, // Added placeholder
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = passWord,
                onValueChange = { passWord = it },
                label = { Text("Password") },
                placeholder = { Text("e.g., password123") }, // Added placeholder
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        try {
                            val resp = authRepo.login(userName.trim(), passWord)
                            authRepo.saveTokens(resp.access, resp.refresh)
                            errorMessage = "Login successful."
                            try {
                                // Clear existing notes for this user
                                val noteDao = DatabaseProvider.get(context).noteDao()
                                noteDao.clear()

                                // Sync guest notes
                                val guestDao = GuestDatabaseProvider.get(context).guestNoteDao()
                                val toSync = guestDao.getAll()
                                //if (toSync.isNotEmpty()) {
                                //    val remoteRepo = NoteRepository(noteDao)
                                //    toSync.forEach { g ->
                                //        remoteRepo.createNote(NoteRequest(g.title, g.description))
                                //    }
                                //    guestDao.clear()
                                //  }
                                guestDao.clear()
                            } catch (e: Exception) {
                                println("Guest note sync failed: ${e.message}")
                            }

                            navController.navigate("notes") {
                                popUpTo("login") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Login failed: ${e.localizedMessage ?: e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logging in...")
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sign-In",
                modifier = Modifier
                    .clickable { navController.navigate("register") }
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Left,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = TextDecoration.Underline
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorMessage, textAlign = TextAlign.Center) // Center-align error message
            }
        }
    }
}