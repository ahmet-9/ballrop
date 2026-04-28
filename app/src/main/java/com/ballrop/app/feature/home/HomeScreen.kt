package com.ballrop.app.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ballrop.app.R
import com.ballrop.app.core.design.BallropColors
import com.ballrop.app.core.storage.GameProgressStorage
import androidx.compose.ui.window.Dialog

@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    onContinueClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val progressStorage = remember { GameProgressStorage(context) }

    var bestLevel by remember { mutableIntStateOf(1) }
    var checkpointStartLevel by remember { mutableIntStateOf(1) }
    var checkpointPart by remember { mutableIntStateOf(1) }
    var checkpointLives by remember { mutableIntStateOf(5) }
    var canContinue by remember { mutableStateOf(false) }
    var targetCorrectCount by remember { mutableIntStateOf(20) }
    var remainingTargetCount by remember { mutableIntStateOf(20) }
    var showNewGameConfirmDialog by remember { mutableStateOf(false) }

    fun refreshProgressState() {
        bestLevel = progressStorage.getBestLevel()

        val continueLevel = progressStorage.getContinueLevel()
        val storedCanContinue = progressStorage.canContinue()

        checkpointStartLevel = continueLevel
        checkpointPart = progressStorage.getCheckpointPart()
        checkpointLives = progressStorage.getCheckpointLives()
        targetCorrectCount = progressStorage.getResumeTargetCorrectCount()
        remainingTargetCount = progressStorage.getResumeRemainingTargetCount()

        canContinue = storedCanContinue
    }

    LaunchedEffect(Unit) {
        refreshProgressState()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshProgressState()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showNewGameConfirmDialog) {
        NewGameConfirmDialog(
            onDismiss = {
                showNewGameConfirmDialog = false
            },
            onConfirm = {
                progressStorage.clearContinueProgress()
                refreshProgressState()
                showNewGameConfirmDialog = false
                onStartClick()
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF010815))
    ) {
        val isTablet = maxWidth >= 600.dp
        val sidePadding = if (isTablet) 28.dp else 18.dp
        val panelMaxWidth = if (isTablet) 560.dp else 420.dp
        val panelHorizontalPadding = if (isTablet) 22.dp else 16.dp
        val panelBottomPadding = if (isTablet) 20.dp else 14.dp
        val panelShape = RoundedCornerShape(28.dp)

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_background_ballrop),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Transparent,
                                0.58f to Color.Transparent,
                                0.78f to Color(0x12000000),
                                1.00f to Color(0x26000000)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = sidePadding, vertical = 12.dp)
            ) {
                Text(
                    text = "RİTMİ YAKALA,\nDOĞRU SEPETE GÖNDER!",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 202.dp),
                    color = Color(0xFFB9D9FF),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 23.sp
                )

                HomePanel(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 250.dp)
                        .fillMaxWidth()
                        .widthIn(max = panelMaxWidth),
                    shape = panelShape,
                    bestLevel = bestLevel,
                    checkpointStartLevel = checkpointStartLevel,
                    checkpointPart = checkpointPart,
                    checkpointLives = checkpointLives,
                    targetCorrectCount = targetCorrectCount,
                    remainingTargetCount = remainingTargetCount,
                    canContinue = canContinue,
                    panelHorizontalPadding = panelHorizontalPadding,
                    panelBottomPadding = panelBottomPadding,
                    onContinueClick = {
                        onContinueClick(checkpointStartLevel)
                    },
                    onStartClick = {
                        if (canContinue) {
                            showNewGameConfirmDialog = true
                        } else {
                            progressStorage.clearContinueProgress()
                            refreshProgressState()
                            onStartClick()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomePanel(
    modifier: Modifier,
    shape: Shape,
    bestLevel: Int,
    checkpointStartLevel: Int,
    checkpointPart: Int,
    checkpointLives: Int,
    targetCorrectCount: Int,
    remainingTargetCount: Int,
    canContinue: Boolean,
    panelHorizontalPadding: Dp,
    panelBottomPadding: Dp,
    onContinueClick: () -> Unit,
    onStartClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)


    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = panelHorizontalPadding,
                    end = panelHorizontalPadding,
                    top = 22.dp,
                    bottom = panelBottomPadding
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            ProgressSummaryRow(
                bestLevel = bestLevel,
                checkpointStartLevel = checkpointStartLevel,
                checkpointPart = checkpointPart,
                checkpointLives = checkpointLives,
                targetCorrectCount = targetCorrectCount,
                remainingTargetCount = remainingTargetCount,
                canContinue = canContinue
            )

            if (canContinue) {
                ContinueGameButton(
                    checkpointStartLevel = checkpointStartLevel,
                    checkpointPart = checkpointPart,
                    onClick = onContinueClick
                )
            }

            StartGameButton(
                text = if (canContinue) "Yeni Oyun Başlat" else "Oyuna Başla",
                compact = canContinue,
                onClick = onStartClick
            )

            QuickInfoCard(
                canContinue = canContinue,
                checkpointStartLevel = checkpointStartLevel,
                checkpointPart = checkpointPart
            )
        }
    }
}

@Composable
private fun ProgressSummaryRow(
    bestLevel: Int,
    checkpointStartLevel: Int,
    checkpointPart: Int,
    checkpointLives: Int,
    targetCorrectCount: Int,
    remainingTargetCount: Int,
    canContinue: Boolean
) {
    val displayLevel = if (canContinue) checkpointStartLevel else 1
    val displayPart = if (canContinue) checkpointPart else 1
    val displayLives = if (canContinue) checkpointLives else 5

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressInfoCard(
                title = if (canContinue) "Devam Leveli" else "Başlangıç",
                value = "Level $displayLevel",
                subtitle = if (canContinue) "Checkpoint aktif" else "Yeni run hazır",
                badgeText = "LV",
                badgeColor = Color(0xFF31A8FF),
                progress = null,
                modifier = Modifier.weight(1f)
            )

            ProgressInfoCard(
                title = "Bölüm",
                value = "$displayPart / 3",
                subtitle = if (canContinue) "Part $displayPart’den devam" else "Part 1’den başlar",
                badgeText = "$displayPart/3",
                badgeColor = Color(0xFF26D9FF),
                progress = displayPart / 3f,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressInfoCard(
                title = "Can Hakkı",
                value = "$displayLives / 5",
                subtitle = "Her part yenilenir",
                badgeText = "♥",
                badgeColor = Color(0xFFFF3D5A),
                progress = displayLives / 5f,
                modifier = Modifier.weight(1f)
            )

            ProgressInfoCard(
                title = "Hedefe Kalan",
                value = "$remainingTargetCount Top",
                subtitle = "Hedef $targetCorrectCount",
                badgeText = "%",
                badgeColor = Color(0xFF9B5CFF),
                progress = if (targetCorrectCount > 0) {
                    1f - (remainingTargetCount.toFloat() / targetCorrectCount.toFloat())
                } else {
                    0f
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProgressInfoCard(
    title: String,
    value: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    progress: Float?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A2C67),
                            Color(0xFF08214E),
                            Color(0xFF06193B)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0x332EA6FF),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProgressBadge(
                    text = badgeText,
                    color = badgeColor,
                    progress = progress
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = BallropColors.TextMuted,
                        maxLines = 1
                    )

                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = BallropColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = BallropColors.TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressBadge(
    text: String,
    color: Color,
    progress: Float?
) {
    Box(
        modifier = Modifier.size(44.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = color.copy(alpha = 0.16f),
                radius = size.minDimension / 2f
            )

            drawCircle(
                color = color.copy(alpha = 0.45f),
                radius = size.minDimension / 2f,
                style = Stroke(width = 1.4.dp.toPx())
            )

            if (progress != null) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 3.dp.toPx()),
                    size = Size(
                        width = size.width - 6.dp.toPx(),
                        height = size.height - 6.dp.toPx()
                    ),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        val displayText = if (progress != null && text == "%") {
            "${(progress * 100).toInt().coerceIn(0, 100)}%"
        } else {
            text
        }

        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NeonActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconGlowColor: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D2A63),
                        Color(0xFF081C46),
                        Color(0xFF04132F)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2EA6FF),
                        Color(0x802E7DFF),
                        Color(0x40205CFF)
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (compact) 14.dp else 16.dp,
                vertical = if (compact) 10.dp else 14.dp
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(
                    RoundedCornerShape(
                        topStart = 22.dp,
                        bottomStart = 22.dp,
                        topEnd = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x1200D1FF),
                            Color(0x082F8CFF),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 44.dp else 52.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                iconGlowColor.copy(alpha = 0.55f),
                                iconGlowColor.copy(alpha = 0.18f),
                                Color(0xFF0A1D45)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = iconGlowColor.copy(alpha = 0.65f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 22.dp else 26.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = if (compact) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = BallropColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = BallropColors.TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF8BBEFF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ContinueGameButton(
    checkpointStartLevel: Int,
    checkpointPart: Int,
    onClick: () -> Unit
) {
    NeonActionCard(
        title = "Kaldığın Yerden Devam Et",
        subtitle = "Level $checkpointStartLevel • Bölüm $checkpointPart/3 noktasından devam et",
        icon = Icons.Filled.Refresh,
        iconGlowColor = Color(0xFF21E38A),
        onClick = onClick
    )
}

@Composable
private fun StartGameButton(
    text: String,
    compact: Boolean,
    onClick: () -> Unit
) {
    NeonActionCard(
        title = text,
        subtitle = "Yeni bir oyun akışı başlat",
        icon = Icons.Filled.PlayArrow,
        iconGlowColor = Color(0xFF31A8FF),
        compact = compact,
        onClick = onClick
    )
}

@Composable
private fun QuickInfoCard(
    canContinue: Boolean,
    checkpointStartLevel: Int,
    checkpointPart: Int
) {
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xE60A1E46),
                        Color(0xE6051636),
                        Color(0xE6020E26)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0x332EA6FF),
                shape = shape
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (canContinue) "Devam Bilgisi" else "Oyun Mantığı",
                style = MaterialTheme.typography.titleMedium,
                color = BallropColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (canContinue) {
                    "Devam edersen Level $checkpointStartLevel • Bölüm $checkpointPart/3 akışından başlarsın. Yeni oyun açarsan mevcut devam hakkın sıfırlanır."
                } else {
                    "Amaç doğru topu doğru sepete sürüklemek. Level, bölüm ve checkpoint sistemi ilerlemeni korur."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = BallropColors.TextSecondary
            )
        }
    }
}

@Composable
private fun NewGameConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF70A2C67),
                            Color(0xF7061B46),
                            Color(0xF702102D)
                        )
                    )
                )
                .border(
                    width = 1.3.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2EA6FF),
                            Color(0xAA1B7CFF),
                            Color(0x66305CFF)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x5531A8FF),
                                    Color(0x2200D1FF),
                                    Color(0xFF071A3D)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xAA2EA6FF),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Yeni Oyun Başlatılsın mı?",
                        color = BallropColors.TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Mevcut devam hakkın silinecek.\nYeni oyun Level 1 • Bölüm 1/3 noktasından başlayacak.",
                        color = BallropColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DialogActionButton(
                        text = "Vazgeç",
                        icon = Icons.Filled.Refresh,
                        glowColor = Color(0xFF21E38A),
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    )

                    DialogActionButton(
                        text = "Başlat",
                        icon = Icons.Filled.PlayArrow,
                        glowColor = Color(0xFF31A8FF),
                        modifier = Modifier.weight(1f),
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    text: String,
    icon: ImageVector,
    glowColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D2A63),
                        Color(0xFF071C46),
                        Color(0xFF04132F)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = glowColor.copy(alpha = 0.65f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )

            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}