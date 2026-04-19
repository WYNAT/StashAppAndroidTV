package com.github.damontecres.stashapp.navigation

import android.util.Log
import android.widget.Toast
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import com.github.damontecres.stashapp.StashApplication
import com.github.damontecres.stashapp.api.fragment.ImageData
import com.github.damontecres.stashapp.api.fragment.MarkerData
import com.github.damontecres.stashapp.api.fragment.StashData
import com.github.damontecres.stashapp.data.DataType
import com.github.damontecres.stashapp.playback.PlaybackMode
import com.github.damontecres.stashapp.suppliers.FilterArgs

/**
 * An [OnItemViewClickedListener] for clicking on [StashData] and [FilterArgs] to navigating to their page
 *
 * @param navigationManager the manager
 * @param filterLookup get the filter and position of the current item clicked (used for slideshow and scene playlist context)
 */
class NavigationOnItemViewClickedListener(
    private val navigationManager: NavigationManager,
    private val filterLookup: ((item: StashData) -> FilterAndPosition?)? = null,
) : OnItemViewClickedListener {
    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder?,
        item: Any?,
        rowViewHolder: RowPresenter.ViewHolder?,
        row: Row?,
    ) {
        val destination =
            when (item) {
                is MarkerData -> {
                    Destination.Playback(
                        item.scene.minimalSceneData.id,
                        (item.seconds * 1000L).toLong(),
                        PlaybackMode.Choose,
                    )
                }

                is ImageData -> {
                    val filterAndPosition = filterLookup!!.invoke(item)
                    if (filterAndPosition != null) {
                        Destination.Slideshow(
                            filterAndPosition.filter,
                            filterAndPosition.position,
                            false,
                        )
                    } else {
                        throw IllegalStateException("filterLookup is null")
                    }
                }

                is StashData -> {
                    val dataType = Destination.getDataType(item)
                    if (dataType == DataType.SCENE) {
                        val filterAndPosition = filterLookup?.invoke(item)
                        // Only use filter context when the filter's dataType actually matches
                        val validFilter = filterAndPosition?.takeIf { it.filter.dataType == DataType.SCENE }
                        Destination.Item(
                            dataType,
                            item.id,
                            validFilter?.filter,
                            validFilter?.position ?: -1,
                        )
                    } else {
                        Destination.fromStashData(item)
                    }
                }

                is FilterArgs -> {
                    Destination.Filter(item, true)
                }

                else -> {
                    null
                }
            }
        if (destination != null) {
            navigationManager.navigate(destination)
        } else {
            val itemInfo = if (item == null) "null" else item::class.java.name
            Log.w(TAG, "Unsupported item: $itemInfo")
            Toast
                .makeText(
                    StashApplication.getApplication(),
                    "Unknown item: $itemInfo, this is probably a bug!",
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    companion object {
        private const val TAG = "NavigationOnItemViewClickedListener"
    }
}
