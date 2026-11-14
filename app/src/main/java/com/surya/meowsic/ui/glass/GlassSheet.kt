package com.surya.meowsic.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.surya.meowsic.ui.theme.LiquidGlassTokens
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Glass bottom sheet — wraps [ModalBottomSheet] with:
 * - Real backdrop blur via haze (API 31+)
 * - Specular bevel rim on the top edge
 * - AMOLED-safe glass tint
 * - Glass drag handle pill
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable BoxScope.() -> Unit
) {
    val cornerShape = RoundedCornerShape(topStart = GlassTokens.CornerSheet, topEnd = GlassTokens.CornerSheet)

    WithoutHaze {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = Color.Transparent,
            scrimColor = Color.Black.copy(alpha = 0.72f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(LiquidGlassTokens.DragHandleColor)
                    )
                }
            },
            shape = cornerShape,
            modifier = modifier
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0F16))
                    .background(LiquidGlassTokens.SurfaceGlassPrimary)
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = LiquidGlassTokens.SpecularBevelHighlight,
                            start = Offset(x = GlassTokens.CornerSheet.toPx(), y = 1.dp.toPx()),
                            end = Offset(x = size.width - GlassTokens.CornerSheet.toPx(), y = 1.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    },
                content = content
            )
        }
    }
}
