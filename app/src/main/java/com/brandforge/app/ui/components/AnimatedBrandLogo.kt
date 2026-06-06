package com.brandforge.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.brandforge.app.R
import com.brandforge.app.ui.theme.ForgeColor

@Composable
fun AnimatedBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.brandforge_logo))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )
    Box(
        modifier = modifier
            .size(size)
            .border(1.dp, ForgeColor.Yellow)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        if (composition == null) {
            PixelTwinAvatar()
        } else {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(size),
            )
        }
    }
}
