package com.github.damontecres.stashapp.presenters

import androidx.appcompat.content.res.AppCompatResources
import com.github.damontecres.stashapp.R
import com.github.damontecres.stashapp.data.DataType
import com.github.damontecres.stashapp.suppliers.FilterArgs

class FilterArgsPresenter(
    callback: LongClickCallBack<FilterArgs>? = null,
) : StashPresenter<FilterArgs>(callback) {
    override fun doOnBindViewHolder(
        cardView: StashImageCardView,
        item: FilterArgs,
    ) {
        cardView.titleText = cardView.context.getString(R.string.stashapp_view_all)

        val w = com.github.damontecres.stashapp.util.getDynamicCardWidth(cardView.context, item.dataType)
        val h = com.github.damontecres.stashapp.util.getDynamicCardHeight(cardView.context, item.dataType)
        cardView.setMainImageDimensions(w, h)

        cardView.imageView.setBackgroundColor(cardView.context.getColor(android.R.color.transparent))
        cardView.imageView.setImageDrawable(
            AppCompatResources.getDrawable(
                cardView.context,
                R.drawable.baseline_camera_indoor_48,
            ),
        )
    }

    companion object {
        const val TAG = "FilterArgsPresenter"
    }
}
