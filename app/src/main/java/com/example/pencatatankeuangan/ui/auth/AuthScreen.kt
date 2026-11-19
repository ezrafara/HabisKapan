package com.example.pencatatankeuangan.ui.auth

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.ui.platform.LocalContext
import com.example.pencatatankeuangan.data.SessionDataStore
import com.example.pencatatankeuangan.data.SupabaseAuthRepository

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================
// WARNA APLIKASI
// ==========================================

private val AuthBlue = Color(0xFF2962ED)
private val AuthNavy = Color(0xFF111B32)
private val AuthMuted = Color(0xFF7686A0)
private val AuthBorder = Color(0xFFDCE5F1)
private val AuthRed = Color(0xFFE52D36)

// ==========================================
// NAVIGASI AUTENTIKASI
// ==========================================

private enum class AuthPage {
    WELCOME,
    LOGIN,
    REGISTER
}

// ==========================================
// SCREEN UTAMA
// ==========================================

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    var currentPage by rememberSaveable {
        mutableStateOf(AuthPage.WELCOME.name)
    }

    val context = LocalContext.current

    val authRepository = remember {
        SupabaseAuthRepository(
            SessionDataStore(context.applicationContext)
        )
    }

    when (currentPage) {

        AuthPage.WELCOME.name -> {
            WelcomeScreen(
                onStart = {
                    currentPage = AuthPage.LOGIN.name
                },
                onLogin = {
                    currentPage = AuthPage.LOGIN.name
                }
            )
        }

        AuthPage.LOGIN.name -> {
            LoginScreen(
                onBack = {
                    currentPage = AuthPage.WELCOME.name
                },
                onRegister = {
                    currentPage = AuthPage.REGISTER.name
                },
                onLoginSuccess = onLoginSuccess,
                authRepository = authRepository
            )
        }

        AuthPage.REGISTER.name -> {
            RegisterScreen(
                onBack = {
                    currentPage = AuthPage.LOGIN.name
                },
                onRegisterSuccess = {
                    currentPage = AuthPage.LOGIN.name
                },
                authRepository = authRepository
            )
        }
    }
}

// ==========================================
// WELCOME SCREEN
// ==========================================

@Composable
private fun WelcomeScreen(
    onStart: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {

        // ILUSTRASI BIRU TANPA LOGO HEADER

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .clip(
                    RoundedCornerShape(
                        bottomStart = 30.dp,
                        bottomEnd = 30.dp
                    )
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF1F7FF),
                            Color(0xFFD9E9FF)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            // ILUSTRASI PRODUK TETAP DITAMPILKAN

            Box(
                modifier = Modifier
                    .size(132.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(25.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = AuthBlue,
                        modifier = Modifier.size(44.dp)
                    )

                    Icon(
                        Icons.Default.Widgets,
                        contentDescription = null,
                        tint = Color(0xFF65A5FF),
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        }

        // DESKRIPSI WELCOME

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 22.dp,
                    vertical = 20.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                "ASISTEN RUMAH TANGGAMU",
                color = AuthBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Text(
                "Kelola Kebutuhan Harian Jadi Lebih Mudah",
                color = AuthNavy,
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 31.sp
            )

            Text(
                "Catat persediaan, rencanakan belanja, " +
                        "dan pantau pengeluaran dalam satu aplikasi.",
                color = AuthMuted,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuthBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text(
                    "Mulai Sekarang",
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(10.dp))

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "Sudah punya akun?",
                    color = AuthMuted,
                    fontSize = 12.sp
                )

                Text(
                    " Masuk",
                    color = AuthBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onLogin()
                    }
                )
            }
        }
    }
}

// ==========================================
// LOGIN SCREEN
// ==========================================


@Composable
private fun LoginScreen(
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    authRepository: SupabaseAuthRepository
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AuthTopBar(onBack = onBack)

        Spacer(Modifier.height(10.dp))

        Text(
            "Selamat Datang Kembali!",
            color = AuthNavy,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Masuk untuk melanjutkan mengelola kebutuhanmu.",
            color = AuthMuted,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(8.dp))

        AuthLabel("Alamat email", true)

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                error = ""
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("nama@email.com", color = AuthMuted)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            shape = RoundedCornerShape(12.dp),
            colors = authInputColors()
        )

        AuthLabel("Kata sandi", true)

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                error = ""
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Minimal 8 karakter", color = AuthMuted)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        showPassword = !showPassword
                    }
                ) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = "Lihat kata sandi",
                        tint = AuthMuted
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = authInputColors()
        )

        Text(
            "Lupa Kata Sandi?",
            color = AuthBlue,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable {
                    showForgotDialog = true
                }
        )

        if (error.isNotBlank()) {
            Text(error, color = AuthRed, fontSize = 12.sp)
        }

        Button(
            onClick = {
                when {
                    email.isBlank() -> {
                        error = "Alamat email wajib diisi."
                    }

                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        error = "Format email tidak valid."
                    }

                    password.length < 8 -> {
                        error = "Kata sandi minimal 8 karakter."
                    }

                    else -> {
                        scope.launch {
                            isLoading = true
                            error = ""

                            val result = authRepository.login(
                                email = email,
                                password = password
                            )

                            isLoading = false

                            if (result.success) {
                                onLoginSuccess()
                            } else {
                                error = result.message
                            }
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Masuk", fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Belum punya akun? ",
                color = AuthMuted,
                fontSize = 12.sp
            )

            Text(
                "Daftar",
                color = AuthBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onRegister()
                }
            )
        }

        Spacer(Modifier.height(14.dp))
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotDialog = false
            },
            title = {
                Text("Lupa Kata Sandi?")
            },
            text = {
                Text(
                    "Fitur pemulihan kata sandi akan kita hubungkan " +
                            "setelah proses login dan registrasi selesai."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForgotDialog = false
                    }
                ) {
                    Text("Mengerti")
                }
            },
            containerColor = Color.White
        )
    }
}


// ==========================================
// REGISTER SCREEN (MODE DEMO)
// ==========================================


@Composable
private fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authRepository: SupabaseAuthRepository
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        AuthTopBar(onBack = onBack)

        Text(
            "Buat Akun HabisKapan",
            color = AuthNavy,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Daftar untuk mulai mengelola kebutuhan harianmu.",
            color = AuthMuted,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(8.dp))

        AuthLabel("Nama lengkap", true)

        AuthTextField(
            value = name,
            onValueChange = {
                name = it
                error = ""
            },
            placeholder = "Nama lengkap"
        )

        AuthLabel("Alamat email", true)

        AuthTextField(
            value = email,
            onValueChange = {
                email = it
                error = ""
            },
            placeholder = "nama@email.com"
        )

        AuthLabel("Kata sandi", true)

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                error = ""
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            placeholder = {
                Text("Minimal 8 karakter")
            },
            shape = RoundedCornerShape(12.dp),
            colors = authInputColors()
        )

        AuthLabel("Konfirmasi kata sandi", true)

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                error = ""
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            placeholder = {
                Text("Ulangi kata sandi")
            },
            shape = RoundedCornerShape(12.dp),
            colors = authInputColors()
        )

        if (error.isNotBlank()) {
            Text(error, color = AuthRed, fontSize = 12.sp)
        }

        Button(
            onClick = {
                when {
                    name.isBlank() -> {
                        error = "Nama lengkap wajib diisi."
                    }

                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        error = "Alamat email tidak valid."
                    }

                    password.length < 8 -> {
                        error = "Kata sandi minimal 8 karakter."
                    }

                    password != confirmPassword -> {
                        error = "Konfirmasi kata sandi tidak sama."
                    }

                    else -> {
                        scope.launch {
                            isLoading = true
                            error = ""

                            val result = authRepository.register(
                                name = name,
                                email = email,
                                password = password
                            )

                            isLoading = false

                            if (result.success) {
                                successMessage = result.message
                                showSuccessDialog = true
                            } else {
                                error = result.message
                            }
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Daftar", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("Registrasi Berhasil")
            },
            text = {
                Text(successMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onRegisterSuccess()
                    }
                ) {
                    Text("Ke Login")
                }
            },
            containerColor = Color.White
        )
    }
}


// ==========================================
// HEADER LOGIN & REGISTER TANPA LOGO
// ==========================================

@Composable
private fun AuthTopBar(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 18.dp,
                bottom = 20.dp
            ),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedIconButton(
            onClick = onBack,
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                AuthBorder
            )
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Kembali",
                tint = AuthNavy
            )
        }
    }
}

// ==========================================
// LABEL INPUT
// ==========================================

@Composable
private fun AuthLabel(
    text: String,
    required: Boolean = false
) {
    Row {
        Text(
            text,
            color = AuthNavy,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (required) {
            Text(
                " *",
                color = AuthRed
            )
        }
    }
}

// ==========================================
// TEXTFIELD UMUM
// ==========================================

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = {
            Text(
                placeholder,
                color = AuthMuted
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = authInputColors()
    )
}

// ==========================================
// WARNA INPUT
// ==========================================

@Composable
private fun authInputColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AuthBlue,
        unfocusedBorderColor = AuthBorder,
        focusedTextColor = AuthNavy,
        unfocusedTextColor = AuthNavy,
        cursorColor = AuthBlue
    )
