package com.example.pencatatankeuangan.ui.usage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pencatatankeuangan.model.ActiveProduct
import com.example.pencatatankeuangan.model.usageDisplayDate

// ======================================================
// KELOLA PRODUK AKTIF (daftar active_products)
// ======================================================
@Composable
fun KelolaProdukAktifScreen(
    activeProducts: List<ActiveProduct>,
    errorMessage: String?,
    onBack: () -> Unit,
    onTambah: () -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var deleteTarget by remember { mutableStateOf<ActiveProduct?>(null) }

    UsageFormScaffold(
        title = "Kelola Produk Aktif",
        onBack = onBack,
        modifier = modifier
    ) {

        if (activeProducts.isEmpty()) {
            Text(
                text = "Belum ada produk aktif.\nTambahkan produk yang sedang kamu pakai.",
                color = UsageMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        }

        activeProducts.forEach { product ->
            val shape = RoundedCornerShape(14.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(shape)
                    .background(Color.White)
                    .border(1.dp, UsageBorder, shape)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductThumb(size = 44.dp)

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.productName,
                        color = UsageNavy,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${product.initialQuantity} ${product.unit} • " +
                                "sejak ${usageDisplayDate(product.startDate)}",
                        color = UsageMuted,
                        fontSize = 10.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus produk aktif",
                    tint = UsageRed,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { deleteTarget = product }
                        .padding(8.dp)
                )
            }
        }

        if (errorMessage != null) {
            Text(text = errorMessage, color = UsageRed, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(6.dp))

        UsagePrimaryButton(
            text = "Tambah Produk Aktif",
            icon = Icons.Default.Add,
            onClick = onTambah
        )
    }

    deleteTarget?.let { target ->
        UsageConfirmDialog(
            title = "Hapus produk aktif?",
            message = "Semua catatan pemakaian ${target.productName} " +
                    "juga akan ikut terhapus.",
            confirmText = "Ya, Hapus",
            onConfirm = {
                onDelete(target.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}
