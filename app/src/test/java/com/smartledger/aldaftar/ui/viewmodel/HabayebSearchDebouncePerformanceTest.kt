package com.smartledger.aldaftar.ui.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class HabayebSearchDebouncePerformanceTest {

    @Test
    fun searchQueryDebounce_filtersExcessiveKeystrokeEmissions() = runTest {
        val searchQuery = MutableStateFlow("")
        
        // Match the exact debounce logic from HabayebFinanceViewModel:
        val debouncedSearchQuery = searchQuery
            .debounce { query ->
                if (query.isEmpty()) 0L else 120L
            }

        val emissions = mutableListOf<String>()
        val collectJob = backgroundScope.launch {
            debouncedSearchQuery.collect { emissions.add(it) }
        }

        // Initial collect
        runCurrent()
        assertEquals(listOf(""), emissions)

        // Simulate typing "a", "ab", "abc" rapidly within 100ms
        searchQuery.value = "a"
        testScheduler.advanceTimeBy(40L)
        searchQuery.value = "ab"
        testScheduler.advanceTimeBy(40L)
        searchQuery.value = "abc"

        // No emissions yet because time advanced by 80ms total (under 120ms debounce limit)
        runCurrent()
        assertEquals(listOf(""), emissions)

        // Advance past the 120ms debounce threshold
        testScheduler.advanceTimeBy(130L)
        runCurrent()

        // Verify only the final "abc" was emitted after debounce
        assertEquals(listOf("", "abc"), emissions)

        // Clear query - should emit instantly with 0ms debounce
        searchQuery.value = ""
        runCurrent()
        assertEquals(listOf("", "abc", ""), emissions)

        collectJob.cancel()
    }
}
