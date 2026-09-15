package com.smartledger.aldaftar.ui.screens.trash.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.financialCreditColor
import com.smartledger.aldaftar.ui.theme.financialDebtColor
import com.smartledger.aldaftar.ui.theme.isDark

private enum class TrashTopBarState {
    DEFAULT,
    SEARCH,
    SELECTION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashTopBarSection(
    isSearchActive: Boolean,
    isSelectionMode: Boolean,
    searchQuery: String,
    selectedCount: Int,
    hasItems: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onClearSelection: () -> Unit,
    onBack: () -> Unit,
    onRestoreSelected: () -> Unit,
    onDeleteSelectedPermanently: () -> Unit,
    onRequestEmptyTrash: () -> Unit
) {
    val isDark = MaterialTheme.isDark
    val creditColor = financialCreditColor(isDark)
    val debtColor = financialDebtColor(isDark)

    val topBarState = when {
        isSelectionMode -> TrashTopBarState.SELECTION
        isSearchActive -> TrashTopBarState.SEARCH
        else -> TrashTopBarState.DEFAULT
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        AnimatedContent(
            targetState = topBarState,
            transitionSpec = {
                (slideInHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)) { it } +
                    fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)) { -it } +
                            fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
                    )
            },
            label = "TrashToolbarTransition"
        ) { state ->
            when (state) {
                TrashTopBarState.SEARCH -> {
                    val searchFocusRequester = remember { FocusRequester() }
                    val focusManager = LocalFocusManager.current
                    RequestFocusAndShowKeyboard(focusRequester = searchFocusRequester)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(25.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(25.dp)
                            )
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.trash_search_placeholder),
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(searchFocusRequester),
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = CairoFontFamily,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Start
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchQueryChange("") },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = stringResource(id = R.string.common_cancel),
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        )
                        IconButton(
                            onClick = {
                                onSearchToggle(false)
                                onSearchQueryChange("")
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.trash_back),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                TrashTopBarState.SELECTION -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onClearSelection,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.trash_cancel_selection),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                border = BorderStroke(
                                    0.8.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                )
                            ) {
                                Text(
                                    text = stringResource(id = R.string.trash_selected_count, selectedCount),
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Restore: 38dp circle inside 48dp standard touch target
                            IconButton(
                                onClick = onRestoreSelected,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(creditColor.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = stringResource(id = R.string.trash_restore_selected),
                                        tint = creditColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Delete Forever: 38dp circle inside 48dp standard touch target
                            IconButton(
                                onClick = onDeleteSelectedPermanently,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(debtColor.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = stringResource(id = R.string.trash_delete_selected_permanently),
                                        tint = debtColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                TrashTopBarState.DEFAULT -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(id = R.string.trash_back),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Text(
                                text = stringResource(id = R.string.trash_title),
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (hasItems) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onSearchToggle(true) },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(id = R.string.trash_search),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onRequestEmptyTrash,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = stringResource(id = R.string.trash_empty_bin),
                                        tint = debtColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
