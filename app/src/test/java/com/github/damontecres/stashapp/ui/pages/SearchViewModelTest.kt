package com.github.damontecres.stashapp.ui.pages

import com.github.damontecres.stashapp.BaseTest
import com.github.damontecres.stashapp.data.DataType
import com.github.damontecres.stashapp.util.StashServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchViewModelTest : BaseTest() {

    @Test
    fun testInitialState() {
        val viewModel = SearchViewModel()
        DataType.entries.forEach { dataType ->
            val liveData = viewModel.mapping[dataType]
            assertTrue(liveData != null)
            assertEquals(emptyList<Any>(), liveData?.value)
        }
    }

    @Test
    fun testClearResetsAllMappings() {
        val viewModel = SearchViewModel()
        viewModel.scenes.value = listOf("scene1", "scene2")
        viewModel.performers.value = listOf("performer1")

        viewModel.clear()

        DataType.entries.forEach { dataType ->
            assertEquals(emptyList<Any>(), viewModel.mapping[dataType]?.value)
        }
    }

    @Test
    fun testSearchWithBlankQueryClears() {
        val viewModel = SearchViewModel()
        val mockServer = mock(StashServer::class.java)
        viewModel.init(mockServer, "", 25)

        viewModel.scenes.value = listOf("scene1")
        viewModel.search("   ", 25)

        assertEquals(emptyList<Any>(), viewModel.scenes.value)
    }
}
