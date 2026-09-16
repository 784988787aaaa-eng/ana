package com.smartledger.aldaftar.data.local

import androidx.room.Dao

@Dao
interface LedgerDao : TransactionDao, CustomCategoryDao



