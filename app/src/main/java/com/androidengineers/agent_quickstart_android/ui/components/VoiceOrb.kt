package com.androidengineers.agent_quickstart_android.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androidengineers.agent_quickstart_android.model.AgentVisualState
import com.androidengineers.agent_quickstart_android.ui.theme.OrbConnecting
import com.androidengineers.agent_quickstart_android.ui.theme.OrbError
import com.androidengineers.agent_quickstart_android.ui.theme.OrbIdle
import com.androidengineers.agent_quickstart_android.ui.theme.OrbListening
import com.androidengineers.agent_quickstart_android.ui.theme.OrbSpeaking
import com.androidengineers.agent_quickstart_android.ui.theme.OrbThinking
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyDark

@Composable
fun VoiceOrb(
    state: AgentVisualState,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceOrbTransition")

    // Breathing pulse for scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = when (state) {
            AgentVisualState.SPEAKING -> 1.15f
            AgentVisualState.LISTENING -> 1.08f
            AgentVisualState.THINKING -> 1.04f
            AgentVisualState.WAITING -> 1.02f
            else -> 1.0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AgentVisualState.SPEAKING -> 600
                    AgentVisualState.LISTENING -> 900
                    AgentVisualState.THINKING -> 1200
                    else -> 2000
                },
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "OrbScale",
    )

    // Wave ripple alpha
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AgentVisualState.SPEAKING -> 1000
                    AgentVisualState.LISTENING -> 1400
                    else -> 2200
                },
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "RippleAlpha",
    )

    val rippleRadiusFactor by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AgentVisualState.SPEAKING -> 1000
                    AgentVisualState.LISTENING -> 1400
                    else -> 2200
                },
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "RippleRadius",
    )

    val primaryColor = when (state) {
        AgentVisualState.WAITING -> OrbConnecting
        AgentVisualState.LISTENING -> OrbListening
        AgentVisualState.THINKING -> OrbThinking
        AgentVisualState.SPEAKING -> OrbSpeaking
        AgentVisualState.DISCONNECTED -> OrbError
        AgentVisualState.IDLE -> OrbIdle
    }

    val secondaryColor = when (state) {
        AgentVisualState.WAITING -> OrbConnecting.copy(alpha = 0.4f)
        AgentVisualState.LISTENING -> Color(0xFF34D399)
        AgentVisualState.THINKING -> Color(0xFFC084FC)
        AgentVisualState.SPEAKING -> Color(0xFF38BDF8)
        AgentVisualState.DISCONNECTED -> Color(0xFFF87171)
        AgentVisualState.IDLE -> Color(0xFF334155)
    }

    Box(
        modifier = modifier.size(size * 1.4f),
        contentAlignment = Alignment.Center,
    ) {
        // Outer concentric wave ripple
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val baseRadius = (size.toPx() / 2f)

            if (state == AgentVisualState.SPEAKING || state == AgentVisualState.LISTENING) {
                drawCircle(
                    color = primaryColor.copy(alpha = rippleAlpha * 0.5f),
                    radius = baseRadius * rippleRadiusFactor,
                    center = center,
                    style = Stroke(width = 3.dp.toPx()),
                )
                drawCircle(
                    color = secondaryColor.copy(alpha = rippleAlpha * 0.3f),
                    radius = baseRadius * (rippleRadiusFactor * 0.85f),
                    center = center,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }

        // Inner glowing orb
        Box(
            modifier = Modifier
                .size(size)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.85f),
                            primaryColor,
                            VoxNavyDark,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // State icon in center
            val icon = when (state) {
                AgentVisualState.LISTENING -> Icons.Default.Mic
                AgentVisualState.THINKING -> Icons.Default.GraphicEq
                AgentVisualState.SPEAKING -> Icons.Default.RecordVoiceOver
                AgentVisualState.DISCONNECTED -> Icons.Default.Warning
                else -> Icons.Default.VolumeUp
            }

            Icon(
                imageVector = icon,
                contentDescription = state.name,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(size * 0.35f),
            )
        }
    }
}
