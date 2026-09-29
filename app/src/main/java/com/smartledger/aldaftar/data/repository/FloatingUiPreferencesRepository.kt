package com.smartledger.aldaftar.data.repository

import android.content.Context
import android.content.SharedPreferences

data class FloatingSearchState(val sizeLevel: Int = 1, val ratioX: Float = .80f, val ratioY: Float = .70f)
data class FloatingAddState(val sizeLevel: Int = 1, val ratioX: Float = -1f, val ratioY: Float = -1f, val hasPosition: Boolean = false)

/** قواعد الاستعادة تمنع القيم التالفة من إخراج العناصر العائمة خارج المجال. */
object FloatingUiStateNormalizer {
    const val MIN_SIZE_LEVEL = 0
    const val MAX_SIZE_LEVEL = 2
    private const val MIN_RATIO = 0f
    private const val MAX_RATIO = 1f

    fun search(state: FloatingSearchState): FloatingSearchState = state.copy(
        sizeLevel = state.sizeLevel.coerceIn(MIN_SIZE_LEVEL, MAX_SIZE_LEVEL),
        ratioX = state.ratioX.normalizedOr(FloatingSearchState().ratioX),
        ratioY = state.ratioY.normalizedOr(FloatingSearchState().ratioY)
    )

    fun add(state: FloatingAddState): FloatingAddState {
        if (!state.hasPosition) {
            return FloatingAddState(
                sizeLevel = state.sizeLevel.coerceIn(MIN_SIZE_LEVEL, MAX_SIZE_LEVEL),
                hasPosition = false
            )
        }
        return state.copy(
            sizeLevel = state.sizeLevel.coerceIn(MIN_SIZE_LEVEL, MAX_SIZE_LEVEL),
            ratioX = state.ratioX.normalizedOr(-1f),
            ratioY = state.ratioY.normalizedOr(-1f)
        )
    }

    private fun Float.normalizedOr(default: Float): Float =
        if (isFinite() && this in MIN_RATIO..MAX_RATIO) this else default
}

class FloatingUiPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences("floating_ui", Context.MODE_PRIVATE)

    fun floatingSearchActive(): Boolean = prefs.getBoolean(KEY_SEARCH_ACTIVE, false)
    fun setFloatingSearchActive(value: Boolean) { prefs.edit().putBoolean(KEY_SEARCH_ACTIVE, value).apply() }

    fun search(): FloatingSearchState = FloatingUiStateNormalizer.search(
        FloatingSearchState(
            prefs.getInt(KEY_SEARCH_SIZE, FloatingSearchState().sizeLevel),
            prefs.getFloat(KEY_SEARCH_X, FloatingSearchState().ratioX),
            prefs.getFloat(KEY_SEARCH_Y, FloatingSearchState().ratioY)
        )
    )

    fun saveSearch(value: FloatingSearchState) {
        val state = FloatingUiStateNormalizer.search(value)
        prefs.edit().putInt(KEY_SEARCH_SIZE, state.sizeLevel).putFloat(KEY_SEARCH_X, state.ratioX)
            .putFloat(KEY_SEARCH_Y, state.ratioY).apply()
    }

    fun add(): FloatingAddState = FloatingUiStateNormalizer.add(
        FloatingAddState(
            prefs.getInt(KEY_ADD_SIZE, FloatingAddState().sizeLevel),
            prefs.getFloat(KEY_ADD_X, FloatingAddState().ratioX),
            prefs.getFloat(KEY_ADD_Y, FloatingAddState().ratioY),
            prefs.getBoolean(KEY_ADD_HAS_POSITION, false)
        )
    )

    fun saveAdd(value: FloatingAddState) {
        val state = FloatingUiStateNormalizer.add(value)
        prefs.edit().putInt(KEY_ADD_SIZE, state.sizeLevel).putFloat(KEY_ADD_X, state.ratioX)
            .putFloat(KEY_ADD_Y, state.ratioY).putBoolean(KEY_ADD_HAS_POSITION, state.hasPosition).apply()
    }

    private companion object {
        const val KEY_SEARCH_ACTIVE = "search_active"
        const val KEY_SEARCH_SIZE = "search_size"
        const val KEY_SEARCH_X = "search_x"
        const val KEY_SEARCH_Y = "search_y"
        const val KEY_ADD_SIZE = "add_size"
        const val KEY_ADD_X = "add_x"
        const val KEY_ADD_Y = "add_y"
        const val KEY_ADD_HAS_POSITION = "add_has_position"
    }
}
