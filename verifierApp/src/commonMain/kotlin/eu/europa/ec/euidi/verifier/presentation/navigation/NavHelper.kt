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

package eu.europa.ec.euidi.verifier.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelable

inline fun <reified T : CommonParcelable> NavController.saveToCurrentBackStack(
    key: String,
    value: T
) {
    currentBackStackEntry?.savedStateHandle?.set(key, value)
}

inline fun <reified T : CommonParcelable> NavController.saveToPreviousBackStack(
    key: String,
    value: T
) {
    previousBackStackEntry?.savedStateHandle?.set(key, value)
}

inline fun <reified T : CommonParcelable> NavController.getFromPreviousBackStack(key: String): T? {
    return previousBackStackEntry?.savedStateHandle?.remove(key)
}

inline fun <reified T : CommonParcelable> NavController.getFromCurrentBackStack(key: String): T? {
    return currentBackStackEntry?.savedStateHandle?.remove(key)
}

/**
 * Saves data to a specified destination's saved state handle.
 */
inline fun <reified T : CommonParcelable> NavController.popToAndSave(
    destination: NavItem,
    key: String,
    value: T,
    inclusive: Boolean = false
) {
    getBackStackEntry(destination).savedStateHandle[key] = value
    popBackStack(
        route = destination,
        inclusive = inclusive
    )
}

/**
 * Defines a slide-in animation from the end of the screen.
 *
 * This function returns a lambda that can be used as an enter transition
 * for composable navigation. The animation slides content into the container
 * from the start (right side for LTR layouts, left side for RTL layouts)
 * with a duration of 300 milliseconds and an EaseOut easing function.
 *
 * @return A lambda function representing the enter transition.
 */
fun slideInFromEnd(): AnimatedContentTransitionScope<*>.() -> EnterTransition? = {
    slideIntoContainer(
        animationSpec = tween(durationMillis = 300, easing = EaseOut),
        towards = AnimatedContentTransitionScope.SlideDirection.Start
    )
}

/**
 * Defines a slide-out animation to the end of the container.
 *
 * This function returns a lambda that produces an [ExitTransition].
 * The animation uses a linear easing curve with a duration of 200 milliseconds.
 *
 * @return A lambda function that, when invoked within an [AnimatedContentTransitionScope],
 * returns an [ExitTransition] or null.
 */
fun slideOutToEnd(): AnimatedContentTransitionScope<*>.() -> ExitTransition? = {
    slideOutOfContainer(
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        towards = AnimatedContentTransitionScope.SlideDirection.End
    )
}

/**
 * Represents a no-animation transition.
 *
 * This function returns a lambda that, when invoked within an [AnimatedContentTransitionScope],
 * returns `null`. This effectively disables any animation for the transition it's applied to.
 *
 * @param R The type of transition (e.g., [EnterTransition], [ExitTransition]).
 * @return A lambda function that always returns `null`, indicating no animation.
 */
fun <R> noAnimation(): AnimatedContentTransitionScope<*>.() -> R? = {
    null
}