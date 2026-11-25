package com.example.pencatatankeuangan.ui.usage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pencatatankeuangan.model.ProductUsageStats
import com.example.pencatatankeuangan.model.UsageEntry
import com.example.pencatatankeuangan.model.usageDisplayDate

// ======================================================
// HALAMAN 2 : DETAIL PEMAKAIAN PRODUK
// ======================================================
@Composable
fun DetailPemakaianScreen(
    entry: UsageEntry,
    stats: ProductUsageStats?,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSelectLog: (Long) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDelete by remember { mutableStateOf(false) }
    val log = entry.log

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UsagePage)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        UsageTopBar(title = "Detail Pemakaian", onBack = onBack)

        // ---------- INFORMASI PRODUK + AKUMULASI ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProductThumb(size = 56.dp)

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = entry.productName,
                        color = UsageNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (stats != null)
                            "Dipakai sejak ${usageDisplayDate(stats.product.startDate)}"
                        else "Data produk aktif tidak ditemukan",
                        color = UsageMuted,
                        fontSize = 11.sp
                    )
                }
            }

            if (stats != null) {
                val unit = stats.product.unit

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Jumlah awal",
                        value = "${stats.product.initialQuantity} $unit"
                    )

                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Total terpakai",
                        value = "${stats.used} $unit",
                        valueColor = UsageRed
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Estimasi sisa",
                        value = "${stats.remaining} $unit",
                        valueColor = UsageBlue
                    )

                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Frekuensi pakai",
                        value = "${stats.frequency} kali"
                    )
                }

                Spacer(Modifier.height(16.dp))

                // BAR SISA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(UsageBlueSoft)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stats.remainingFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(UsageBlue)
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "${(stats.remainingFraction * 100).toInt()}% tersisa" +
                            " (hanya menghitung pemakaian bersatuan $unit)",
                    color = UsageMuted,
                    fontSize = 10.sp
                )
            }
        }

        // ---------- CATATAN YANG DIPILIH ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Catatan Pemakaian",
                color = UsageNavy,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            DetailLine("Jumlah digunakan", log.quantityLabel.removePrefix("−"))
            DetailLine("Tanggal", usageDisplayDate(log.date))
            DetailLine("Waktu", log.time)
            DetailLine("Catatan", log.note.ifBlank { "-" }, showDivider = false)

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(text = errorMessage, color = UsageRed, fontSize = 12.sp)
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = UsageBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Edit", color = UsageBlue, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { showDelete = true },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = UsageRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Hapus", color = UsageRed, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ---------- RIWAYAT PEMAKAIAN PRODUK INI ----------
        if (stats != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "Riwayat Pemakaian",
                    color = UsageNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                stats.logs.forEach { item ->
                    val selected = item.id == log.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selected) UsageBlueSoft else Color.White)
                            .clickable { onSelectLog(item.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${usageDisplayDate(item.date)} • ${item.time}",
                                color = UsageNavy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = item.note.ifBlank { "Tanpa catatan" },
                                color = UsageMuted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = item.quantityLabel,
                            color = UsageRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showDelete) {
        UsageConfirmDialog(
            title = "Hapus catatan ini?",
            message = "Catatan pemakaian yang dihapus tidak dapat dikembalikan " +
                    "dan estimasi sisa produk akan dihitung ulang.",
            confirmText = "Ya, Hapus",
            onConfirm = {
                showDelete = false
                onDelete()
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun StatTile(
    modifier: Modifier,
    label: String,
    value: String,
    valueColor: Color = UsageNavy
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(UsagePage)
            .padding(12.dp)
    ) {
        Text(text = label, color = UsageMuted, fontSize = 10.sp)

        Spacer(Modifier.height(4.dp))

        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
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
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = UsageMuted, fontSize = 12.sp)

        Text(
            text = value,
            color = UsageNavy,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }

    if (showDivider) {
        HorizontalDivider(color = UsageBorder)
    }
}
