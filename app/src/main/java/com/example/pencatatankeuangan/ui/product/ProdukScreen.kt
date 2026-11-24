package com.example.pencatatankeuangan.ui.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.pencatatankeuangan.model.ProductItem
import com.example.pencatatankeuangan.viewmodel.ProductViewModel
import com.example.pencatatankeuangan.viewmodel.ProductUiState

private val Blue = Color(0xFF2962ED)
private val Navy = Color(0xFF101B33)
private val Muted = Color(0xFF8190A9)
private val PageBg = Color(0xFFF6F8FC)
private val BorderColor = Color(0xFFDCE5F1)
private val Green = Color(0xFF12A65B)
private val GreenBg = Color(0xFFE5F8ED)
private val Red = Color(0xFFE52935)
private val RedBg = Color(0xFFFFE8E8)

private enum class ProductPage {
    LIST, DETAIL, ADD, EDIT
}

// ==========================================
// SCREEN UTAMA
// ==========================================

@Composable
fun ProdukScreen(
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel = viewModel(),
    onNavigateHome: () -> Unit = {},
    onNavigateShopping: () -> Unit = {},
    onNavigateActivity: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onAddToShopping: (ProductItem) -> Unit = {}
) {
    val state by productViewModel.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    var currentPage by rememberSaveable {
        mutableStateOf(ProductPage.LIST.name)
    }

    var selectedId by rememberSaveable {
        mutableStateOf(-1L)
    }

    var showDelete by remember {
        mutableStateOf(false)
    }

    val selectedProduct = state.products.firstOrNull {
        it.id == selectedId
    }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHost.showSnackbar(message)
            productViewModel.clearMessage()
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
                    contentColor = Color.White
                )
            }
        },
        bottomBar = {
            if (currentPage == ProductPage.LIST.name) {
                ProductBottomBar(
                    onHome = onNavigateHome,
                    onShopping = onNavigateShopping,
                    onActivity = onNavigateActivity,
                    onProfile = onNavigateProfile
                )
            }
        }
    ) { padding ->

        when (currentPage) {

            ProductPage.LIST.name -> {
                ProductList(
                    state = state,
                    onSearch = productViewModel::setSearchQuery,
                    onCategory = productViewModel::setCategory,
                    onProductClick = { product ->
                        selectedId = product.id
                        currentPage = ProductPage.DETAIL.name
                    },
                    onAdd = {
                        currentPage = ProductPage.ADD.name
                    },
                    modifier = Modifier.padding(padding)
                )
            }

            ProductPage.DETAIL.name -> {
                if (selectedProduct != null) {
                    ProductDetail(
                        product = selectedProduct,
                        onBack = {
                            currentPage = ProductPage.LIST.name
                        },
                        onEdit = {
                            currentPage = ProductPage.EDIT.name
                        },
                        onDelete = {
                            showDelete = true
                        },
                        onAddToShopping = {
                            onAddToShopping(selectedProduct)
                        },
                        modifier = Modifier.padding(padding)
                    )
                }
            }

            ProductPage.ADD.name -> {
                ProductForm(
                    title = "Tambah Produk",
                    product = null,
                    onBack = {
                        currentPage = ProductPage.LIST.name
                    },
                    isSaving = state.isSaving,
                    onSave = { name, brand, category,
                               unit, stock, note, imageUri ->

                        productViewModel.addProduct(
                            name, brand, category,
                            unit, stock, note,
                            imageUri = imageUri
                        ) { success ->
                            if (success) {
                                currentPage = ProductPage.LIST.name
                            }
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }

            ProductPage.EDIT.name -> {
                if (selectedProduct != null) {
                    ProductForm(
                        title = "Edit Produk",
                        product = selectedProduct,
                        onBack = {
                            currentPage = ProductPage.DETAIL.name
                        },
                        isSaving = state.isSaving,
                        onSave = { name, brand, category,
                                   unit, stock, note, imageUri ->

                            productViewModel.updateProduct(
                                selectedProduct.copy(
                                    name = name,
                                    brand = brand,
                                    category = category,
                                    unit = unit,
                                    stock = stock,
                                    note = note
                                ),
                                newImageUri = imageUri
                            ) { success ->
                                if (success) {
                                    currentPage = ProductPage.DETAIL.name
                                }
                            }
                        },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }

    if (showDelete && selectedProduct != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Red
                )
            },
            title = { Text("Hapus produk ini?") },
            text = {
                Text("Data yang dihapus tidak dapat dikembalikan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.deleteProduct(
                            selectedProduct.id
                        )
                        showDelete = false
                        currentPage = ProductPage.LIST.name
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red
                    )
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDelete = false }
                ) {
                    Text("Batal")
                }
            },
            containerColor = Color.White
        )
    }
}

// ==========================================
// DAFTAR PRODUK
// ==========================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductList(
    state: ProductUiState,
    onSearch: (String) -> Unit,
    onCategory: (String) -> Unit,
    onProductClick: (ProductItem) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Semua", "Perawatan Diri", "Kebersihan", "Dapur"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.padding(top = 12.dp)) {
            Text(
                "KATALOG RUMAH",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = Blue
            )
            Text(
                "Produk Saya",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
            Text(
                "${state.products.size} produk terpantau",
                fontSize = 12.sp,
                color = Muted
            )
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Cari nama atau merek produk...",
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = Muted
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = productInputColors()
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = state.selectedCategory == category,
                    onClick = { onCategory(category) },
                    label = {
                        Text(category, fontSize = 11.sp)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Blue,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Blue
                )
            } else if (state.filteredProducts.isEmpty()) {
                Text(
                    "Produk tidak ditemukan",
                    modifier = Modifier.align(Alignment.Center),
                    color = Muted
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 78.dp)
                ) {
                    items(
                        state.filteredProducts,
                        key = { it.id }
                    ) { product ->
                        ProductCard(
                            product = product,
                            onClick = {
                                onProductClick(product)
                            }
                        )
                    }
                }
            }

            Button(
                onClick = onAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 12.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(Modifier.width(6.dp))
                Text("Tambah")
            }
        }
    }
}

// ==========================================
// KARTU PRODUK
// ==========================================

@Composable
private fun ProductCard(
    product: ProductItem,
    onClick: () -> Unit
) {
    val isLow = product.stock <= 20

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            ProductImage(
                imageUrl = product.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
            )

            Box(
                modifier = Modifier
                    .background(
                        if (isLow) RedBg else GreenBg,
                        RoundedCornerShape(7.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    product.availabilityLabel,
                    fontSize = 10.sp,
                    color = if (isLow) Red else Green
                )
            }

            Text(
                product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Navy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                "${product.brand} • ${product.category}",
                fontSize = 10.sp,
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                "${product.stock} ${product.unit} tersedia",
                fontSize = 10.sp,
                color = Muted
            )
        }
    }
}

// Menampilkan foto produk; placeholder jika belum ada / gagal dimuat
@Composable
private fun ProductImage(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    if (imageUrl.isNullOrBlank()) {
        ProductImagePlaceholder(modifier)
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Foto produk",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(12.dp))
        )
    }
}

@Composable
private fun ProductImagePlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            Color(0xFFEAF0F8),
            RoundedCornerShape(12.dp)
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Inventory2,
            contentDescription = "Foto produk",
            tint = Color(0xFF94A6BF),
            modifier = Modifier.size(40.dp)
        )
    }
}

// ==========================================
// DETAIL PRODUK
// ==========================================

@Composable
private fun ProductDetail(
    product: ProductItem,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProductTopBar(
            title = "Detail Produk",
            onBack = onBack,
            onEdit = onEdit
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(Modifier.padding(12.dp)) {
                ProductImage(
                    imageUrl = product.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    product.name,
                    color = Navy,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    product.brand,
                    color = Muted,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    product.category,
                    color = Blue,
                    fontSize = 11.sp
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    "Informasi Produk",
                    fontSize = 16.sp,
                    color = Navy
                )
                Spacer(Modifier.height(10.dp))

                ProductDetailLine("Merek", product.brand)
                ProductDetailLine("Kategori", product.category)
                ProductDetailLine("Satuan", product.unit)
                ProductDetailLine(
                    "Stok saat ini",
                    "${product.stock} ${product.unit}"
                )
                ProductDetailLine(
                    "Catatan",
                    product.note.ifBlank { "-" },
                    divider = false
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                border = BorderStroke(1.dp, Color(0xFFBCD2FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = Blue
                )
                Spacer(Modifier.width(6.dp))
                Text("Edit Data", color = Blue)
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.height(50.dp),
                border = BorderStroke(1.dp, Color(0xFFFFBFC3)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Red
                )
                Text("Hapus", color = Red)
            }
        }

        Button(
            onClick = onAddToShopping,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text("Tambah ke Daftar Belanja")
        }
    }
}

@Composable
private fun ProductDetailLine(
    label: String,
    value: String,
    divider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Muted, fontSize = 12.sp)

        Text(
            value,
            color = Navy,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    if (divider) {
        HorizontalDivider(color = BorderColor)
    }
}

// ==========================================
// FORM TAMBAH DAN EDIT PRODUK
// ==========================================

@Composable
private fun ProductForm(
    title: String,
    product: ProductItem?,
    onBack: () -> Unit,
    isSaving: Boolean,
    onSave: (String, String, String, String, Int, String, Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    var imageUri by rememberSaveable(product?.id) {
        mutableStateOf<Uri?>(null)
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) imageUri = uri
    }

    var name by rememberSaveable(product?.id) {
        mutableStateOf(product?.name ?: "")
    }

    var brand by rememberSaveable(product?.id) {
        mutableStateOf(product?.brand ?: "")
    }

    var category by rememberSaveable(product?.id) {
        mutableStateOf(product?.category ?: "Perawatan Diri")
    }

    var unit by rememberSaveable(product?.id) {
        mutableStateOf(product?.unit ?: "ml")
    }

    var stock by rememberSaveable(product?.id) {
        mutableStateOf(product?.stock?.toString() ?: "0")
    }

    var note by rememberSaveable(product?.id) {
        mutableStateOf(product?.note ?: "")
    }

    var error by remember {
        mutableStateOf("")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        ProductTopBar(
            title = title,
            onBack = onBack
        )

        // PILIH FOTO DARI GALERI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(PageBg)
                .clickable(enabled = !isSaving) {
                    photoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val preview: Any? = imageUri ?: product?.imageUrl

            if (preview != null) {
                AsyncImage(
                    model = preview,
                    contentDescription = "Foto produk",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Text(
                    "Ketuk untuk mengganti foto",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0x99101B33))
                        .padding(6.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Blue,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Unggah foto produk",
                        color = Blue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "JPG, PNG, atau WEBP, maks. 5 MB",
                        color = Muted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        ProductLabel("Nama produk", true)

        ProductInput(
            value = name,
            onValueChange = { name = it },
            placeholder = "Contoh: Sabun Mandi Lifebuoy"
        )

        ProductLabel("Merek", true)

        ProductInput(
            value = brand,
            onValueChange = { brand = it },
            placeholder = "Contoh: Lifebuoy"
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(Modifier.weight(1f)) {
                ProductLabel("Kategori")

                ProductDropdown(
                    value = category,
                    options = listOf(
                        "Perawatan Diri",
                        "Kebersihan",
                        "Dapur"
                    ),
                    onSelect = { category = it }
                )
            }

            Column(Modifier.weight(1f)) {
                ProductLabel("Satuan")

                ProductDropdown(
                    value = unit,
                    options = listOf(
                        "ml", "gram", "pcs", "roll"
                    ),
                    onSelect = { unit = it }
                )
            }
        }

        ProductLabel("Stok awal", true)

        OutlinedTextField(
            value = stock,
            onValueChange = { input ->
                stock = input.filter { it.isDigit() }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            shape = RoundedCornerShape(12.dp),
            colors = productInputColors()
        )

        ProductLabel("Catatan (opsional)")

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp),
            placeholder = {
                Text("Tambahkan informasi lain...")
            },
            shape = RoundedCornerShape(12.dp),
            colors = productInputColors()
        )

        if (error.isNotBlank()) {
            Text(
                error,
                color = Red,
                fontSize = 12.sp
            )
        }

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
                tint = Blue
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Pastikan data sudah benar agar ringkasan dan perkiraan stok tetap akurat.",
                fontSize = 11.sp,
                color = Muted
            )
        }

        Button(
            onClick = {
                val parsedStock = stock.toIntOrNull()

                when {
                    name.isBlank() ->
                        error = "Nama produk wajib diisi."

                    brand.isBlank() ->
                        error = "Merek wajib diisi."

                    parsedStock == null ->
                        error = "Stok harus berupa angka."

                    else -> {
                        error = ""

                        onSave(
                            name.trim(),
                            brand.trim(),
                            category,
                            unit,
                            parsedStock,
                            note.trim(),
                            imageUri
                        )
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Menyimpan...", fontWeight = FontWeight.Bold)
            } else {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (product == null)
                        "Simpan Data"
                    else
                        "Simpan Perubahan",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(14.dp))
    }
}

// ==========================================
// KOMPONEN FORM
// ==========================================

@Composable
private fun ProductLabel(
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
private fun ProductInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                color = Muted,
                fontSize = 13.sp
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = productInputColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDropdown(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = productInputColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun productInputColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Blue,
        unfocusedBorderColor = BorderColor,
        focusedTextColor = Navy,
        unfocusedTextColor = Navy
    )

// ==========================================
// HEADER
// ==========================================

@Composable
private fun ProductTopBar(
    title: String,
    onBack: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
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
            title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            color = Navy
        )

        if (onEdit != null) {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Navy
                )
            }
        } else {
            Spacer(Modifier.width(48.dp))
        }
    }
}

// ==========================================
// NAVBAR PRODUK AKTIF
// ==========================================

@Composable
private fun ProductBottomBar(
    onHome: () -> Unit,
    onShopping: () -> Unit,
    onActivity: () -> Unit,
    onProfile: () -> Unit
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
            selected = true,
            onClick = {},
            icon = {
                Icon(Icons.Default.Inventory2, null)
            },
            label = {
                Text("Produk", fontSize = 10.sp)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Blue,
                selectedTextColor = Blue,
                indicatorColor = Color(0xFFEAF1FF)
            )
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