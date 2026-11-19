
package com.example.pencatatankeuangan.ui.finance

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pencatatankeuangan.model.FinanceTransaction
import com.example.pencatatankeuangan.model.TransactionType
import com.example.pencatatankeuangan.viewmodel.FinanceUiState
import com.example.pencatatankeuangan.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.draw.clip

private val Blue = Color(0xFF2459E6)
private val Navy = Color(0xFF101B33)
private val Muted = Color(0xFF71819B)
private val Page = Color(0xFFF6F8FC)
private val Border = Color(0xFFDCE5F1)

private val Green = Color(0xFF0DAA58)
private val GreenBg = Color(0xFFE0F8EA)

private val Red = Color(0xFFE72F36)
private val RedBg = Color(0xFFFFE5E6)

private enum class FinancePage {
    HOME,
    FORM,
    DETAIL
}

private enum class FormMode {
    ADD,
    EDIT,
    OPENING_BALANCE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PencatatanKeuanganScreen(
    modifier: Modifier = Modifier,
    financeViewModel: FinanceViewModel = viewModel(),
    onNavigateHome: () -> Unit = {},
    onNavigateProduct: () -> Unit = {},
    onNavigateShopping: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    activityTab: String = "Keuangan",
    onActivityTabSelected: (String) -> Unit = {},
    purchaseContent: @Composable () -> Unit = {},
    usageContent: @Composable () -> Unit = {},
    headerAction: @Composable () -> Unit = {}
) {

    val state by financeViewModel.uiState.collectAsState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var page by rememberSaveable {
        mutableStateOf(FinancePage.HOME.name)
    }

    var formMode by rememberSaveable {
        mutableStateOf(FormMode.ADD.name)
    }

    var selectedId by rememberSaveable {
        mutableStateOf(-1L)
    }

    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val selectedTransaction =
        state.transactions.firstOrNull {
            it.id == selectedId
        }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            financeViewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Page,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ) {
                Snackbar(
                    snackbarData = it,
                    containerColor = Navy,
                    contentColor = Color.White
                )
            }
        },
        bottomBar = {
            if (page == FinancePage.HOME.name) {
                FinanceBottomBar(
                    onHome = onNavigateHome,
                    onProduct = onNavigateProduct,
                    onShopping = onNavigateShopping,
                    onProfile = onNavigateProfile
                )
            }
        }
    ) { padding ->

        when (page) {

            FinancePage.HOME.name -> {
                FinanceHome(
                    state = state,
                    onAdd = {
                        formMode = FormMode.ADD.name
                        page = FinancePage.FORM.name
                    },
                    onOpeningBalance = {
                        formMode = FormMode.OPENING_BALANCE.name
                        page = FinancePage.FORM.name
                    },
                    onTransactionClick = {
                        selectedId = it.id
                        page = FinancePage.DETAIL.name
                    },
                    activityTab = activityTab,
                    onActivityTabSelected = onActivityTabSelected,
                    purchaseContent = purchaseContent,
                    usageContent = usageContent,
                    headerAction = headerAction,
                    modifier = Modifier.padding(padding)
                )
            }

            FinancePage.FORM.name -> {
                FinanceFormScreen(
                    mode = FormMode.valueOf(formMode),
                    transaction =
                        if (formMode == FormMode.EDIT.name)
                            selectedTransaction
                        else null,
                    openingBalance = state.openingBalance,
                    onBack = {
                        page =
                            if (formMode == FormMode.EDIT.name) {
                                FinancePage.DETAIL.name
                            } else {
                                FinancePage.HOME.name
                            }
                    },
                    onSave = {
                            title,
                            type,
                            category,
                            amount,
                            date,
                            note ->

                        if (
                            formMode == FormMode.EDIT.name &&
                            selectedTransaction != null
                        ) {
                            financeViewModel.updateTransaction(
                                selectedTransaction.copy(
                                    title = title,
                                    type = type,
                                    category = category,
                                    amount = amount,
                                    date = date,
                                    note = note
                                )
                            )
                            page = FinancePage.DETAIL.name
                        } else {
                            financeViewModel.addTransaction(
                                title = title,
                                type = type,
                                category = category,
                                amount = amount,
                                date = date,
                                note = note
                            )
                            page = FinancePage.HOME.name
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }

            FinancePage.DETAIL.name -> {
                if (selectedTransaction != null) {
                    FinanceDetailScreen(
                        transaction = selectedTransaction,
                        onBack = {
                            page = FinancePage.HOME.name
                        },
                        onEdit = {
                            formMode = FormMode.EDIT.name
                            page = FinancePage.FORM.name
                        },
                        onDelete = {
                            showDeleteDialog = true
                        },
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    page = FinancePage.HOME.name
                }
            }
        }
    }

    // KONFIRMASI HAPUS
    if (
        showDeleteDialog &&
        selectedTransaction != null
    ) {
        ModalBottomSheet(
            onDismissRequest = {
                showDeleteDialog = false
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Red,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Hapus data ini?",
                    color = Navy,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Data yang dihapus tidak dapat dikembalikan.",
                    color = Muted,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Tidak",
                            color = Blue
                        )
                    }

                    Button(
                        onClick = {
                            financeViewModel.deleteTransaction(
                                selectedTransaction.id
                            )
                            showDeleteDialog = false
                            page = FinancePage.HOME.name
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ya, Lanjutkan")
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun FinanceHome(
    state: FinanceUiState,
    onAdd: () -> Unit,
    onOpeningBalance: () -> Unit,
    onTransactionClick: (FinanceTransaction) -> Unit,
    activityTab: String,
    onActivityTabSelected: (String) -> Unit,
    purchaseContent: @Composable () -> Unit,
    usageContent: @Composable () -> Unit,
    headerAction: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "CATATAN HARIAN",
                color = Blue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Aktivitas",
                color = Navy,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Pantau pemakaian dan keuanganmu",
                color = Muted,
                fontSize = 12.sp
            )
        }

        headerAction()
        }

        // TAB AKTIVITAS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFFE8EDF5),
                    RoundedCornerShape(12.dp)
                )
                .padding(4.dp)
        ) {
            listOf(
                "Pemakaian",
                "Pembelian",
                "Keuangan"
            ).forEach { tab ->

                val isSelected = activityTab == tab

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) Color.White
                            else Color.Transparent
                        )
                        .clickable {
                            onActivityTabSelected(tab)
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Blue else Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // KONTEN TAB PEMBELIAN
        if (activityTab == "Pembelian") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                purchaseContent()
            }
            return
        }

        // KONTEN TAB PEMAKAIAN
        if (activityTab == "Pemakaian") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                usageContent()
            }
            return
        }

        // SALDO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Blue,
                    RoundedCornerShape(20.dp)
                )
                .padding(18.dp)
        ) {
            Text(
                "Saldo Saat Ini",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )

            Spacer(Modifier.height(7.dp))

            Text(
                formatRupiah(state.currentBalance),
                color = Color.White,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ringkasan keuanganmu",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = onAdd
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White
                    )

                    Text(
                        "Transaksi",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // PEMASUKAN DAN PENGELUARAN
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Pemasukan",
                amount = state.totalIncome,
                icon = Icons.Default.TrendingUp,
                tint = Green,
                background = GreenBg
            )

            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Pengeluaran",
                amount = state.totalExpense,
                icon = Icons.Default.TrendingDown,
                tint = Red,
                background = RedBg
            )
        }

        // HEADER RIWAYAT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Transaksi Terbaru",
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "${state.transactions.size} transaksi",
                color = Muted,
                fontSize = 11.sp
            )
        }

        // LIST TRANSAKSI
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Blue
                )
            }
        } else if (state.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Belum ada transaksi",
                    color = Muted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(
                    items = state.transactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onClick = {
                            onTransactionClick(transaction)
                        }
                    )

                    HorizontalDivider(
                        color = Border
                    )
                }
            }
        }

        // SALDO AWAL
        OutlinedButton(
            onClick = onOpeningBalance,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Blue
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Atur Saldo Awal",
                color = Blue
            )
        }

        // TAMBAH TRANSAKSI
        Button(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(bottom = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null
            )

            Spacer(Modifier.width(7.dp))

            Text("Tambah Transaksi")
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    title: String,
    amount: Long,
    icon: ImageVector,
    tint: Color,
    background: Color
) {
    Column(
        modifier = modifier
            .background(
                Color.White,
                RoundedCornerShape(16.dp)
            )
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    background,
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint
            )
        }

        Text(
            title,
            color = Muted,
            fontSize = 11.sp
        )

        Text(
            formatRupiah(amount),
            color = Navy,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TransactionRow(
    transaction: FinanceTransaction,
    onClick: () -> Unit
) {
    val isIncome =
        transaction.type == TransactionType.INCOME ||
                transaction.type == TransactionType.INITIAL_BALANCE

    val tint = if (isIncome) Green else Red
    val background = if (isIncome) GreenBg else RedBg

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable {
                onClick()
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    background,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isIncome)
                    Icons.Default.TrendingUp
                else
                    Icons.Default.TrendingDown,
                contentDescription = null,
                tint = tint
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                transaction.title,
                color = Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                "${transaction.category} • ${formatDisplayDate(transaction.date)}",
                color = Muted,
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        Text(
            (if (isIncome) "+" else "-") +
                    formatRupiah(transaction.amount),
            color = tint,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinanceFormScreen(
    mode: FormMode,
    transaction: FinanceTransaction?,
    openingBalance: Long,
    onBack: () -> Unit,
    onSave: (
        String,
        TransactionType,
        String,
        Long,
        String,
        String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOpening = mode == FormMode.OPENING_BALANCE
    val isEdit = mode == FormMode.EDIT

    var type by rememberSaveable(transaction?.id, mode) {
        mutableStateOf(
            transaction?.type
                ?: if (isOpening)
                    TransactionType.INITIAL_BALANCE
                else
                    TransactionType.INCOME
        )
    }

    var amount by rememberSaveable(transaction?.id, mode) {
        mutableStateOf(
            transaction?.amount?.toString()
                ?: if (isOpening && openingBalance > 0)
                    openingBalance.toString()
                else ""
        )
    }

    var title by rememberSaveable(transaction?.id, mode) {
        mutableStateOf(
            transaction?.title
                ?: if (isOpening) "Saldo Awal" else ""
        )
    }

    var category by rememberSaveable(transaction?.id, mode) {
        mutableStateOf(
            transaction?.category ?: "Gaji"
        )
    }

    var date by rememberSaveable(transaction?.id, mode) {
        mutableStateOf(
            transaction?.date ?: todayIsoDate()
        )
    }

    var note by rememberSaveable(transaction?.id, mode) {
        mutableStateOf(
            transaction?.note ?: ""
        )
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = isoDateToMillis(date)
    )

    val categories =
        if (type == TransactionType.INCOME) {
            listOf(
                "Gaji",
                "Bonus",
                "Penjualan",
                "Hadiah",
                "Lainnya"
            )
        } else {
            listOf(
                "Kebutuhan Rumah",
                "Makanan",
                "Transportasi",
                "Perawatan Diri",
                "Tagihan",
                "Hiburan",
                "Lainnya"
            )
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Navy
                )
            }

            Text(
                when {
                    isOpening -> "Atur Saldo Awal"
                    isEdit -> "Edit Transaksi"
                    else -> "Tambah Transaksi"
                },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.size(48.dp))
        }

        HorizontalDivider(color = Border)

        // PILIH PEMASUKAN / PENGELUARAN
        if (!isOpening) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TypeButton(
                    modifier = Modifier.weight(1f),
                    label = "Pemasukan",
                    selected = type == TransactionType.INCOME,
                    selectedColor = Green,
                    selectedBg = GreenBg,
                    icon = Icons.Default.TrendingUp
                ) {
                    type = TransactionType.INCOME
                    category = "Gaji"
                }

                TypeButton(
                    modifier = Modifier.weight(1f),
                    label = "Pengeluaran",
                    selected = type == TransactionType.EXPENSE,
                    selectedColor = Red,
                    selectedBg = RedBg,
                    icon = Icons.Default.TrendingDown
                ) {
                    type = TransactionType.EXPENSE
                    category = "Kebutuhan Rumah"
                }
            }
        } else {
            Text(
                "Masukkan jumlah saldo yang tersedia saat mulai menggunakan aplikasi.",
                color = Muted,
                fontSize = 12.sp
            )
        }

        // NOMINAL
        FormLabel("Jumlah", true)

        OutlinedTextField(
            value = formatNumberInput(amount),
            onValueChange = { input ->
                amount = input.filter {
                    it.isDigit()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            prefix = {
                Text(
                    "Rp ",
                    color = Navy,
                    fontWeight = FontWeight.Bold
                )
            },
            placeholder = {
                Text("0", color = Muted)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            shape = RoundedCornerShape(14.dp),
            colors = financeFieldColors()
        )

        // JUDUL
        FormLabel("Judul Transaksi", true)

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Contoh: Belanja bulanan")
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = financeFieldColors()
        )

        // KATEGORI
        FormLabel("Kategori")

        Box {
            OutlinedTextField(
                value = if (isOpening) "Saldo Awal" else category,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        categoryExpanded = true
                    },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            categoryExpanded = true
                        }
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = financeFieldColors()
            )

            DropdownMenu(
                expanded = categoryExpanded && !isOpening,
                onDismissRequest = {
                    categoryExpanded = false
                }
            ) {
                categories.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(it)
                        },
                        onClick = {
                            category = it
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // TANGGAL
        FormLabel("Tanggal", true)

        OutlinedTextField(
            value = formatDisplayDate(date),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDatePicker = true
                },
            trailingIcon = {
                IconButton(
                    onClick = {
                        showDatePicker = true
                    }
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Pilih tanggal",
                        tint = Blue
                    )
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = financeFieldColors()
        )

        // KALENDER
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = {
                    showDatePicker = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                date = millisToIsoDate(it)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text(
                            "Pilih",
                            color = Blue
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                        }
                    ) {
                        Text(
                            "Batal",
                            color = Muted
                        )
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = true,
                    title = {
                        Text(
                            "Pilih tanggal transaksi",
                            modifier = Modifier.padding(
                                start = 24.dp,
                                top = 16.dp
                            )
                        )
                    }
                )
            }
        }

        // CATATAN
        FormLabel("Catatan (Opsional)")

        OutlinedTextField(
            value = note,
            onValueChange = {
                note = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = {
                Text("Tambahkan catatan...")
            },
            shape = RoundedCornerShape(14.dp),
            colors = financeFieldColors()
        )

        // VALIDASI
        if (errorMessage != null) {
            Text(
                errorMessage!!,
                color = Red,
                fontSize = 12.sp
            )
        }

        // SIMPAN
        Button(
            onClick = {
                val nominal = amount.toLongOrNull()

                when {
                    title.isBlank() -> {
                        errorMessage = "Judul transaksi wajib diisi."
                    }

                    nominal == null || nominal <= 0L -> {
                        errorMessage = "Masukkan nominal yang benar."
                    }

                    else -> {
                        errorMessage = null

                        onSave(
                            title.trim(),
                            if (isOpening)
                                TransactionType.INITIAL_BALANCE
                            else
                                type,
                            if (isOpening)
                                "Saldo Awal"
                            else
                                category,
                            nominal,
                            date,
                            note.trim()
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                if (isEdit)
                    "Simpan Perubahan"
                else
                    "Simpan Data",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TypeButton(
    modifier: Modifier,
    label: String,
    selected: Boolean,
    selectedColor: Color,
    selectedBg: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (selected) selectedColor else Border
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor =
                if (selected) selectedBg else Color.White
        )
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) selectedColor else Muted
        )

        Spacer(Modifier.width(6.dp))

        Text(
            label,
            color = if (selected) selectedColor else Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FormLabel(
    label: String,
    required: Boolean = false
) {
    Row {
        Text(
            label,
            color = Navy,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )

        if (required) {
            Text(
                " *",
                color = Red
            )
        }
    }
}

@Composable
private fun financeFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Blue,
        unfocusedBorderColor = Border,
        focusedTextColor = Navy,
        unfocusedTextColor = Navy,
        cursorColor = Blue
    )

@Composable
private fun FinanceDetailScreen(
    transaction: FinanceTransaction,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome =
        transaction.type == TransactionType.INCOME ||
                transaction.type == TransactionType.INITIAL_BALANCE

    val tint = if (isIncome) Green else Red
    val bg = if (isIncome) GreenBg else RedBg

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Page)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Navy
                )
            }

            Text(
                "Detail Transaksi",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.size(48.dp))
        }

        // KARTU NOMINAL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(22.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .background(
                        bg,
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIncome)
                        Icons.Default.TrendingUp
                    else
                        Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                if (isIncome) "Pemasukan" else "Pengeluaran",
                color = Muted
            )

            Spacer(Modifier.height(8.dp))

            Text(
                formatRupiah(transaction.amount),
                color = Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            Spacer(Modifier.height(10.dp))

            Text(
                transaction.title,
                color = Navy,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        // INFORMASI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(20.dp)
                )
                .padding(18.dp)
        ) {
            Text(
                "Informasi Transaksi",
                color = Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(12.dp))

            DetailLine(
                "Jenis",
                when (transaction.type) {
                    TransactionType.INCOME -> "Pemasukan"
                    TransactionType.EXPENSE -> "Pengeluaran"
                    TransactionType.INITIAL_BALANCE -> "Saldo Awal"
                }
            )

            DetailLine(
                "Kategori",
                transaction.category
            )

            DetailLine(
                "Tanggal",
                formatDisplayDate(transaction.date)
            )

            DetailLine(
                "Jumlah",
                formatRupiah(transaction.amount)
            )

            DetailLine(
                "Catatan",
                transaction.note.ifBlank { "-" },
                false
            )
        }

        // TOMBOL EDIT DAN HAPUS
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = Blue
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    "Edit Transaksi",
                    color = Blue
                )
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Red
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    "Hapus",
                    color = Red
                )
            }
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = Muted,
            fontSize = 12.sp
        )

        Text(
            value,
            color = Navy,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }

    if (showDivider) {
        HorizontalDivider(color = Border)
    }
}


@Composable
private fun FinanceBottomBar(
    onHome: () -> Unit,
    onProduct: () -> Unit,
    onShopping: () -> Unit,
    onProfile: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        // BERANDA
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

        // PRODUK
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

        // BELANJA
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

        // AKTIVITAS
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Icon(Icons.Default.BarChart, null)
            },
            label = {
                Text("Aktivitas", fontSize = 10.sp)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Blue,
                selectedTextColor = Blue,
                indicatorColor = Color(0xFFEAF1FF)
            )
        )

        // PROFIL
        NavigationBarItem(
            selected = false,
            onClick = onProfile,
            icon = {
                Icon(Icons.Default.Person, null)
            },
            label = {
                Text("Profil", fontSize = 10.sp)
            }
        )
    }
}

// ======================================
// FORMAT RUPIAH
// ======================================

private fun formatRupiah(value: Long): String {
    return "Rp " +
            NumberFormat
                .getNumberInstance(Locale("in", "ID"))
                .format(value)
}

// ======================================
// FORMAT INPUT ANGKA
// ======================================

private fun formatNumberInput(value: String): String {
    if (value.isBlank()) return ""

    val number = value.toLongOrNull() ?: return value

    return NumberFormat
        .getNumberInstance(Locale("in", "ID"))
        .format(number)
}

// ======================================
// TANGGAL SEKARANG
// ======================================

private fun todayIsoDate(): String {
    val formatter = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    )

    formatter.timeZone = TimeZone.getTimeZone("UTC")

    return formatter.format(Date())
}

// ======================================
// KONVERSI TANGGAL KE MILLISECONDS
// ======================================

private fun isoDateToMillis(value: String): Long {
    return try {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        formatter.isLenient = false
        formatter.timeZone = TimeZone.getTimeZone("UTC")

        formatter.parse(value)?.time
            ?: System.currentTimeMillis()

    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

// ======================================
// MILLISECONDS KE TANGGAL
// ======================================

private fun millisToIsoDate(value: Long): String {
    val formatter = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    )

    formatter.timeZone = TimeZone.getTimeZone("UTC")

    return formatter.format(Date(value))
}

// ======================================
// FORMAT TANGGAL TAMPILAN
// ======================================

private fun formatDisplayDate(value: String): String {
    return try {
        val input = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        input.timeZone = TimeZone.getTimeZone("UTC")

        val date = input.parse(value) ?: return value

        val output = SimpleDateFormat(
            "d MMMM yyyy",
            Locale("id", "ID")
        )

        output.timeZone = TimeZone.getTimeZone("UTC")

        output.format(date)

    } catch (e: Exception) {
        value
    }
}
