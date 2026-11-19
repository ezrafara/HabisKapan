package com.example.pencatatankeuangan.ui.purchase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager

import com.example.pencatatankeuangan.model.Pembelian
import com.example.pencatatankeuangan.model.PembelianRequest
import com.example.pencatatankeuangan.model.todayIso

@Composable
fun FormPembelianScreen(
    awal: Pembelian?,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSimpan: (PembelianRequest) -> Unit
) {
    val isEdit = awal != null
    val focusManager = LocalFocusManager.current

    var namaProduk by rememberSaveable {
        mutableStateOf(awal?.namaProduk ?: "")
    }
    var merek by rememberSaveable {
        mutableStateOf(awal?.merek ?: "")
    }
    var kategori by rememberSaveable {
        mutableStateOf(awal?.kategori ?: "")
    }
    var toko by rememberSaveable {
        mutableStateOf(awal?.toko ?: "")
    }
    var tanggal by rememberSaveable {
        mutableStateOf(awal?.tanggal ?: todayIso())
    }
    var jumlah by rememberSaveable {
        mutableStateOf(awal?.jumlah?.toString() ?: "1")
    }
    var harga by rememberSaveable {
        mutableStateOf(awal?.hargaPerItem?.toString() ?: "")
    }
    var imageUrl by rememberSaveable {
        mutableStateOf(awal?.imageUrl ?: "")
    }
    var submitted by rememberSaveable {
        mutableStateOf(false)
    }

    val tanggalValid = Regex("""\d{4}-\d{2}-\d{2}""").matches(tanggal)
    val jumlahInt = jumlah.toIntOrNull()
    val hargaLong = harga.toLongOrNull()

    val namaError = submitted && namaProduk.isBlank()
    val tokoError = submitted && toko.isBlank()
    val tanggalError = submitted && !tanggalValid
    val jumlahError = submitted && (jumlahInt == null || jumlahInt < 1)
    val hargaError = submitted && (hargaLong == null || hargaLong < 0)

    Scaffold(
        containerColor = BgScreen,
        // Tombol simpan selalu terlihat di bagian bawah layar.
        bottomBar = {
            Surface(
                color = BgScreen,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        submitted = true
                        focusManager.clearFocus()

                        val valid =
                            namaProduk.isNotBlank() &&
                                    toko.isNotBlank() &&
                                    tanggalValid &&
                                    jumlahInt != null && jumlahInt >= 1 &&
                                    hargaLong != null && hargaLong >= 0

                        if (valid) {
                            onSimpan(
                                PembelianRequest(
                                    namaProduk = namaProduk.trim(),
                                    merek = merek.trim().ifBlank { null },
                                    kategori = kategori.trim().ifBlank { null },
                                    toko = toko.trim(),
                                    tanggal = tanggal,
                                    jumlah = jumlahInt!!,
                                    hargaPerItem = hargaLong!!,
                                    imageUrl = imageUrl.trim().ifBlank { null }
                                )
                            )
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = if (isEdit) "Simpan Perubahan" else "Simpan Pembelian",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
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
                    text = if (isEdit) "Edit Pembelian" else "Tambah Pembelian",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(Modifier.size(44.dp))
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Informasi Produk",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(Modifier.height(14.dp))

            FormField(
                label = "Nama Produk *",
                value = namaProduk,
                onValueChange = { namaProduk = it },
                placeholder = "Contoh: Minyak Goreng",
                isError = namaError,
                errorText = "Nama produk wajib diisi"
            )

            Spacer(Modifier.height(12.dp))

            FormField(
                label = "Merek",
                value = merek,
                onValueChange = { merek = it },
                placeholder = "Contoh: Bimoli"
            )

            Spacer(Modifier.height(12.dp))

            FormField(
                label = "Kategori",
                value = kategori,
                onValueChange = { kategori = it },
                placeholder = "Contoh: Dapur"
            )

            Spacer(Modifier.height(12.dp))

            FormField(
                label = "Toko *",
                value = toko,
                onValueChange = { toko = it },
                placeholder = "Contoh: Super Indo",
                isError = tokoError,
                errorText = "Nama toko wajib diisi"
            )

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Detail Pembelian",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(Modifier.height(14.dp))

            FormField(
                label = "Tanggal Pembelian *",
                value = tanggal,
                onValueChange = { tanggal = it },
                placeholder = "yyyy-MM-dd",
                isError = tanggalError,
                errorText = "Gunakan format yyyy-MM-dd"
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    FormField(
                        label = "Jumlah *",
                        value = jumlah,
                        onValueChange = { jumlah = it },
                        placeholder = "1",
                        isError = jumlahError,
                        errorText = "Minimal 1",
                        keyboardType = KeyboardType.Number
                    )
                }

                Box(Modifier.weight(1.4f)) {
                    FormField(
                        label = "Harga per Item *",
                        value = harga,
                        onValueChange = { harga = it },
                        placeholder = "21400",
                        isError = hargaError,
                        errorText = "Masukkan angka",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            FormField(
                label = "URL Gambar (Opsional)",
                value = imageUrl,
                onValueChange = { imageUrl = it },
                placeholder = "https://...",
                keyboardType = KeyboardType.Uri
            )

            Spacer(Modifier.height(18.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = RedDanger,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
            }


            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorText: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )

        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
            supportingText = if (isError) {
                { Text(errorText) }
            } else {
                null
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextGray,
                    fontSize = 13.sp
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                unfocusedBorderColor = BorderColor,
                focusedBorderColor = BluePrimary,
                errorBorderColor = RedDanger
            )
        )
    }
}
