package com.example.pencatatankeuangan.ui.purchase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.pencatatankeuangan.model.Pembelian
import com.example.pencatatankeuangan.model.formatTanggal
import com.example.pencatatankeuangan.model.formatRupiah

@Composable
fun DetailPembelianScreen(
    pembelian: Pembelian?,
    isLoading: Boolean,
    aksiError: String?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor = BgScreen
    ) { inner ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(12.dp))

            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBox(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    description = "Kembali",
                    onClick = onBack
                )

                Text(
                    text = "Detail Pembelian",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                IconBox(
                    icon = Icons.Outlined.Edit,
                    description = "Edit",
                    onClick = onEdit
                )
            }

            Spacer(Modifier.height(18.dp))

            // KONDISI DATA KOSONG
            if (pembelian == null) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = BluePrimary
                        )
                    } else {
                        Text(
                            text = "Data tidak ditemukan",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }

            } else {

                // KARTU INFORMASI PRODUK
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        ProductImage(
                            url = pembelian.imageUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = pembelian.namaProduk,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )

                                if (!pembelian.merek.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        text = pembelian.merek,
                                        fontSize = 13.sp,
                                        color = TextGray
                                    )
                                }
                            }

                            if (!pembelian.kategori.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BlueLight)
                                        .padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        )
                                ) {
                                    Text(
                                        text = pembelian.kategori,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BluePrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // DETAIL PEMBELIAN
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 14.dp
                        )
                    ) {

                        Text(
                            text = "Informasi Pembelian",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Spacer(Modifier.height(8.dp))

                        InfoRow(
                            label = "Tanggal pembelian",
                            value = formatTanggal(pembelian.tanggal)
                        )

                        HorizontalDivider(color = BorderColor)

                        InfoRow(
                            label = "Jumlah",
                            value = "${pembelian.jumlah} item"
                        )

                        HorizontalDivider(color = BorderColor)

                        InfoRow(
                            label = "Harga per item",
                            value = formatRupiah(pembelian.hargaPerItem)
                        )

                        HorizontalDivider(color = BorderColor)

                        InfoRow(
                            label = "Total harga",
                            value = formatRupiah(pembelian.totalHarga)
                        )

                        HorizontalDivider(color = BorderColor)

                        InfoRow(
                            label = "Toko",
                            value = pembelian.toko
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ERROR AKSI
                if (aksiError != null) {
                    Text(
                        text = aksiError,
                        color = RedDanger,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(10.dp))
                }

                // TOMBOL EDIT DAN HAPUS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BluePrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = BluePrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Edit Data",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            showDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, RedDanger),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = RedDanger
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = "Hapus",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // DIALOG KONFIRMASI HAPUS
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(
                    text = "Hapus pembelian?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Data yang dihapus tidak bisa dikembalikan."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "Hapus",
                        color = RedDanger,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextGray
        )

        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}
