package com.example.psm_pia
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.psm_pia.ui.theme.PSM_PIATheme
import kotlinx.coroutines.launch
import java.net.URLEncoder
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
//fd
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PSM_PIATheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MyApp()
                }
            }
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable(
            route = "dashboard?gmail={gmail}&username={username}&phone={phone}&id={id}",
            arguments = listOf(
                navArgument("gmail") { type = NavType.StringType; defaultValue = "" },
                navArgument("username") { type = NavType.StringType; defaultValue = "" },
                navArgument("phone") { type = NavType.StringType; defaultValue = "" },
                navArgument("id") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val gmail = backStackEntry.arguments?.getString("gmail") ?: ""
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val userId = backStackEntry.arguments?.getInt("id") ?: 0

            if (userId == 0 || gmail.isEmpty() || username.isEmpty() || phone.isEmpty()) {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else {
                DashboardScreen(
                    navController = navController,
                    gmail = gmail,
                    username = username,
                    phone = phone,
                    userId = userId
                )
            }
        }
        composable(
            route = "profile?gmail={gmail}&username={username}&phone={phone}&id={id}",
            arguments = listOf(
                navArgument("gmail") { type = NavType.StringType; defaultValue = "" },
                navArgument("username") { type = NavType.StringType; defaultValue = "" },
                navArgument("phone") { type = NavType.StringType; defaultValue = "" },
                navArgument("id") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val gmail = backStackEntry.arguments?.getString("gmail") ?: ""
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val userId = backStackEntry.arguments?.getInt("id") ?: 0

            if (userId == 0 || gmail.isEmpty() || username.isEmpty() || phone.isEmpty()) {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else {
                ProfileScreen(
                    navController = navController,
                    gmail = gmail,
                    username = username,
                    phone = phone,
                    userId = userId
                )
            }
        }
        composable(
            route = "add_recipe/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            if (userId == 0) {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else {
                AddRecipeScreen(
                    navController = navController,
                    userId = userId
                )
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    var gmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = gmail,
                onValueChange = { gmail = it },
                label = { Text("Gmail") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !isLoading
            )

            Button(
                onClick = {
                    if (gmail.isNotEmpty() && password.isNotEmpty()) {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val response = RetrofitClient.apiService.loginUser(
                                    gmail = gmail,
                                    contraseña = password
                                )
                                if (response.isSuccessful && response.body()?.success == true) {
                                    val userData = response.body()!!
                                    val encodedGmail = URLEncoder.encode(userData.gmail ?: gmail, "UTF-8")
                                    val encodedUsername = URLEncoder.encode(userData.nombre_usuario ?: "", "UTF-8")
                                    val encodedPhone = URLEncoder.encode(userData.telefono ?: "", "UTF-8")
                                    val userId = userData.id ?: 0
                                    if (userId == 0) {
                                        snackbarHostState.showSnackbar(
                                            message = "Error: No se recibió el ID del usuario",
                                            duration = SnackbarDuration.Long
                                        )
                                        isLoading = false
                                        return@launch
                                    }
                                    navController.navigate(
                                        "dashboard?gmail=$encodedGmail&username=$encodedUsername&phone=$encodedPhone&id=$userId"
                                    ) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = response.body()?.message ?: "Error desconocido",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Error de red: ${e.message}",
                                    duration = SnackbarDuration.Long
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Por favor, completa todos los campos",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Ingresar")
                }
            }

            OutlinedButton(
                onClick = { navController.navigate("register") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Crear Cuenta")
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavHostController) {
    var gmail by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var gmailError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun isValidGmail(gmail: String): Boolean {
        val gmailRegex = Regex("^[a-zA-Z0-9._%+-]+@gmail\\.com$")
        return gmailRegex.matches(gmail)
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^\\d{10}$"))
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLetter() }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = gmail,
                onValueChange = {
                    // Eliminamos comillas y espacios al inicio y final
                    val cleanedInput = it.trim().trim('"')
                    gmail = cleanedInput
                    gmailError = if (cleanedInput.isEmpty()) "El campo es obligatorio" else if (!isValidGmail(cleanedInput)) "Debe terminar en @gmail.com" else null
                },
                label = { Text("Gmail") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = gmailError != null,
                supportingText = { gmailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                enabled = !isLoading
            )

            OutlinedTextField(
                value = username,
                onValueChange = {
                    // Eliminamos comillas y espacios al inicio y final
                    val cleanedInput = it.trim().trim('"')
                    username = cleanedInput
                    usernameError = if (cleanedInput.isEmpty()) "El campo es obligatorio" else null
                },
                label = { Text("Nombre de Usuario") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                isError = usernameError != null,
                supportingText = { usernameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                enabled = !isLoading
            )

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    if (it.all { char -> char.isDigit() } && it.length <= 10) {
                        // Eliminamos comillas y espacios al inicio y final
                        val cleanedInput = it.trim().trim('"')
                        phone = cleanedInput
                        phoneError = if (cleanedInput.isEmpty()) "El campo es obligatorio" else if (!isValidPhone(cleanedInput)) {
                            "Debe tener exactamente 10 dígitos numéricos"
                        } else null
                    }
                },
                label = { Text("Teléfono") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError != null,
                supportingText = { phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                enabled = !isLoading
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    // Eliminamos comillas y espacios al inicio y final
                    val cleanedInput = it.trim().trim('"')
                    password = cleanedInput
                    passwordError = if (cleanedInput.isEmpty()) "El campo es obligatorio" else if (!isValidPassword(cleanedInput)) {
                        "Debe tener al menos 8 caracteres, con letras y números"
                    } else null
                },
                label = { Text("Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                enabled = !isLoading
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    // Eliminamos comillas y espacios al inicio y final
                    val cleanedInput = it.trim().trim('"')
                    confirmPassword = cleanedInput
                    confirmPasswordError = if (cleanedInput.isEmpty()) "El campo es obligatorio" else if (cleanedInput != password) {
                        "Las contraseñas no coinciden"
                    } else null
                },
                label = { Text("Confirmar Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = confirmPasswordError != null,
                supportingText = { confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                enabled = !isLoading
            )

            Button(
                onClick = {
                    gmailError = if (gmail.isEmpty()) "El campo es obligatorio" else if (!isValidGmail(gmail)) "Debe terminar en @gmail.com" else null
                    usernameError = if (username.isEmpty()) "El campo es obligatorio" else null
                    phoneError = if (phone.isEmpty()) "El campo es obligatorio" else if (!isValidPhone(phone)) {
                        "Debe tener exactamente 10 dígitos numéricos"
                    } else null
                    passwordError = if (password.isEmpty()) "El campo es obligatorio" else if (!isValidPassword(password)) {
                        "Debe tener al menos 8 caracteres, con letras y números"
                    } else null
                    confirmPasswordError = if (confirmPassword.isEmpty()) "El campo es obligatorio" else if (confirmPassword != password) {
                        "Las contraseñas no coinciden"
                    } else null

                    if (gmailError == null && usernameError == null && phoneError == null && passwordError == null && confirmPasswordError == null) {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val response = RetrofitClient.apiService.registerUser(
                                    gmail = gmail,
                                    nombreUsuario = username,
                                    telefono = phone,
                                    contrasena = password,
                                    imagen = "https://example.com/default.jpg"
                                )
                                if (response.isSuccessful && response.body()?.success == true) {
                                    snackbarHostState.showSnackbar(
                                        message = "Usuario registrado exitosamente",
                                        duration = SnackbarDuration.Long
                                    )
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = response.body()?.message ?: "Error al registrar",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Error de red: ${e.message}",
                                    duration = SnackbarDuration.Long
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Registrar")
                }
            }

            OutlinedButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Volver al Inicio de Sesión")
            }
        }
    }
}

@Composable
fun DashboardScreen(
    navController: NavHostController,
    gmail: String,
    username: String,
    phone: String,
    userId: Int
) {
    var recetas by remember { mutableStateOf<List<Receta>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Cargar recetas al iniciar la pantalla
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.apiService.getRecipes(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    recetas = response.body()?.recetas ?: emptyList()
                } else {
                    errorMessage = response.body()?.message ?: "Error al cargar recetas"
                    snackbarHostState.showSnackbar(errorMessage ?: "Error desconocido")
                }
            } catch (e: Exception) {
                errorMessage = "Error de red: ${e.message}"
                snackbarHostState.showSnackbar(errorMessage ?: "Error de red")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Kitchen Lab",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Salir")
            }

            Button(
                onClick = { /* Lógica para buscar recetas*/ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Buscar")
            }

            Button(
                onClick = {
                    val encodedGmail = URLEncoder.encode(gmail, "UTF-8")
                    val encodedUsername = URLEncoder.encode(username, "UTF-8")
                    val encodedPhone = URLEncoder.encode(phone, "UTF-8")
                    navController.navigate("profile?gmail=$encodedGmail&username=$encodedUsername&phone=$encodedPhone&id=$userId")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Perfil")
            }

            Button(
                onClick = { navController.navigate("add_recipe/$userId") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Crear Receta")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Error al cargar recetas",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else if (recetas.isEmpty()) {
                Text(
                    text = "No hay recetas disponibles",
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(recetas) { receta ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .border(1.dp, Color.Gray)
                                .padding(8.dp)
                        ) {
                            Text(text = "Nombre: ${receta.nombreReceta}")
                            Text(text = "Ingredientes: ${receta.ingredientes}")
                            Text(text = "Instrucciones: ${receta.instrucciones}")
                            Text(text = "País: ${receta.paisOrigen}")
                            Text(text = "Dificultad: ${receta.dificultad}")
                            Text(text = "Tipo: ${receta.tipoPlatillo}")
                            Text(text = "Personas: ${receta.cantidadPersonas}")
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun ProfileScreen(
    navController: NavHostController,
    gmail: String,
    username: String,
    phone: String,
    userId: Int
) {
    var updatedGmail by remember { mutableStateOf(gmail) }
    var updatedUsername by remember { mutableStateOf(username) }
    var updatedPhone by remember { mutableStateOf(phone) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var gmailError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordConfirmation by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun isValidGmail(gmail: String): Boolean {
        val gmailRegex = Regex("^[a-zA-Z0-9._%+-]+@gmail\\.com$")
        return gmailRegex.matches(gmail)
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^\\d{10}$"))
    }

    // Diálogo para editar perfil
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Perfil") },
            text = {
                Column {
                    OutlinedTextField(
                        value = updatedGmail,
                        onValueChange = {
                            updatedGmail = it
                            gmailError = if (it.isEmpty()) "El campo es obligatorio" else if (!isValidGmail(it)) "Debe terminar en @gmail.com" else null
                        },
                        label = { Text("Gmail") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = gmailError != null,
                        supportingText = { gmailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )

                    OutlinedTextField(
                        value = updatedUsername,
                        onValueChange = {
                            updatedUsername = it
                            usernameError = if (it.isEmpty()) "El campo es obligatorio" else null
                        },
                        label = { Text("Nombre de Usuario") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        isError = usernameError != null,
                        supportingText = { usernameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )

                    OutlinedTextField(
                        value = updatedPhone,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                updatedPhone = it
                                phoneError = if (it.isEmpty()) "El campo es obligatorio" else if (!isValidPhone(it)) {
                                    "Debe tener exactamente 10 dígitos numéricos"
                                } else null
                            }
                        },
                        label = { Text("Teléfono") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = phoneError != null,
                        supportingText = { phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        gmailError = if (updatedGmail.isEmpty()) "El campo es obligatorio" else if (!isValidGmail(updatedGmail)) "Debe terminar en @gmail.com" else null
                        usernameError = if (updatedUsername.isEmpty()) "El campo es obligatorio" else null
                        phoneError = if (updatedPhone.isEmpty()) "El campo es obligatorio" else if (!isValidPhone(updatedPhone)) {
                            "Debe tener exactamente 10 dígitos numéricos"
                        } else null

                        if (gmailError == null && usernameError == null && phoneError == null) {
                            coroutineScope.launch {
                                try {
                                    val response = RetrofitClient.apiService.updateUser(
                                        originalGmail = gmail,
                                        newGmail = updatedGmail,
                                        nombreUsuario = updatedUsername,
                                        telefono = updatedPhone
                                    )
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        snackbarHostState.showSnackbar(
                                            message = "Perfil actualizado correctamente. Usa tu nuevo Gmail ($updatedGmail) para iniciar sesión la próxima vez.",
                                            duration = SnackbarDuration.Long
                                        )
                                        showEditDialog = false
                                        val encodedGmail = java.net.URLEncoder.encode(updatedGmail, "UTF-8")
                                        val encodedUsername = java.net.URLEncoder.encode(updatedUsername, "UTF-8")
                                        val encodedPhone = java.net.URLEncoder.encode(updatedPhone, "UTF-8")
                                        navController.navigate("profile?gmail=$encodedGmail&username=$encodedUsername&phone=$encodedPhone&id=$userId") {
                                            popUpTo("profile") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            message = response.body()?.message ?: "Error al actualizar",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        message = "Error de red: ${e.message}",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para confirmar eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Cuenta") },
            text = {
                Column {
                    Text("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordConfirmation,
                        onValueChange = { passwordConfirmation = it },
                        label = { Text("Ingresa tu contraseña para confirmar") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = passwordError != null,
                        supportingText = { passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        passwordError = if (passwordConfirmation.isEmpty()) "Ingresa tu contraseña" else null
                        if (passwordError == null) {
                            coroutineScope.launch {
                                try {
                                    val response = RetrofitClient.apiService.deleteUser(gmail = gmail)
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        snackbarHostState.showSnackbar(
                                            message = "Cuenta eliminada correctamente",
                                            duration = SnackbarDuration.Short
                                        )
                                        showDeleteDialog = false
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            message = response.body()?.message ?: "Error al eliminar la cuenta",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        message = "Error de red: ${e.message}",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (data.visuals.message.contains("Error")) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = if (data.visuals.message.contains("Error")) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Kitchen Lab",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { navController.navigate("login") }) {
                    Text("Salir")
                }
                Button(onClick = { /* Lógica para favoritos */ }) {
                    Text("Favoritos")
                }
                Button(onClick = { navController.navigate("add_recipe/$userId") }) { // Pasamos el userId en la ruta
                    Text("Crear Receta")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Información del Usuario:", style = MaterialTheme.typography.titleMedium)
                if (gmail.isNotEmpty() && username.isNotEmpty() && phone.isNotEmpty()) {
                    Text(text = "Correo Electrónico: $gmail", modifier = Modifier.padding(top = 8.dp))
                    Text(text = "Nombre de Usuario: $username", modifier = Modifier.padding(top = 8.dp))
                    Text(text = "Teléfono: $phone", modifier = Modifier.padding(top = 8.dp))
                } else {
                    Text(
                        text = "Error: No se pudieron cargar los datos del usuario.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Editar Perfil")
            }

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar Cuenta")
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    PSM_PIATheme {
        LoginScreen(rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    PSM_PIATheme {
        RegisterScreen(rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    PSM_PIATheme {
        DashboardScreen(
            navController = rememberNavController(),
            gmail = "test@gmail.com",
            username = "UsuarioEjemplo",
            phone = "1234567890",
            userId = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    PSM_PIATheme {
        ProfileScreen(
            navController = rememberNavController(),
            gmail = "test@gmail.com",
            username = "UsuarioEjemplo",
            phone = "1234567890",
            userId = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(navController: NavHostController, userId: Int) {
    var nombreReceta by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var instrucciones by remember { mutableStateOf("") }
    var paisOrigen by remember { mutableStateOf("") }
    var dificultad by remember { mutableStateOf("Principiante") }
    var tipoPlatillo by remember { mutableStateOf("Almuerzo") }
    var cantidadPersonas by remember { mutableStateOf("Individual") }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var ingredientesError by remember { mutableStateOf<String?>(null) }
    var instruccionesError by remember { mutableStateOf<String?>(null) }
    var paisOrigenError by remember { mutableStateOf<String?>(null) }
    var expandedDificultad by remember { mutableStateOf(false) }
    var expandedTipoPlatillo by remember { mutableStateOf(false) }
    var expandedCantidadPersonas by remember { mutableStateOf(false) }

    val dificultades = listOf("Principiante", "Intermedio", "Avanzado")
    val tiposPlatillo = listOf("Almuerzo", "Comida", "Cena", "Merienda")
    val cantidadesPersonas = listOf("Individual", "Dúo", "Grupal")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Texto descriptivo para cantidad de personas
    val cantidadPersonasTexto by remember(cantidadPersonas) {
        derivedStateOf {
            when (cantidadPersonas) {
                "Individual" -> "Platillo para 1 persona"
                "Dúo" -> "Platillo para 2 personas"
                "Grupal" -> "Platillo para 3 a 6 personas"
                else -> ""
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (data.visuals.message.contains("Error")) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = if (data.visuals.message.contains("Error")) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Agregar Receta",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = nombreReceta,
                onValueChange = {
                    nombreReceta = it
                    nombreError = if (it.isEmpty()) "El campo es obligatorio" else null
                },
                label = { Text("Nombre del Platillo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                isError = nombreError != null,
                supportingText = { nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            OutlinedTextField(
                value = paisOrigen,
                onValueChange = {
                    paisOrigen = it
                    paisOrigenError = if (it.isEmpty()) "El campo es obligatorio" else null
                },
                label = { Text("País de Origen") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                isError = paisOrigenError != null,
                supportingText = { paisOrigenError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            OutlinedTextField(
                value = ingredientes,
                onValueChange = {
                    ingredientes = it
                    ingredientesError = if (it.isEmpty()) "El campo es obligatorio" else null
                },
                label = { Text("Ingredientes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                isError = ingredientesError != null,
                supportingText = { ingredientesError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            OutlinedTextField(
                value = instrucciones,
                onValueChange = {
                    instrucciones = it
                    instruccionesError = if (it.isEmpty()) "El campo es obligatorio" else null
                },
                label = { Text("Instrucciones") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                isError = instruccionesError != null,
                supportingText = { instruccionesError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            // Selector de dificultad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = dificultad,
                    onValueChange = {},
                    label = { Text("Dificultad") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedDificultad = !expandedDificultad }) {
                            Icon(
                                imageVector = if (expandedDificultad) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir"
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = expandedDificultad,
                    onDismissRequest = { expandedDificultad = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    dificultades.forEach { nivel ->
                        DropdownMenuItem(
                            text = { Text(nivel) },
                            onClick = {
                                dificultad = nivel
                                expandedDificultad = false
                            }
                        )
                    }
                }
            }

            // Selector de tipo de platillo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = tipoPlatillo,
                    onValueChange = {},
                    label = { Text("Tipo de Platillo") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedTipoPlatillo = !expandedTipoPlatillo }) {
                            Icon(
                                imageVector = if (expandedTipoPlatillo) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir"
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = expandedTipoPlatillo,
                    onDismissRequest = { expandedTipoPlatillo = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tiposPlatillo.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                tipoPlatillo = tipo
                                expandedTipoPlatillo = false
                            }
                        )
                    }
                }
            }

            // Selector de cantidad de personas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                OutlinedTextField(
                    value = cantidadPersonas,
                    onValueChange = {},
                    label = { Text("Cantidad de Personas") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedCantidadPersonas = !expandedCantidadPersonas }) {
                            Icon(
                                imageVector = if (expandedCantidadPersonas) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir"
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = expandedCantidadPersonas,
                    onDismissRequest = { expandedCantidadPersonas = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    cantidadesPersonas.forEach { cantidad ->
                        DropdownMenuItem(
                            text = { Text(cantidad) },
                            onClick = {
                                cantidadPersonas = cantidad
                                expandedCantidadPersonas = false
                            }
                        )
                    }
                }
            }

            // Texto descriptivo para cantidad de personas
            Text(
                text = cantidadPersonasTexto,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    nombreError = if (nombreReceta.isEmpty()) "El campo es obligatorio" else null
                    paisOrigenError = if (paisOrigen.isEmpty()) "El campo es obligatorio" else null
                    ingredientesError = if (ingredientes.isEmpty()) "El campo es obligatorio" else null
                    instruccionesError = if (instrucciones.isEmpty()) "El campo es obligatorio" else null

                    if (nombreError == null && paisOrigenError == null && ingredientesError == null && instruccionesError == null) {
                        coroutineScope.launch {
                            try {
                                val response = RetrofitClient.apiService.addRecipe(
                                    idUsuario = userId,
                                    nombreReceta = nombreReceta,
                                    ingredientes = ingredientes,
                                    instrucciones = instrucciones,
                                    paisOrigen = paisOrigen,
                                    dificultad = dificultad,
                                    tipoPlatillo = tipoPlatillo,
                                    cantidadPersonas = cantidadPersonas
                                )
                                if (response.isSuccessful && response.body()?.success == true) {
                                    snackbarHostState.showSnackbar(
                                        message = "Receta agregada correctamente",
                                        duration = SnackbarDuration.Short
                                    )
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = response.body()?.message ?: "Error al agregar la receta",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Error de red: ${e.message}",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Por favor, corrige los errores en el formulario",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar Receta")
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}

