package com.smartledger.aldaftar.ui.security

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.smartledger.aldaftar.ui.screens.security.lock.PasscodeKeypadContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SecurityKeypadRuntimeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun keypadRendersAllDigitsAndEmitsInputImmediately() {
        val entered = StringBuilder()

        composeRule.setContent {
            MaterialTheme {
                PasscodeKeypadContent(
                    enteredPasscode = entered.toString(),
                    isCheckingPasscode = false,
                    shakeOffsetPx = 0f,
                    isBiometricSupported = false,
                    onKeyPress = { entered.append(it) },
                    onDeleteClick = {
                        if (entered.isNotEmpty()) entered.deleteCharAt(entered.lastIndex)
                    },
                    onForgotClick = {},
                    onBiometricClick = {}
                )
            }
        }

        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach {
            composeRule.onNodeWithTag("keypad_btn_$it").assertExists()
        }

        composeRule.onNodeWithTag("keypad_btn_1").performClick()
        composeRule.onNodeWithTag("keypad_btn_2").performClick()
        composeRule.onNodeWithTag("keypad_btn_3").performClick()

        composeRule.runOnIdle {
            assertEquals("123", entered.toString())
        }
    }

    @Test
    fun keypadDeleteRemovesTheLastDigit() {
        val entered = StringBuilder("1234")

        composeRule.setContent {
            MaterialTheme {
                PasscodeKeypadContent(
                    enteredPasscode = entered.toString(),
                    isCheckingPasscode = false,
                    shakeOffsetPx = 0f,
                    isBiometricSupported = false,
                    onKeyPress = { entered.append(it) },
                    onDeleteClick = {
                        if (entered.isNotEmpty()) entered.deleteCharAt(entered.lastIndex)
                    },
                    onForgotClick = {},
                    onBiometricClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("keypad_btn_حذف").performClick()

        composeRule.runOnIdle {
            assertEquals("123", entered.toString())
        }
    }
}
