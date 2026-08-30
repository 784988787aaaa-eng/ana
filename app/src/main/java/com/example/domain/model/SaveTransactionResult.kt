package com.example.domain.model

/**
 * Result model representing the outcome of an atomic transaction or customer save mutation.
 * Guarantees a single source of truth for save operations, preventing race conditions
 * and eliminating fake success feedback when trial limits are reached.
 */
sealed interface SaveTransactionResult {
    /**
     * Save succeeded and was safely persisted into Room database.
     * @param transactionId The ID of the persisted record.
     */
    data class Success(val transactionId: String) : SaveTransactionResult

    /**
     * Save was rejected because the trial limit (100 free transactions) has been reached
     * and the device is not activated.
     */
    object TrialExpired : SaveTransactionResult

    /**
     * Save failed due to an unexpected error or validation failure.
     * @param message Optional error description.
     */
    data class Error(val message: String? = null) : SaveTransactionResult
}
