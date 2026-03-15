package com.ballrop.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ballrop.app.core.design.BallropColors
import com.ballrop.app.core.design.BallropDimens

@Composable
fun HomeScreen(
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BallropColors.BackgroundPrimary)
            .padding(BallropDimens.Space24)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BallropDimens.Space20)
        ) {
            Text(
                text = "BALLROP",
                style = MaterialTheme.typography.headlineLarge,
                color = BallropColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Renkli topları doğru sepetlere sürükle.",
                style = MaterialTheme.typography.bodyLarge,
                color = BallropColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .widthIn(min = 220.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(BallropDimens.Radius16)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BallropColors.BasketBlue,
                    contentColor = BallropColors.White
                )
            ) {
                Text(
                    text = "Oyuna Başla",
                    modifier = Modifier.padding(vertical = BallropDimens.Space8),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}