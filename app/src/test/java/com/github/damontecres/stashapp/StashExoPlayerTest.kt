package com.github.damontecres.stashapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.damontecres.stashapp.util.StashServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StashExoPlayerTest : BaseTest() {

    private lateinit var context: Context
    private val server = StashServer("http://localhost", null)

    @Before
    override fun setup() {
        super.setup()
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    override fun tearDown() {
        super.tearDown()
        StashExoPlayer.releasePlayer()
    }

    @Test
    fun testGetInstance() {
        val player1 = StashExoPlayer.getInstance(context, server)
        assertNotNull(player1)
        
        val player2 = StashExoPlayer.getInstance(context, server)
        assertSame("Should return the same instance", player1, player2)
    }

    @Test
    fun testReleasePlayer() {
        val player1 = StashExoPlayer.getInstance(context, server)
        StashExoPlayer.releasePlayer()
        
        val player2 = StashExoPlayer.getInstance(context, server)
        assertNotSame("Should return a new instance after release", player1, player2)
    }
}
