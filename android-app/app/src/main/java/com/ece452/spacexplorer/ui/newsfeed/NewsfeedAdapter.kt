package com.ece452.spacexplorer.ui.newsfeed

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.squareup.picasso.Picasso
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ece452.spacexplorer.R
import com.ece452.spacexplorer.networking.models.NewsCommentResponse
import com.ece452.spacexplorer.networking.models.data.NewsResponse
import com.ece452.spacexplorer.networking.models.userinteractions.LikesDislikesType
import com.ece452.spacexplorer.utils.DataManager
import com.ece452.spacexplorer.utils.LikeDislikeManager
import com.ece452.spacexplorer.utils.UserInteractionsManager

class NewsfeedAdapter(
    private val context: Context,
    private var events: MutableList<NewsResponse>,
    private val parentRecyclerView: RecyclerView
) : RecyclerView.Adapter<NewsfeedAdapter.ViewHolder>() {

    private val newsComments: MutableMap<String, ArrayList<NewsCommentResponse>> = mutableMapOf()
    private val likeStatuses: MutableMap<String, String> = mutableMapOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val likeButton: ToggleButton = itemView.findViewById(R.id.like_button)
        val dislikeButton: ToggleButton = itemView.findViewById(R.id.dislike_button)
        val commentButton: ToggleButton = itemView.findViewById(R.id.comment_button)
        val addCommentContainer: LinearLayout = itemView.findViewById(R.id.add_comment_container)
        val commentInput: EditText = itemView.findViewById(R.id.comment_input)
        val sendCommentButton: ImageButton = itemView.findViewById(R.id.send_comment_button)
        val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.comments_recycler_view)
        val title: TextView = itemView.findViewById(R.id.newscard_heading)
        val description: TextView = itemView.findViewById(R.id.newscard_description)
        val image: ImageView = itemView.findViewById(R.id.newscard_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.newsfeed_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        likeStatuses[event.article_id] = event.like_status
        val likeStatus = likeStatuses[event.article_id]

        holder.commentButton.text = event.comment_count.toString()
        holder.commentButton.textOff = event.comment_count.toString()
        holder.commentButton.textOn = event.comment_count.toString()

        holder.title.text = event.title
        holder.description.text = event.description
        holder.likeButton.text = event.likes.toString()
        holder.dislikeButton.text = event.dislikes.toString()
        holder.likeButton.textOn = event.likes.toString()
        holder.dislikeButton.textOn = event.dislikes.toString()
        holder.likeButton.textOff = event.likes.toString()
        holder.dislikeButton.textOff = event.dislikes.toString()

        holder.image.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
            startActivity(context, i, null)
        }

        Picasso.get()
            .load(event.url_to_image).fit()
            .into(holder.image)

        holder.likeButton.isChecked = (likeStatus == "like")
        holder.dislikeButton.isChecked = (likeStatus == "dislike")

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

        holder.commentButton.setOnClickListener {
            toggleCommentsVisibility(holder, event.article_id, position)
        }

        holder.sendCommentButton.setOnClickListener {
            handleCommentSubmission(holder, position)
        }

        // Set touch listener to disable parent RecyclerView scroll
        holder.commentsRecyclerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> parentRecyclerView.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parentRecyclerView.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    override fun getItemCount(): Int = events.size

    private fun handleLike(holder: ViewHolder, position: Int) {
        val event = events[position]
        updateLikeStatus(holder, LikesDislikesType.LIKE, event.article_id)
        likeStatuses[event.article_id] = "like"
    }

    private fun handleUnlike(holder: ViewHolder, position: Int) {
        val event = events[position]
        updateLikeStatus(holder, LikesDislikesType.UNLIKE, event.article_id)
        likeStatuses[event.article_id] = "neutral"
    }

    private fun handleDislike(holder: ViewHolder, position: Int) {
        val event = events[position]
        updateLikeStatus(holder, LikesDislikesType.DISLIKE, event.article_id)
        likeStatuses[event.article_id] = "dislike"
    }

    private fun handleUndislike(holder: ViewHolder, position: Int) {
        val event = events[position]
        updateLikeStatus(holder, LikesDislikesType.UNDISLIKE, event.article_id)
        likeStatuses[event.article_id] = "neutral"
    }

    private fun updateLikeStatus(holder: ViewHolder, interaction: LikesDislikesType, articleId: String) {
        UserInteractionsManager.putNewsLike(interaction, articleId) { success, response ->
            if (success) {
                response?.let {
                    holder.likeButton.text = it.likes.toString()
                    holder.dislikeButton.text = it.dislikes.toString()
                    holder.likeButton.textOn = it.likes.toString()
                    holder.dislikeButton.textOn = it.dislikes.toString()
                    holder.likeButton.textOff = it.likes.toString()
                    holder.dislikeButton.textOff = it.dislikes.toString()
                }
            } else {
                Log.e("NewsfeedAdapter", "Failed to update like status for article $articleId")
            }
        }
    }

    private fun handleCommentSubmission(holder: ViewHolder, position: Int) {
        val commentText = holder.commentInput.text.toString().trim()
        if (commentText.isNotEmpty()) {
            val event = events[position]

            UserInteractionsManager.putNewsComment(event.article_id, commentText) { success, _ ->
                if (success) {
                    fetchComments(event.article_id) {
                        val comments = newsComments[event.article_id] ?: arrayListOf()
                        holder.commentButton.text = comments.size.toString()
                        holder.commentButton.textOff = comments.size.toString()
                        holder.commentButton.textOn = comments.size.toString()
                        if (holder.addCommentContainer.visibility == View.VISIBLE) {
                            updateCommentsRecyclerView(holder, comments)
                        }
                        holder.commentInput.text = null
                    }
                } else {
                    Toast.makeText(holder.itemView.context, "Failed to submit comment", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(holder.itemView.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchComments(articleId: String, callback: () -> Unit) {
        DataManager.getNewsComments(articleId) { success, commentsList ->
            if (success) {
                commentsList?.let { fetchedComments ->
                    newsComments[articleId] = fetchedComments
                }
                callback()
            } else {
                Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleCommentsVisibility(holder: ViewHolder, articleId: String, position: Int) {
        if (holder.addCommentContainer.visibility == View.GONE) {
            fetchComments(articleId) {
                val comments = newsComments[articleId] ?: arrayListOf()
                holder.commentButton.text = comments.size.toString()
                holder.commentButton.textOff = comments.size.toString()
                holder.commentButton.textOn = comments.size.toString()
                if (holder.addCommentContainer.visibility == View.VISIBLE) {
                    updateCommentsRecyclerView(holder, comments)
                }
            }
            holder.addCommentContainer.visibility = View.VISIBLE
            updateCommentsRecyclerView(holder, newsComments[articleId] ?: arrayListOf())
        } else {
            holder.addCommentContainer.visibility = View.GONE
        }
    }


    private fun updateCommentsRecyclerView(holder: ViewHolder, comments: ArrayList<NewsCommentResponse>) {
        val commentAdapter = CommentAdapter(context, comments)
        holder.commentsRecyclerView.adapter = commentAdapter
        holder.commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        commentAdapter.notifyDataSetChanged()
    }
}
