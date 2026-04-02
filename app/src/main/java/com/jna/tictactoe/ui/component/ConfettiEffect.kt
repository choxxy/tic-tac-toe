package com.jna.tictactoe.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.jna.tictactoe.ui.theme.*
import kotlinx.coroutines.isActive
import kotlin.random.Random

/**
 * A celebratory confetti effect that renders particles on a Canvas.
 * Particles follow basic physics: initial velocity and gravity.
 */
@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    durationMillis: Int = 4000
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val particles = remember { mutableStateListOf<ConfettiParticle>() }
    val colors = listOf(
        ZenithPrimary, ZenithSecondary, ZenithTertiary,
        Color(0xFFFFD700), // Gold
        Color(0xFF00FA9A), // Medium Spring Green
        Color(0xFF1E90FF), // Dodger Blue
        Color(0xFFFF69B4)  // Hot Pink
    )

    var effectActive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Initial burst
        repeat(100) {
            particles.add(createParticle(screenWidthPx, colors))
        }
        
        val startTime = System.currentTimeMillis()
        while (effectActive && isActive) {
            withFrameMillis { frameTime ->
                val elapsed = frameTime - startTime
                if (elapsed > durationMillis) {
                    effectActive = false
                    return@withFrameMillis
                }

                particles.forEach { p ->
                    p.update()
                }
            }
        }
    }

    if (effectActive) {
        Canvas(modifier = modifier.fillMaxSize()) {
            particles.forEach { p ->
                if (p.alpha > 0f) {
                    rotate(p.rotation, Offset(p.x + p.size / 2, p.y + p.size / 2)) {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(p.x, p.y),
                            size = Size(p.size, p.size * 0.6f),
                            alpha = p.alpha
                        )
                    }
                }
            }
        }
    }
}

private fun createParticle(screenWidthPx: Float, colors: List<Color>): ConfettiParticle {
    return ConfettiParticle(
        x = Random.nextFloat() * screenWidthPx,
        y = -Random.nextFloat() * 200f - 50f, // Start above screen at different heights
        vx = (Random.nextFloat() - 0.5f) * 15f,
        vy = Random.nextFloat() * 10f + 5f,
        size = Random.nextFloat() * 20f + 15f,
        color = colors.random(),
        rotation = Random.nextFloat() * 360f,
        rotationSpeed = (Random.nextFloat() - 0.5f) * 15f
    )
}

private class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color,
    var rotation: Float,
    val rotationSpeed: Float
) {
    var alpha = 1f
    private val gravity = 0.4f
    private val airResistance = 0.98f

    fun update() {
        x += vx
        y += vy
        vy += gravity
        vx *= airResistance
        rotation += rotationSpeed
        
        // Fade out as it falls
        if (y > 1500f) {
            alpha = (alpha - 0.02f).coerceAtLeast(0f)
        }
    }
}
