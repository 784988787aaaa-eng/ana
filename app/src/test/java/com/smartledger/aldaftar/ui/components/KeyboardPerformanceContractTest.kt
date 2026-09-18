package com.smartledger.aldaftar.ui.components

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Cold/open/input performance guardrails. These are source-level release
 * contracts; actual frame/IME latency must still be measured on devices.
 */
class KeyboardPerformanceContractTest {
    private fun source(path: String): String = File("app/src/main/java/$path").readText()

    @Test
    fun keyboardFirstAttemptIsImmediateAfterSingleAttachmentFrame() {
        val src = source("com/smartledger/aldaftar/ui/components/KeyboardFocus.kt")
        val frame = src.indexOf("awaitFrame()")
        val focus = src.indexOf("focusRequester.requestFocus()", frame)
        val show = src.indexOf("keyboardController?.show()", focus)
        assertTrue("IME focus must happen immediately after attachment frame", frame >= 0 && focus > frame && show > focus)
        assertTrue("Fallback retries must remain tightly bounded", src.contains("attempts.coerceIn(1, 3)"))
        assertTrue("Fallback delay must remain frame-scale", src.contains("delayMs.coerceIn(8L, 32L)"))
    }

    @Test
    fun inputSurfacesMustNotUseBlockingSleepOrRunBlocking() {
        val root = File("app/src/main/java/com/smartledger/aldaftar/ui")
        val offenders = root.walkTopDown().filter { it.isFile && it.extension == "kt" }.flatMap { file ->
            file.readLines().asSequence().withIndex().filter { (_, line) ->
                line.contains("Thread.sleep(") || line.contains("runBlocking(")
            }.map { (i, line) -> "${file.path}:${i + 1}:$line" }
        }.toList()
        assertTrue("Blocking UI calls found: $offenders", offenders.isEmpty())
    }

    @Test
    fun competitiveDialogMotionLeavesInputInteractiveDuringShortAnimation() {
        val tokens = source("com/smartledger/aldaftar/ui/theme/DesignTokens.kt")
        val enter = Regex("DURATION_DIALOG_ENTER\\s*=\\s*(\\d+)").find(tokens)?.groupValues?.get(1)?.toInt()
        val exit = Regex("DURATION_DIALOG_EXIT\\s*=\\s*(\\d+)").find(tokens)?.groupValues?.get(1)?.toInt()
        assertTrue("Enter animation must stay short", enter != null && enter <= 180)
        assertTrue("Exit animation must stay short", exit != null && exit <= 140)
    }

    @Test
    fun mainActivityKeepsSplashOnlyForRequiredSettingsLoad() {
        val src = File("app/src/main/java/com/smartledger/aldaftar/MainActivity.kt").readText()
        assertTrue(src.contains("setKeepOnScreenCondition { !financeViewModel.isSettingsLoaded.value }"))
        assertTrue("No arbitrary startup delay is allowed", !src.contains("delay(") && !src.contains("Thread.sleep("))
    }
}
