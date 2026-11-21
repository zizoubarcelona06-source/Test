package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavyPrimaryDark
import com.example.ui.theme.OrangeDeep
import com.example.ui.theme.StatusDanger

/**
 * Custom geometric BTP Manager emblem faithfully combining architectural skyscrapers,
 * an orange tower crane, a bright construction hard hat, and a sweeping navy/orange wave.
 */
@Composable
fun BtpBrandEmblem(
    size: Dp = 52.dp,
    onDarkBackground: Boolean = true,
    showBadgeFrame: Boolean = true,
    modifier: Modifier = Modifier
) {
    val content: @Composable () -> Unit = {
        Canvas(modifier = Modifier.padding(if (showBadgeFrame) size * 0.10f else 0.dp)) {
            val w = this.size.width
            val h = this.size.height

            val buildingColor = if (onDarkBackground) Color.White else NavyPrimary
            val slitColor = if (onDarkBackground) NavyPrimaryDark else Color.White

            // 1. Tower Crane in Construction Orange (Upper Right)
            val craneOrange = AmberSecondary
            val mastX = w * 0.65f
            val mastTop = h * 0.14f
            val mastBot = h * 0.68f
            // Vertical truss legs
            drawLine(
                color = craneOrange,
                start = Offset(mastX - w * 0.036f, mastBot),
                end = Offset(mastX - w * 0.036f, mastTop),
                strokeWidth = w * 0.032f
            )
            drawLine(
                color = craneOrange,
                start = Offset(mastX + w * 0.036f, mastBot),
                end = Offset(mastX + w * 0.036f, mastTop),
                strokeWidth = w * 0.032f
            )
            // Truss rungs
            for (step in 0..3) {
                val yA = mastTop + h * (0.07f + step * 0.11f)
                val yB = yA + h * 0.09f
                drawLine(
                    color = craneOrange,
                    start = Offset(mastX - w * 0.036f, yA),
                    end = Offset(mastX + w * 0.036f, yB),
                    strokeWidth = w * 0.022f
                )
            }
            // Crane horizontal boom
            val boomY = h * 0.21f
            drawLine(
                color = craneOrange,
                start = Offset(w * 0.30f, boomY),
                end = Offset(w * 0.90f, boomY),
                strokeWidth = w * 0.042f,
                cap = StrokeCap.Round
            )
            // Peak & tension cables
            val peak = Offset(mastX, h * 0.05f)
            drawLine(
                color = craneOrange,
                start = peak,
                end = Offset(w * 0.35f, boomY),
                strokeWidth = w * 0.024f
            )
            drawLine(
                color = craneOrange,
                start = peak,
                end = Offset(w * 0.87f, boomY),
                strokeWidth = w * 0.024f
            )
            // Counterweight & hook cable
            drawRoundRect(
                color = OrangeDeep,
                topLeft = Offset(w * 0.31f, boomY + h * 0.015f),
                size = Size(w * 0.09f, h * 0.065f),
                cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
            )
            drawLine(
                color = AmberBright,
                start = Offset(w * 0.83f, boomY),
                end = Offset(w * 0.83f, h * 0.37f),
                strokeWidth = w * 0.024f
            )
            drawArc(
                color = AmberBright,
                startAngle = 0f,
                sweepAngle = 215f,
                useCenter = false,
                topLeft = Offset(w * 0.80f, h * 0.36f),
                size = Size(w * 0.06f, h * 0.06f),
                style = Stroke(width = w * 0.024f, cap = StrokeCap.Round)
            )

            // 2. Stepped Architectural Skyscrapers (Left & Center)
            // Far left tall angled building
            val leftTower = Path().apply {
                moveTo(w * 0.13f, h * 0.72f)
                lineTo(w * 0.13f, h * 0.30f)
                lineTo(w * 0.28f, h * 0.22f)
                lineTo(w * 0.28f, h * 0.68f)
                close()
            }
            drawPath(leftTower, color = buildingColor)
            for (row in 0..4) {
                val wy = h * (0.33f + row * 0.062f)
                drawRect(
                    color = slitColor,
                    topLeft = Offset(w * 0.165f, wy),
                    size = Size(w * 0.075f, h * 0.032f)
                )
            }

            // Middle twin-column skyscraper
            val midTower = Path().apply {
                moveTo(w * 0.31f, h * 0.68f)
                lineTo(w * 0.31f, h * 0.29f)
                lineTo(w * 0.44f, h * 0.29f)
                lineTo(w * 0.44f, h * 0.67f)
                close()
            }
            drawPath(midTower, color = buildingColor)
            drawRect(
                color = slitColor,
                topLeft = Offset(w * 0.345f, h * 0.34f),
                size = Size(w * 0.028f, h * 0.27f)
            )
            drawRect(
                color = slitColor,
                topLeft = Offset(w * 0.39f, h * 0.34f),
                size = Size(w * 0.028f, h * 0.27f)
            )

            // Foreground sloped corporate block
            val centerBlock = Path().apply {
                moveTo(w * 0.46f, h * 0.69f)
                lineTo(w * 0.46f, h * 0.42f)
                lineTo(w * 0.62f, h * 0.37f)
                lineTo(w * 0.62f, h * 0.69f)
                close()
            }
            drawPath(centerBlock, color = buildingColor)

            // 3. Construction Safety Hard Hat (Foreground Right)
            val helmetCenter = Offset(w * 0.69f, h * 0.59f)
            drawArc(
                brush = Brush.verticalGradient(listOf(AmberBright, AmberSecondary, OrangeDeep)),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(helmetCenter.x - w * 0.16f, helmetCenter.y - h * 0.17f),
                size = Size(w * 0.32f, h * 0.26f)
            )
            // Helmet top crown ridge
            drawRoundRect(
                color = AmberBright,
                topLeft = Offset(helmetCenter.x - w * 0.035f, helmetCenter.y - h * 0.185f),
                size = Size(w * 0.07f, h * 0.11f),
                cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
            )
            // Helmet brim
            drawRoundRect(
                color = OrangeDeep,
                topLeft = Offset(helmetCenter.x - w * 0.19f, helmetCenter.y - h * 0.045f),
                size = Size(w * 0.38f, h * 0.055f),
                cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
            )

            // 4. Sweeping Dynamic Swoosh Wave across base (Orange & Navy)
            val orangeWave = Path().apply {
                moveTo(w * 0.04f, h * 0.78f)
                quadraticBezierTo(w * 0.38f, h * 0.58f, w * 0.94f, h * 0.74f)
                quadraticBezierTo(w * 0.42f, h * 0.67f, w * 0.04f, h * 0.84f)
                close()
            }
            drawPath(orangeWave, color = AmberSecondary)

            val navyWave = Path().apply {
                moveTo(w * 0.06f, h * 0.87f)
                quadraticBezierTo(w * 0.44f, h * 0.69f, w * 0.96f, h * 0.80f)
                quadraticBezierTo(w * 0.48f, h * 0.77f, w * 0.06f, h * 0.92f)
                close()
            }
            drawPath(
                navyWave,
                color = if (onDarkBackground) Color.White.copy(alpha = 0.88f) else NavyPrimary
            )
        }
    }

    if (showBadgeFrame) {
        Surface(
            modifier = modifier.size(size),
            shape = RoundedCornerShape(size * 0.24f),
            color = if (onDarkBackground) NavyPrimaryDark else Color.White,
            border = BorderStroke(1.25.dp, AmberSecondary.copy(alpha = 0.75f)),
            shadowElevation = 4.dp
        ) {
            content()
        }
    } else {
        Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/**
 * Centered vertical corporate brand lockup matching the signature BTP MANAGER poster & login hero.
 */
@Composable
fun BtpVerticalBrandLockup(
    subtitle: String? = null,
    onDarkBackground: Boolean = true,
    emblemSize: Dp = 88.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BtpBrandEmblem(
            size = emblemSize,
            onDarkBackground = onDarkBackground,
            showBadgeFrame = false
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "BTP",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 34.sp,
                lineHeight = 36.sp,
                letterSpacing = 1.6.sp
            ),
            fontWeight = FontWeight.ExtraBold,
            color = if (onDarkBackground) Color.White else NavyPrimary
        )
        Text(
            text = "MANAGER",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 21.sp,
                lineHeight = 24.sp,
                letterSpacing = 2.0.sp
            ),
            fontWeight = FontWeight.ExtraBold,
            color = AmberSecondary
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(0.86f)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.22f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AmberSecondary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (onDarkBackground) Color.White.copy(alpha = 0.90f) else NavyPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(0.22f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AmberSecondary)
                )
            }
        }
    }
}

/**
 * Horizontal brand lockup displaying BTP MANAGER emblem and wordmark.
 */
@Composable
fun BtpBrandHeaderLockup(
    subtitle: String? = null,
    onDarkBackground: Boolean = false,
    useRasterLogo: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (useRasterLogo) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NavyPrimaryDark,
                border = BorderStroke(1.5.dp, AmberSecondary.copy(alpha = 0.8f)),
                shadowElevation = 6.dp,
                modifier = Modifier.size(56.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_btp_manager_logo),
                    contentDescription = "BTP MANAGER",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                )
            }
        } else {
            BtpBrandEmblem(size = 44.dp, onDarkBackground = onDarkBackground)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "BTP",
                    style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 0.8.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (onDarkBackground) Color.White else NavyPrimary
                )
                Spacer(modifier = Modifier.width(5.dp))
                Surface(
                    color = AmberSecondary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "MANAGER",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.9.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (onDarkBackground) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Unified corporate icon tile for all 16 BTP modules, KPIs, and cards.
 * Uses a clean, crisp tinted geometric surface with vibrant architectural iconography.
 */
@Composable
fun BtpUnifiedModuleIcon(
    icon: ImageVector,
    isAccentOrange: Boolean = false,
    isAlert: Boolean = false,
    size: Dp = 44.dp,
    accentColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val resolvedAccent = when {
        isAlert -> StatusDanger
        accentColor != null -> accentColor
        isAccentOrange -> AmberSecondary
        else -> NavyPrimary
    }
    val surfaceBg = resolvedAccent.copy(alpha = if (isAlert) 0.14f else 0.12f)
    val borderColor = resolvedAccent.copy(alpha = if (isAlert) 0.35f else 0.24f)

    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(size * 0.28f),
        color = surfaceBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = resolvedAccent,
                modifier = Modifier.size(size * 0.56f)
            )
        }
    }
}
