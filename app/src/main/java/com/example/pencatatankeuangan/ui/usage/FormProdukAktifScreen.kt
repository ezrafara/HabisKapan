package com.example.pencatatankeuangan.ui.usage

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pencatatankeuangan.model.ProductItem
import com.example.pencatatankeuangan.model.usageDisplayDate
import com.example.pencatatankeuangan.model.usageToday

// ======================================================
// FORM "TAMBAH PRODUK AKTIF"  (tabel active_products)
// Dipakai untuk mendaftarkan kemasan produk yang sedang dipakai.
// ======================================================
@Composable
fun FormProdukAktifScreen(
    products: List<ProductItem>,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSimpan: (
        product: ProductItem,
        quantity: Int,
        unit: String,
        startDate: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProductId by rememberSaveable {
        mutableStateOf(products.firstOrNull()?.id ?: -1L)
    }

    val selected = products.firstOrNull { it.id == selectedProductId }

    var quantity by rememberSaveable { mutableStateOf("1") }

    var unit by rememberSaveable {
        mutableStateOf(selected?.unit ?: "ml")
    }

    // kosong = pakai hari ini
    var startDate by rememberSaveable { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val unitOptions = (UsageUnits + listOfNotNull(selected?.unit)).distinct()

    UsageFormScaffold(
        title = "Tambah Produk Aktif",
        onBack = onBack,
        modifier = modifier
    ) {

        FormLabel("Pilih produk")
        Spacer(Modifier.height(8.dp))

        DropdownField(
            value = selected?.name ?: "Belum ada produk",
            options = products.map { it.name },
            onSelect = { index ->
                val picked = products[index]
                selectedProductId = picked.id
                unit = picked.unit
                localError = null
            }
        )

        Spacer(Modifier.height(16.dp))

        FormLabel("Kuantitas awal", required = true)
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
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        FormLabel("Satuan")
        Spacer(Modifier.height(8.dp))

        DropdownField(
            value = unit,
            options = unitOptions,
            onSelect = { index -> unit = unitOptions[index] }
        )

        Spacer(Modifier.height(16.dp))

        FormLabel("Tanggal mulai")
        Spacer(Modifier.height(8.dp))

        FieldBox(onClick = { showDatePicker = true }) {
            Text(
                text = if (startDate.isBlank()) "" else usageDisplayDate(startDate),
                color = UsageNavy,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(16.dp))

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

        UsagePrimaryButton(
            text = if (isSaving) "Menyimpan..." else "Simpan Data",
            icon = Icons.Default.Check,
            enabled = !isSaving,
            onClick = {
                val qty = quantity.toIntOrNull()

                when {
                    selected == null -> {
                        localError = "Pilih produk terlebih dahulu."
                    }

                    qty == null || qty <= 0 -> {
                        localError = "Masukkan kuantitas yang benar."
                    }

                    else -> {
                        localError = null
                        onSimpan(
                            selected,
                            qty,
                            unit,
                            startDate.ifBlank { usageToday() }
                        )
                    }
                }
            }
        )
    }

    if (showDatePicker) {
        UsageDatePickerDialog(
            initialDate = startDate.ifBlank { usageToday() },
            title = "Pilih tanggal mulai",
            onDismiss = { showDatePicker = false },
            onPick = { startDate = it }
        )
    }
}
