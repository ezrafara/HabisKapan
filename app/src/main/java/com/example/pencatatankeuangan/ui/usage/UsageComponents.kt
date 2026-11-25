package com.example.pencatatankeuangan.ui.usage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.ArrowBack
import com.example.pencatatankeuangan.model.usageIsoToMillis
import com.example.pencatatankeuangan.model.usageMillisToIso
import java.util.Locale
import com.example.pencatatankeuangan.ui.theme.AppDialog
import androidx.compose.material.icons.filled.Delete

// ======================================================
// WARNA
// ======================================================
internal val UsageBlue = Color(0xFF2459E6)
internal val UsageNavy = Color(0xFF101B33)
internal val UsageMuted = Color(0xFF71819B)
internal val UsagePage = Color(0xFFF6F8FC)
internal val UsageBorder = Color(0xFFDCE5F1)
internal val UsageBlueSoft = Color(0xFFEAF1FF)
internal val UsageRed = Color(0xFFE72F36)

// ======================================================
// TOMBOL IKON KOTAK (kalender di header, panah kembali)
// ======================================================
@Composable
fun UsageIconBox(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, UsageBorder, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = UsageNavy,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ======================================================
// BAR ATAS (panah kembali + judul)
// ======================================================
@Composable
internal fun UsageTopBar(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            UsageIconBox(
                icon = Icons.Default.ArrowBack,
                description = "Kembali",
                onClick = onBack
            )
        }

        Text(
            text = title,
            color = UsageNavy,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// Kerangka halaman form: bagian putih + sisa abu-abu (seperti desain)
@Composable
internal fun UsageFormScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UsagePage)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {
            UsageTopBar(title = title, onBack = onBack)

            Spacer(Modifier.height(4.dp))

            content()

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ======================================================
// KOMPONEN FORM
// ======================================================
@Composable
internal fun FormLabel(label: String, required: Boolean = false) {
    Row {
        Text(
            text = label,
            color = UsageNavy,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (required) {
            Text(
                text = " *",
                color = UsageRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun FieldBox(
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    contentAlignment: Alignment = Alignment.CenterStart,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, UsageBorder, shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp),
        contentAlignment = contentAlignment,
        content = content
    )
}

@Composable
internal fun DropdownField(
    value: String,
    options: List<String>,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FieldBox(onClick = { if (options.isNotEmpty()) expanded = true }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    color = UsageNavy,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = UsageNavy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Tombol biru utama ("Simpan Data")
@Composable
internal fun UsagePrimaryButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = UsageBlue,
                spotColor = UsageBlue
            )
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) UsageBlue else UsageBlue.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Placeholder foto produk
@Composable
internal fun ProductThumb(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(UsageBlueSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Inventory2,
            contentDescription = null,
            tint = UsageBlue,
            modifier = Modifier.size(size / 2)
        )
    }
}

// ======================================================
// DIALOG
// ======================================================
@Composable
internal fun UsageConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        title = title,
        message = message,
        icon = Icons.Default.Delete,
        destructive = true,
        confirmText = confirmText,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageDatePickerDialog(
    initialDate: String,
    title: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = usageIsoToMillis(initialDate)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let {
                        onPick(usageMillisToIso(it))
                    }
                    onDismiss()
                }
            ) {
                Text("Pilih", color = UsageBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = UsageMuted)
            }
        }
    ) {
        DatePicker(
            state = pickerState,
            showModeToggle = true,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UsageTimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    val parts = initialTime.split(":")

    val state = rememberTimePickerState(
        initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 0,
        initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pilih waktu pemakaian",
                    color = UsageNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                TimePicker(state = state)

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = UsageMuted)
                    }

                    TextButton(
                        onClick = {
                            onPick(
                                String.format(
                                    Locale.US,
                                    "%02d:%02d",
                                    state.hour,
                                    state.minute
                                )
                            )
                            onDismiss()
                        }
                    ) {
                        Text("Pilih", color = UsageBlue)
                    }
                }
            }
        }
    }
}
