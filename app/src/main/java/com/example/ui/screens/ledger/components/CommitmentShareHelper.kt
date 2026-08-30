package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * مساعد مشاركة الالتزامات المالية (Commitment Share Helper)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * كائن مساعد متخصص في توليد وتصدير تقرير نصي شامل للالتزامات والأهداف المالية:
 * 1. يقوم بتجميع وبناء نص التقرير مرتباً بأسماء الالتزامات ومبالغها وإجمالي المتبقي والنقد المتاح.
 * 2. يضمن تحويل الأرقام إلى النسق الغربي الموحد عبر toWesternDigits.
 * 3. يستهدف تطبيق واتساب (com.whatsapp) بشكل أساسي مع توفير fallback فوري لمنظومة المشاركة العامة للنظام (Android Sharesheet).
 * =====================================================================================
 */

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.R
import com.example.data.local.entities.FixedCommitment
import java.math.BigDecimal

/*
 * =====================================================================================
 * دالة مساعدة لتحويل الأرقام المشرقية إلى غربية (toWesternDigits)
 * =====================================================================================
 */
private fun String.toWesternDigits(): String {
    var result = this
    val eastern = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val western = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    for (i in 0..9) {
        result = result.replace(eastern[i], western[i])
    }
    return result
}

/**
 * كائن إدارة مشاركة الالتزامات المالية والمصروفات الثابتة (Commitments Sharing Helper)
 */
object CommitmentShareHelper {
    private const val TAG = "CommitmentShareHelper"

    /*
     * =====================================================================================
     * دالة إنشاء ومشاركة تقرير الالتزامات (shareCommitments)
     * -------------------------------------------------------------------------------------
     * [المُدخلات]:
     * - context: سياق أندرويد لتشغيل Intent وقراءة الموارد النصية.
     * - commitments: قائمة كائنات الالتزامات الثابتة.
     * - computedCommitments: قائمة ثلاثية تحوي (الالتزام، المخصص، المتبقي).
     * - totalCash: إجمالي السيولة النقدية المتاحة.
     * - currencySymbol: رمز العملة المعتمد.
     * - formatCurrency: دالة تنسيق المبالغ المالية.
     * =====================================================================================
     */
    fun shareCommitments(
        context: Context,
        commitments: List<FixedCommitment>,
        computedCommitments: List<Triple<FixedCommitment, BigDecimal, BigDecimal>>,
        totalCash: BigDecimal,
        currencySymbol: String,
        formatCurrency: (BigDecimal, String) -> String
    ) {
        val builder = StringBuilder()
        builder.append(context.getString(R.string.ledger_commitment_box_title).replace("🎯", "").trim()).append("\n\n")
        var idx = 1
        commitments.forEach { fc ->
            val line = context.getString(R.string.ledger_commitment_share_format, idx, fc.name, formatCurrency(fc.targetAmount, currencySymbol))
            builder.append(line.toWesternDigits())
            idx++
        }

        val totalReq = commitments.fold(BigDecimal.ZERO) { acc, fc -> acc.add(fc.targetAmount) }
        val totalRemaining = computedCommitments.fold(BigDecimal.ZERO) { acc, triple -> acc.add(triple.third) }

        builder.append(context.getString(R.string.ledger_commitment_total_req, formatCurrency(totalReq, currencySymbol)).toWesternDigits())
        builder.append(context.getString(R.string.ledger_commitment_total_current, formatCurrency(totalCash, currencySymbol)).toWesternDigits())
        builder.append(context.getString(R.string.ledger_commitment_total_remaining, formatCurrency(totalRemaining, currencySymbol)).toWesternDigits())

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, builder.toString())
        }
        try {
            shareIntent.setPackage("com.whatsapp")
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            try {
                shareIntent.setPackage(null)
                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.ledger_share_via)))
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to launch share intent", t)
            }
        }
    }
}

