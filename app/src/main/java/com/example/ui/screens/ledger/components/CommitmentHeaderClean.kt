package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * شريط رأس واجهة الالتزامات المالية (Commitment Header Clean Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * مكون واجهة رسومية أنيق وخفيف يعرض شريط العنوان العلوي لقسم الأهداف والالتزامات:
 * 1. يعرض العنوان الرئيسي "الأهداف والالتزامات" بوضوح وخط عريض.
 * 2. يحتوي على زر إغلاق دائري (Close Button) على الطرف الأيمن.
 * 3. يحتوي على زر مشاركة تقرير الالتزامات (Share Button) على الطرف المقابل.
 * =====================================================================================
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/*
 * =====================================================================================
 * دالة العرض لشريط الرأس (CommitmentHeaderClean Composable)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - onCloseClick: رد النداء عند النقر على زر الإغلاق للعودة للشاشة السابقة.
 * - onShareClick: رد النداء عند النقر على زر مشاركة ملخص الالتزامات.
 * =====================================================================================
 */
@Composable
fun CommitmentHeaderClean(
    onCloseClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // زر الإغلاق ✕
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(id = R.string.report_btn_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        // عنوان الواجهة
        Text(
            text = "الأهداف والالتزامات",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )

        // زر المشاركة 🔗
        IconButton(
            onClick = onShareClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(id = R.string.ledger_whatsapp_whatsapp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

