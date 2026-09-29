package com.smartledger.aldaftar.ui.root

import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import com.smartledger.aldaftar.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLaunchRuntimeTest {
    @Test
    fun mainActivityReachesResumedStateWithinCiSafetyBudget() {
        val startedAt = SystemClock.elapsedRealtime()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                check(!activity.isFinishing) { "MainActivity must remain active after launch" }
            }
        }
        val elapsedMs = SystemClock.elapsedRealtime() - startedAt
        println("MainActivity launch-to-resumed elapsedMs=$elapsedMs")
        assertTrue(
            "MainActivity launch exceeded the CI safety budget: ${elapsedMs}ms",
            elapsedMs < 15_000
        )
    }
}
