package com.ece452.spacexplorer.ui.newsfeed

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.ece452.spacexplorer.R
import com.ece452.spacexplorer.networking.models.NewsCommentResponse
import com.ece452.spacexplorer.networking.models.userinteractions.LikesDislikesType
import com.ece452.spacexplorer.utils.LikeDislikeManager
import com.ece452.spacexplorer.utils.UserInteractionsManager
import com.ece452.spacexplorer.ui.events.EventCardHelper

class CommentAdapter(
    private val context: Context,
    private val comments: MutableList<NewsCommentResponse>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    
    private val likeStatuses: MutableMap<String, String> = mutableMapOf()

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentText: TextView = itemView.findViewById(R.id.comment_text)
        val likeButton: ToggleButton = itemView.findViewById(R.id.like_button)
        val dislikeButton: ToggleButton = itemView.findViewById(R.id.dislike_button)
        val username: TextView = itemView.findViewById(R.id.username)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_card, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        likeStatuses[comment.article_id + comment.comment_id] = comment.like_status
        val likeStatus = likeStatuses[comment.article_id + comment.comment_id]

        holder.commentText.text = comment.comment
        holder.username.text = comment.username
        holder.timestamp.text = EventCardHelper.getCommentTime(comment.timestamp)
        holder.likeButton.text = comment.likes.toString()
        holder.dislikeButton.text = comment.dislikes.toString()
        holder.likeButton.textOff = comment.likes.toString()
        holder.dislikeButton.textOff = comment.dislikes.toString()
        holder.likeButton.textOn = comment.likes.toString()
        holder.dislikeButton.textOn = comment.dislikes.toString()

        holder.likeButton.isChecked = likeStatus == "like"
        holder.dislikeButton.isChecked = likeStatus == "dislike"

        LikeDislikeManager.updateLikeButton(holder.likeButton, holder.likeButton.isChecked)
        LikeDislikeManager.updateDislikeButton(holder.dislikeButton, holder.dislikeButton.isChecked)

        holder.likeButton.setOnClickListener {
            LikeDislikeManager.updateLikeButton(holder.likeButton, holder.likeButton.isChecked)
            if (holder.likeButton.isChecked) {
                if (holder.dislikeButton.isChecked){
                    holder.dislikeButton.isChecked = false
                    LikeDislikeManager.updateDislikeButton(holder.dislikeButton, false)
                }
                handleLike(holder, position)
            } else {
                handleUnlike(holder, position)
            }
        }

        holder.dislikeButton.setOnClickListener {
            LikeDislikeManager.updateDislikeButton(holder.dislikeButton, holder.dislikeButton.isChecked)
            if (holder.dislikeButton.isChecked) {
                if (holder.likeButton.isChecked){
                    holder.likeButton.isChecked = false
                    LikeDislikeManager.updateLikeButton(holder.likeButton, false)
                }
                handleDislike(holder, position)
            } else {
                handleUndislike(holder, position)
            }
        }
    }

    override fun getItemCount(): Int = comments.size

    private fun handleLike(holder: CommentViewHolder, position: Int) {
        val event = comments[position]
        updateLikeStatus(holder, LikesDislikesType.LIKE, event.article_id, event.comment_id)
        likeStatuses[event.article_id + event.comment_id] = "like"
    }

    private fun handleUnlike(holder: CommentViewHolder, position: Int) {
        val event = comments[position]
        updateLikeStatus(holder, LikesDislikesType.UNLIKE, event.article_id, event.comment_id)
        likeStatuses[event.article_id + event.comment_id] = "neutral"
    }

    private fun handleDislike(holder: CommentViewHolder, position: Int) {
        val event = comments[position]
        updateLikeStatus(holder, LikesDislikesType.DISLIKE, event.article_id, event.comment_id)
        likeStatuses[event.article_id + event.comment_id] = "dislike"
    }

    private fun handleUndislike(holder: CommentViewHolder, position: Int) {
        val event = comments[position]
        updateLikeStatus(holder, LikesDislikesType.UNDISLIKE, event.article_id, event.comment_id)
        likeStatuses[event.article_id + event.comment_id] = "neutral"
    }

    private fun updateLikeStatus(holder: CommentViewHolder, interaction: LikesDislikesType, articleId: String, commentId: String) {
        UserInteractionsManager.putNewsCommentLike(interaction, articleId, commentId) { success, response ->
            if (success) {
                response?.let {
                    holder.likeButton.text = it.likes.toString()
                    holder.dislikeButton.text = it.dislikes.toString()
                    holder.likeButton.textOff = it.likes.toString()
                    holder.dislikeButton.textOff = it.dislikes.toString()
                    holder.likeButton.textOn = it.likes.toString()
                    holder.dislikeButton.textOn = it.dislikes.toString()
                }
            } else {
                Log.e("CommentAdapter", "Failed to update like status for comment $commentId")
            }
        }
    }
}
