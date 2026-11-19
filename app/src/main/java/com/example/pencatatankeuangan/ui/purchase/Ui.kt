package com.example.pencatatankeuangan.ui.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

// Warna mengikuti standar aplikasi HabisKapan
val BluePrimary = Color(0xFF2962ED)
val Navy = Color(0xFF101B33)
val TextDark = Navy
val TextGray = Color(0xFF8190A9)
val BgScreen = Color(0xFFF6F8FC)
val BorderColor = Color(0xFFDCE5F1)
val RedDanger = Color(0xFFE52935)
val GreenSuccess = Color(0xFF12A65B)

// Warna pendukung
val BlueLight = Color(0xFFEAF1FF)
val RedLight = Color(0xFFFFE5E6)

// Tombol ikon kotak
@Composable
fun IconBox(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, BorderColor, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Navy,
            modifier = Modifier.size(22.dp)
        )
    }
}

// Gambar produk dan placeholder
@Composable
fun ProductImage(
    url: String?,
    modifier: Modifier,
    shape: RoundedCornerShape
) {
    if (url.isNullOrBlank()) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(BlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = BluePrimary
            )
        }
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = modifier.clip(shape),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(BlueLight),
            error = ColorPainter(BlueLight)
        )
    }
}
