package com.github.damontecres.stashapp.playback

import com.github.damontecres.stashapp.proto.Resolution
import com.github.damontecres.stashapp.proto.StreamChoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StreamUtilsTest {

    @Test
    fun testStreamChoiceFromLabel() {
        assertEquals(StreamChoice.HLS, streamChoiceFromLabel("HLS"))
        assertEquals(StreamChoice.DASH, streamChoiceFromLabel("dash"))
        assertEquals(StreamChoice.MP4, streamChoiceFromLabel("MP4"))
        assertEquals(StreamChoice.UNRECOGNIZED, streamChoiceFromLabel("unknown"))
    }

    @Test
    fun testResolutionFromLabel() {
        assertEquals(Resolution.RES_2160P, resolutionFromLabel("2160p"))
        assertEquals(Resolution.RES_1080P, resolutionFromLabel("1080P"))
        assertEquals(Resolution.UNSPECIFIED, resolutionFromLabel("none"))
    }

    @Test
    fun testCheckIfAlwaysTranscode() {
        val streams = mapOf(
            "HLS (1080p)" to "url1",
            "HLS (720p)" to "url2",
            "MP4 (1080p)" to "url3"
        )
        
        // Scene is 2160p, always transcode above 1080p
        val result = checkIfAlwaysTranscode(
            videoResolution = 2160,
            streams = streams,
            streamChoice = StreamChoice.HLS,
            alwaysTarget = Resolution.RES_1080P
        )
        assertEquals("HLS (1080p)", result)
        
        // Scene is 1080p, always transcode above 1080p -> should return null (direct play ok)
        val result2 = checkIfAlwaysTranscode(
            videoResolution = 1080,
            streams = streams,
            streamChoice = StreamChoice.HLS,
            alwaysTarget = Resolution.RES_1080P
        )
        assertNull(result2)

        // Scene is 2160p, always transcode above 720p
        val result3 = checkIfAlwaysTranscode(
            videoResolution = 2160,
            streams = streams,
            streamChoice = StreamChoice.HLS,
            alwaysTarget = Resolution.RES_720P
        )
        assertEquals("HLS (720p)", result3)
    }
}
