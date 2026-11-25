package com.example.pencatatankeuangan.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pencatatankeuangan.model.FinanceTransaction
import com.example.pencatatankeuangan.model.ShoppingItem
import com.example.pencatatankeuangan.model.UsageLog
import com.example.pencatatankeuangan.model.TransactionType
import com.example.pencatatankeuangan.model.ProductItem
import com.example.pencatatankeuangan.viewmodel.ProductUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Blue = Color(0xFF2962ED)
private val Navy = Color(0xFF101B33)
private val Muted = Color(0xFF8190A9)
private val Page = Color(0xFFF6F8FC)
private val Line = Color(0xFFDCE5F1)

@Composable
fun HomeScreen(
    productState: ProductUiState,
    transactions: List<FinanceTransaction>,
    onAddProduct: () -> Unit,
    onUsage: () -> Unit,
    onShopping: () -> Unit,
    onPurchase: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenProfile: () -> Unit,
    userName: String = "",
    usageLogs: List<UsageLog> = emptyList(),
    shoppingItems: List<ShoppingItem> = emptyList()
) {
    val firstName = userName.trim().substringBefore(' ')
        .replaceFirstChar { it.uppercase() }
    val initial = firstName.firstOrNull()?.toString() ?: ""

    val today = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        .format(Date()).uppercase(Locale("id", "ID"))
    val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val usageTodayCount = usageLogs.count { it.date == todayDate }
    val unpurchased = shoppingItems.count { !it.isPurchased }
    val expensesThisMonth = transactions.filter {
        it.type == TransactionType.EXPENSE && it.date.startsWith(month)
    }.sumOf { it.amount }
    val latest = transactions.sortedByDescending { it.date }.take(3)
    val lowStock = productState.products.filter { it.stock <= 25 }.sortedBy { it.stock }.take(5)
    val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        .apply { maximumFractionDigits = 0 }

    Scaffold(
        containerColor = Page,

        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(Icons.Default.Home, null)
                    },
                    label = {
                        Text("Beranda", fontSize = 10.sp)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Blue,
                        selectedTextColor = Blue,
                        indicatorColor = Color(0xFFEAF1FF)
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onOpenProducts,
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
                    onClick = onOpenActivity,
                    icon = {
                        Icon(Icons.Default.BarChart, null)
                    },
                    label = {
                        Text("Aktivitas", fontSize = 10.sp)
                    }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onOpenProfile,
                    icon = {
                        Icon(Icons.Default.Person, null)
                    },
                    label = {
                        Text("Profil", fontSize = 10.sp)
                    }
                )
            }
        }

    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(today, color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(7.dp))
                        Text(if (firstName.isBlank()) "Halo!" else "Halo, $firstName!", color = Navy, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("Yuk, cek kebutuhanmu hari ini.", color = Muted, fontSize = 12.sp)
                    }
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Blue), contentAlignment = Alignment.Center) {
                        Text(initial, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Blue)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("RINGKASAN HARI INI", color = Color(0xFFDCE7FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Text("Semua kebutuhanmu,\nterkendali.", color = Color.White, fontSize = 19.sp, lineHeight = 25.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("${productState.products.size} produk terdaftar dalam kebutuhanmu.", color = Color(0xFFE0E9FF), fontSize = 11.sp)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("Total Produk", productState.products.size.toString(), "produk terdaftar", Icons.Default.Inventory2, Modifier.weight(1f))
                    SummaryCard("Pemakaian Hari Ini", "$usageTodayCount", if (usageTodayCount == 0) "belum ada pemakaian tercatat" else "aktivitas tercatat hari ini", Icons.Default.Bolt, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("Belum Dibeli", "$unpurchased", if (unpurchased == 0) "semua kebutuhan sudah dibeli" else "barang di daftar belanja", Icons.Default.ShoppingCart, Modifier.weight(1f))
                    SummaryCard("Pengeluaran", currency.format(expensesThisMonth), "bulan ini", Icons.Default.AccountBalanceWallet, Modifier.weight(1f), compact = true)
                }
            }
            item { SectionTitle("Aksi Cepat") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickAction("Tambah Produk", Icons.Default.Inventory2, onAddProduct, Modifier.weight(1f))
                        QuickAction("Catat Pemakaian", Icons.Default.Bolt, onUsage, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickAction("Tambah Belanja", Icons.Default.ShoppingCart, onShopping, Modifier.weight(1f))
                        QuickAction("Catat Pembelian", Icons.Default.ShoppingBag, onPurchase, Modifier.weight(1f))
                    }
                }
            }
            item { SectionTitle("Perlu Diperhatikan", "Lihat Semua", onOpenProducts) }
            if (productState.isLoading) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth(), color = Blue) }
            } else if (lowStock.isEmpty()) {
                item { EmptyCard("Belum ada produk dengan stok menipis.") }
            } else {
                items(lowStock, key = { it.id }) { product -> LowStockRow(product) }
            }
            item { SectionTitle("Aktivitas Terbaru", "Lihat Semua", onOpenActivity) }
            if (latest.isEmpty()) {
                item { EmptyCard("Belum ada aktivitas yang tercatat.") }
            } else {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(16.dp)) {
                        Column {
                            latest.forEachIndexed { index, transaction ->
                                Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF4FF)), contentAlignment = Alignment.Center) {
                                        Icon(if (transaction.type == TransactionType.EXPENSE) Icons.Default.TrendingDown else Icons.Default.TrendingUp, null, tint = Blue)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(transaction.title, color = Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${transaction.category} • ${transaction.date}", color = Muted, fontSize = 10.sp)
                                    }
                                    Text(currency.format(transaction.amount), color = if (transaction.type == TransactionType.EXPENSE) Color(0xFFE53935) else Color(0xFF159B59), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                if (index != latest.lastIndex) HorizontalDivider(color = Line)
                            }
                        }
                    }
                }
            }
            item { SectionTitle("Prioritas Belanja", "Lihat Produk", onOpenProducts) }
            if (lowStock.isEmpty()) {
                item { EmptyCard("Produk dengan stok rendah akan muncul di sini.") }
            } else {
                items(lowStock, key = { "priority-${it.id}" }) { product ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(72.dp).height(42.dp).clip(RoundedCornerShape(9.dp)).background(Color(0xFFEEF4FF)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Inventory2, null, tint = Blue)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(product.name, color = Navy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("Sisa ${product.stock} ${product.unit}", color = Muted, fontSize = 10.sp)
                            }
                            Surface(color = if (product.stock <= 12) Color(0xFFFFE5E5) else Color(0xFFFFF2CC), shape = RoundedCornerShape(20.dp)) {
                                Text(if (product.stock <= 12) "Tinggi" else "Periksa", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = if (product.stock <= 12) Color(0xFFC62828) else Color(0xFF946200), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun SummaryCard(title: String, value: String, caption: String, icon: ImageVector, modifier: Modifier = Modifier, compact: Boolean = false) {
    Card(modifier = modifier.height(112.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(17.dp)) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), color = Color(0xFF526581), fontSize = 9.sp)
                Icon(icon, null, tint = Blue, modifier = Modifier.size(17.dp))
            }
            Text(value, color = Navy, fontSize = if (compact) 14.sp else 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(caption, color = Muted, fontSize = 8.sp, maxLines = 2)
        }
    }
}

@Composable private fun QuickAction(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(62.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(15.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFEEF4FF)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Blue, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(8.dp))
            Text(title, Modifier.weight(1f), color = Navy, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFB8C6D9), modifier = Modifier.size(17.dp))
        }
    }
}

@Composable private fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        if (action != null && onAction != null) Text(action, Modifier.clickable(onClick = onAction), color = Blue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun LowStockRow(product: ProductItem) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFEEF4FF)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Inventory2, null, tint = Blue, modifier = Modifier.size(25.dp)) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, color = Navy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Tersisa ${product.stock} ${product.unit}", color = Muted, fontSize = 10.sp)
            }
            Surface(color = Color(0xFFFFF2CC), shape = RoundedCornerShape(20.dp)) { Text("Mulai menipis", Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = Color(0xFF946200), fontSize = 8.sp) }
        }
    }
}

@Composable private fun EmptyCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(14.dp)) {
        Text(message, Modifier.fillMaxWidth().padding(16.dp), color = Muted, fontSize = 11.sp)
    }
}
