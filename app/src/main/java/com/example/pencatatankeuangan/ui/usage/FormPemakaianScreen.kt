package com.example.pencatatankeuangan.ui.usage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pencatatankeuangan.model.ActiveProduct
import com.example.pencatatankeuangan.model.UsageLog
import com.example.pencatatankeuangan.model.usageDisplayDate
import com.example.pencatatankeuangan.model.usageNowTime
import com.example.pencatatankeuangan.model.usageToday

internal val UsageUnits = listOf("ml", "gram", "liter", "kg", "roll", "pcs", "lembar")

// ======================================================
// HALAMAN 3 : FORM TAMBAH / EDIT PEMAKAIAN
// (form yang sama dipakai untuk tambah dan edit)
// ======================================================
@Composable
fun FormPemakaianScreen(
    activeProducts: List<ActiveProduct>,
    initial: UsageLog?,                 // null = tambah, ada = edit
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onTambahProdukAktif: () -> Unit,
    onSimpan: (
        activeProductId: Long,
        quantity: Int,
        unit: String,
        date: String,
        time: String,
        note: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEdit = initial != null

    var selectedId by rememberSaveable(initial?.id) {
        mutableStateOf(
            initial?.activeProductId
                ?: activeProducts.firstOrNull()?.id
                ?: -1L
        )
    }

    val selected = activeProducts.firstOrNull { it.id == selectedId }

    var quantity by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.quantity?.toString() ?: "")
    }

    var unit by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.unit ?: selected?.unit ?: "ml")
    }

    var date by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.date ?: usageToday())
    }

    var time by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.time ?: usageNowTime())
    }

    var note by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.note ?: "")
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val unitOptions = (UsageUnits + listOfNotNull(selected?.unit)).distinct()

    UsageFormScaffold(
        title = if (isEdit) "Edit Pemakaian" else "Tambah Pemakaian",
        onBack = onBack,
        modifier = modifier
    ) {

        // PILIH PRODUK (dari produk aktif / katalog)
        FormLabel("Pilih produk", required = true)
        Spacer(Modifier.height(8.dp))

        if (activeProducts.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(UsageBlueSoft)
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Belum ada produk aktif.",
                    color = UsageMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )

                TextButton(onClick = onTambahProdukAktif) {
                    Text(
                        "Tambah produk aktif",
                        color = UsageBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            DropdownField(
                value = selected?.productName ?: "Pilih produk",
                options = activeProducts.map { it.productName },
                onSelect = { index ->
                    val picked = activeProducts[index]
                    selectedId = picked.id
                    unit = picked.unit
                    localError = null
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // JUMLAH DIGUNAKAN
        FormLabel("Jumlah digunakan", required = true)
        Spacer(Modifier.height(8.dp))

        FieldBox {
            BasicTextField(
                value = quantity,
                onValueChange = { input ->
                    quantity = input.filter { it.isDigit() }.take(6)
                },
                singleLine = true,
                textStyle = TextStyle(color = UsageNavy, fontSize = 13.sp),
                cursorBrush = SolidColor(UsageBlue),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (quantity.isEmpty()) {
                        Text("0", color = UsageMuted, fontSize = 13.sp)
                    }
                    inner()
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // SATUAN
        FormLabel("Satuan")
        Spacer(Modifier.height(8.dp))

        DropdownField(
            value = unit,
            options = unitOptions,
            onSelect = { index -> unit = unitOptions[index] }
        )

        Spacer(Modifier.height(16.dp))

        // TANGGAL + WAKTU
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            Column(modifier = Modifier.weight(1f)) {
                FormLabel("Tanggal", required = true)
                Spacer(Modifier.height(8.dp))

                FieldBox(onClick = { showDatePicker = true }) {
                    Text(
                        text = usageDisplayDate(date),
                        color = UsageNavy,
                        fontSize = 13.sp
                    )
                }
            }

            Column(modifier = Modifier.width(120.dp)) {
                FormLabel("Waktu", required = true)
                Spacer(Modifier.height(8.dp))

                FieldBox(onClick = { showTimePicker = true }) {
                    Text(text = time, color = UsageNavy, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // CATATAN
        FormLabel("Catatan (opsional)")
        Spacer(Modifier.height(8.dp))

        FieldBox(
            height = 84.dp,
            contentAlignment = Alignment.TopStart
        ) {
            BasicTextField(
                value = note,
                onValueChange = { note = it.take(120) },
                textStyle = TextStyle(color = UsageNavy, fontSize = 13.sp),
                cursorBrush = SolidColor(UsageBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                decorationBox = { inner ->
                    if (note.isEmpty()) {
                        Text(
                            "Contoh: Mandi pagi",
                            color = UsageMuted,
                            fontSize = 13.sp
                        )
                    }
                    inner()
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // INFO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(UsageBlueSoft)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = UsageBlue,
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Pastikan data sudah benar agar ringkasan dan perkiraan stok tetap akurat.",
                color = UsageMuted,
                fontSize = 9.sp,
                lineHeight = 13.sp
            )
        }

        val shownError = localError ?: errorMessage

        if (shownError != null) {
            Spacer(Modifier.height(10.dp))
            Text(text = shownError, color = UsageRed, fontSize = 12.sp)
        }

        Spacer(Modifier.height(16.dp))

        // SIMPAN
        UsagePrimaryButton(
            text = when {
                isSaving -> "Menyimpan..."
                isEdit -> "Simpan Perubahan"
                else -> "Simpan Data"
            },
            icon = Icons.Default.Check,
            enabled = !isSaving,
            onClick = {
                val qty = quantity.toIntOrNull()

                when {
                    selected == null -> {
                        localError = "Pilih produk aktif terlebih dahulu."
                    }

                    qty == null || qty <= 0 -> {
                        localError = "Masukkan jumlah yang benar."
                    }

                    else -> {
                        localError = null
                        onSimpan(
                            selected.id,
                            qty,
                            unit,
                            date,
                            time,
                            note.trim()
                        )
                    }
                }
            }
        )
    }

    if (showDatePicker) {
        UsageDatePickerDialog(
            initialDate = date,
            title = "Pilih tanggal pemakaian",
            onDismiss = { showDatePicker = false },
            onPick = { date = it }
        )
    }

    if (showTimePicker) {
        UsageTimePickerDialog(
            initialTime = time,
            onDismiss = { showTimePicker = false },
            onPick = { time = it }
        )
    }
}
