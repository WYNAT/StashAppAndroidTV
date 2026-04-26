package com.github.damontecres.stashapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk = [33], application = StashApplication::class)
abstract class BaseTest {
    protected val testDispatcher = StandardTestDispatcher()

    @Before
    open fun setup() {
        Dispatchers.setMain(testDispatcher)
        try {
            val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
            if (context is StashApplication) {
                StashApplication.application = context
            }
        } catch (e: Exception) {
            // Not a robolectric test or provider not available
        }
    }

    @After
    open fun tearDown() {
        Dispatchers.resetMain()
    }
}
