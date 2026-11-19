package com.example.pencatatankeuangan.ui.profile

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.pencatatankeuangan.model.UserProfile
import com.example.pencatatankeuangan.viewmodel.ProfileViewModel

private val Blue = Color(0xFF2962ED)
private val Navy = Color(0xFF101B33)
private val Muted = Color(0xFF8190A9)
private val PageBg = Color(0xFFF6F8FC)
private val BorderColor = Color(0xFFDCE5F1)
private val Red = Color(0xFFE52935)
private val Green = Color(0xFF12A65B)

private enum class ProfilePage {
    HOME,
    EDIT,
    PASSWORD,
    ABOUT
}

// ==========================================
// SCREEN UTAMA PROFIL
// ==========================================

@Composable
fun ProfilScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = viewModel(),
    onNavigateHome: () -> Unit = {},
    onNavigateProduct: () -> Unit = {},
    onNavigateShopping: () -> Unit = {},
    onNavigateActivity: () -> Unit = {},
    onLogout: () -> Unit = {}
) {

    val state by profileViewModel.uiState.collectAsState()

    val snackbarHost = remember {
        SnackbarHostState()
    }

    var currentPage by rememberSaveable {
        mutableStateOf(ProfilePage.HOME.name)
    }

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    var showHelpDialog by remember {
        mutableStateOf(false)
    }

    // NOTIFIKASI
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHost.showSnackbar(message)
            profileViewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PageBg,

        snackbarHost = {
            SnackbarHost(snackbarHost) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Navy,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },

        bottomBar = {
            if (currentPage == ProfilePage.HOME.name) {
                ProfileBottomBar(
                    onHome = onNavigateHome,
                    onProduct = onNavigateProduct,
                    onShopping = onNavigateShopping,
                    onActivity = onNavigateActivity
                )
            }
        }

    ) { padding ->

        when (currentPage) {

            ProfilePage.HOME.name -> {

                ProfileHome(
                    profile = state.profile,

                    onEdit = {
                        currentPage = ProfilePage.EDIT.name
                    },

                    onPassword = {
                        currentPage = ProfilePage.PASSWORD.name
                    },

                    onAbout = {
                        currentPage = ProfilePage.ABOUT.name
                    },

                    onNotificationChange =
                        profileViewModel::toggleNotification,

                    onHelp = {
                        showHelpDialog = true
                    },

                    onLogout = {
                        showLogoutDialog = true
                    },

                    modifier = Modifier.padding(padding)
                )
            }

            ProfilePage.EDIT.name -> {

                EditProfileScreen(
                    profile = state.profile,

                    onBack = {
                        currentPage = ProfilePage.HOME.name
                    },

                    onSave = { name ->
                        profileViewModel.updateProfile(name)
                        currentPage = ProfilePage.HOME.name
                    },

                    onPhotoSelected = { uri ->
                        profileViewModel.updatePhoto(uri)
                    },

                    modifier = Modifier.padding(padding)
                )
            }

            ProfilePage.PASSWORD.name -> {

                ChangePasswordScreen(
                    onBack = {
                        currentPage = ProfilePage.HOME.name
                    },

                    modifier = Modifier.padding(padding)
                )
            }

            ProfilePage.ABOUT.name -> {

                AboutAppScreen(
                    onBack = {
                        currentPage = ProfilePage.HOME.name
                    },

                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // DIALOG LOGOUT
    if (showLogoutDialog) {

        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },

            title = {
                Text("Keluar dari Akun?")
            },

            text = {
                Text(
                    "Apakah kamu yakin ingin keluar dari akun HabisKapan?"
                )
            },

            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red
                    )
                ) {
                    Text("Ya, Keluar")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("Batal")
                }
            },

            containerColor = Color.White
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = {
                showHelpDialog = false
            },
            title = {
                Text("Pusat Bantuan")
            },
            text = {
                Text(
                    "Gunakan menu Produk untuk mengelola kebutuhan, " +
                            "dan Aktivitas untuk melihat catatan keuangan."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showHelpDialog = false
                    }
                ) {
                    Text("Mengerti")
                }
            }
        )
    }
}

// ==========================================
// HALAMAN PROFIL
// ==========================================

@Composable
private fun ProfileHome(
    profile: UserProfile,
    onEdit: () -> Unit,
    onPassword: () -> Unit,
    onAbout: () -> Unit,
    onNotificationChange: (Boolean) -> Unit,
    onHelp: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),

        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),

            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    "AKUN SAYA",
                    color = Blue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )

                Text(
                    "Profil",
                    color = Navy,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .background(
                        Color.White,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Pengaturan",
                    tint = Navy
                )
            }
        }

        // KARTU PROFIL
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ProfileAvatar(
                    name = profile.fullName,
                    photoUri = profile.photoUri,
                    size = 76
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    profile.fullName,
                    color = Navy,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    profile.email,
                    color = Muted,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFEAF1FF),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(
                            horizontal = 12.dp,
                            vertical = 5.dp
                        )
                ) {
                    Text(
                        "Akun aktif",
                        color = Blue,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // PENGATURAN AKUN
        Text(
            "Pengaturan Akun",
            fontSize = 15.sp,
            color = Navy,
            fontWeight = FontWeight.Medium
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column {

                ProfileMenuItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Profil",
                    subtitle = "Perbarui nama dan foto",
                    onClick = onEdit
                )

                HorizontalDivider(color = BorderColor)

                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Ubah Kata Sandi",
                    subtitle = "Jaga keamanan akunmu",
                    onClick = onPassword
                )

                HorizontalDivider(color = BorderColor)

                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "Tentang Aplikasi",
                    subtitle = "Versi dan informasi fitur",
                    onClick = onAbout
                )
            }
        }

        // LAINNYA
        Text(
            "Lainnya",
            color = Navy,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    ProfileMenuIcon(
                        Icons.Default.Notifications
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            "Notifikasi",
                            color = Navy,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )

                        Text(
                            "Pengingat stok dan belanja aktif",
                            color = Muted,
                            fontSize = 10.sp
                        )
                    }

                    Switch(
                        checked = profile.notificationEnabled,
                        onCheckedChange = onNotificationChange,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Blue
                        )
                    )
                }

                HorizontalDivider(color = BorderColor)

                ProfileMenuItem(
                    icon = Icons.Default.HelpOutline,
                    title = "Pusat Bantuan",
                    subtitle = "Panduan menggunakan aplikasi",
                    onClick = onHelp
                )
            }
        }

        // LOGOUT
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),

            border = BorderStroke(
                1.dp,
                Color(0xFFFFC9CD)
            ),

            shape = RoundedCornerShape(12.dp)
        ) {

            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                tint = Red,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Keluar dari Akun",
                color = Red,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }

        Text(
            "HabisKapan versi 1.0.0",
            color = Muted,
            fontSize = 10.sp,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Spacer(Modifier.height(12.dp))
    }
}

// ==========================================
// FOTO / AVATAR PROFIL
// ==========================================

@Composable
private fun ProfileAvatar(
    name: String,
    photoUri: String?,
    size: Int,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val bitmap = remember(photoUri) {
        try {
            if (photoUri == null) {
                null
            } else {
                context.contentResolver
                    .openInputStream(
                        android.net.Uri.parse(photoUri)
                    )?.use {
                        BitmapFactory.decodeStream(it)
                    }
            }
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Blue),

        contentAlignment = Alignment.Center
    ) {

        if (bitmap != null) {

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

        } else {

            Text(
                text = name
                    .firstOrNull()
                    ?.uppercase()
                    ?: "D",

                color = Color.White,
                fontSize = (size / 2.5f).sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(23.dp)
                .background(
                    Color.White,
                    RoundedCornerShape(7.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Blue,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ==========================================
// EDIT PROFIL
// ==========================================

@Composable
private fun EditProfileScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    onPhotoSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var name by rememberSaveable {
        mutableStateOf(profile.fullName)
    }

    var error by remember {
        mutableStateOf("")
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onPhotoSelected(it.toString())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),

        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        ProfileTopBar(
            title = "Edit Profil",
            onBack = onBack
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ProfileAvatar(
                name = name,
                photoUri = profile.photoUri,
                size = 85,
                modifier = Modifier.clickable {
                    photoLauncher.launch("image/*")
                }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Ketuk foto untuk mengganti",
                color = Muted,
                fontSize = 11.sp
            )
        }

        ProfileLabel("Nama lengkap", true)

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(13.dp),
            colors = profileInputColors()
        )

        ProfileLabel("Alamat email")

        OutlinedTextField(
            value = profile.email,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(13.dp),
            colors = profileInputColors()
        )

        // INFORMASI EMAIL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFFEEF5FF),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Blue,
                modifier = Modifier.size(19.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Email terhubung dengan akun dan belum dapat diubah dalam mode demo.",
                color = Muted,
                fontSize = 11.sp
            )
        }

        if (error.isNotBlank()) {
            Text(
                error,
                color = Red,
                fontSize = 12.sp
            )
        }

        Button(
            onClick = {

                if (name.isBlank()) {
                    error = "Nama lengkap wajib diisi."
                } else {
                    error = ""
                    onSave(name.trim())
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            ),

            shape = RoundedCornerShape(13.dp)
        ) {

            Icon(
                Icons.Default.Check,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Simpan Perubahan",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// UBAH KATA SANDI
// ==========================================

@Composable
private fun ChangePasswordScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    var currentPassword by rememberSaveable {
        mutableStateOf("")
    }

    var newPassword by rememberSaveable {
        mutableStateOf("")
    }

    var confirmPassword by rememberSaveable {
        mutableStateOf("")
    }

    var showCurrent by remember {
        mutableStateOf(false)
    }

    var showNew by remember {
        mutableStateOf(false)
    }

    var showConfirm by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf("")
    }

    var showDemoInfo by remember {
        mutableStateOf(false)
    }

    val hasMinLength = newPassword.length >= 8

    val hasLettersAndNumbers =
        newPassword.any { it.isLetter() } &&
                newPassword.any { it.isDigit() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),

        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        ProfileTopBar(
            title = "Ubah Kata Sandi",
            onBack = onBack
        )

        ProfileLabel("Kata sandi saat ini")

        PasswordInput(
            value = currentPassword,
            onValueChange = {
                currentPassword = it
            },
            visible = showCurrent,
            onToggleVisibility = {
                showCurrent = !showCurrent
            }
        )

        ProfileLabel("Kata sandi baru")

        PasswordInput(
            value = newPassword,
            onValueChange = {
                newPassword = it
            },
            visible = showNew,
            onToggleVisibility = {
                showNew = !showNew
            },
            placeholder = "Minimal 8 karakter"
        )

        ProfileLabel("Konfirmasi kata sandi baru")

        PasswordInput(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
            },
            visible = showConfirm,
            onToggleVisibility = {
                showConfirm = !showConfirm
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    PageBg,
                    RoundedCornerShape(14.dp)
                )
                .padding(14.dp),

            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                "Kata sandi harus memiliki:",
                color = Navy,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )

            PasswordRule(
                "Minimal 8 karakter",
                hasMinLength
            )

            PasswordRule(
                "Kombinasi huruf dan angka",
                hasLettersAndNumbers
            )
        }

        if (error.isNotBlank()) {
            Text(
                error,
                color = Red,
                fontSize = 12.sp
            )
        }

        Button(
            onClick = {

                when {

                    currentPassword.isBlank() -> {
                        error = "Kata sandi saat ini wajib diisi."
                    }

                    !hasMinLength -> {
                        error = "Minimal 8 karakter."
                    }

                    !hasLettersAndNumbers -> {
                        error = "Gunakan kombinasi huruf dan angka."
                    }

                    newPassword != confirmPassword -> {
                        error = "Konfirmasi kata sandi tidak sama."
                    }

                    else -> {
                        error = ""
                        showDemoInfo = true
                    }
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            ),

            shape = RoundedCornerShape(13.dp)
        ) {

            Icon(
                Icons.Default.Check,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Simpan Perubahan",
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showDemoInfo) {
        AlertDialog(
            onDismissRequest = {
                showDemoInfo = false
            },
            title = {
                Text("Mode Demo")
            },
            text = {
                Text(
                    "Validasi form berhasil, tetapi kata sandi belum " +
                            "diubah karena autentikasi Supabase belum dihubungkan."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDemoInfo = false
                    }
                ) {
                    Text("Mengerti")
                }
            }
        )
    }
}

// ==========================================
// HALAMAN TENTANG APLIKASI
// ==========================================

@Composable
private fun AboutAppScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 18.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        ProfileTopBar(
            title = "Tentang Aplikasi",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(
                        Blue,
                        RoundedCornerShape(18.dp)
                    ),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "HabisKapan",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Text(
                "Versi 1.0.0",
                color = Muted,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(28.dp))

            Text(
                "Kebutuhan harian, selalu terencana.",
                color = Navy,
                fontSize = 19.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "HabisKapan membantu pengguna Indonesia " +
                        "mencatat persediaan, pemakaian, belanja, " +
                        "pembelian, dan keuangan rumah tangga.",
                color = Muted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column {

                AboutFeatureRow("Katalog Produk")
                HorizontalDivider(color = BorderColor)

                AboutFeatureRow("Pemakaian Harian")
                HorizontalDivider(color = BorderColor)

                AboutFeatureRow("Riwayat Pembelian")
                HorizontalDivider(color = BorderColor)

                AboutFeatureRow("Catatan Keuangan")
                HorizontalDivider(color = BorderColor)

                AboutFeatureRow("Daftar Belanja")
            }
        }

        Text(
            "Dibuat untuk hidup yang lebih tertata.",
            color = Muted,
            fontSize = 10.sp,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )
    }
}

// ==========================================
// KOMPONEN UI PROFIL
// ==========================================

@Composable
private fun ProfileTopBar(
    title: String,
    onBack: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = onBack,
            modifier = Modifier.size(46.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Kembali",
                tint = Navy
            )
        }

        Text(
            title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            color = Navy
        )

        Spacer(Modifier.width(46.dp))
    }

    HorizontalDivider(color = BorderColor)
}

@Composable
private fun ProfileMenuIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {

    Box(
        modifier = Modifier
            .size(38.dp)
            .background(
                Color(0xFFEAF1FF),
                RoundedCornerShape(12.dp)
            ),

        contentAlignment = Alignment.Center
    ) {

        Icon(
            icon,
            contentDescription = null,
            tint = Blue,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        ProfileMenuIcon(icon)

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                title,
                color = Navy,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                subtitle,
                color = Muted,
                fontSize = 10.sp
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ProfileLabel(
    text: String,
    required: Boolean = false
) {

    Row {

        Text(
            text,
            color = Navy,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (required) {
            Text(" *", color = Red)
        }
    }
}

@Composable
private fun PasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    placeholder: String = ""
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,

        visualTransformation =
            if (visible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),

        placeholder = {
            Text(placeholder, color = Muted)
        },

        trailingIcon = {
            IconButton(
                onClick = onToggleVisibility
            ) {

                Icon(
                    if (visible)
                        Icons.Default.VisibilityOff
                    else
                        Icons.Default.Visibility,

                    contentDescription = "Tampilkan kata sandi",
                    tint = Muted
                )
            }
        },

        shape = RoundedCornerShape(13.dp),
        colors = profileInputColors()
    )
}

@Composable
private fun PasswordRule(
    label: String,
    valid: Boolean
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Icon(
            if (valid)
                Icons.Default.CheckCircle
            else
                Icons.Default.RadioButtonUnchecked,

            contentDescription = null,

            tint = if (valid) Green else Muted,

            modifier = Modifier.size(16.dp)
        )

        Text(
            label,
            color = Muted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun AboutFeatureRow(
    title: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(13.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        ProfileMenuIcon(
            Icons.Default.Checklist
        )

        Spacer(Modifier.width(12.dp))

        Text(
            title,
            modifier = Modifier.weight(1f),
            color = Navy,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Icon(
            Icons.Default.CheckCircleOutline,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun profileInputColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Blue,
        unfocusedBorderColor = BorderColor,
        focusedTextColor = Navy,
        unfocusedTextColor = Navy
    )

// ==========================================
// NAVBAR PROFIL AKTIF
// ==========================================

@Composable
private fun ProfileBottomBar(
    onHome: () -> Unit,
    onProduct: () -> Unit,
    onShopping: () -> Unit,
    onActivity: () -> Unit
) {

    NavigationBar(
        containerColor = Color.White
    ) {

        NavigationBarItem(
            selected = false,
            onClick = onHome,
            icon = {
                Icon(Icons.Default.Home, null)
            },
            label = {
                Text("Beranda", fontSize = 10.sp)
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = onProduct,
            icon = {
                Icon(Icons.Default.Inventory2, null)
            },
            label = {
                Text("Produk", fontSize = 10.sp)
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = onShopping,
            icon = {
                Icon(Icons.Default.ShoppingCart, null)
            },
            label = {
                Text("Belanja", fontSize = 10.sp)
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = onActivity,
            icon = {
                Icon(Icons.Default.BarChart, null)
            },
            label = {
                Text("Aktivitas", fontSize = 10.sp)
            }
        )

        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Icon(Icons.Default.Person, null)
            },
            label = {
                Text("Profil", fontSize = 10.sp)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Blue,
                selectedTextColor = Blue,
                indicatorColor = Color(0xFFEAF1FF)
            )
        )
    }
}
