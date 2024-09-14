package com.ece452.spacexplorer.utils

import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import com.ece452.spacexplorer.R

object LikeDislikeManager {

    // Function to get the like color based on state
    private fun getLikeColor(isLiked: Boolean): Int {
        return if (isLiked) R.color.color_like_filled else R.color.color_like
    }

    // Function to get the dislike color based on state
    private fun getDislikeColor(isDisliked: Boolean): Int {
        return if (isDisliked) R.color.color_dislike_filled else R.color.color_dislike
    }

    // Update the like button UI
    fun updateLikeButton(button: ToggleButton, isLiked: Boolean) {
        button.compoundDrawableTintList = ContextCompat.getColorStateList(button.context, getDislikeColor(isLiked))
    }

    // Update the dislike button UI
    fun updateDislikeButton(button: ToggleButton, isDisliked: Boolean) {
        button.compoundDrawableTintList = ContextCompat.getColorStateList(button.context, getDislikeColor(isDisliked))
    }
}
