package com.example.pencatatankeuangan.ui.purchase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import java.util.Locale

import com.example.pencatatankeuangan.model.Pembelian
import com.example.pencatatankeuangan.model.bulanTahun
import com.example.pencatatankeuangan.model.formatRupiah
import com.example.pencatatankeuangan.model.formatTanggal
import com.example.pencatatankeuangan.model.todayIso
import com.example.pencatatankeuangan.ui.theme.AppDialog

/**
 * HALAMAN 1 - LIST RIWAYAT PEMBELIAN
 *
 * Layar ini dipasang di dalam slot `purchaseContent` milik PencatatanKeuanganScreen,
 * jadi HEADER ("Aktivitas"), TAB, dan BOTTOM BAR sudah disediakan layar tersebut.
 * Di sini hanya: kolom cari, chip bulan, daftar (LazyColumn), dan tombol "+ Tambah".
 */
@Composable
fun RiwayatListScreen(
    daftar: List<Pembelian>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onItemClick: (Long) -> Unit,
    onTambah: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    // Filter bulan lewat tombol kalender:
    //   null      = otomatis (bulan pembelian terbaru)
    //   "ALL"     = semua bulan
    //   "yyyy-MM" = bulan yang dipilih pengguna
    var pilihan by rememberSaveable { mutableStateOf<String?>(null) }
    var showMonthDialog by rememberSaveable { mutableStateOf(false) }

    val bulanTerbaru = remember(daftar) {
        daftar.maxOfOrNull { it.tanggal }?.take(7)
    }

    val bulanAktif: String? = when (pilihan) {
        null -> bulanTerbaru
        "ALL" -> null
        else -> pilihan
    }

    val filtered = remember(daftar, query, bulanAktif) {
        daftar.filter { p ->
            (bulanAktif == null || p.tanggal.startsWith(bulanAktif)) &&
                    (query.isBlank() ||
                            p.namaProduk.contains(query, ignoreCase = true) ||
                            p.toko.contains(query, ignoreCase = true))
        }
    }

    val bulanLabel = when {
        pilihan == "ALL" -> "Semua bulan"
        bulanAktif != null -> bulanTahun("$bulanAktif-01")
        else -> bulanTahun(todayIso())
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ---------- SEARCH ----------
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = {
                    Text(
                        "Cari produk atau toko...",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Cari",
                        tint = TextGray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = BorderColor,
                    focusedBorderColor = BluePrimary
                )
            )

            Spacer(Modifier.height(14.dp))

            // ---------- CHIP BULAN + JUMLAH PEMBELIAN ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val chipShape = RoundedCornerShape(12.dp)

                Row(
                    modifier = Modifier
                        .clip(chipShape)
                        .background(Color.White)
                        .border(1.dp, BorderColor, chipShape)
                        .clickable { showMonthDialog = true }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = bulanLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Text(
                    text = "${filtered.size} pembelian",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            Spacer(Modifier.height(12.dp))

            // ---------- ISI ----------
            when {
                isLoading && daftar.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                }

                errorMessage != null && daftar.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage,
                            color = RedDanger,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BluePrimary
                            )
                        ) {
                            Text("Coba lagi")
                        }
                    }
                }

                filtered.isEmpty() -> {
                    InfoKosong(
                        if (daftar.isNotEmpty()) "Tidak ada pembelian yang cocok"
                        else "Belum ada pembelian"
                    )
                }

                else -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        // LazyColumn untuk daftar transaksi pembelian
                        LazyColumn {
                            itemsIndexed(
                                items = filtered,
                                key = { _, item -> item.id }
                            ) { index, item ->

                                PembelianRow(item) {
                                    onItemClick(item.id)
                                }

                                if (index < filtered.lastIndex) {
                                    HorizontalDivider(color = BorderColor)
                                }
                            }

                            // ruang agar baris terakhir tidak tertutup tombol Tambah
                            item {
                                Spacer(
                                    Modifier.height(
                                        if (filtered.size > 4) 72.dp else 0.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------- DIALOG PILIH BULAN (tombol kalender) ----------
        if (showMonthDialog) {
            PilihBulanDialog(
                bulanSekarang = bulanAktif,
                onPilihBulan = {
                    pilihan = it
                    showMonthDialog = false
                },
                onSemuaBulan = {
                    pilihan = "ALL"
                    showMonthDialog = false
                },
                onDismiss = { showMonthDialog = false }
            )
        }

        // ---------- TOMBOL + TAMBAH ----------
        ExtendedFloatingActionButton(
            onClick = onTambah,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp),
            containerColor = BluePrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Tambah pembelian"
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Tambah",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PembelianRow(
    item: Pembelian,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductImage(
            url = item.imageUrl,
            modifier = Modifier.size(70.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.namaProduk,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${formatTanggal(item.tanggal)} • ${item.toko}",
                fontSize = 11.sp,
                color = TextGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${item.jumlah} item × ${formatRupiah(item.hargaPerItem)}",
                fontSize = 11.sp,
                color = TextGray
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = formatRupiah(item.totalHarga),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}

@Composable
private fun InfoKosong(teks: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = teks,
            color = TextGray,
            fontSize = 14.sp
        )
    }
}

/** Dialog pilih bulan: pilih tahun dengan panah, lalu klik salah satu bulan. */
@Composable
private fun PilihBulanDialog(
    bulanSekarang: String?,          // format "yyyy-MM" atau null
    onPilihBulan: (String) -> Unit,
    onSemuaBulan: () -> Unit,
    onDismiss: () -> Unit
) {
    val tahunAwal = bulanSekarang?.take(4)?.toIntOrNull()
        ?: todayIso().take(4).toInt()

    var tahun by remember { mutableIntStateOf(tahunAwal) }

    val namaBulan = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    AppDialog(
        title = "Pilih bulan",
        confirmText = "Semua bulan",
        onConfirm = onSemuaBulan,
        onDismiss = onDismiss,
        content = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { tahun-- }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Tahun sebelumnya"
                        )
                    }

                    Text(
                        text = "$tahun",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    IconButton(onClick = { tahun++ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Tahun berikutnya"
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                for (baris in 0 until 4) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (kolom in 0 until 3) {
                            val nomor = baris * 3 + kolom + 1
                            val kunci = String.format(Locale.US, "%04d-%02d", tahun, nomor)
                            val terpilih = kunci == bulanSekarang

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (terpilih) BluePrimary else BlueLight)
                                    .clickable { onPilihBulan(kunci) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = namaBulan[nomor - 1],
                                    color = if (terpilih) Color.White else BluePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}