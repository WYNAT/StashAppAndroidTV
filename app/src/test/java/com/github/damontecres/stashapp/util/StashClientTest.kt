package com.github.damontecres.stashapp.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.damontecres.stashapp.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StashClientTest : BaseTest() {

    @Test
    fun testCleanServerUrl() {
        assertEquals("http://localhost/graphql", StashClient.cleanServerUrl("localhost"))
        assertEquals("http://192.168.1.1/graphql", StashClient.cleanServerUrl("192.168.1.1"))
        assertEquals("https://my-stash.com/graphql", StashClient.cleanServerUrl("https://my-stash.com"))
        assertEquals("http://myserver:9999/graphql", StashClient.cleanServerUrl("myserver:9999"))
        assertEquals("http://myserver/graphql", StashClient.cleanServerUrl("myserver/graphql"))
    }

    @Test
    fun testCreateUserAgent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userAgent = StashClient.createUserAgent(context)
        
        assertTrue(userAgent.startsWith("StashAppAndroidTV/"))
        assertTrue(userAgent.contains("sdk/"))
    }

    @Test
    fun testGetServerRoot() {
        assertEquals("http://localhost", StashClient.getServerRoot("localhost"))
        assertEquals("http://localhost", StashClient.getServerRoot("http://localhost/graphql"))
        assertEquals("https://my-stash.com", StashClient.getServerRoot("https://my-stash.com/graphql"))
    }
    
    @Test
    fun testCreateLoginUrl() {
        assertEquals("http://localhost/login", StashClient.createLoginUrl("localhost"))
        assertEquals("http://localhost/login", StashClient.createLoginUrl("http://localhost/graphql"))
    }
}
