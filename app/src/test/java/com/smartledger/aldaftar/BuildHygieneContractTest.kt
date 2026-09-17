package com.smartledger.aldaftar

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

/** Release gate for obvious source-level build blockers and forbidden legacy paths. */
class BuildHygieneContractTest {
    @Test
    fun productionSourceContainsNoLegacyRoomMigrationsOrAlwaysVisibleIme() {
        val root = File(System.getProperty("user.dir"))
        val sourceRoot = sequenceOf(
            File(root, "app/src/main/java"),
            File(root, "app/src/main/kotlin")
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
}
