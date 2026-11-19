package com.example.pencatatankeuangan.ui.usage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pencatatankeuangan.model.UsageEntry
import com.example.pencatatankeuangan.model.usageDisplayDate
import com.example.pencatatankeuangan.viewmodel.UsageUiState

private val GrayBar = Color(0xFFE8EDF5)

// ======================================================
// HALAMAN 1 : DAFTAR PEMAKAIAN HARIAN
// (isi tab "Pemakaian", memakai LazyColumn)
// ======================================================
@Composable
fun PemakaianContent(
    state: UsageUiState,
    onItemClick: (UsageEntry) -> Unit,
    onTambah: () -> Unit,
    onKelolaProduk: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = state.visibleEntries

    Box(modifier = modifier.fillMaxSize()) {

        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // BAR TANGGAL + KARTU RINGKASAN + JUDUL
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GrayBar)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = usageDisplayDate(state.selectedDate, withDay = true),
                            color = UsageMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    SummaryCard(
                        total = state.totalQuantity,
                        count = state.activityCount
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (state.isToday) "Riwayat Hari Ini"
                        else "Riwayat ${usageDisplayDate(state.selectedDate)}",
                        color = UsageNavy,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))
                }
            }

            // LOADING / ERROR / KOSONG
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = UsageBlue)
                    }
                }
            } else if (state.errorMessage != null && state.logs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = UsageRed,
                            fontSize = 12.sp
                        )

                        TextButton(onClick = onRetry) {
                            Text("Coba lagi", color = UsageBlue)
                        }
                    }
                }
            } else if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada pemakaian pada tanggal ini",
                            color = UsageMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // DAFTAR AKTIVITAS (waktu terbaru di atas)
            itemsIndexed(
                items = entries,
                key = { _, entry -> entry.log.id }
            ) { index, entry ->
                TimelineRow(
                    entry = entry,
                    isLast = index == entries.lastIndex,
                    onClick = { onItemClick(entry) }
                )
            }

            // KELOLA PRODUK AKTIF
            item {
                Column {
                    Spacer(Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = onKelolaProduk,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFD3F5)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = UsageBlue,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Kelola Produk Aktif",
                            color = UsageBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // ruang agar tombol Tambah tidak menutupi item terakhir
                    Spacer(Modifier.height(88.dp))
                }
            }
        }

        // TOMBOL TAMBAH (MENGAMBANG)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = UsageBlue,
                    spotColor = UsageBlue
                )
                .clip(RoundedCornerShape(16.dp))
                .background(UsageBlue)
                .clickable(onClick = onTambah)
                .padding(horizontal = 20.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Tambah",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ======================================================
// KARTU BIRU RINGKASAN
// ======================================================
@Composable
private fun SummaryCard(total: Int, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(UsageBlue)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.FlashOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Total digunakan",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = total.toString(),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = "unit*",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "*Ditampilkan sebagai jumlah aktivitas lintas unit",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 8.5.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.18f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Aktivitas",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 9.sp
            )
        }
    }
}

// ======================================================
// BARIS TIMELINE (waktu, foto, nama, jumlah + satuan)
// ======================================================
@Composable
private fun TimelineRow(
    entry: UsageEntry,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
    ) {

        // WAKTU
        Text(
            text = entry.log.time,
            color = UsageMuted,
            fontSize = 10.sp,
            modifier = Modifier
                .width(42.dp)
                .padding(top = 15.dp)
        )

        // TITIK + GARIS
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(UsageBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(UsageBlue)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(UsageBlue.copy(alpha = 0.15f))
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // FOTO + NAMA + JUMLAH
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            ProductThumb(size = 44.dp)

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = entry.productName,
                    color = UsageNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = entry.log.note.ifBlank { "Tanpa catatan" },
                    color = UsageMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier.height(44.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = entry.log.quantityLabel,
                    color = UsageRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
