package com.smartledger.aldaftar.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartledger.aldaftar.ui.theme.MizanAnimationTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Common animated dialog wrapper implementing Mizan Motion Tokens:
 * Enter: 160ms (Fade In + Subtle Scale 0.98 -> 1.0)
 * Exit: 120ms (Fade Out + Subtle Scale 1.0 -> 0.98)
 * Ensures exit animation completes gracefully before dismissing composition.
 */
@Composable
fun MizanAnimatedDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = true
    ),
    content: @Composable (dismissWithAnimation: () -> Unit) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val dismissWithAnimation: () -> Unit = {
        hideKeyboardAndClearFocus(focusManager, keyboardController)
        if (!isDismissing) {
            isDismissing = true
            isVisible = false
            scope.launch {
                delay(MizanAnimationTokens.DURATION_DIALOG_EXIT.toLong())
                onDismissRequest()
            }
        }
    }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    DisposableEffect(Unit) {
        onDispose {
            hideKeyboardAndClearFocus(focusManager, keyboardController)
        }
    }

    Dialog(
        onDismissRequest = {
            dismissWithAnimation()
        },
        properties = properties
    ) {
        // A Compose Dialog owns a separate Android Window. Configure it
        // explicitly so IME resize is deterministic.
        ConfigureDialogImeWindow()

        BackHandler(enabled = true) {
            dismissWithAnimation()
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = MizanAnimationTokens.DURATION_DIALOG_ENTER,
                    easing = LinearOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = 0.98f,
                animationSpec = tween(
                    durationMillis = MizanAnimationTokens.DURATION_DIALOG_ENTER,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = MizanAnimationTokens.DURATION_DIALOG_EXIT,
                    easing = FastOutLinearInEasing
                )
            ) + scaleOut(
                targetScale = 0.98f,
                animationSpec = tween(
                    durationMillis = MizanAnimationTokens.DURATION_DIALOG_EXIT,
                    easing = FastOutLinearInEasing
                )
            ),
            modifier = modifier
        ) {
            content(dismissWithAnimation)
        }
    }
}
