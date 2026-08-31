/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt
 * القطاع المعماري: ViewModels & UI State.
 *
 * الوصف المعماري:
 * مدير الحالة المركزي لعمليات المالية العامة؛ ينسق البيانات والأحداث والحسابات مع دورة حياة Compose.
 *
 * الرؤية التعليمية والبصرية:
 * تخيل شاشة الهاتف أثناء تفاعل المستخدم: يضغط على زر أو يغيّر قيمة،
 * فتتولد إشارة، ثم تُعالج في طبقة الحالة، ثم تتغير الحالة التي تقرأها
 * Compose لإعادة رسم الشاشة. هذا الملف يقع في تلك السلسلة ويجب قراءته
 * باعتباره عقداً بين «ما فعله المستخدم» و«ما تراه الشاشة».
 *
 * قاعدة الثبات البرمجي:
 * النص التنفيذي الأصلي محفوظ حرفياً بعد هذا الرأس. الإضافات هنا توثيقية
 * فقط ولا تستبدل أي تعليمة أو اسماً أو قيمة أو منطقاً تنفيذياً.
 */

// --- الفهرس التوثيقي للعناصر البرمجية ---
// السطر 50: class FinanceViewModel — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 53: private const val PREFS_TRASH — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 54: private const val KEY_TRASH_AUTO_CLEANUP_PERIOD — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 55: private const val CLEANUP_PERIOD_NEVER — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 56: private const val TRANSACTION_TYPE_EXPENSE — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 57: private const val PREFIX_HABAYEB — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 60: private val repository — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 62: private val _autoCleanupPeriod — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 63: val autoCleanupPeriod — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 65: private val _uiEventChannel — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 66: val uiEventFlow — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 68: private fun sendUiEvent — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 73: val database — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 75: val trashPrefs — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 79: private val navigationPrefs — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 81: val tabOrderState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 84: val defaultStartDestinationState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 87: fun saveTabOrder — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 93: fun saveDefaultStart — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 99: val isSettingsLoaded — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 101: val settingsState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 106: val commitmentsState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 109: val transactionsState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 112: val customCategoriesState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 115: val deletedItemsFlow — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 117: val totalTransactionsCount — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 124: private val sharedPrefs — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 126: fun hasShownOnboarding — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 130: fun markOnboardingShown — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 136: private val _searchQuery — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 137: val searchQuery — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 139: fun updateSearchQuery — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 143: val searchResultsState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 146: val normalizedQuery — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 153: fun calculateSumByType — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 154: val targetType — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 155: var sum — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 157: val txType — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 165: val totalCashState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 170: val dailyExpenseComparisonState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 172: val nowSec — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 173: val todayKey — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 174: val yesterdayKey — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 176: var todayExpenses — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 177: var yesterdayExpenses — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 180: val txType — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 182: val txDate — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 195: val ledgerUiState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 213: val monthlyLedgerState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 218: fun saveSettings — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 224: fun addTransaction — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 231: val id — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 232: val tx — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 244: fun permanentlyDeleteDeletedItem — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 254: fun permanentlyDeleteMultipleItems — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 264: fun restoreMultipleItems — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 267: val context — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 278: fun restoreDeletedItem — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 281: val context — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 292: fun restoreSingleTransactionFromBundle — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 295: val context — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 306: fun updateAutoCleanupPeriod — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 309: val context — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 310: val trashPrefs — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 319: val ageInMillis — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 321: val thresholdTime — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 322: val items — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 323: val expiredItems — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 335: fun cleanLedgerTrashItems — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 338: val systemHabayeb — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 339: val allItems — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 340: val nonHabayebItems — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 352: fun emptyTrash — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 355: val systemHabayeb — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 356: val allItems — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 357: val habayebItems — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 369: fun deleteTransaction — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 381: fun deleteTransactionById — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 384: val tx — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 396: fun deleteTransactionsBulk — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 399: val allTxs — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 400: val idSet — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 401: val toDelete — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 413: fun updateTransaction — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 424: fun saveCommitment — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 427: val count — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 428: val fc — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 437: fun updateCommitmentDirectly — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 448: fun reorderCommitment — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 451: val currentList — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 454: val targetIndex — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 455: val currentIndex — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 458: val item — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 461: val updatedList — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 473: fun deleteCommitment — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 476: val oldFc — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 488: fun saveCustomCategory — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 499: fun deleteCustomCategory — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 510: fun deleteAllData — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// --- نهاية الفهرس التوثيقي ---

package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.NavigationPreferences
import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.CustomCategory
import com.example.data.local.entities.DeletedItemEntity
import com.example.data.local.entities.FixedCommitment
import com.example.data.local.entities.TransactionDb
import com.example.data.repository.FinanceRepository
import com.example.domain.DateUtils
import com.example.domain.StringUtils
import com.example.domain.model.TransactionType
import com.example.ui.state.MainLedgerUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

import com.example.ui.viewmodel.ledger.DayLedger
import com.example.ui.viewmodel.ledger.MonthLedger
import com.example.ui.viewmodel.ledger.TrashRestoreHandler

typealias MonthLedger = com.example.ui.viewmodel.ledger.MonthLedger
typealias DayLedger = com.example.ui.viewmodel.ledger.DayLedger

/**
 * نموذج العرض المالي الرئيسي (FinanceViewModel)
 * مسؤول عن تنسيق حالة الواجهة (UI Orchestration) وربط تفاعلات المستخدم مع المستودع المالي
 * دون تخزين منطق محاسبي أو التعامل المباشر مع طبقة التخزين.
 */
class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_TRASH = "trash_prefs"
        private const val KEY_TRASH_AUTO_CLEANUP_PERIOD = "trash_auto_cleanup_period"
        private const val CLEANUP_PERIOD_NEVER = "never"
        private const val TRANSACTION_TYPE_EXPENSE = "EXPENSE"
        private const val PREFIX_HABAYEB = "habayeb_"
    }

    private val repository: FinanceRepository

    private val _autoCleanupPeriod = MutableStateFlow(CLEANUP_PERIOD_NEVER)
    val autoCleanupPeriod: StateFlow<String> = _autoCleanupPeriod.asStateFlow()

    private val _uiEventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    private fun sendUiEvent(event: UiEvent) {
        _uiEventChannel.trySend(event)
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database, application)
        val trashPrefs = application.getSharedPreferences(PREFS_TRASH, Context.MODE_PRIVATE)
        _autoCleanupPeriod.value = trashPrefs.getString(KEY_TRASH_AUTO_CLEANUP_PERIOD, CLEANUP_PERIOD_NEVER) ?: CLEANUP_PERIOD_NEVER
    }

    private val navigationPrefs = NavigationPreferences(application)

    val tabOrderState: StateFlow<String> = navigationPrefs.tabOrderFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NavigationPreferences.DEFAULT_ORDER)

    val defaultStartDestinationState: StateFlow<String> = navigationPrefs.defaultStartFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NavigationPreferences.DEFAULT_START)

    fun saveTabOrder(order: String) {
        viewModelScope.launch {
            navigationPrefs.saveTabOrder(order)
        }
    }

    fun saveDefaultStart(start: String) {
        viewModelScope.launch {
            navigationPrefs.saveDefaultStart(start)
        }
    }

    val isSettingsLoaded = MutableStateFlow(false)

    val settingsState: StateFlow<AppSettings> = repository.settingsFlow
        .onEach { isSettingsLoaded.value = true }
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val commitmentsState: StateFlow<List<FixedCommitment>> = repository.commitmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionsState: StateFlow<List<TransactionDb>> = repository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customCategoriesState: StateFlow<List<CustomCategory>> = repository.customCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedItemsFlow: Flow<List<DeletedItemEntity>> = repository.deletedItemsFlow

    val totalTransactionsCount: StateFlow<Int> = combine(
        repository.getTransactionsCountFlow(),
        repository.getHabayebTransactionsCountFlow()
    ) { mainCount, habayebCount ->
        mainCount + habayebCount
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val sharedPrefs = application.getSharedPreferences(FinanceConstants.PREFS_NAME, Context.MODE_PRIVATE)

    fun hasShownOnboarding(): Boolean {
        return sharedPrefs.getBoolean(FinanceConstants.KEY_ONBOARDING_SHOWN, false)
    }

    fun markOnboardingShown() {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPrefs.edit().putBoolean(FinanceConstants.KEY_ONBOARDING_SHOWN, true).apply()
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val searchResultsState: StateFlow<List<TransactionDb>> = combine(transactionsState, _searchQuery) { transactions, query ->
        if (query.isBlank()) emptyList()
        else {
            val normalizedQuery = StringUtils.normalizeArabic(query, getApplication<Application>())
            transactions.filter { tx ->
                StringUtils.normalizeArabic(tx.description, getApplication<Application>()).contains(normalizedQuery, ignoreCase = true)
            }.sortedByDescending { it.timestamp }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun calculateSumByType(transactions: List<TransactionDb>, type: String): BigDecimal {
        val targetType = TransactionType.fromValue(type)
        var sum = BigDecimal.ZERO
        for (tx in transactions) {
            val txType = TransactionType.fromValue(tx.type)
            if (txType == targetType || tx.type.equals(type, ignoreCase = true)) {
                sum = sum.add(tx.amount)
            }
        }
        return sum.setScale(2, RoundingMode.HALF_EVEN)
    }

    val totalCashState: StateFlow<BigDecimal> = repository.getTotalCashFlow()
        .map { it.setScale(2, RoundingMode.HALF_EVEN) }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val dailyExpenseComparisonState: StateFlow<Pair<BigDecimal, BigDecimal>> = transactionsState
        .map { txList ->
            val nowSec = System.currentTimeMillis() / 1000
            val todayKey = DateUtils.formatDateFull(nowSec)
            val yesterdayKey = DateUtils.formatDateFull(nowSec - 86400L)

            var todayExpenses = BigDecimal.ZERO
            var yesterdayExpenses = BigDecimal.ZERO

            for (tx in txList) {
                val txType = TransactionType.fromValue(tx.type)
                if (txType == TransactionType.EXPENSE || tx.type.equals(TRANSACTION_TYPE_EXPENSE, ignoreCase = true)) {
                    val txDate = DateUtils.formatDateFull(tx.timestamp)
                    if (txDate == todayKey) {
                        todayExpenses = todayExpenses.add(tx.amount)
                    } else if (txDate == yesterdayKey) {
                        yesterdayExpenses = yesterdayExpenses.add(tx.amount)
                    }
                }
            }
            Pair(todayExpenses, yesterdayExpenses)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(BigDecimal.ZERO, BigDecimal.ZERO))

    val ledgerUiState: StateFlow<MainLedgerUiState> = combine(
        searchResultsState,
        totalCashState,
        _searchQuery
    ) { txList, totalCash, query ->
        MainLedgerUiState(
            transactions = txList,
            totalCash = totalCash,
            isSearching = query.isNotBlank(),
            isLoading = false
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainLedgerUiState()
    )

    val monthlyLedgerState: StateFlow<List<MonthLedger>> = transactionsState
        .map { txList -> LedgerCalculator.computeMonthlyLedger(txList) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveSettings(settings: AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings)
        }
    }

    fun addTransaction(type: String, category: String, amount: BigDecimal, description: String, timestamp: Long = System.currentTimeMillis() / 1000, presetId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.isTrialExpiredDirect()) {
                sendUiEvent(UiEvent.ShowToast(R.string.licensing_dialog_desc, true))
                sendUiEvent(UiEvent.ShowActivationDialog)
                return@launch
            }
            val id = presetId ?: "tx_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            val tx = TransactionDb(
                id = id,
                timestamp = timestamp,
                type = type,
                category = category,
                amount = amount,
                description = description
            )
            repository.saveTransaction(tx)
        }
    }

    fun permanentlyDeleteDeletedItem(item: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.removeDeletedItem(item)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun permanentlyDeleteMultipleItems(items: List<DeletedItemEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                items.forEach { repository.removeDeletedItem(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restoreMultipleItems(items: List<DeletedItemEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                items.forEach { repository.restoreDeletedItem(it) }
                items.forEach { TrashRestoreHandler.restorePrefsForDeletedItem(context, it) }
                sendUiEvent(UiEvent.ShowToast(R.string.toast_restore_success))
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun restoreDeletedItem(item: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                repository.restoreDeletedItem(item)
                TrashRestoreHandler.restorePrefsForDeletedItem(context, item)
                sendUiEvent(UiEvent.ShowToast(R.string.toast_restore_success))
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun restoreSingleTransactionFromBundle(itemId: String, txId: String, item: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                repository.restoreSingleTransactionFromBundle(itemId, txId)
                TrashRestoreHandler.restorePrefsForDeletedItem(context, item)
                sendUiEvent(UiEvent.ShowToast(R.string.toast_restore_success))
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun updateAutoCleanupPeriod(period: String) {
        _autoCleanupPeriod.value = period
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val trashPrefs = context.getSharedPreferences(PREFS_TRASH, Context.MODE_PRIVATE)
            trashPrefs.edit().putString(KEY_TRASH_AUTO_CLEANUP_PERIOD, period).apply()

            // Schedule background worker
            com.example.TrashCleanupWorker.schedulePeriodicCleanup(context, period)

            // Immediate execution of cleanup if not "never"
            if (period != CLEANUP_PERIOD_NEVER) {
                try {
                    val ageInMillis = com.example.TrashCleanupWorker.getPeriodDurationMillis(period)
                    if (ageInMillis > 0L) {
                        val thresholdTime = System.currentTimeMillis() - ageInMillis
                        val items = repository.getAllDeletedItemsDirect()
                        val expiredItems = items.filter { it.deletedAt < thresholdTime }
                        expiredItems.forEach { item ->
                            repository.removeDeletedItem(item)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun cleanLedgerTrashItems() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val systemHabayeb = getApplication<Application>().getString(R.string.source_system_habayeb)
                val allItems = repository.getAllDeletedItemsDirect()
                val nonHabayebItems = allItems.filter {
                    it.sourceSystem != systemHabayeb && !it.originalTableName.startsWith(PREFIX_HABAYEB)
                }
                nonHabayebItems.forEach {
                    repository.removeDeletedItem(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val systemHabayeb = getApplication<Application>().getString(R.string.source_system_habayeb)
                val allItems = repository.getAllDeletedItemsDirect()
                val habayebItems = allItems.filter {
                    it.sourceSystem == systemHabayeb || it.originalTableName.startsWith(PREFIX_HABAYEB)
                }
                habayebItems.forEach {
                    repository.removeDeletedItem(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTransaction(tx: TransactionDb) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.softDeleteTransactionToTrash(tx)
                repository.deleteTransaction(tx)
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun deleteTransactionById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tx = transactionsState.value.find { it.id == id }
                if (tx != null) {
                    repository.softDeleteTransactionToTrash(tx)
                }
                repository.deleteTransactionById(id)
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun deleteTransactionsBulk(ids: List<String>, bundleTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allTxs = transactionsState.value
                val idSet = ids.toSet()
                val toDelete = allTxs.filter { idSet.contains(it.id) }
                if (toDelete.isNotEmpty()) {
                    repository.softDeleteTransactionBundleToTrash(toDelete, bundleTitle)
                    toDelete.forEach { repository.deleteTransactionById(it.id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun updateTransaction(tx: TransactionDb) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveTransaction(tx)
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun saveCommitment(name: String, targetAmount: BigDecimal, currentProgress: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val count = commitmentsState.value.size
                val fc = FixedCommitment(name, targetAmount, currentProgress, count)
                repository.saveCommitment(fc)
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_save_failed))
            }
        }
    }

    fun updateCommitmentDirectly(commitment: FixedCommitment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveCommitment(commitment)
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun reorderCommitment(commitment: FixedCommitment, toPosition: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentList = commitmentsState.value.toMutableList()
                currentList.sortBy { it.orderIndex }

                val targetIndex = (toPosition - 1).coerceIn(0, currentList.size - 1)
                val currentIndex = currentList.indexOfFirst { it.name == commitment.name }

                if (currentIndex != -1 && currentIndex != targetIndex) {
                    val item = currentList.removeAt(currentIndex)
                    currentList.add(targetIndex, item)

                    val updatedList = currentList.mapIndexed { index, fc ->
                        fc.copy(orderIndex = index)
                    }

                    repository.updateCommitments(updatedList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCommitment(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val oldFc = commitmentsState.value.find { it.name == name }
                if (oldFc != null) {
                    repository.softDeleteCommitmentToTrash(oldFc)
                }
                repository.deleteCommitment(name)
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun saveCustomCategory(name: String, tabType: String, emoji: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveCustomCategory(CustomCategory(name = name, tabType = tabType, iconEmoji = emoji))
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_save_failed))
            }
        }
    }

    fun deleteCustomCategory(customCategory: CustomCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteCustomCategory(customCategory)
            } catch (e: Exception) {
                e.printStackTrace()
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteAllData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) يجب أن تبقى ViewModel طبقة تنسيق للحالة والأحداث، لا مستودعاً لقواعد
 *    المجال المالية التي ينبغي أن تعيش في طبقاتها المتخصصة.
 * 2) يوصى مستقبلاً بمراجعة دورة حياة كل Coroutine/Flow والتأكد من ارتباطها
 *    بـ viewModelScope أو نطاقها المقصود لمنع التسرب أو العمل بعد زوال الشاشة.
 * 3) عند تعديل UiState يجب الحفاظ على دلالة الحالات الانتقالية مثل التحميل،
 *    النجاح، الخطأ، والفراغ حتى لا تظهر واجهة مضللة للمستخدم.
 * 4) أي تغيير في الأحداث أو العقود العامة يجب أن يرافقه Regression Test
 *    يثبت أن التفاعل الحالي في Compose لم يتغير.
 * 5) الحسابات المالية والـ BigDecimal يجب أن تبقى في مسارها الدقيق، وألا
 *    تتحول إلى Double/Float داخل طبقة العرض إلا بقرار موثق وصريح.
 * 6) هذه التوصيات مرجعية مستقبلية فقط ولا تمثل أي تغيير في التنفيذ الحالي.
 */
