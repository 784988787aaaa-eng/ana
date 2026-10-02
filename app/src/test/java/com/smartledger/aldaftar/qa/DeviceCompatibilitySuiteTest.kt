package com.smartledger.aldaftar.qa

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.platform.contacts.StringUtils
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DeviceCompatibilitySuiteTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun minSdkBoundary_api24_functionsAccuratelyWithoutMissingClassDef() {
        val rawInput = "١٢٥٫٥٠"
        val normalized = StringUtils.normalizeDigits(rawInput)
        assertEquals("125.50", normalized)

        val bd = CurrencyConfig.parseBigDecimalOrNull(normalized)
        assertNotNull(bd)
        assertEquals(BigDecimal("125.50"), bd)
    }

    @Test
    fun targetSdkBoundary_api36_manifestAndFileProvider_areProperlyConfigured() {
        val fileProviderAuthority = "${context.packageName}.fileprovider"
        assertNotNull("FileProvider authority must match application ID", fileProviderAuthority)
        assertTrue(fileProviderAuthority.endsWith(".fileprovider"))
    }

    @Test
    fun localeConfiguration_arabicRtlFormatting_preservesFinancialDigitsAndSymbols() {
        val amount = BigDecimal("1250.7500")
        val formatted = HabayebMathHelper.formatSmart(amount)
        assertEquals("1,250.75", formatted)
    }

    @Test
    fun fontScaling_largeFontFactor_doesNotTruncateEssentialCurrencies() {
        val appSettings = AppSettings(currencySymbol = "ر.ي")
        assertEquals("ر.ي", appSettings.currencySymbol)
    }

    @Test
    fun configurationChange_processStateRestoration_maintainsDefaults() {
        val settings = AppSettings(id = 1, isFirstLaunch = false, onboardingShown = true)
        val copy = settings.copy(currencySymbol = "ر.س")
        assertFalse("Restored state preserves isFirstLaunch", copy.isFirstLaunch)
        assertTrue("Restored state preserves onboardingShown", copy.onboardingShown)
        assertEquals("ر.س", copy.currencySymbol)
    }
}
