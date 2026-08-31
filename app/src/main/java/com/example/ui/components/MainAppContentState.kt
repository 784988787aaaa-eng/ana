package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// تم فصل إدارة الحالة عن تركيب الواجهة للحفاظ على وضوح مسؤوليات المكونات.
class MainAppContentState(
    initialIsDrawerOpen: Boolean = false,
    private val onMenuClickAction: (() -> Unit)? = null
) {
    var isDrawerOpen by mutableStateOf(initialIsDrawerOpen)

    fun handleMenuClick() {
        onMenuClickAction?.invoke()
    }

    fun openDrawer() {
        isDrawerOpen = true
        onMenuClickAction?.invoke()
    }

    fun closeDrawer() {
        isDrawerOpen = false
    }

    fun toggleDrawer() {
        if (isDrawerOpen) {
            closeDrawer()
        } else {
            openDrawer()
        }
    }

    fun updateDrawerState(isOpen: Boolean) {
        isDrawerOpen = isOpen
    }
}

@Composable
fun rememberMainAppContentState(
    isDrawerOpen: Boolean = false,
    onMenuClick: (() -> Unit)? = null
): MainAppContentState {
    val state = remember(isDrawerOpen, onMenuClick) {
        MainAppContentState(initialIsDrawerOpen = isDrawerOpen, onMenuClickAction = onMenuClick)
    }
    state.updateDrawerState(isDrawerOpen)
    return state
}
