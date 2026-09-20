package com.smartledger.aldaftar.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.smartledger.aldaftar.ui.screens.habayeb.components.AddTransactionFormFields
import org.junit.Rule
import org.junit.Test

/**
 * Device-side Compose contract for the shared transaction editor.
 * It validates the real semantics tree rather than source text alone.
 */
class KeyboardImeRuntimeContractTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun transactionImeNextMovesToDescriptionAndDoneInvokesSave() {
        var saved = false

        composeRule.setContent {
            var amount by remember { mutableStateOf(TextFieldValue("")) }
            var description by remember { mutableStateOf(TextFieldValue("")) }
            val amountFocus = remember { FocusRequester() }
            val descriptionFocus = remember { FocusRequester() }

            MaterialTheme {
                AddTransactionFormFields(
                    amountTfv = amount,
                    onAmountChange = { amount = it },
                    descTfv = description,
                    onDescChange = { description = it },
                    selectedTransactionCurrency = "YER",
                    dateMillis = 0L,
                    dynamicThemeColor = Color.Black,
                    amountFocusRequester = amountFocus,
                    descFocusRequester = descriptionFocus,
                    onOpenCalculator = {},
                    onOpenDatePicker = {},
                    onDone = { saved = true }
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(2)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performClick()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performImeAction()
        composeRule.onAllNodes(hasSetTextAction()).onLast().assertIsFocused()

        composeRule.onAllNodes(hasSetTextAction()).onLast().performImeAction()
        composeRule.runOnIdle { check(saved) { "IME Done must invoke the transaction save callback" } }
    }

    @Test
    fun transactionFormRequestsInitialFocusWithoutUserTap() {
        composeRule.setContent {
            var amount by remember { mutableStateOf(TextFieldValue("")) }
            var description by remember { mutableStateOf(TextFieldValue("")) }
            val amountFocus = remember { FocusRequester() }
            val descriptionFocus = remember { FocusRequester() }

            MaterialTheme {
                AddTransactionFormFields(
                    amountTfv = amount,
                    onAmountChange = { amount = it },
                    descTfv = description,
                    onDescChange = { description = it },
                    selectedTransactionCurrency = "YER",
                    dateMillis = 0L,
                    dynamicThemeColor = Color.Black,
                    amountFocusRequester = amountFocus,
                    descFocusRequester = descriptionFocus,
                    onOpenCalculator = {},
                    onOpenDatePicker = {},
                    onDone = {}
                )
                RequestFocusAndShowKeyboard(
                    focusRequester = amountFocus,
                    autoShow = true
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("transaction_amount_input").assertIsFocused()
    }
}
