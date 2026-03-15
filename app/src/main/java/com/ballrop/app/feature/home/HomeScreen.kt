package com.ballrop.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ballrop.app.core.design.BallropColors
import com.ballrop.app.core.design.BallropDimens
import com.ballrop.app.core.storage.GameProgressStorage

@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    onContinueClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val progressStorage = remember { GameProgressStorage(context) }

    var bestScore by remember { mutableIntStateOf(0) }
    var bestLevel by remember { mutableIntStateOf(1) }
    var checkpointStartLevel by remember { mutableIntStateOf(1) }
    var canContinue by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bestScore = progressStorage.getBestScore()
        bestLevel = progressStorage.getBestLevel()
        checkpointStartLevel = progressStorage.getCheckpointStartLevel()
        canContinue = progressStorage.canContinue()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BallropColors.BackgroundPrimary)
    ) {
        val isTabletLike = maxWidth >= 700.dp
        val horizontalPadding = if (isTabletLike) 48.dp else BallropDimens.Space20
        val topSpacing = if (isTabletLike) 28.dp else BallropDimens.Space20
        val heroHeight = if (isTabletLike) 320.dp else 220.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = horizontalPadding, vertical = BallropDimens.Space16),
            verticalArrangement = Arrangement.spacedBy(BallropDimens.Space16)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(BallropDimens.Space8)
            ) {
                Text(
                    text = "BALLROP",
                    style = MaterialTheme.typography.headlineLarge,
                    color = BallropColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Renkli topları doğru sepetlere sürükle, ritmi yakala ve en yüksek skoru yap.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BallropColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            HeroPlayfieldCard(
                heroHeight = heroHeight
            )

            ProgressSummaryRow(
                bestScore = bestScore,
                bestLevel = bestLevel,
                checkpointStartLevel = checkpointStartLevel,
                canContinue = canContinue
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(BallropDimens.Space12)
            ) {
                if (canContinue) {
                    ContinueGameButton(
                        checkpointStartLevel = checkpointStartLevel,
                        onClick = {
                            onContinueClick(checkpointStartLevel)
                        }
                    )
                }

                StartGameButton(
                    text = if (canContinue) "Yeni Oyun" else "Oyuna Başla",
                    onClick = {
                        progressStorage.clearContinueProgress()
                        onStartClick()
                    }
                )

                QuickInfoCard(
                    canContinue = canContinue,
                    checkpointStartLevel = checkpointStartLevel
                )
            }
        }
    }
}

@Composable
private fun HeroPlayfieldCard(
    heroHeight: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heroHeight)
            .clip(RoundedCornerShape(BallropDimens.Radius24))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BallropColors.Surface,
                        BallropColors.SurfaceSoft
                    )
                )
            )
            .padding(BallropDimens.Space20)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(58.dp)
                .clip(RoundedCornerShape(29.dp))
                .background(BallropColors.BallBlue.copy(alpha = 0.90f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 12.dp)
                .size(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(BallropColors.BallRed.copy(alpha = 0.90f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 12.dp)
        ) {
            Text(
                text = "Hız + Refleks + Renk",
                style = MaterialTheme.typography.titleLarge,
                color = BallropColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space16)
        ) {
            BasketPreview(
                title = "Mavi Sepet",
                modifier = Modifier.weight(1f),
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BallropColors.BasketBlue,
                        BallropColors.BasketBlueDark
                    )
                )
            )

            BasketPreview(
                title = "Kırmızı Sepet",
                modifier = Modifier.weight(1f),
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BallropColors.BasketRed,
                        BallropColors.BasketRedDark
                    )
                )
            )
        }
    }
}

@Composable
private fun BasketPreview(
    title: String,
    modifier: Modifier = Modifier,
    brush: Brush
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BallropDimens.Space8)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .clip(RoundedCornerShape(BallropDimens.Radius20))
                .background(brush = brush)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = BallropColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProgressSummaryRow(
    bestScore: Int,
    bestLevel: Int,
    checkpointStartLevel: Int,
    canContinue: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space12)
    ) {
        ProgressCard(
            title = "En İyi Skor",
            value = bestScore.toString(),
            modifier = Modifier.weight(1f)
        )

        ProgressCard(
            title = "En Yüksek Level",
            value = bestLevel.toString(),
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = BallropDimens.Space12),
        horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space12)
    ) {
        ProgressCard(
            title = "Checkpoint",
            value = if (canContinue) "Level $checkpointStartLevel" else "-",
            modifier = Modifier.weight(1f)
        )

        ProgressCard(
            title = "Durum",
            value = if (canContinue) "Devam Hazır" else "Yeni Run",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProgressCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(BallropDimens.Radius18))
            .background(BallropColors.SurfaceSoft.copy(alpha = 0.95f))
            .padding(
                horizontal = BallropDimens.Space16,
                vertical = BallropDimens.Space14
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BallropDimens.Space6)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = BallropColors.TextMuted,
            textAlign = TextAlign.Center
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = BallropColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ContinueGameButton(
    checkpointStartLevel: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(BallropDimens.Radius20),
        colors = ButtonDefaults.buttonColors(
            containerColor = BallropColors.Success,
            contentColor = BallropColors.White
        )
    ) {
        Text(
            text = "Devam Et • Level $checkpointStartLevel",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StartGameButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(BallropDimens.Radius20),
        colors = ButtonDefaults.buttonColors(
            containerColor = BallropColors.BasketBlue,
            contentColor = BallropColors.White
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuickInfoCard(
    canContinue: Boolean,
    checkpointStartLevel: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BallropDimens.Radius18))
            .background(BallropColors.Surface.copy(alpha = 0.92f))
            .padding(BallropDimens.Space16),
        verticalArrangement = Arrangement.spacedBy(BallropDimens.Space8)
    ) {
        Text(
            text = if (canContinue) "Checkpoint Sistemi" else "Nasıl Oynanır?",
            style = MaterialTheme.typography.titleMedium,
            color = BallropColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (canContinue) {
                "Her 3 levelde bir checkpoint açılır. Şu an devam edersen Level $checkpointStartLevel noktasından başlarsın."
            } else {
                "• Düşen topları parmağınla sürükle\n• Mavi topu mavi sepete, kırmızı topu kırmızı sepete bırak\n• Yanlış yerleştirme ve kaçırma puan düşürür\n• Her 3 levelde bir checkpoint açılır"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = BallropColors.TextSecondary
        )
    }
}
