package com.smartledger.aldaftar.data.backup

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DailyBackupContractTest {
    private fun source(path: String): String = File("src/main/java/$path").readText()
    @Test fun dailyBackupIsUniqueAndRunsThroughCoordinator() {
        val scheduler = source("com/smartledger/aldaftar/data/backup/BackupScheduler.kt")
        val coordinator = source("com/smartledger/aldaftar/data/backup/AutomaticBackupCoordinator.kt")
        assertTrue(scheduler.contains("enqueueUniquePeriodicWork"))
        assertTrue(scheduler.contains("WORK_NAME"))
        assertTrue(coordinator.contains("engine.createAutomatic()"))
    }
    @Test fun cloudFailureMustNotDiscardTheLocalBackup() {
        assertTrue(source("com/smartledger/aldaftar/data/backup/AutomaticBackupCoordinator.kt").contains("Result.LocalOnly(local, it, publicUri)"))
    }
    @Test fun concurrentDailyRunsAreSerialized() {
        val src = source("com/smartledger/aldaftar/data/backup/AutomaticBackupCoordinator.kt")
        assertTrue(src.contains("AtomicBoolean(false)"))
        assertTrue(src.contains("Result.AlreadyRunning"))
    }
    @Test fun dailyBackupIsLocalFirstThenCloud() {
        val src = source("com/smartledger/aldaftar/data/backup/AutomaticBackupCoordinator.kt")
        val local = src.indexOf("engine.createAutomatic()")
        val cloud = src.indexOf("cloud.upload(local, local.name)")
        assertTrue(local >= 0 && cloud > local)
    }
}
