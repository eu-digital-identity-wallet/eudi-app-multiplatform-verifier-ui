/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package eu.europa.ec.euidi.verifier.presentation.component.extension

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private data class ClickState(
    val event: () -> Unit,
    var currentTimeInMillis: Long
)

@OptIn(ExperimentalTime::class)
fun Modifier.throttledClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    indication: Indication? = null,
    interactionSource: MutableInteractionSource? = null,
    throttleDuration: Long = 1_000L,
    onClick: () -> Unit,
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "clickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
    }
) {

    var lastClicked: Long by remember { mutableLongStateOf(0) }

    val debounceState = remember {
        MutableSharedFlow<ClickState>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }

    LaunchedEffect(Unit) {
        debounceState
            .collect {
                if (lastClicked <= 0 || (it.currentTimeInMillis - lastClicked) >= throttleDuration) {
                    it.event()
                    lastClicked = it.currentTimeInMillis
                }
            }
    }

    Modifier
        .clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            onClick = {
                debounceState.tryEmit(
                    ClickState(
                        onClick,
                        Clock.System.now().toEpochMilliseconds()
                    )
                )
            },
            role = role,
            indication = indication ?: LocalIndication.current,
            interactionSource = interactionSource ?: remember { MutableInteractionSource() }
        )
}

/**
 * Configure component to receive clicks via input or accessibility "click" event.
 *
 * Add this modifier to the element to make it clickable within its bounds and show a default
 * indication when it's pressed.
 *
 * This version has no [MutableInteractionSource] or [Indication] parameters, default indication from
 * [LocalIndication] will be used. To specify [MutableInteractionSource] or [Indication], use another
 * overload.
 *
 * If you need to support double click or long click alongside the single click, consider
 * using [combinedClickable].
 *
 * Same as [clickable]. Difference here is that ripple effect will not be shown to element modifier
 * applied at.
 *
 * @param onClick will be called when user clicks on the element
 */
inline fun Modifier.clickableNoRipple(
    enabled: Boolean = true,
    crossinline onClick: () -> Unit
): Modifier = composed {
    clickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

fun Modifier.withStickyBottomPadding(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
) = composed {
    val layoutDirection = LocalLayoutDirection.current
    padding(
        top = paddingValues.calculateTopPadding(),
        bottom = 0.dp,
        start = paddingValues.calculateStartPadding(layoutDirection),
        end = paddingValues.calculateEndPadding(layoutDirection)
    )
}