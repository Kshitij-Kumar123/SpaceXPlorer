package com.ece452.spacexplorer.ui.events

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ece452.spacexplorer.R
import com.ece452.spacexplorer.databinding.FragmentEventsBinding
import com.ece452.spacexplorer.networking.models.data.EventResponse
import com.ece452.spacexplorer.utils.DataManager
import com.ece452.spacexplorer.utils.UserInteractionsManager

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventsViewModel = ViewModelProvider(this)[EventsViewModel::class.java]
        val swipeRefreshLayout = binding.swiperefresh
        swipeRefreshLayout.isRefreshing = true
        binding.eventCardContainer.removeAllViews()


        DataManager.getUserEvents { success, events ->
            if (success) {
                Log.d("Getting Subscribed Events", "User Events: $events")
                if (events != null) {
                    eventsViewModel.setEvents(events)
                }
            } else {
                Log.e("Getting Subscribed Events", "Error: Failed to get user events")
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            DataManager.getUserEvents { success, events ->
                if (success) {
                    Log.d("GettingStartedActivity", "User Events: $events")
                    if (events != null) {
                        eventsViewModel.setEvents(events)
                    }
                } else {
                    Log.e("GettingStartedActivity", "Error: Failed to get user events")
                }
            }
        }

        eventsViewModel.events.observe(viewLifecycleOwner) { newEvents ->
            updateEvents(newEvents) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun updateEvents(newEvents: ArrayList<EventResponse>, afterUpdate: () -> Unit = {}) {
        val inflater = LayoutInflater.from(context)
        val eventCardHelper =
            EventCardHelper(requireContext(), inflater, binding.eventCardContainer)
        binding.eventCardContainer.removeAllViews()

        if (newEvents.isEmpty()) {
            binding.noEventsText.visibility = View.VISIBLE
            afterUpdate()
            return
        } else {
            binding.noEventsText.visibility = View.GONE
        }

        for (event in newEvents) {
            val eventCard = eventCardHelper.createEventCard(event)
            val subscribeButton = eventCard.findViewById<ToggleButton>(R.id.subscribe_button)
            subscribeButton.isSelected = true

            subscribeButton.apply {
                setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (event.is_subscribed) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                    ),
                    null,
                    null
                )
                setOnCheckedChangeListener { _, _ ->

                    UserInteractionsManager.deleteUserSubscription(
                        event.event_id,
                    ) { success, response ->
                        if (success) {
                            Log.d("EventCardSubscribe", "User Subscriptions: $response")
                            Toast.makeText(
                                requireContext(),
                                "Unsubscribed from ${eventCardHelper.getEventName(event)}",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Log.e("EventCardSubscribe", "Error: Failed to subscribe")
                        }
                    }

                    slideRight(eventCard)
                    Handler(Looper.getMainLooper()).postDelayed({
                        slideUpRemainingCards(eventCard)
                        binding.eventCardContainer.removeView(eventCard)
                    }, 400)
                }

//                Changing the drawableTop icon for the toggle button for subscribe button
                setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (isSelected) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                    ),
                    null,
                    null
                )
            }
            binding.eventCardContainer.addView(eventCard, createLayoutParams())
        }
        afterUpdate()
    }

    private fun slideUpRemainingCards(event: View) {
        val container = binding.eventCardContainer
        val indexStart = container.indexOfChild(event) + 1
        for (i in indexStart until container.childCount) {
            val childView = container.getChildAt(i)
            val animate = TranslateAnimation(
                0f,
                0f,
                childView.height.toFloat(),
                0f
            )
            animate.duration = 400
            animate.fillAfter = true
            childView.startAnimation(animate)
        }
    }

    private fun slideRight(view: View) {
        val parentWidth = view.width
        val animate = TranslateAnimation(
            0f,
            parentWidth.toFloat(),
            0f,
            0f
        )
        animate.duration = 400
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    private fun createLayoutParams(): ViewGroup.MarginLayoutParams {
        val layoutParamWithMargin = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParamWithMargin.setMargins(8, 8, 8, 16)
        return layoutParamWithMargin
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
