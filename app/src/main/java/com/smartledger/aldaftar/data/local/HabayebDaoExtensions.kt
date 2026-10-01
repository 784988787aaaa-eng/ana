package com.smartledger.aldaftar.data.local

/**
 * Clears every pinned-customer row through the DAO's existing primitives.
 *
 * Callers that need this operation together with other database mutations must
 * invoke it inside their Room transaction boundary (as the current backup/reset
 * callers do), so the multi-row delete remains atomic with the surrounding work.
 */
suspend fun HabayebDao.clearAllPins() {
    val pins = getAllPinsDirect()
    for (pin in pins) {
        deletePinnedCustomer(
            scope = pin.scopeCategoryId,
            customerId = pin.customerId
        )
    }
}
