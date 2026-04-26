package com.github.damontecres.stashapp.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.damontecres.stashapp.BaseTest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HandyManagerTest : BaseTest() {

    private lateinit var server: MockWebServer
    private val originalBaseUrl = HandyManager.BASE_URL
    private val originalHostingUrl = HandyManager.HOSTING_URL

    @Before
    override fun setup() {
        super.setup()
        server = MockWebServer()
        server.start()
        HandyManager.BASE_URL = server.url("/api/handy/v2").toString().removeSuffix("/")
        HandyManager.HOSTING_URL = server.url("/api/hosting/v2").toString().removeSuffix("/")
        
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(context.getString(com.github.damontecres.stashapp.R.string.pref_key_handy_connection_key), "dummy_key").apply()
        
        server.enqueue(MockResponse().setResponseCode(200).setBody("{\"serverTime\": ${System.currentTimeMillis()}}"))
        HandyManager.initialize(context)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        server.shutdown()
        HandyManager.BASE_URL = originalBaseUrl
        HandyManager.HOSTING_URL = originalHostingUrl
    }

    @Test
    fun testConnectionSuccess() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{\"connected\": true}"))
        
        // Mock preferences connection key
        // HandyManager uses PreferenceManager.getDefaultSharedPreferences(appContext)
        // Robolectric handles SharedPreferences automatically.
        
        val result = HandyManager.testConnection()
        // result is Pair<Boolean, String>
        assertTrue(result.first)
    }

    @Test
    fun testConnectionFailure() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("{\"error\": {\"message\": \"Invalid key\"}}"))
        
        val result = HandyManager.testConnection()
        assertFalse(result.first)
        assertTrue(result.second.contains("Invalid key") || result.second.contains("401"))
    }

    @Test
    fun testSetMode() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        
        val result = HandyManager.setMode(1)
        assertTrue(result is HandyManager.HandyResult.Success)
    }
}
