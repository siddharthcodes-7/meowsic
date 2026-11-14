package com.surya.meowsic.presentation.library

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.surya.meowsic.ui.glass.GlassButton
import com.surya.meowsic.ui.glass.GlassSurface
import com.surya.meowsic.ui.glass.GlassVariant
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, coverPath: String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var coverImagePath by remember { mutableStateOf<String?>(null) }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@rememberLauncherForActivityResult
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (original == null) return@rememberLauncherForActivityResult
            val side = minOf(original.width, original.height)
            val x = (original.width - side) / 2
            val y = (original.height - side) / 2
            val cropped = Bitmap.createBitmap(original, x, y, side, side)
            val scaled = Bitmap.createScaledBitmap(cropped, 1024, 1024, true)
            val coversDir = File(context.filesDir, "playlist_covers")
            coversDir.mkdirs()
            val outFile = File(coversDir, "${UUID.randomUUID()}.jpg")
            FileOutputStream(outFile).use { fos ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }
            coverImagePath = outFile.absolutePath
        } catch (e: Exception) {
            // ignore
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            variant = GlassVariant.ELEVATED,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "New Playlist",
                    color = LiquidGlassTokens.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Cover image picker
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, LiquidGlassTokens.SpecularRimBorder, RoundedCornerShape(12.dp))
                ) {
                    if (coverImagePath != null) {
                        AsyncImage(
                            model = coverImagePath,
                            contentDescription = "Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(20.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add cover",
                                tint = LiquidGlassTokens.TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("Add cover", color = LiquidGlassTokens.TextMuted, fontSize = 11.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        GlassButton(
                            onClick = {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(12.dp)
                        ) {}
                    }
                }

                // Name field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Playlist name",
                        color = LiquidGlassTokens.TextSecondary,
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, LiquidGlassTokens.SpecularRimBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            textStyle = TextStyle(
                                color = LiquidGlassTokens.TextPrimary,
                                fontSize = 16.sp
                            ),
                            cursorBrush = SolidColor(LiquidGlassTokens.TextPrimary),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (name.isEmpty()) {
                                    Text("My Playlist", color = LiquidGlassTokens.TextMuted, fontSize = 16.sp)
                                }
                                inner()
                            }
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Cancel", color = LiquidGlassTokens.TextSecondary, fontSize = 14.sp)
                    }
                    GlassButton(
                        onClick = {
                            val finalName = name.trim().ifBlank { "My Playlist" }
                            onSave(finalName, coverImagePath)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            "Save",
                            color = LiquidGlassTokens.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
