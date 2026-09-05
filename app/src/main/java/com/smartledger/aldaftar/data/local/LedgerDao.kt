package com.smartledger.aldaftar.data.local

import androidx.room.Dao

/** واجهة موحدة تجمع الوصول إلى قيود اليومية والتصنيفات المخصصة دون تكرار التنفيذ. */
@Dao
interface LedgerDao : TransactionDao, CustomCategoryDao
