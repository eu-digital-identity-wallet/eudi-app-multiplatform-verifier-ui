/*
 * Copyright (c) 2026 European Commission
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

package eu.europa.ec.euidi.verifier.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Base class for [eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel] tests.
 *
 * `viewModelScope` runs on [Dispatchers.Main], so it is replaced with a test dispatcher for the
 * duration of each test. An [UnconfinedTestDispatcher] is used so that the event collector started
 * in the ViewModel's `init` subscribes eagerly and events dispatched via `setEvent` are processed
 * synchronously — removing the need to manually advance the scheduler in most cases.
 *
 * The same [testDispatcher] should be passed to `runTest` so the ViewModel and the test body share
 * a single scheduler.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class MviViewModelTest {

    protected val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUpMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDownMainDispatcher() {
        Dispatchers.resetMain()
    }
}
