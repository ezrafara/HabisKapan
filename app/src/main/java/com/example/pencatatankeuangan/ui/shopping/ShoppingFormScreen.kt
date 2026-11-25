package com.example.pencatatankeuangan.ui.shopping

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pencatatankeuangan.model.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingFormScreen(
    item: ShoppingItem? = null,
    onBack: () -> Unit,
    onSave: (String, Int, String, String) -> Unit
) {
    var productName by remember(item?.id) { mutableStateOf(item?.productName ?: "") }
    var quantity by remember(item?.id) { mutableStateOf(item?.quantity?.toString() ?: "1") }
    var priority by remember(item?.id) { mutableStateOf(item?.priority ?: "Sedang") }
    var note by remember(item?.id) { mutableStateOf(item?.note ?: "") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Kembali")
        }

        Text(
            if (item == null) "Tambah Barang" else "Edit Barang",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text("Nama Barang", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = productName,
            onValueChange = {
                productName = it
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Contoh: Pasta Gigi") },
            singleLine = true,
            isError = errorMessage.isNotBlank() && productName.isBlank()
        )

        Text("Jumlah", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = quantity,
            onValueChange = {
                quantity = it.filter(Char::isDigit).take(5)
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Masukkan jumlah barang") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = errorMessage.contains("Jumlah")
        )

        Text("Prioritas", fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Rendah", "Sedang", "Tinggi").forEach { option ->
                FilterChip(
                    selected = priority == option,
                    onClick = { priority = option },
                    label = { Text(option) }
                )
            }
        }

        Text("Catatan (Opsional)", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            placeholder = { Text("Tambahkan catatan barang...") }
        )

        if (errorMessage.isNotBlank()) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val parsedQuantity = quantity.toIntOrNull()
                when {
                    productName.isBlank() ->
                        errorMessage = "Nama barang wajib diisi."
                    parsedQuantity == null || parsedQuantity < 1 ->
                        errorMessage = "Jumlah minimal 1."
                    else -> {
                        onSave(
                            productName.trim(),
                            parsedQuantity,
                            priority,
                            note.trim()
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2962ED),
                contentColor = Color.White
            )
        ) {
            Text(
                if (item == null) "Simpan Barang" else "Simpan Perubahan",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
