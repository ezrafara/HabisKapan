package com.example.pencatatankeuangan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth

import com.example.pencatatankeuangan.data.SessionDataStore
import com.example.pencatatankeuangan.data.SupabaseAuthRepository

import com.example.pencatatankeuangan.model.ActiveProduct
import com.example.pencatatankeuangan.model.ShoppingItem
import com.example.pencatatankeuangan.model.UsageLog

import com.example.pencatatankeuangan.ui.auth.AuthScreen
import com.example.pencatatankeuangan.ui.home.HomeScreen
import com.example.pencatatankeuangan.ui.shopping.DaftarBelanjaScreen
import com.example.pencatatankeuangan.ui.shopping.ShoppingDetailScreen
import com.example.pencatatankeuangan.ui.shopping.ShoppingFormScreen
import com.example.pencatatankeuangan.ui.finance.PencatatanKeuanganScreen
import com.example.pencatatankeuangan.ui.product.ProdukScreen
import com.example.pencatatankeuangan.ui.profile.ProfilScreen
import com.example.pencatatankeuangan.ui.purchase.RiwayatListScreen
import com.example.pencatatankeuangan.ui.purchase.FormPembelianScreen
import com.example.pencatatankeuangan.ui.purchase.DetailPembelianScreen

import com.example.pencatatankeuangan.ui.usage.DetailPemakaianScreen
import com.example.pencatatankeuangan.ui.usage.FormPemakaianScreen
import com.example.pencatatankeuangan.ui.usage.FormProdukAktifScreen
import com.example.pencatatankeuangan.ui.usage.KelolaProdukAktifScreen
import com.example.pencatatankeuangan.ui.usage.PemakaianContent
import com.example.pencatatankeuangan.ui.usage.UsageDatePickerDialog
import com.example.pencatatankeuangan.ui.usage.UsageIconBox

import com.example.pencatatankeuangan.viewmodel.ShoppingViewModel
import com.example.pencatatankeuangan.viewmodel.UsageViewModel
import com.example.pencatatankeuangan.viewmodel.PurchaseViewModel
import com.example.pencatatankeuangan.viewmodel.ProductViewModel
import com.example.pencatatankeuangan.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MaterialTheme {

                // ==========================================
                // SUPABASE AUTHENTICATION
                // ==========================================

                val context = LocalContext.current

                val authRepository = remember {
                    SupabaseAuthRepository(
                        SessionDataStore(context.applicationContext)
                    )
                }

                val coroutineScope = rememberCoroutineScope()

                var isCheckingSession by remember {
                    mutableStateOf(true)
                }

                var isLoggedIn by rememberSaveable {
                    mutableStateOf(false)
                }

                // Pulihkan sesi login ketika aplikasi dibuka
                LaunchedEffect(Unit) {
                    isLoggedIn = authRepository.restoreSession()
                    isCheckingSession = false
                }

                // ==========================================
                // NAVBAR UTAMA
                // ==========================================

                var selectedPage by rememberSaveable {
                    mutableStateOf("home")
                }

                // ==========================================
                // TAB AKTIVITAS
                // ==========================================

                var activityTab by rememberSaveable {
                    mutableStateOf("Keuangan")
                }

                // ==========================================
                // SUBHALAMAN PEMBELIAN
                // ==========================================

                var purchasePage by rememberSaveable {
                    mutableStateOf("list")
                }

                var selectedPurchaseId by rememberSaveable {
                    mutableStateOf<Long?>(null)
                }

                // ==========================================
                // SUBHALAMAN PEMAKAIAN
                // ==========================================

                var usagePage by rememberSaveable {
                    mutableStateOf("list")
                }

                var selectedUsageId by rememberSaveable {
                    mutableStateOf(-1L)
                }

                var editingUsage by rememberSaveable {
                    mutableStateOf(false)
                }

                var showUsageDatePicker by rememberSaveable {
                    mutableStateOf(false)
                }

                // ==========================================
                // FITUR BELANJA
                // ==========================================

                var selectedShoppingItem by remember {
                    mutableStateOf<ShoppingItem?>(null)
                }

                var editingShoppingItem by remember {
                    mutableStateOf<ShoppingItem?>(null)
                }

                // ==========================================
                // PEMERIKSAAN SESI LOGIN
                // ==========================================

                if (isCheckingSession) {

                    Box(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                } else if (!isLoggedIn) {

                    AuthScreen(
                        onLoginSuccess = {
                            isLoggedIn = true
                            selectedPage = "home"
                            activityTab = "Keuangan"
                        }
                    )

                } else {

                    // ==========================================
                    // VIEWMODEL
                    // ==========================================

                    val purchaseViewModel: PurchaseViewModel = viewModel()
                    val productViewModel: ProductViewModel = viewModel()
                    val financeViewModel: FinanceViewModel = viewModel()
                    val shoppingViewModel: ShoppingViewModel = viewModel()
                    val usageViewModel: UsageViewModel = viewModel()

                    val usageState by usageViewModel.uiState.collectAsState()
                    val productState by productViewModel.uiState.collectAsState()
                    val financeState by financeViewModel.uiState.collectAsState()

                    val selectedUsage = usageState.entries
                        .firstOrNull { it.log.id == selectedUsageId }

                    val currentPurchaseId: Long? = selectedPurchaseId

                    val selectedPurchase = purchaseViewModel.daftar
                        .firstOrNull { item ->
                            item.id == currentPurchaseId
                        }

                    // ==========================================
                    // BACK ANDROID DI TAB PEMBELIAN
                    // ==========================================

                    BackHandler(
                        enabled = selectedPage == "aktivitas" &&
                                activityTab == "Pembelian"
                    ) {
                        when (purchasePage) {

                            "form" -> {
                                purchasePage =
                                    if (currentPurchaseId == null) {
                                        "list"
                                    } else {
                                        "detail"
                                    }
                            }

                            "detail" -> {
                                purchasePage = "list"
                                selectedPurchaseId = null
                            }

                            else -> {
                                activityTab = "Keuangan"
                                selectedPurchaseId = null
                                purchasePage = "list"
                            }
                        }
                    }

                    // ==========================================
                    // BACK ANDROID DI SUBHALAMAN PEMAKAIAN
                    // ==========================================

                    BackHandler(
                        enabled = selectedPage == "aktivitas" &&
                                activityTab == "Pemakaian" &&
                                usagePage != "list"
                    ) {
                        usagePage = when (usagePage) {

                            "form" -> {
                                if (editingUsage && selectedUsage != null) {
                                    "detail"
                                } else {
                                    "list"
                                }
                            }

                            "active_form" -> "active_list"

                            else -> "list"
                        }
                    }

                    // ==========================================
                    // NAVIGASI HALAMAN
                    // ==========================================

                    when (selectedPage) {

                        // ======================================
                        // BERANDA
                        // ======================================

                        "home" -> {

                            HomeScreen(
                                productState = productState,
                                transactions = financeState.transactions,

                                onAddProduct = {
                                    selectedPage = "produk"
                                },

                                onUsage = {
                                    selectedPage = "aktivitas"
                                    activityTab = "Pemakaian"
                                    usagePage = "list"
                                },

                                onShopping = {
                                    selectedPage = "belanja"
                                },

                                onPurchase = {
                                    selectedPage = "aktivitas"
                                    activityTab = "Pembelian"
                                    purchasePage = "form"
                                    selectedPurchaseId = null
                                    purchaseViewModel.bersihkanAksiError()
                                    purchaseViewModel.muatDaftar()
                                },

                                onOpenProducts = {
                                    selectedPage = "produk"
                                },

                                onOpenActivity = {
                                    selectedPage = "aktivitas"
                                },

                                onOpenProfile = {
                                    selectedPage = "profil"
                                }
                            )
                        }

                        // ======================================
                        // HALAMAN PRODUK
                        // ======================================

                        "produk" -> {

                            ProdukScreen(
                                productViewModel = productViewModel,

                                onNavigateHome = {
                                    selectedPage = "home"
                                },

                                onNavigateShopping = {
                                    selectedPage = "belanja"
                                },

                                onNavigateActivity = {
                                    selectedPage = "aktivitas"
                                },

                                onNavigateProfile = {
                                    selectedPage = "profil"
                                },

                                onAddToShopping = { product ->

                                    shoppingViewModel.addItemFromProduct(
                                        productName = product.name,
                                        quantity = 1,
                                        priority = "Sedang",
                                        note = product.brand
                                    )

                                    selectedPage = "belanja"
                                }
                            )
                        }

                        // ======================================
                        // HALAMAN PROFIL
                        // ======================================

                        "profil" -> {

                            ProfilScreen(
                                onNavigateHome = {
                                    selectedPage = "home"
                                },

                                onNavigateProduct = {
                                    selectedPage = "produk"
                                },

                                onNavigateShopping = {
                                    selectedPage = "belanja"
                                },

                                onNavigateActivity = {
                                    selectedPage = "aktivitas"
                                },

                                onLogout = {

                                    coroutineScope.launch {

                                        authRepository.logout()

                                        isLoggedIn = false

                                        selectedPage = "home"
                                        activityTab = "Keuangan"
                                        purchasePage = "list"
                                        selectedPurchaseId = null

                                        selectedShoppingItem = null
                                        editingShoppingItem = null
                                    }
                                }
                            )
                        }

                        // ======================================
                        // DAFTAR BELANJA
                        // ======================================

                        "belanja" -> {

                            DaftarBelanjaScreen(
                                shoppingViewModel = shoppingViewModel,

                                onNavigateHome = {
                                    selectedPage = "home"
                                },

                                onNavigateProduct = {
                                    selectedPage = "produk"
                                },

                                onNavigateActivity = {
                                    selectedPage = "aktivitas"
                                },

                                onNavigateProfile = {
                                    selectedPage = "profil"
                                },

                                onAddItem = {
                                    editingShoppingItem = null
                                    selectedPage = "form"
                                },

                                onItemClick = { item ->
                                    selectedShoppingItem = item
                                    selectedPage = "detail"
                                }
                            )
                        }

                        // ======================================
                        // DETAIL BELANJA
                        // ======================================

                        "detail" -> {

                            val item = selectedShoppingItem

                            if (item != null) {

                                ShoppingDetailScreen(
                                    item = item,
                                    shoppingViewModel = shoppingViewModel,

                                    onBack = {
                                        selectedPage = "belanja"
                                    },

                                    onEdit = { selectedItem ->
                                        editingShoppingItem = selectedItem
                                        selectedPage = "form"
                                    },

                                    onDelete = {
                                        selectedShoppingItem = null
                                        selectedPage = "belanja"
                                    }
                                )

                            } else {
                                selectedPage = "belanja"
                            }
                        }

                        // ======================================
                        // FORM TAMBAH / EDIT BELANJA
                        // ======================================

                        "form" -> {

                            ShoppingFormScreen(
                                item = editingShoppingItem,

                                onBack = {
                                    selectedPage =
                                        if (editingShoppingItem == null) {
                                            "belanja"
                                        } else {
                                            "detail"
                                        }
                                },

                                onSave = {
                                        productName,
                                        quantity,
                                        priority,
                                        note ->

                                    val itemToEdit = editingShoppingItem

                                    if (itemToEdit == null) {

                                        shoppingViewModel.addItem(
                                            productName = productName,
                                            quantity = quantity,
                                            priority = priority,
                                            note = note
                                        )

                                    } else {

                                        shoppingViewModel.updateItem(
                                            itemToEdit.copy(
                                                productName = productName,
                                                quantity = quantity,
                                                priority = priority,
                                                note = note
                                            )
                                        )

                                        selectedShoppingItem = itemToEdit.copy(
                                            productName = productName,
                                            quantity = quantity,
                                            priority = priority,
                                            note = note
                                        )
                                    }

                                    editingShoppingItem = null

                                    selectedPage =
                                        if (itemToEdit == null) {
                                            "belanja"
                                        } else {
                                            "detail"
                                        }
                                }
                            )
                        }

                        // ======================================
                        // HALAMAN AKTIVITAS
                        // ======================================

                        else -> {

                            if (
                                activityTab == "Pemakaian" &&
                                usagePage != "list"
                            ) {

                                when (usagePage) {

                                    // ==================================
                                    // DETAIL PEMAKAIAN
                                    // ==================================

                                    "detail" -> {

                                        val entry = selectedUsage

                                        if (entry == null) {

                                            LaunchedEffect(Unit) {
                                                usagePage = "list"
                                            }

                                        } else {

                                            DetailPemakaianScreen(
                                                entry = entry,

                                                stats = usageState.statsFor(
                                                    entry.log.activeProductId
                                                ),

                                                isSaving = usageState.isSaving,
                                                errorMessage = usageState.errorMessage,

                                                onBack = {
                                                    usagePage = "list"
                                                },

                                                onSelectLog = {
                                                    selectedUsageId = it
                                                },

                                                onEdit = {
                                                    usageViewModel.clearError()
                                                    editingUsage = true
                                                    usagePage = "form"
                                                },

                                                onDelete = {
                                                    usageViewModel.deleteUsage(
                                                        entry.log.id
                                                    ) {
                                                        selectedUsageId = -1L
                                                        usagePage = "list"
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    // ==================================
                                    // FORM TAMBAH / EDIT PEMAKAIAN
                                    // ==================================

                                    "form" -> {

                                        val editTarget =
                                            if (editingUsage) {
                                                selectedUsage?.log
                                            } else {
                                                null
                                            }

                                        FormPemakaianScreen(
                                            activeProducts = usageState.activeProducts,
                                            initial = editTarget,
                                            isSaving = usageState.isSaving,
                                            errorMessage = usageState.errorMessage,

                                            onBack = {
                                                usagePage =
                                                    if (editTarget != null) {
                                                        "detail"
                                                    } else {
                                                        "list"
                                                    }
                                            },

                                            onTambahProdukAktif = {
                                                usageViewModel.clearError()
                                                usagePage = "active_form"
                                            },

                                            onSimpan = {
                                                    activeProductId,
                                                    quantity,
                                                    unit,
                                                    date,
                                                    time,
                                                    note ->

                                                if (editTarget == null) {

                                                    usageViewModel.addUsage(
                                                        UsageLog(
                                                            id = 0L,
                                                            activeProductId = activeProductId,
                                                            quantity = quantity,
                                                            unit = unit,
                                                            date = date,
                                                            time = time,
                                                            note = note
                                                        )
                                                    ) {
                                                        usagePage = "list"
                                                    }

                                                } else {

                                                    usageViewModel.updateUsage(
                                                        editTarget.copy(
                                                            activeProductId = activeProductId,
                                                            quantity = quantity,
                                                            unit = unit,
                                                            date = date,
                                                            time = time,
                                                            note = note
                                                        )
                                                    ) {
                                                        usagePage = "detail"
                                                    }
                                                }
                                            }
                                        )
                                    }

                                    // ==================================
                                    // DAFTAR PRODUK AKTIF
                                    // ==================================

                                    "active_list" -> {

                                        KelolaProdukAktifScreen(
                                            activeProducts = usageState.activeProducts,
                                            errorMessage = usageState.errorMessage,

                                            onBack = {
                                                usagePage = "list"
                                            },

                                            onTambah = {
                                                usageViewModel.clearError()
                                                usagePage = "active_form"
                                            },

                                            onDelete = { activeProductId ->
                                                usageViewModel.deleteActiveProduct(
                                                    activeProductId
                                                )
                                            }
                                        )
                                    }

                                    // ==================================
                                    // FORM TAMBAH PRODUK AKTIF
                                    // ==================================

                                    "active_form" -> {

                                        FormProdukAktifScreen(
                                            products = productState.products,

                                            isSaving = usageState.isSaving,
                                            errorMessage = usageState.errorMessage,

                                            onBack = {
                                                usagePage = "active_list"
                                            },

                                            onSimpan = {
                                                    product,
                                                    quantity,
                                                    unit,
                                                    startDate ->

                                                usageViewModel.addActiveProduct(
                                                    ActiveProduct(
                                                        id = 0L,
                                                        productId = product.id,
                                                        productName = product.name,
                                                        initialQuantity = quantity,
                                                        unit = unit,
                                                        startDate = startDate
                                                    )
                                                ) {
                                                    usagePage = "active_list"
                                                }
                                            }
                                        )
                                    }
                                }

                            } else {

                                // ==================================
                                // HALAMAN PENCATATAN KEUANGAN
                                // ==================================

                                PencatatanKeuanganScreen(

                                    onNavigateHome = {
                                        selectedPage = "home"
                                    },

                                    onNavigateProduct = {
                                        selectedPage = "produk"
                                    },

                                    onNavigateShopping = {
                                        selectedPage = "belanja"
                                    },

                                    onNavigateProfile = {
                                        selectedPage = "profil"
                                    },

                                    activityTab = activityTab,

                                    onActivityTabSelected = { tab ->

                                        activityTab = tab
                                        usagePage = "list"

                                        if (tab == "Pembelian") {
                                            purchasePage = "list"
                                            selectedPurchaseId = null
                                            purchaseViewModel.muatDaftar()
                                        }
                                    },

                                    // DAFTAR PEMAKAIAN HARIAN
                                    usageContent = {

                                        PemakaianContent(
                                            state = usageState,

                                            onItemClick = { entry ->
                                                selectedUsageId = entry.log.id
                                                usagePage = "detail"
                                            },

                                            onTambah = {
                                                usageViewModel.clearError()
                                                editingUsage = false
                                                usagePage = "form"
                                            },

                                            onKelolaProduk = {
                                                usageViewModel.clearError()
                                                usagePage = "active_list"
                                            },

                                            onRetry = {
                                                usageViewModel.load()
                                            }
                                        )
                                    },

                                    // IKON KALENDER
                                    headerAction = {

                                        if (activityTab == "Pemakaian") {

                                            UsageIconBox(
                                                icon = Icons.Outlined.CalendarMonth,
                                                description = "Pilih tanggal",
                                                onClick = {
                                                    showUsageDatePicker = true
                                                }
                                            )
                                        }
                                    },

                                    // KONTEN RIWAYAT PEMBELIAN
                                    purchaseContent = {

                                        when (purchasePage) {

                                            // DAFTAR PEMBELIAN
                                            "list" -> {

                                                RiwayatListScreen(
                                                    daftar = purchaseViewModel.daftar,
                                                    isLoading = purchaseViewModel.isLoading,
                                                    errorMessage = purchaseViewModel.errorMessage,

                                                    onRetry = {
                                                        purchaseViewModel.muatDaftar()
                                                    },

                                                    onItemClick = { id: Long ->
                                                        selectedPurchaseId = id
                                                        purchasePage = "detail"
                                                    },

                                                    onTambah = {
                                                        selectedPurchaseId = null
                                                        purchaseViewModel.bersihkanAksiError()
                                                        purchasePage = "form"
                                                    }
                                                )
                                            }

                                            // FORM TAMBAH / EDIT PEMBELIAN
                                            "form" -> {

                                                FormPembelianScreen(
                                                    awal = selectedPurchase,
                                                    isSaving = purchaseViewModel.isSaving,
                                                    errorMessage = purchaseViewModel.aksiError,

                                                    onBack = {
                                                        purchasePage =
                                                            if (currentPurchaseId == null) {
                                                                "list"
                                                            } else {
                                                                "detail"
                                                            }
                                                    },

                                                    onSimpan = { request ->

                                                        val id: Long? = selectedPurchaseId

                                                        if (id == null) {

                                                            purchaseViewModel.tambah(request) {
                                                                selectedPurchaseId = null
                                                                purchasePage = "list"
                                                            }

                                                        } else {

                                                            purchaseViewModel.ubah(id, request) {
                                                                purchasePage = "detail"
                                                            }
                                                        }
                                                    }
                                                )
                                            }

                                            // DETAIL PEMBELIAN
                                            "detail" -> {

                                                DetailPembelianScreen(
                                                    pembelian = selectedPurchase,
                                                    isLoading = false,
                                                    aksiError = purchaseViewModel.aksiError,

                                                    onBack = {
                                                        selectedPurchaseId = null
                                                        purchasePage = "list"
                                                    },

                                                    onEdit = {
                                                        purchaseViewModel.bersihkanAksiError()
                                                        purchasePage = "form"
                                                    },

                                                    onDelete = {

                                                        val id: Long? = selectedPurchaseId

                                                        if (id != null) {

                                                            purchaseViewModel.hapus(id) {
                                                                selectedPurchaseId = null
                                                                purchasePage = "list"
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // ==========================================
                    // KALENDER RIWAYAT PEMAKAIAN
                    // ==========================================

                    if (showUsageDatePicker) {

                        UsageDatePickerDialog(
                            initialDate = usageState.selectedDate,
                            title = "Pilih tanggal riwayat",

                            onDismiss = {
                                showUsageDatePicker = false
                            },

                            onPick = {
                                usageViewModel.setDate(it)
                            }
                        )
                    }
                }
            }
        }
    }
}