package com.smartledger.aldaftar.domain.communication

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class CommunicationChannelType {
    data class WhatsApp(val message: String) : CommunicationChannelType()
    data class SMS(val message: String) : CommunicationChannelType()
}

data class PendingCommunicationRequest(
    val transactionId: String,
    val customerId: String,
    val customerPhone: String,
    val queue: List<CommunicationChannelType>,
    val currentChannel: CommunicationChannelType,
    val isLaunched: Boolean = false
)

/**
 * منسق تدفق التواصل التلقائي متعدد القنوات مع حماية دورة حياة أندرويد ومنع التكرار.
 * يضمن:
 * 1. عدم فتح القناتين معاً بصورة غير آمنة: فتح واتساب أولاً، ثم SMS عند العودة.
 * 2. منع التكرار الناتج عن استدارة الشاشة (recomposition / configuration change).
 * 3. العمل حصرياً مع المعاملات الجديدة المنشأة بنجاح في قاعدة البيانات.
 * 4. الأمان المالي الكامل: عدم ربط صحة البيانات المالية بنتيجة Intent التواصل.
 */
class AutoCommunicationCoordinator {

    private val _pendingRequest = MutableStateFlow<PendingCommunicationRequest?>(null)
    val pendingRequest: StateFlow<PendingCommunicationRequest?> = _pendingRequest.asStateFlow()

    private val processedTxIds = mutableSetOf<String>()

    fun enqueue(
        transactionId: String,
        customerId: String,
        customerPhone: String,
        channels: List<CommunicationChannelType>
    ) {
        if (channels.isEmpty() || processedTxIds.contains(transactionId)) return
        processedTxIds.add(transactionId)

        _pendingRequest.value = PendingCommunicationRequest(
            transactionId = transactionId,
            customerId = customerId,
            customerPhone = customerPhone,
            queue = channels,
            currentChannel = channels.first(),
            isLaunched = false
        )
    }

    fun markCurrentChannelLaunched() {
        val current = _pendingRequest.value ?: return
        if (!current.isLaunched) {
            _pendingRequest.value = current.copy(isLaunched = true)
        }
    }

    /**
     * يستدعى عند استئناف واجهة التطبيق (Activity ON_RESUME).
     * إذا كانت القناة الحالية قد أُطلقت مسبقاً، يتم الانتقال إلى القناة التالية إن وجدت، أو إنهاء التدفق.
     */
    fun onActivityResumed() {
        val current = _pendingRequest.value ?: return
        if (!current.isLaunched) return

        val remainingQueue = current.queue.drop(1)
        if (remainingQueue.isNotEmpty()) {
            _pendingRequest.value = current.copy(
                queue = remainingQueue,
                currentChannel = remainingQueue.first(),
                isLaunched = false
            )
        } else {
            _pendingRequest.value = null
        }
    }

    fun dismiss() {
        _pendingRequest.value = null
    }

    fun clearForTests() {
        _pendingRequest.value = null
        processedTxIds.clear()
    }
}
