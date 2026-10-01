package com.smartledger.aldaftar.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HabayebCategoryDataRepositoryTest {
    @Test fun nullCategoryMapsToSingleInternalGlobalScope() {
        assertEquals(HabayebCategoryDataRepository.GLOBAL_SCOPE_CATEGORY_ID, HabayebCategoryDataRepository.scopeCategoryId(null))
    }

    @Test fun categoryScopeIsPreserved() {
        assertEquals(17, HabayebCategoryDataRepository.scopeCategoryId(17))
    }

    @Test fun deletingCategoryLinkedCustomerRemovesAndTrashesRecurringConfiguration() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        try {
            val category = CustomCategory(id = 7, name = "متابعة", tabType = "HABAYEB", iconEmoji = "•")
            val customer = HabayebCustomer("c7", "عميل 7", "", "", 7L, categoryId = category.id)
            val recurring = RecurringConfigEntity(
                id = "r7", originalTxId = "t7", customerId = customer.id, customerName = customer.name,
                amount = BigDecimal("100"), type = "OWED_BY_THEM", description = "متابعة", frequency = "DAILY",
                daysOfWeek = emptyList(), daysOfMonth = emptyList(), timeHour = 9, timeMinute = 0,
                startDateMillis = 7L, endDateMillis = 70L, lastExecutedTimestamp = 0L
            )
            db.customCategoryDao().insertCategory(category)
            db.habayebDao().insertCustomer(customer)
            db.recurringConfigDao().save(recurring)

            HabayebCategoryDataRepository(db, CategoryRepository(db.customCategoryDao()))
                .deleteCategory(category, deleteLinkedAccounts = true)

            assertEquals(0, db.habayebDao().getCustomerByIdDirect(customer.id)?.let { 1 } ?: 0)
            assertTrue(db.recurringConfigDao().byCustomer(customer.id).isEmpty())
            val trash = db.trashDao().getDeletedItemByIdDirect("bundle_${customer.id}")!!
            val restoredPayload = org.json.JSONObject(trash.jsonData)
            assertEquals(1, restoredPayload.getJSONArray("recurringConfigs").length())
        } finally {
            db.close()
        }
    }
}
