package com.smartledger.aldaftar

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Release gate for obvious source-level build blockers, secret leaks, and forbidden legacy paths. */
class BuildHygieneContractTest {
    private fun projectRoot(): File {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        return if (File(cwd, "app/src/main").exists()) cwd else cwd.parentFile ?: cwd
    }

    @Test
    fun productionSourceContainsNoLegacyRoomMigrationsOrAlwaysVisibleIme() {
        val root = projectRoot()
        val sourceRoot = sequenceOf(
            File(root, "app/src/main/java"),
            File(root, "src/main/java"),
            File(root, "app/src/main/kotlin"),
            File(root, "src/main/kotlin")
        ).firstOrNull { it.exists() } ?: error("Production source root not found")

        sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val source = file.readText()
                assertFalse("Legacy Room migration found in ${file.path}", source.contains("MIGRATION_1_2"))
                assertFalse("Legacy Room migration found in ${file.path}", source.contains("MIGRATION_2_3"))
                assertFalse("Legacy Room migration found in ${file.path}", source.contains("MIGRATION_3_4"))
                assertFalse("Always-visible IME policy found in ${file.path}", source.contains("SOFT_INPUT_STATE_ALWAYS_VISIBLE"))
            }
    }

    @Test
    fun buildAndProductionSourcesContainNoPrivateKeysOrAdminSecrets() {
        val root = projectRoot()
        val forbiddenTokens = listOf(
            "SMARTLEDGER_" + "LOCAL_LICENSE_PRIVATE",
            "SMARTLEDGER_" + "ADMIN_SECRET",
            "-----BEGIN " + "PRIVATE KEY-----",
            "-----BEGIN EC " + "PRIVATE KEY-----",
            "-----BEGIN RSA " + "PRIVATE KEY-----",
            "Mansour" + "#2100"
        )

        val filesToScan = buildList {
            listOf(File(root, "app/build.gradle.kts"), File(root, "build.gradle.kts"))
                .filter { it.exists() }
                .forEach { add(it) }
            File(root, "app/src/main")
                .takeIf { it.exists() }
                ?.walkTopDown()
                ?.filter { it.isFile }
                ?.forEach { add(it) }
        }

        assertTrue("Expected production files to scan", filesToScan.isNotEmpty())
        filesToScan.forEach { file ->
            val content = file.readText()
            forbiddenTokens.forEach { token ->
                assertFalse("Forbidden secret or private key marker found in ${file.path}", content.contains(token))
            }
        }

        val ecPublicAsset = File(root, "app/src/main/assets/license_local_ec_public.pem")
        assertTrue("Missing ECDSA public key asset: ${ecPublicAsset.path}", ecPublicAsset.exists())
        val pemText = ecPublicAsset.readText().trim()
        assertTrue(
            "ECDSA asset must be a PEM public key",
            pemText.contains("-----BEGIN PUBLIC KEY-----") && pemText.contains("-----END PUBLIC KEY-----")
        )
        val firstBlock = pemText
            .substringAfter("-----BEGIN PUBLIC KEY-----")
            .substringBefore("-----END PUBLIC KEY-----")
            .replace(Regex("\\s+"), "")
        val decoded = java.util.Base64.getDecoder().decode(firstBlock)
        val parsed = java.security.KeyFactory.getInstance("EC")
            .generatePublic(java.security.spec.X509EncodedKeySpec(decoded))
        assertTrue("ECDSA asset must be an ECPublicKey", parsed is java.security.interfaces.ECPublicKey)
    }
}
