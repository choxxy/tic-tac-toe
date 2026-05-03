package com.jna.tictactoe.screen.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jna.tictactoe.R
import com.jna.tictactoe.ui.theme.TictactoeTheme
import com.jna.tictactoe.ui.theme.ZenithOnBackground as OnBackgroundColor
import com.jna.tictactoe.ui.theme.ZenithOnSurfaceVariant as OnSurfaceVariantColor
import com.jna.tictactoe.ui.theme.ZenithOutlineVariant as OutlineVariantColor
import com.jna.tictactoe.ui.theme.ZenithPrimary as PrimaryColor
import com.jna.tictactoe.ui.theme.ZenithSurface as SurfaceColor
import com.jna.tictactoe.ui.theme.ZenithSurfaceContainerHigh as SurfaceContainerHigh
import com.jna.tictactoe.ui.theme.ZenithSurfaceContainerLow as SurfaceContainerLow
import com.jna.tictactoe.ui.theme.ZenithSurfaceContainerLowest as SurfaceContainerLowest
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onTimeout()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceColor)
    ) {
        // Top tonal strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(212.dp)
                .background(SurfaceContainerLow.copy(alpha = 0.3f))
        )

        // Bottom-right ambient glow blob (radial gradient simulates the blur)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SurfaceContainerHigh.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(size.width, size.height),
                    radius = size.width * 1.1f
                )
            )
        }

        // Decorative dot: top-left quadrant
        Box(
            modifier = Modifier
                .padding(start = 88.dp, top = 232.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(OutlineVariantColor.copy(alpha = 0.2f))
        )

        // Decorative dot: bottom-right area
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 128.dp, bottom = 296.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(OutlineVariantColor.copy(alpha = 0.2f))
        )

        // Central branding cluster
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoCard()
            Spacer(modifier = Modifier.height(48.dp))
            BrandIdentity()
        }

        // Bottom anchor
        BottomAnchor(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun LogoCard() {
    Box {
        // Card with elevation
        Box(
            modifier = Modifier
                .size(128.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false,
                    ambientColor = PrimaryColor.copy(alpha = 0.08f),
                    spotColor = PrimaryColor.copy(alpha = 0.08f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerLowest),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "logo",
            )
        }

        // Blue accent dot with white ring, anchored to top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(SurfaceContainerLowest),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(PrimaryColor)
            )
        }
    }
}

private fun DrawScope.drawZenithIcon() {
    val center = Offset(size.width / 2f, size.height / 2f)

    // Dark background
    drawRect(color = Color(0xFF030810))

    // Outer ambient glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x25214F8C), Color.Transparent),
            center = center,
            radius = size.width * 0.7f
        )
    )

    // Inner blue glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x554B8EFF), Color.Transparent),
            center = center,
            radius = size.width * 0.28f
        )
    )

    // Vertical ray (long axis)
    val longRay = size.height * 0.48f
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0xAAD0E4FF), Color(0xFFFFFFFF), Color(0xAAD0E4FF), Color.Transparent),
            startY = center.y - longRay,
            endY = center.y + longRay
        ),
        start = Offset(center.x, center.y - longRay),
        end = Offset(center.x, center.y + longRay),
        strokeWidth = 1.5f
    )

    // Horizontal ray (shorter axis)
    val shortRay = size.width * 0.44f
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(Color.Transparent, Color(0xAAD0E4FF), Color(0xFFFFFFFF), Color(0xAAD0E4FF), Color.Transparent),
            start = Offset(center.x - shortRay, center.y),
            end = Offset(center.x + shortRay, center.y)
        ),
        start = Offset(center.x - shortRay, center.y),
        end = Offset(center.x + shortRay, center.y),
        strokeWidth = 1.5f
    )

    // Diamond center mark
    val d = size.width * 0.055f
    val diamond = Path().apply {
        moveTo(center.x, center.y - d * 1.7f)
        lineTo(center.x + d, center.y)
        lineTo(center.x, center.y + d * 1.7f)
        lineTo(center.x - d, center.y)
        close()
    }
    drawPath(diamond, color = Color.White)
}

@Composable
private fun BrandIdentity() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "TIC TAC TOE",
            color = OnBackgroundColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 0.4.em
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("PRECISION", "STRATEGY", "FORM").forEachIndexed { index, label ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(OutlineVariantColor.copy(alpha = 0.4f))
                    )
                }
                Text(
                    text = label,
                    color = OnSurfaceVariantColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.em
                )
            }
        }
    }
}

@Composable
private fun BottomAnchor(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.width(1.dp).height(64.dp)) {
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(OutlineVariantColor.copy(alpha = 0.1f), Color.Transparent),
                    startY = 0f,
                    endY = size.height
                ),
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "EST. MMXXIV",
            color = OnSurfaceVariantColor.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 0.3.em
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SplashScreenPreview() {
    TictactoeTheme {
        SplashScreen(onTimeout = {})
    }
}
