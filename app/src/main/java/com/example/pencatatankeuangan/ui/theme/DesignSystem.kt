package com.example.pencatatankeuangan.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ======================================================
// WARNA DESAIN SISTEM HABISKAPAN
// ======================================================
val AppBlue = Color(0xFF2962ED)
val AppBlueSoft = Color(0xFFEEF5FF)
val AppNavy = Color(0xFF101B33)
val AppMuted = Color(0xFF8190A9)
val AppPageBg = Color(0xFFF6F8FC)
val AppBorder = Color(0xFFDCE5F1)
val AppRed = Color(0xFFE52935)
val AppRedBg = Color(0xFFFFE8E8)
val AppGreen = Color(0xFF12A65B)
val AppGreenBg = Color(0xFFE5F8ED)

// ======================================================
// DIALOG SERAGAM
//
// - Kartu putih, sudut 20dp
// - Ikon dalam lingkaran (merah untuk aksi berbahaya, biru untuk info)
// - Judul Navy tebal, isi Muted
// - Tombol utama berisi (merah jika destructive, biru jika tidak)
// - Tombol batal berupa outline (isi dismissText = null untuk dialog info)
// ======================================================
@Composable
fun AppDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    message: String? = null,
    icon: ImageVector? = null,
    dismissText: String? = "Batal",
    destructive: Boolean = false,
    dismissOnOutside: Boolean = true,
    content: (@Composable () -> Unit)? = null
) {
    val accent = if (destructive) AppRed else AppBlue
    val accentSoft = if (destructive) AppRedBg else AppBlueSoft

    val iconSlot: (@Composable () -> Unit)? = if (icon != null) {
        {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent
                )
            }
        }
    } else null

    val dismissSlot: (@Composable () -> Unit)? = if (dismissText != null) {
        {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppNavy
                )
            ) {
                Text(dismissText, fontWeight = FontWeight.SemiBold)
            }
        }
    } else null

    AlertDialog(
        onDismissRequest = if (dismissOnOutside) onDismiss else ({}),
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        icon = iconSlot,
        title = {
            Text(
                text = title,
                color = AppNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = if (icon != null) TextAlign.Center else TextAlign.Start
            )
        },
        text = {
            Column {
                if (message != null) {
                    Text(
                        text = message,
                        color = AppMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = if (icon != null) TextAlign.Center else TextAlign.Start
                    )
                }

                if (content != null) {
                    if (message != null) Spacer(Modifier.height(12.dp))
                    content()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                )
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = dismissSlot
    )
}
