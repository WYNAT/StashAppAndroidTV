package com.github.damontecres.stashapp.api

import com.github.damontecres.stashapp.BaseTest
import com.github.damontecres.stashapp.LiveTestRule
import com.github.damontecres.stashapp.util.StashServer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LiveApiTest : BaseTest() {

    @get:Rule
    val liveTestRule = LiveTestRule()

    private val serverUrl = System.getenv("STASH_LIVE_TEST_URL") ?: LiveTestRule.DEFAULT_URL
    private val apiKey = System.getenv("STASH_LIVE_TEST_API_KEY")

    @Test
    fun testGetServerConfiguration() = runTest {
        val server = StashServer(serverUrl, apiKey)
        val config = server.queryEngine.getServerConfiguration()
        
        assertNotNull(config)
        assertNotNull(config.configuration)
        println("Server version: ${config.version?.version}")
    }

    @Test
    fun testFindScenes() = runTest {
        val server = StashServer(serverUrl, apiKey)
        val scenes = server.queryEngine.findScenes(useRandom = false)
        
        assertNotNull(scenes)
        // We don't assert scenes.isNotEmpty() because the server might be empty, 
        // but we verify the call succeeds.
        println("Found ${scenes.size} scenes")
    }

    @Test
    fun testGetPerformers() = runTest {
        val server = StashServer(serverUrl, apiKey)
        val performers = server.queryEngine.findPerformers(useRandom = false)
        
        assertNotNull(performers)
        println("Found ${performers.size} performers")
    }
}
