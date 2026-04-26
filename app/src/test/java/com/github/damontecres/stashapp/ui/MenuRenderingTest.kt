package com.github.damontecres.stashapp.ui

import com.github.damontecres.stashapp.BaseTest
import com.github.damontecres.stashapp.api.fragment.SlimSceneData
import com.github.damontecres.stashapp.presenters.ScenePresenter
import com.github.damontecres.stashapp.presenters.StashPresenter
import com.github.damontecres.stashapp.util.Version
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MenuRenderingTest : BaseTest() {

    @Test
    fun testVersionSupport() {
        // MINIMUM_STASH_VERSION is 0.30.0
        assertTrue(Version.isStashVersionSupported(Version(0, 30, 0)))
        assertTrue(Version.isStashVersionSupported(Version(1, 0, 0)))
        assertFalse(Version.isStashVersionSupported(Version(0, 26, 0)))
    }

    @Test
    fun testDefaultClassPresenterSelector() {
        val selector = StashPresenter.defaultClassPresenterSelector()
        
        // Mock SlimSceneData instead of instantiating it to avoid constructor issues
        val mockScene = org.mockito.Mockito.mock(SlimSceneData::class.java)
        
        val presenter = selector.getPresenter(mockScene)
        assertTrue(presenter is ScenePresenter)
    }

    @Test
    fun testPopUpItems() {
        val presenter = object : StashPresenter<String>() {
            override fun doOnBindViewHolder(cardView: com.github.damontecres.stashapp.presenters.StashImageCardView, item: String) {}
        }
        
        val items = presenter.longClickCallBack.getPopUpItems("test")
        assertTrue(items.any { it.id == StashPresenter.PopUpItem.DEFAULT_ID })
    }
}
