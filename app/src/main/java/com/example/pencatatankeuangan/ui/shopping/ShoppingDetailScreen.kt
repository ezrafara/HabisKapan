package com.example.pencatatankeuangan.ui.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pencatatankeuangan.model.ShoppingItem
import com.example.pencatatankeuangan.viewmodel.ShoppingViewModel

private val DetailBlue = Color(0xFF2962ED)
private val DetailNavy = Color(0xFF101B33)
private val DetailMuted = Color(0xFF8190A9)
private val DetailBg = Color(0xFFF6F8FC)

@Composable
fun ShoppingDetailScreen(
    item: ShoppingItem,
    onBack: () -> Unit,
    onEdit: (ShoppingItem) -> Unit,
    onDelete: () -> Unit,
    shoppingViewModel: ShoppingViewModel = viewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val state by shoppingViewModel.uiState.collectAsState()
    val currentItem = state.items.find { it.id == item.id } ?: item

    Scaffold(containerColor = DetailBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = DetailNavy)
                }
                Text(
                    "Detail Item Belanja",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DetailNavy
                )
            }

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .background(Color(0xFFEAF1FF), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when {
                                currentItem.productName.contains("gigi", true) -> "🪥"
                                currentItem.productName.contains("sabun", true) -> "🧴"
                                currentItem.productName.contains("detergen", true) -> "🧼"
                                currentItem.productName.contains("sampo", true) -> "🧴"
                                else -> "🛍️"
                            },
                            fontSize = 46.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        currentItem.productName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DetailNavy
                    )
                    Spacer(Modifier.height(8.dp))
                    StatusLabel(currentItem.isPurchased)
                }
            }

            Spacer(Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    DetailRow("Jumlah", "${currentItem.quantity} item", Icons.Default.Inventory)
                    HorizontalDivider(color = Color(0xFFEDF0F5))
                    DetailRow("Prioritas", currentItem.priority, Icons.Default.Flag)
                    HorizontalDivider(color = Color(0xFFEDF0F5))
                    DetailRow(
                        "Status",
                        if (currentItem.isPurchased) "Sudah Dibeli" else "Belum Dibeli",
                        Icons.Default.ShoppingCart
                    )
                    HorizontalDivider(color = Color(0xFFEDF0F5))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notes, null, tint = DetailMuted)
                            Spacer(Modifier.width(9.dp))
                            Text("Catatan", color = DetailMuted, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(
                            currentItem.note.ifBlank { "Tidak ada catatan" },
                            fontWeight = FontWeight.Medium,
                            color = DetailNavy,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { shoppingViewModel.togglePurchased(currentItem.id) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentItem.isPurchased) Color(0xFFEDF1F6) else DetailBlue,
                    contentColor = if (currentItem.isPurchased) DetailNavy else Color.White
                )
            ) {
                Icon(
                    if (currentItem.isPurchased) Icons.Default.Refresh else Icons.Default.CheckCircle,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (currentItem.isPurchased) "Batalkan Status Dibeli" else "Tandai Sudah Dibeli",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { onEdit(currentItem) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Edit")
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Hapus")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Item?") },
            text = {
                Text("Apakah kamu yakin ingin menghapus ${currentItem.productName} dari daftar belanja?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        shoppingViewModel.deleteItem(currentItem.id)
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun StatusLabel(isPurchased: Boolean) {
    Surface(
        color = if (isPurchased) Color(0xFFE5F8ED) else Color(0xFFFFF3DD),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            if (isPurchased) "Sudah Dibeli" else "Belum Dibeli",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isPurchased) Color(0xFF12A65B) else Color(0xFFD28A16)
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = DetailMuted)
        Spacer(Modifier.width(10.dp))
        Text(label, color = DetailMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = DetailNavy, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
