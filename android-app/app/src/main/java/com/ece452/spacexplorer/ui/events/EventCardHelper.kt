package com.ece452.spacexplorer.ui.events

import DetailItem
import DetailListAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.ece452.spacexplorer.R
import com.ece452.spacexplorer.networking.models.EventCommentResponse
import com.ece452.spacexplorer.networking.models.data.Donki
import com.ece452.spacexplorer.networking.models.data.EventResponse
import com.ece452.spacexplorer.networking.models.data.EventType
import com.ece452.spacexplorer.networking.models.data.Launch
import com.ece452.spacexplorer.networking.models.data.Neo
import com.ece452.spacexplorer.networking.models.data.Solar
import com.ece452.spacexplorer.networking.models.data.propertiesToList
import com.ece452.spacexplorer.networking.models.userinteractions.LikesDislikesType
import com.ece452.spacexplorer.utils.DataManager
import com.ece452.spacexplorer.utils.UserInteractionsManager
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class EventCardHelper(
    private val context: Context,
    private val inflater: LayoutInflater,
    private val container: ViewGroup
) {
    private val lock = Any()

    @SuppressLint("UseCompatTextViewDrawableApis")
    fun createEventCard(event: EventResponse): View {
        val eventCard = inflater.inflate(R.layout.event_card, container, false)
        val expandableLayout = eventCard.findViewById<LinearLayout>(R.id.expandable_card)
        val subTitle = eventCard.findViewById<TextView>(R.id.subtitle_text)
        val expandIcon = eventCard.findViewById<ImageView>(R.id.collapse_button)
        val cardDescriptionContainer = eventCard.findViewById<LinearLayout>(R.id.description)
        val cardImage = eventCard.findViewById<ImageView>(R.id.card_image)
        val commentLayout = eventCard.findViewById<LinearLayout>(R.id.scrolling_layout)
        val addCommentContainer =
            eventCard.findViewById<ConstraintLayout>(R.id.add_comment_container)
        val sendCommentButton = eventCard.findViewById<ImageButton>(R.id.send_comment_button)
        val likeButton = eventCard.findViewById<ToggleButton>(R.id.like_button)
        val dislikeButton = eventCard.findViewById<ToggleButton>(R.id.dislike_button)
        val commentScroll = eventCard.findViewById<NestedScrollView>(R.id.nested_scroll_view)
        val descriptionList = eventCard.findViewById<ListView>(R.id.descriptionList)

        cardImage.visibility = View.VISIBLE

        when (event) {

            is Donki -> {
//                Show summary of the Donki event text if it exists as subtitle in card
                val summaryParagraph = findSummary(event.message_body)
                setupEventCardHeading(
                    eventCard,
                    getEventName(event),
                    ""
                )
                subTitle.text = summaryParagraph
                setupDescription(descriptionList, event)
//                card description and image container is not visible for this event
                cardDescriptionContainer.visibility = View.GONE
                cardImage.visibility = View.GONE
            }

            is Launch -> {

                setupEventCardHeading(
                    eventCard,
                    getEventName(event),
                    event.status_desc + " " + event.mission_info
                )

                likeButton.text = event.likes.toString()
                dislikeButton.text = event.dislikes.toString()


//                Load image src from our API response, with placeholder rocket image
                Picasso.get()
                    .load(event.launch_image)
                    .placeholder(R.drawable.rocket)
                    .into(cardImage)

                cardImage.layoutParams.height = 650

                val descriptionCard = inflater.inflate(
                    R.layout.event_description_card,
                    cardDescriptionContainer,
                    false
                )

//                For card description, show the mission and pad name for the launch event
                val valueTextView = descriptionCard.findViewById<TextView>(R.id.value_text)
                val labelTextView = descriptionCard.findViewById<TextView>(R.id.label_text)
                valueTextView.text = event.mission_name
                labelTextView.text = this.context.getString(R.string.mission_name)
                cardDescriptionContainer.addView(descriptionCard)

                val descriptionCard2 = inflater.inflate(
                    R.layout.event_description_card,
                    cardDescriptionContainer,
                    false
                )
                val valueTextView2 = descriptionCard2.findViewById<TextView>(R.id.value_text)
                val labelTextView2 = descriptionCard2.findViewById<TextView>(R.id.label_text)
                valueTextView2.text = event.pad_name
                labelTextView2.text = this.context.getString(R.string.pad_name)
                cardDescriptionContainer.addView(descriptionCard2)
            }

            is Neo -> {
                cardDescriptionContainer.visibility = View.GONE
                setupEventCardHeading(eventCard, getEventName(event), event.name)

                var imgSrc = R.drawable.asteroid

//                select img src based on event properties like hazardous or NEO size
                if (event.is_hazardous) {
                    imgSrc = R.drawable.hazardous_asteroid
                } else if (event.diameter > 0.5) {
                    imgSrc = R.drawable.large_asteroid
                } else if (event.diameter < 0.2) {
                    imgSrc = R.drawable.small_asteroid
                }

                cardImage.setImageResource(imgSrc)
                setupDescription(descriptionList, event)
            }

            is Solar -> {
                var imgSrc = R.drawable.solar_eclipse
                when (event.eclipse_type) {
                    "Annular" -> {
                        imgSrc = R.drawable.annular_eclipse
                    }

                    "Partial" -> {
                        imgSrc = R.drawable.partial_eclipse
                    }

                    "Hybrid" -> {
                        imgSrc = R.drawable.hybrid_eclipse
                    }
                }
                cardDescriptionContainer.visibility = View.GONE
                cardImage.setImageResource(imgSrc)

                setupEventCardHeading(eventCard, getEventName(event), "")
                setupDescription(descriptionList, event)
            }
        }

        setupSendCommentButton(
            event.event_id,
            sendCommentButton,
            commentLayout,
            addCommentContainer,
            commentScroll
        )

        likeButton.text = event.likes.toString()
        dislikeButton.text = event.dislikes.toString()

        likeButton.isChecked = event.like_status == "like"
        dislikeButton.isChecked = event.like_status == "dislike"

//        compoundDrawableTint adds a purple tint to the button, showing if button
//        has been selected or not
        if (likeButton.isChecked) {
            likeButton.compoundDrawableTintList =
                ContextCompat.getColorStateList(context, R.color.purple_200)
        }

        if (dislikeButton.isChecked) {
            dislikeButton.compoundDrawableTintList =
                ContextCompat.getColorStateList(context, R.color.purple_200)
        }

        setupLikeOrDislikeButton(
            likeButton,
            dislikeButton,
            event.likes,
            event.dislikes,
            LikesDislikesType.LIKE,
            event.event_id,
            "",
            ::likeOrDislikeEventApiCall
        )
        setupLikeOrDislikeButton(
            dislikeButton,
            likeButton,
            event.dislikes,
            event.likes,
            LikesDislikesType.DISLIKE,
            event.event_id,
            "",
            ::likeOrDislikeEventApiCall
        )


        var isExpanded = false
        expandIcon.setOnClickListener {
            if (isExpanded) {
                collapse(expandableLayout, expandIcon, cardDescriptionContainer)
                commentLayout.removeAllViews()
            } else {
                expand(expandableLayout, expandIcon)
                loadComments(commentLayout, commentScroll, event.event_id)
                setupDescription(descriptionList, event)
            }
            isExpanded = !isExpanded
        }

        eventCard.setOnClickListener {
            if (isExpanded) {
                collapse(expandableLayout, expandIcon, cardDescriptionContainer)
                commentLayout.removeAllViews()
            } else {
                expand(expandableLayout, expandIcon)
                loadComments(commentLayout, commentScroll, event.event_id)
                setupDescription(descriptionList, event)
            }
            isExpanded = !isExpanded
        }
        return eventCard
    }

    private fun findSummary(message: String): String? {
//        Get rid of indents, every heading in the message is prefixed with ##
//        so we can split on ## and trim out i.e. remove spaces and get rid of blank lines
//        get then the paragraph which starts the summary
        val paragraphs =
            message.trimIndent().split("##").map { "##$it".trim() }.filter { it.isNotBlank() }
        return paragraphs.find { it.startsWith("## Summary:") }
    }

    fun getEventName(event: EventResponse): String {
        when (event) {
            is Donki -> {
                return EventType.DONKI.name.replaceFirstChar { it.uppercase() }
            }

            is Launch -> {
                return event.name.replaceFirstChar { it.uppercase() }
            }

            is Neo -> {
                return "Near Earth Object (NEO)"
            }

            is Solar -> {
                val eventType = event.event_type.replaceFirstChar { it.uppercase() }
                val eclipseType = event.eclipse_type.replaceFirstChar { it.uppercase() }
                return "$eventType $eclipseType Eclipse"
            }
        }
    }

    companion object {
        fun getCommentTime(utcTimestamp: String): String {
            val utcTimestampDateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            utcTimestampDateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")

            val utcTimestampDate: Date =
                utcTimestampDateFormat.parse(utcTimestamp) ?: return ""
            val currentTimestampDate = Date()
            val duration = currentTimestampDate.time - utcTimestampDate.time

            val days = TimeUnit.MILLISECONDS.toDays(duration)
            val hours = TimeUnit.MILLISECONDS.toHours(duration)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration)

            return when {
                days > 1 -> "$days days ago"
                days == 1L -> "1 day ago"
                hours > 1 -> "$hours hours ago"
                hours == 1L -> "1 hour ago"
                minutes > 1 -> "$minutes minutes ago"
                minutes == 1L -> "1 min ago"
                else -> "$seconds seconds ago"
            }
        }
    }

    private fun setupEventCardHeading(eventCard: View, title: String, subTitle: String) {
        val cardTitle = eventCard.findViewById<TextView>(R.id.title_text)
        val cardSubTitle = eventCard.findViewById<TextView>(R.id.subtitle_text)
        cardTitle.text = title
        cardSubTitle.text = subTitle
    }

    private fun setupDescription(descriptionList: ListView, event: EventResponse) {
        val descriptions = propertiesToList(event)

//        these are event attributes which are already shown elsewhere in the card
//        so, do not reshow it again
        val descriptionsToNotShow = listOf(
            "event_type", "event_id", "likes", "dislikes",
            "name", "mission_info", "launch_image",
            "status_success", "status_desc", "status_code", "last_updated",
            "message_body", "is_subscribed", "like_status"
        )

        val values = ArrayList<DetailItem>()
        for (description in descriptions) {
            val currentDescription =
                DetailItem(description.first, description.second.toString())
            if (description.first !in descriptionsToNotShow) {
                values.add(currentDescription)
            }
        }

//        Custom DetailItem Adapter class to view items in a ListView
        val adapter = DetailListAdapter(context, R.layout.list_item, values)
        descriptionList.adapter = adapter

        setListViewHeightBasedOnChildren(descriptionList)
    }

    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        var finalHeight = 0
        val unboundedHeightMeasure =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

//        change the height of the card by measuring the height of every card
//        and then adding it to the final height
//        set this final height to the listView's height
        for (i in 0 until listView.adapter.count) {
            val item = listView.adapter.getView(i, null, listView)
            item.measure(unboundedHeightMeasure, unboundedHeightMeasure)
            finalHeight += item.measuredHeight
        }
        listView.layoutParams.height = finalHeight
    }

    private fun setupSendCommentButton(
        eventId: String,
        sendCommentButton: ImageButton,
        commentContainer: LinearLayout,
        addCommentContainer: ConstraintLayout,
        commentLayout: NestedScrollView
    ) {
        sendCommentButton.setOnClickListener {
            val editText = addCommentContainer.findViewById<EditText>(R.id.comment_input)
            val commentValue = editText.text.toString()
            UserInteractionsManager.putEventComment(
                eventId,
                commentValue,
            ) { success, response ->
                if (success) {
                    if (response != null) {
                        Toast.makeText(context, "Comment added", Toast.LENGTH_SHORT).show()
                        val commentCard = createCommentCard(response)

//                        create and add a comment card to the container,
//                        show it to the user by scrolling the nested scroll view
                        commentContainer.addView(commentCard, createLayoutParams())
                        commentLayout.visibility = View.VISIBLE
                        commentLayout.layoutParams.height = 600
                        editText.text.clear()
                        commentLayout.post {
                            commentLayout.fullScroll(NestedScrollView.FOCUS_DOWN)
                        }
                    }
                } else {
                    Log.e("setupSendCommentButton", "Error: Failed to comment on event")
                }
            }
        }
    }

    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun createCommentCard(comment: EventCommentResponse): View? {
        val commentCard = inflater.inflate(R.layout.comment_card, container, false)
        val usernameText = commentCard.findViewById<TextView>(R.id.username)
        val timestampText = commentCard.findViewById<TextView>(R.id.timestamp)
        val commentText = commentCard.findViewById<TextView>(R.id.comment_text)
        val likeButton = commentCard.findViewById<ToggleButton>(R.id.like_button)
        val dislikeButton = commentCard.findViewById<ToggleButton>(R.id.dislike_button)

        likeButton.text = comment.likes.toString()
        dislikeButton.text = comment.dislikes.toString()

        likeButton.isChecked = comment.like_status == "like"
        dislikeButton.isChecked = comment.like_status == "dislike"

        if (likeButton.isChecked) {
            likeButton.compoundDrawableTintList =
                ContextCompat.getColorStateList(context, R.color.purple_200)
        }

        if (dislikeButton.isChecked) {
            dislikeButton.compoundDrawableTintList =
                ContextCompat.getColorStateList(context, R.color.purple_200)
        }

        setupLikeOrDislikeButton(
            likeButton,
            dislikeButton,
            comment.likes,
            comment.dislikes,
            LikesDislikesType.LIKE,
            comment.event_id,
            comment.comment_id,
            ::likeOrDislikeCommentApiCall
        )
        setupLikeOrDislikeButton(
            dislikeButton,
            likeButton,
            comment.dislikes,
            comment.likes,
            LikesDislikesType.DISLIKE,
            comment.event_id,
            comment.comment_id,
            ::likeOrDislikeCommentApiCall
        )

        commentText.text = comment.comment
        usernameText.text = comment.username
        timestampText.text = getCommentTime(comment.timestamp)

        return commentCard
    }

    private fun likeOrDislikeEventApiCall(
        action: LikesDislikesType,
        eventId: String,
        commentId: String = "",
        callback: (Boolean) -> Unit
    ) {
        UserInteractionsManager.putEventLike(
            action,
            eventId
        ) { success, _ ->
            if (success) {
                callback(true)
            } else {
                Log.e("LikeOrDislikeEvent", "Error: Failed to like event")
                callback(false)
            }
        }
    }

    private fun likeOrDislikeCommentApiCall(
        action: LikesDislikesType,
        eventId: String,
        commentId: String,
        callback: (Boolean) -> Unit
    ) {

        UserInteractionsManager.putEventCommentLike(
            action,
            eventId,
            commentId
        ) { success, response ->
            if (success) {
                callback(true)
            } else {
                Log.e("likeOrDislikeCommentApiCall", "Error: Failed to like comment: $response")
                callback(false)
            }
        }
    }


    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun setupLikeOrDislikeButton(
        toggleButton: ToggleButton,
        otherToggleButton: ToggleButton,
        count: Int = 0,
        otherCount: Int = 0,
        type: LikesDislikesType,
        eventId: String = "",
        commentId: String = "",
        callFunction: (LikesDislikesType, String, String, (Boolean) -> Unit) -> Unit
    ) {

        fun updateButtons(button: ToggleButton, isChecked: Boolean, value: Int) {
            // util function to update a button
            button.isChecked = isChecked
            button.textOff = value.toString()
            button.textOn = value.toString()
            button.text = value.toString()
            val color = if (isChecked) R.color.purple_200 else R.color.black
            button.compoundDrawableTintList = ContextCompat.getColorStateList(context, color)

        }

        toggleButton.text = count.toString()
        otherToggleButton.text = otherCount.toString()

        toggleButton.textOn = count.toString()
        otherToggleButton.textOn = otherCount.toString()

        toggleButton.textOff = count.toString()
        otherToggleButton.textOff = otherCount.toString()

        toggleButton.setOnClickListener { _ ->

            // The lock use here isnt required for normal use, its mainly to handle situations where the event
            // handler is called twice in rapid succession, IE someone likes something and
            // immediately presses dislike *before* the like routine finishes.
            // This lock stops the second invocation of the event handler from proceeding until after
            // the first call has completed.
            synchronized(lock) {
                if (otherToggleButton.isChecked) {
                    // Case where we need to swap because the other button is selected

                    // Define the operation that needs to be done to return to a neutral state
                    val otherType =
                        if (type == LikesDislikesType.LIKE) LikesDislikesType.UNDISLIKE else LikesDislikesType.UNLIKE


                    // Make a function call to reset to a neutral state based on the defined reverse operation
                    callFunction(otherType, eventId, commentId) { success ->
                        if (success) {
                            // If successful in resetting the state of likes/dislikes update the other button to reflect the decrement

                            // Updated "Other Button"
                            updateButtons(
                                otherToggleButton,
                                false,
                                otherToggleButton.text.toString().toInt() - 1
                            )

                            // Make an api call to update the toggled button with its selected action
                            callFunction(type, eventId, commentId) { success2 ->

                                if (success2) {
                                    // If successful update the clicked buttons UI
                                    updateButtons(
                                        toggleButton,
                                        true,
                                        toggleButton.text.toString().toInt() + 1
                                    )

                                }
                            }


                        } else {
                            // Error handling on API failing
                        }
                    }
                } else {
                    // Case where other button is not selected
                    if (toggleButton.isChecked) {
                        // Weird nomenclature, this executes if the button that was selected is clicked, IE the button is being toggled on

                        // Make API call to Like/Dislike the event based on clicked button
                        callFunction(type, eventId, commentId) { success2 ->
                            if (success2) {
                                // Update UI
                                updateButtons(
                                    toggleButton,
                                    true,
                                    toggleButton.text.toString().toInt() + 1
                                )

                            }
                        }


                    } else {
                        // This executes if the button is being deselected IE undo the change that was made


                        val otherType =
                            if (type == LikesDislikesType.LIKE) LikesDislikesType.UNLIKE else LikesDislikesType.UNDISLIKE


                        // Call function to undo the change (Unlike or Undislike)
                        callFunction(otherType, eventId, commentId) { success3 ->
                            if (success3) {
                                // update UI of button
                                updateButtons(
                                    toggleButton,
                                    false,
                                    toggleButton.text.toString().toInt() - 1
                                )


                            }
                        }
                    }
                }
            }
        }
    }


    private fun expand(
        expandableLayout: LinearLayout,
        expandIcon: ImageView,
    ) {
        expandableLayout.visibility = View.VISIBLE
        expandableLayout.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        expandableLayout.visibility = View.VISIBLE
        expandIcon.animate().rotation(180f).setDuration(300).start()
    }

    private fun collapse(
        expandableLayout: LinearLayout,
        expandIcon: ImageView,
        cardDescription: LinearLayout
    ) {
        expandableLayout.visibility = View.GONE
        cardDescription.layoutParams.height = 200
        expandIcon.animate().rotation(0f).setDuration(300).start()
    }

    private fun loadComments(
        commentContainer: LinearLayout,
        commentScroll: NestedScrollView,
        eventId: String
    ) {
        commentContainer.removeAllViews()
        DataManager.getEventComments(
            eventId,
        ) { success, comments ->
            if (success) {
                if (comments != null) {
                    commentScroll.visibility = View.GONE
                    if (comments.isNotEmpty()) {
                        commentScroll.visibility = View.VISIBLE
                        for (comment in comments) {
                            val commentCard = createCommentCard(comment)
                            commentContainer.addView(commentCard, createLayoutParams())
                        }
                    }
                }
            } else {
                Log.e("LoadComments", "Error: Failed to get comments $comments")
            }
        }
    }

    private fun createLayoutParams(): ViewGroup.MarginLayoutParams {
        val layoutParamWithMargin = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layoutParamWithMargin.setMargins(8, 8, 8, 16)
        return layoutParamWithMargin
    }
}
