package com.smartledger.aldaftar.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class HabayebCategoryDataRepositoryTest {
    @Test fun nullCategoryMapsToSingleInternalGlobalScope() {
        assertEquals(HabayebCategoryDataRepository.GLOBAL_SCOPE_CATEGORY_ID, HabayebCategoryDataRepository.scopeCategoryId(null))
    }

    @Test fun categoryScopeIsPreserved() {
        assertEquals(17, HabayebCategoryDataRepository.scopeCategoryId(17))
    }
}
