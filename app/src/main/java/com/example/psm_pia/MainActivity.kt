package com.example.psm_pia
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import kotlinx.coroutines.launch
import retrofit2.Response
import com.example.psm_pia.ui.theme.PSM_PIATheme
import java.net.URLEncoder

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
            route = "dashboard?gmail={gmail}&username={username}&phone={phone}",
            arguments = listOf(
                navArgument("gmail") { type = NavType.StringType; defaultValue = "" },
                navArgument("username") { type = NavType.StringType; defaultValue = "" },
                navArgument("phone") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            DashboardScreen(
                navController,
                gmail = backStackEntry.arguments?.getString("gmail") ?: "",
                username = backStackEntry.arguments?.getString("username") ?: "",
                phone = backStackEntry.arguments?.getString("phone") ?: ""
            )
        }
        composable(
            route = "profile?gmail={gmail}&username={username}&phone={phone}",
            arguments = listOf(
                navArgument("gmail") { type = NavType.StringType; defaultValue = "" },
                navArgument("username") { type = NavType.StringType; defaultValue = "" },
                navArgument("phone") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val gmail = backStackEntry.arguments?.getString("gmail") ?: ""
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""

            if (gmail.isEmpty() || username.isEmpty() || phone.isEmpty()) {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            }

            ProfileScreen(
                navController,
                gmail = gmail,
                username = username,
                phone = phone
            )
        }
        composable("add_recipe") { AddRecipeScreen(navController) }
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
                                    navController.navigate(
                                        "dashboard?gmail=$encodedGmail&username=$encodedUsername&phone=$encodedPhone"
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
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var gmailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun isValidGmail(gmail: String): Boolean {
        val gmailRegex = Regex("^[a-zA-Z0-9._%+-]+@gmail\\.com$")
        return gmailRegex.matches(gmail)
    }

    fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$")
        return passwordRegex.matches(password)
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^\\d{10}$"))
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
                text = "Registro de Usuario",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = gmail,
                onValueChange = {
                    gmail = it
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
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de Usuario") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = if (it.isEmpty()) "El campo es obligatorio" else if (!isValidPassword(it)) {
                        "Debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
                    } else null
                },
                label = { Text("Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    if (it.all { char -> char.isDigit() } && it.length <= 10) {
                        phone = it
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

            Button(
                onClick = {
                    imageUri = "android.resource://com.example.psm_pia/drawable/sample_image"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Subir Foto")
            }

            imageUri?.let { uri ->
                Text(
                    text = "Imagen seleccionada: $uri",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    gmailError = if (gmail.isEmpty()) "El campo es obligatorio" else if (!isValidGmail(gmail)) "Debe terminar en @gmail.com" else null
                    passwordError = if (password.isEmpty()) "El campo es obligatorio" else if (!isValidPassword(password)) {
                        "Debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
                    } else null
                    phoneError = if (phone.isEmpty()) "El campo es obligatorio" else if (!isValidPhone(phone)) {
                        "Debe tener exactamente 10 dígitos numéricos"
                    } else null

                    if (gmailError == null && passwordError == null && phoneError == null && username.isNotEmpty()) {
                        coroutineScope.launch {
                            try {
                                val response = RetrofitClient.apiService.registerUser(
                                    gmail = gmail,
                                    nombreUsuario = username,
                                    contrasena = password,
                                    telefono = phone,
                                    imagen = imageUri
                                )
                                if (response.isSuccessful && response.body()?.success == true) {
                                    snackbarHostState.showSnackbar(
                                        message = response.body()?.message ?: "Usuario registrado",
                                        duration = SnackbarDuration.Short
                                    )
                                    navController.popBackStack()
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
                Text("Registrar")
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

@Composable
fun DashboardScreen(
    navController: NavHostController,
    gmail: String,
    username: String,
    phone: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            onClick = { /* Lógica para buscar recetas */ },
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
                navController.navigate("profile?gmail=$encodedGmail&username=$encodedUsername&phone=$encodedPhone")
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Perfil")
        }

        Text(
            text = "Aquí se mostrarán las recetas (futuro desarrollo)",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun ProfileScreen(
    navController: NavHostController,
    gmail: String,
    username: String,
    phone: String
) {
    var updatedGmail by remember { mutableStateOf(gmail) }
    var updatedUsername by remember { mutableStateOf(username) }
    var updatedPhone by remember { mutableStateOf(phone) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) } // Nuevo estado para el diálogo de eliminación
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
                                        navController.navigate("profile?gmail=$encodedGmail&username=$encodedUsername&phone=$encodedPhone") {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                        navController.navigate("dashboard?gmail=$encodedGmail&username=$encodedUsername&phone=$encodedPhone") {
                                            popUpTo("dashboard") { inclusive = true }
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
                        // Aquí podrías agregar una solicitud al servidor para verificar la contraseña antes de eliminar
                        // Por ahora, solo verificamos que no esté vacía
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
                Button(onClick = { navController.navigate("add_recipe") }) {
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

            // Botón para eliminar cuenta
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
        ProfileScreen(
            navController = rememberNavController(),
            gmail = "test@gmail.com",
            username = "UsuarioEjemplo",
            phone = "1234567890"
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
            phone = "1234567890"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(navController: NavHostController) {
    var dishName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    // Estado para el dropdown de dificultad
    var difficulty by remember { mutableStateOf("Principiante") }
    val difficultyOptions = listOf("Principiante", "Intermedio", "Avanzado")
    var expandedDifficulty by remember { mutableStateOf(false) }

    // Estado para el dropdown de tipo de platillo
    var dishType by remember { mutableStateOf("Almuerzo") }
    val dishTypeOptions = listOf("Almuerzo", "Comida", "Cena", "Merienda")
    var expandedDishType by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Agregar Nueva Receta",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Campo: Nombre del platillo
        OutlinedTextField(
            value = dishName,
            onValueChange = { dishName = it },
            label = { Text("Nombre del Platillo") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Campo: País de origen
        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("País de Origen") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Dropdown: Dificultad
        ExposedDropdownMenuBox(
            expanded = expandedDifficulty,
            onExpandedChange = { expandedDifficulty = !expandedDifficulty }
        ) {
            OutlinedTextField(
                value = difficulty,
                onValueChange = {},
                readOnly = true,
                label = { Text("Dificultad") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDifficulty)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedDifficulty,
                onDismissRequest = { expandedDifficulty = false }
            ) {
                difficultyOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            difficulty = option
                            expandedDifficulty = false
                        }
                    )
                }
            }
        }

        // Dropdown: Tipo de platillo
        ExposedDropdownMenuBox(
            expanded = expandedDishType,
            onExpandedChange = { expandedDishType = !expandedDishType }
        ) {
            OutlinedTextField(
                value = dishType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Platillo") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDishType)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedDishType,
                onDismissRequest = { expandedDishType = false }
            ) {
                dishTypeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            dishType = option
                            expandedDishType = false
                        }
                    )
                }
            }
        }

        // Campo: Descripción
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(120.dp),
            maxLines = 5
        )

        // Botón: Subir foto
        Button(
            onClick = {
                photoUri = "android.resource://com.example.psm_pia/drawable/sample_recipe_image"
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Subir Foto del Platillo")
        }

        // Mostrar URI de la foto (si existe)
        photoUri?.let { uri ->
            Text(
                text = "Foto seleccionada: $uri",
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Botón: Guardar receta
        Button(
            onClick = {
                // Aquí puedes agregar lógica para guardar la receta (por ejemplo, en una base de datos)
                // Por ahora, solo regresa a la pantalla anterior
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Guardar Receta")
        }

        // Botón: Volver
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

