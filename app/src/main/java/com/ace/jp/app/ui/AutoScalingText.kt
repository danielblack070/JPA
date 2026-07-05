package com.ace.jp.app.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoScalingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    initialFontSize: TextUnit = 12.sp,
    currentUnifiedSize: TextUnit = initialFontSize,
    onSizeShrunk: (TextUnit) -> Unit = {}
) {
    var localFontSize by remember { mutableStateOf(initialFontSize) }

    // Always respect the globally decided smallest size if it's smaller than our local guess
    val activeFontSize = if (currentUnifiedSize < localFontSize) currentUnifiedSize else localFontSize
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontSize = activeFontSize),
        maxLines = 1,
        overflow = TextOverflow.Clip,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            // If it overflows at the current size, calculation kicks in
            if (textLayoutResult.hasVisualOverflow) {
                val newSize = (activeFontSize.value - 0.5f).sp
                localFontSize = newSize
                onSizeShrunk(newSize) // Report the smaller size requirement upward
            }
        }
    )
}