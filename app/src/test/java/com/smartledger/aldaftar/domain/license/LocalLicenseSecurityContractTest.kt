package com.smartledger.aldaftar.domain.license

import android.content.Context
import android.provider.Settings
import android.util.Base64
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.backup.BackupEngine
import com.smartledger.aldaftar.data.license.LicenseRepository
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.platform.license.DeviceIdentity
import com.smartledger.aldaftar.platform.license.LicenseCrypto
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.math.BigDecimal
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec

@RunWith(RobolectricTestRunner::class)
class LocalLicenseSecurityContractTest {
    private lateinit var context: Context
    private lateinit var ecKeyPair: KeyPair
    private lateinit var crypto: LicenseCrypto
    private lateinit var repository: LicenseRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        Settings.Secure.putString(context.contentResolver, Settings.Secure.ANDROID_ID, "a1b2c3d4e5f60708")
        ecKeyPair = generateP256KeyPair()
        crypto = LicenseCrypto(context = context, localEcPublicKeyOverride = ecKeyPair.public)
        repository = LicenseRepository(context = context, crypto = crypto)
    }

    private fun generateP256KeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC")
        generator.initialize(ECGenParameterSpec("secp256r1"))
        return generator.generateKeyPair()
    }

    private fun b64UrlNoPad(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

    private fun signSlk1Token(
        payload: JSONObject,
        keyPair: KeyPair = ecKeyPair,
        useRawP1363Signature: Boolean = true
    ): String {
        val encodedPayload = b64UrlNoPad(payload.toString().toByteArray(Charsets.UTF_8))
        val signingInput = "${LicenseCrypto.SLK1_PREFIX}.$encodedPayload"
        val signer = Signature.getInstance("SHA256withECDSA")
        signer.initSign(keyPair.private)
        signer.update(signingInput.toByteArray(Charsets.UTF_8))
        val derSig = signer.sign()
        val finalSig = if (useRawP1363Signature) derToP1363(derSig) else derSig
        return "$signingInput.${b64UrlNoPad(finalSig)}"
    }

    private fun derToP1363(der: ByteArray): ByteArray {
        require(der[0] == 0x30.toByte())
        var offset = 2
        if ((der[1].toInt() and 0x80) != 0) {
            offset = 2 + (der[1].toInt() and 0x7F)
        }
        require(der[offset] == 0x02.toByte())
        val rLen = der[offset + 1].toInt() and 0xFF
        val rStart = offset + 2
        val rBytes = der.copyOfRange(rStart, rStart + rLen)

        val sOffset = rStart + rLen
        require(der[sOffset] == 0x02.toByte())
        val sLen = der[sOffset + 1].toInt() and 0xFF
        val sStart = sOffset + 2
        val sBytes = der.copyOfRange(sStart, sStart + sLen)

        val out = ByteArray(64)
        copyFixed32(rBytes, out, 0)
        copyFixed32(sBytes, out, 32)
        return out
    }

    private fun copyFixed32(src: ByteArray, dst: ByteArray, dstOffset: Int) {
        val clean = if (src.size > 32 && src[0].toInt() == 0) src.copyOfRange(src.size - 32, src.size) else src
        val startInDst = dstOffset + (32 - clean.size)
        System.arraycopy(clean, 0, dst, startInDst, clean.size)
    }

    private fun buildPayload(
        deviceCode: String = repository.deviceCode(),
        plan: String = "LIFETIME",
        iat: Long = 1_750_000_000_000L,
        trialEndsAt: Long? = null,
        licenseId: String = "LIC-TEST-001"
    ): JSONObject {
        return JSONObject()
            .put("v", 1)
            .put("kid", LicenseCrypto.KID_LOCAL2)
            .put("product", LicenseCrypto.PRODUCT_SMARTLEDGER)
            .put("type", "LOCAL")
            .put("licenseId", licenseId)
            .put("deviceCode", deviceCode)
            .put("plan", plan)
            .put("iat", iat)
            .apply {
                if (trialEndsAt != null) {
                    put("trialEndsAt", trialEndsAt)
                }
            }
    }

    @Test
    fun validSlk1EcdsaLifetimeLicenseActivatesAndAllowsOver100Operations() = runTest {
        val now = 1_750_000_005_000L
        val rawToken = signSlk1Token(buildPayload(), useRawP1363Signature = true)
        assertTrue("Compact SLK1 token should stay short (<360 chars), was ${rawToken.length}", rawToken.length < 360)

        // Wrap with WhatsApp quotes and invisible bidi marks to test sanitization
        val pastedFromWhatsapp = "\u200F«كود التفعيل:\n$rawToken\n»\u200E"
        val snap = repository.applySignedToken(pastedFromWhatsapp, now = now)

        assertEquals(LicenseType.LOCAL, snap.type)
        assertEquals(LicensePlan.LIFETIME, snap.plan)
        assertEquals(LicenseStatus.ACTIVE, snap.status)
        assertTrue(snap.isLifetime)
        assertTrue(snap.isPaid)
        assertTrue(repository.isEligibleToCreate(currentUsed = 150))

        // Verify DER ECDSA signatures are also accepted
        val derToken = signSlk1Token(buildPayload(licenseId = "LIC-DER-002"), useRawP1363Signature = false)
        assertEquals(LicenseStatus.ACTIVE, repository.applySignedToken(derToken, now = now).status)

        val authorized = repository.runAuthorizedCreation(currentUsed = { 120 }, slots = 1) { "created_ok" }
        assertEquals("created_ok", authorized)
    }

    @Test
    fun tamperedPayloadOrSignatureIsRejectedAndPreservesPriorValidLicense() {
        val now = 1_750_000_005_000L
        val validToken = signSlk1Token(buildPayload())
        val active = repository.applySignedToken(validToken, now = now)
        assertEquals(LicenseStatus.ACTIVE, active.status)

        val parts = validToken.split('.').toMutableList()

        // 1-byte tamper in payload
        val tamperedPayloadChars = parts[1].toCharArray()
        tamperedPayloadChars[4] = if (tamperedPayloadChars[4] == 'A') 'B' else 'A'
        val badPayloadToken = "${parts[0]}.${String(tamperedPayloadChars)}.${parts[2]}"

        // 1-byte tamper in signature
        val tamperedSigChars = parts[2].toCharArray()
        tamperedSigChars[10] = if (tamperedSigChars[10] == 'A') 'B' else 'A'
        val badSigToken = "${parts[0]}.${parts[1]}.${String(tamperedSigChars)}"

        // Unsigned alg=none JWT attempt
        val algNoneJwt = "${b64UrlNoPad("""{"alg":"none","kid":"LOCAL1"}""".toByteArray())}.${parts[1]}."

        // Different EC keypair
        val rogueToken = signSlk1Token(buildPayload(), keyPair = generateP256KeyPair())

        listOf(badPayloadToken, badSigToken, algNoneJwt, rogueToken, "SLK1.incomplete").forEach { bad ->
            try {
                repository.applySignedToken(bad, now = now)
                fail("Expected tampered/invalid token to be rejected: $bad")
            } catch (_: Exception) {
                // Expected
            }
        }

        val currentAfterAttacks = repository.snapshot(now = now)
        assertEquals(LicenseStatus.ACTIVE, currentAfterAttacks.status)
        assertTrue(currentAfterAttacks.isPaid)
        assertTrue(currentAfterAttacks.isLifetime)
    }

    @Test
    fun tokenBoundToDifferentDeviceCodeIsRejected() {
        val otherDigest = ByteArray(32) { idx -> (idx * 17 + 5).toByte() }
        val otherDeviceCode = DeviceIdentity.formatDeviceCodeFromDigest(otherDigest)
        assertNotEquals(repository.deviceCode(), otherDeviceCode)
        assertTrue(DeviceIdentity.isValidDeviceCode(otherDeviceCode))

        val foreignToken = signSlk1Token(buildPayload(deviceCode = otherDeviceCode))
        try {
            repository.applySignedToken(foreignToken, now = 1_750_000_005_000L)
            fail("Expected foreign device token to fail")
        } catch (ex: IllegalArgumentException) {
            assertTrue(ex.message.orEmpty().contains("لا يخص هذا الجهاز"))
        }

        assertFalse(repository.snapshot(now = 1_750_000_005_000L).isPaid)
    }

    @Test
    fun localTrialLicenseTracksActiveExpiryAndClockRollback() {
        val issuedAt = System.currentTimeMillis() - 60_000L
        val endsAt = issuedAt + 7L * 24 * 60 * 60 * 1000L // +7 days
        val trialToken = signSlk1Token(buildPayload(plan = "TRIAL", iat = issuedAt, trialEndsAt = endsAt))

        // Active right after issuance
        val activeSnap = repository.applySignedToken(trialToken, now = issuedAt + 1_000L)
        assertEquals(LicenseType.LOCAL, activeSnap.type)
        assertEquals(LicensePlan.TRIAL, activeSnap.plan)
        assertEquals(LicenseStatus.ACTIVE, activeSnap.status)
        assertFalse(activeSnap.isLifetime)
        assertTrue(activeSnap.isTrialActive)
        assertTrue(activeSnap.isPaid)
        assertEquals(7, activeSnap.remainingDays)

        // Advance clock to day 3 (still active, updates lastSeenAt)
        val day3 = issuedAt + 3L * 24 * 60 * 60 * 1000L
        val day3Snap = repository.snapshot(now = day3)
        assertEquals(LicenseStatus.ACTIVE, day3Snap.status)
        assertEquals(4, day3Snap.remainingDays)

        // Clock rolled back by 1 day (< lastSeenAt - 5 min) -> VERIFICATION_REQUIRED + CLOCK_ROLLED_BACK
        val rolledBackSnap = repository.snapshot(now = day3 - 24L * 60 * 60 * 1000L)
        assertEquals(LicenseStatus.VERIFICATION_REQUIRED, rolledBackSnap.status)
        assertEquals(RevocationReason.CLOCK_ROLLED_BACK, rolledBackSnap.revocationReason)
        assertFalse(rolledBackSnap.isPaid)

        // Expired after trialEndsAt
        val expiredSnap = repository.snapshot(now = endsAt + 1_000L)
        assertEquals(LicenseStatus.TRIAL_EXPIRED, expiredSnap.status)
        assertEquals(RevocationReason.TRIAL_EXPIRED, expiredSnap.revocationReason)
        assertFalse(expiredSnap.isPaid)
        assertTrue(expiredSnap.isTrialExpired)
    }

    @Test
    fun deviceCodeIsDeterministicAcrossRestartsAndDetectsCheckCharacterTypos() {
        val id1 = DeviceIdentity(context)
        val code1 = id1.deviceCode()
        val id2 = DeviceIdentity(context)
        val code2 = id2.deviceCode()

        assertEquals(code1, code2)
        assertTrue(code1.matches(Regex("^SLD-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$")))
        assertTrue(DeviceIdentity.isValidDeviceCode(code1))
        assertTrue(DeviceIdentity.isValidDeviceCode("  $code1  ".lowercase()))

        // Flip any character inside the payload or check character -> checksum detects typo
        val chars = code1.toCharArray()
        val idxToMutate = chars.lastIndex
        chars[idxToMutate] = if (chars[idxToMutate] == 'A') 'B' else 'A'
        assertFalse(DeviceIdentity.isValidDeviceCode(String(chars)))

        val payloadIdx = 4
        val mutatedPayload = code1.toCharArray()
        mutatedPayload[payloadIdx] = if (mutatedPayload[payloadIdx] == 'A') 'B' else 'A'
        assertFalse(DeviceIdentity.isValidDeviceCode(String(mutatedPayload)))

        // Changing ANDROID_ID to known emulator broken value uses persistent fallback seed deterministically
        Settings.Secure.putString(context.contentResolver, Settings.Secure.ANDROID_ID, "9774d56d682e549c")
        val fallback1 = DeviceIdentity(context).deviceCode()
        val fallback2 = DeviceIdentity(context).deviceCode()
        assertEquals(fallback1, fallback2)
        assertTrue(DeviceIdentity.isValidDeviceCode(fallback1))
        assertNotEquals(code1, fallback1)
    }

    @Test
    fun localLicenseSurvivesBackupRestoreRoundTrip() = runTest {
        val now = 1_750_000_005_000L
        val validToken = signSlk1Token(buildPayload())
        val initial = repository.applySignedToken(validToken, now = now)
        assertEquals(LicenseStatus.ACTIVE, initial.status)

        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val backupFile = File.createTempFile("smartledger-license-backup-", ".sna", context.cacheDir)
        try {
            db.settingsDao().insertOrUpdateSettings(AppSettings(id = 1, currencySymbol = "ر.س"))
            db.habayebDao().insertCustomer(HabayebCustomer("cust-1", "عميل اختبار", "", "", 1L))
            db.habayebDao().insertTransaction(
                HabayebTransaction(
                    id = "tx-1",
                    customerId = "cust-1",
                    type = "OWED_BY_THEM",
                    amount = BigDecimal("250.00"),
                    timestamp = 2L,
                    description = "فاتورة"
                )
            )

            val engine = BackupEngine(context, db)
            engine.create(backupFile)

            db.habayebDao().clearAllTransactions()
            engine.restore(backupFile)

            assertNotNull(db.habayebDao().getTransactionById("tx-1"))

            // Fresh repository instance reading persisted EncryptedSharedPreferences after restore
            val freshRepo = LicenseRepository(context = context, crypto = crypto)
            val afterRestore = freshRepo.snapshot(now = now + 5_000L)
            assertEquals(LicenseType.LOCAL, afterRestore.type)
            assertEquals(LicenseStatus.ACTIVE, afterRestore.status)
            assertTrue(afterRestore.isLifetime)
            assertTrue(afterRestore.isPaid)
        } finally {
            backupFile.delete()
            db.close()
        }
    }

    @Test
    fun keyRotationSupportsMultipleKidsAndRejectsUnknownOrTraversalKid() {
        val now = 1_750_000_005_000L
        val keyV2 = generateP256KeyPair()
        val keyV3 = generateP256KeyPair()
        val rotatedCrypto = LicenseCrypto(
            context = context,
            localEcKeyringOverride = mapOf(
                "LOCAL2" to keyV2.public,
                "LOCAL3" to keyV3.public
            )
        )
        val rotatedRepo = LicenseRepository(context = context, crypto = rotatedCrypto)

        val tokenV2 = signSlk1Token(buildPayload(licenseId = "LIC-V2").put("kid", "LOCAL2"), keyPair = keyV2)
        assertEquals(LicenseStatus.ACTIVE, rotatedRepo.applySignedToken(tokenV2, now = now).status)

        val tokenV3 = signSlk1Token(buildPayload(licenseId = "LIC-V3").put("kid", "LOCAL3"), keyPair = keyV3)
        assertEquals(LicenseStatus.ACTIVE, rotatedRepo.applySignedToken(tokenV3, now = now).status)

        listOf("LOCAL99", "LOCAL1", "../LOCAL2").forEach { badKid ->
            val badToken = signSlk1Token(buildPayload(licenseId = "LIC-BAD").put("kid", badKid), keyPair = keyV3)
            try {
                rotatedRepo.applySignedToken(badToken, now = now)
                fail("Expected bad/unknown kid to be rejected: $badKid")
            } catch (_: Exception) {
                // Expected
            }
        }
    }
}
