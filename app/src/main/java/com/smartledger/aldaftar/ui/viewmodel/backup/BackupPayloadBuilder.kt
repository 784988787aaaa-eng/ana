package com.smartledger.aldaftar.ui.viewmodel.backup

import android.content.Context
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.repository.FinanceRepository
import com.smartledger.aldaftar.data.serialization.BackupPayloadSerializer
import com.smartledger.aldaftar.data.serialization.MzdBackupSerializer
import kotlinx.coroutines.flow.first

object BackupPayloadBuilder {
    suspend fun buildBackupJson(
        repository: FinanceRepository,
        isMzd: Boolean,
        context: Context
    ): String {
        val currentSettings = repository.settingsFlow.first() ?: AppSettings()
        val commitments = repository.commitmentsFlow.first()
        val transactions = repository.transactionsFlow.first()
        val habayebCusts = repository.getAllCustomersDirect()
        val habayebTxs = repository.getAllTransactionsDirect()
        val deletedItems = repository.deletedItemsFlow.first()
        return if (isMzd) {
            MzdBackupSerializer.exportBackupToJson(currentSettings, commitments, transactions, habayebCusts, habayebTxs, deletedItems, context)
        } else {
            BackupPayloadSerializer.exportBackupToJson(currentSettings, commitments, transactions, habayebCusts, habayebTxs, deletedItems, context)
        }
    }
}
