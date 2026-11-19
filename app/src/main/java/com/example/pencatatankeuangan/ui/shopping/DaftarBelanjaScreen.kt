package com.example.pencatatankeuangan.ui.shopping

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pencatatankeuangan.model.ShoppingItem
import com.example.pencatatankeuangan.viewmodel.ShoppingViewModel

private val Blue = Color(0xFF2962ED)
private val Navy = Color(0xFF101B33)
private val Muted = Color(0xFF8190A9)
private val PageBg = Color(0xFFF6F8FC)
private val BorderColor = Color(0xFFDCE5F1)
private val Green = Color(0xFF12A65B)
private val Red = Color(0xFFE52935)

@Composable
fun DaftarBelanjaScreen(
    modifier: Modifier = Modifier,
    shoppingViewModel: ShoppingViewModel = viewModel(),
    onNavigateHome: () -> Unit = {},
    onNavigateProduct: () -> Unit = {},
    onNavigateActivity: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onAddItem: () -> Unit = {},
    onItemClick: (ShoppingItem) -> Unit = {}
) {
    val state by shoppingViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            shoppingViewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PageBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ShoppingBottomBar(
                onHome = onNavigateHome,
                onProduct = onNavigateProduct,
                onActivity = onNavigateActivity,
                onProfile = onNavigateProfile
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddItem,
                containerColor = Blue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah item")
                Spacer(Modifier.width(6.dp))
                Text("Tambah", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            ShoppingHeader(
                totalCount = state.totalCount,
                purchasedCount = state.purchasedCount,
                progress = state.progress
            )

            Spacer(Modifier.height(22.dp))

            ShoppingFilter(
                selectedFilter = state.selectedFilter,
                onFilterSelected = shoppingViewModel::setFilter
            )

            Spacer(Modifier.height(16.dp))

            if (state.filteredItems.isEmpty()) {
                EmptyShoppingList(
                    selectedFilter = state.selectedFilter,
                    onAddItem = onAddItem
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 2.dp, bottom = 90.dp)
                ) {
                    items(
                        items = state.filteredItems,
                        key = { it.id }
                    ) { item ->
                        ShoppingItemCard(
                            item = item,
                            onCheckedChange = {
                                shoppingViewModel.togglePurchased(item.id)
                            },
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingHeader(
    totalCount: Int,
    purchasedCount: Int,
    progress: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "RENCANA BELANJA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Blue,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "Daftar Belanja",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$purchasedCount dari $totalCount item sudah dibeli",
                fontSize = 12.sp,
                color = Muted
            )
        }

        Box(
            modifier = Modifier.size(58.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxSize(),
                color = Blue,
                trackColor = Color(0xFFE5EBF5),
                strokeWidth = 5.dp
            )
            Text(
                "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Blue
            )
        }
    }
}

@Composable
private fun ShoppingFilter(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("Semua", "Belum Dibeli", "Sudah Dibeli")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEAF0F8), RoundedCornerShape(13.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        filters.forEach { filter ->
            val selected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selected) Color.White else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 3.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    filter,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Blue else Muted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItem,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isPurchased,
                onCheckedChange = { onCheckedChange() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Blue,
                    uncheckedColor = Color(0xFFB8C5D8)
                )
            )

            Spacer(Modifier.width(7.dp))
            ProductEmoji(item.productName)
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isPurchased) Muted else Navy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "${item.quantity} item • ${item.note.ifBlank { "Belum ada catatan" }}",
                    fontSize = 10.sp,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                PriorityLabel(item.priority)
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Lihat detail",
                tint = Color(0xFF9AA9BD)
            )
        }
    }
}

@Composable
private fun ProductEmoji(productName: String) {
    val emoji = when {
        productName.contains("pasta", true) || productName.contains("gigi", true) -> "🪥"
        productName.contains("sabun", true) -> "🧴"
        productName.contains("minyak", true) -> "🫒"
        productName.contains("sampo", true) -> "🧴"
        productName.contains("detergen", true) -> "🧼"
        else -> "🛍️"
    }

    Box(
        modifier = Modifier
            .size(43.dp)
            .background(Color(0xFFEAF1FF), RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 22.sp)
    }
}

@Composable
private fun PriorityLabel(priority: String) {
    val backgroundColor = when (priority) {
        "Tinggi" -> Color(0xFFFFE8E8)
        "Sedang" -> Color(0xFFFFF3DD)
        else -> Color(0xFFE5F8ED)
    }
    val textColor = when (priority) {
        "Tinggi" -> Red
        "Sedang" -> Color(0xFFD28A16)
        else -> Green
    }

    Surface(color = backgroundColor, shape = RoundedCornerShape(5.dp)) {
        Text(
            priority,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun EmptyShoppingList(
    selectedFilter: String,
    onAddItem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 55.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = Color(0xFFB7C5D8),
            modifier = Modifier.size(55.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (selectedFilter == "Semua") "Belum ada item belanja"
            else "Tidak ada item pada kategori ini",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Navy
        )
        Spacer(Modifier.height(5.dp))
        Text(
            if (selectedFilter == "Semua") "Tambahkan kebutuhan belanja kamu."
            else "Coba pilih filter lainnya.",
            fontSize = 12.sp,
            color = Muted
        )
        if (selectedFilter == "Semua") {
            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = onAddItem) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text("Tambah Barang")
            }
        }
    }
}

@Composable
private fun ShoppingBottomBar(
    onHome: () -> Unit,
    onProduct: () -> Unit,
    onActivity: () -> Unit,
    onProfile: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = false,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Beranda", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onProduct,
            icon = { Icon(Icons.Default.Inventory2, null) },
            label = { Text("Produk", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.ShoppingCart, null) },
            label = { Text("Belanja", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Blue,
                selectedTextColor = Blue,
                indicatorColor = Color(0xFFEAF1FF)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onActivity,
            icon = { Icon(Icons.Default.BarChart, null) },
            label = { Text("Aktivitas", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfile,
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Profil", fontSize = 10.sp) }
        )
    }
}
