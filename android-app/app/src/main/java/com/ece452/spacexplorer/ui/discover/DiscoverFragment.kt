package com.ece452.spacexplorer.ui.discover

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.SearchView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ece452.spacexplorer.R
import com.ece452.spacexplorer.databinding.FragmentDiscoverBinding
import com.ece452.spacexplorer.networking.models.data.EventResponse
import com.ece452.spacexplorer.networking.models.data.EventType
import com.ece452.spacexplorer.networking.models.data.Launch
import com.ece452.spacexplorer.networking.models.data.Solar
import com.ece452.spacexplorer.ui.events.EventCardHelper
import com.ece452.spacexplorer.utils.DataManager
import com.ece452.spacexplorer.utils.UserInteractionsManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import com.ece452.spacexplorer.utils.NotificationManager

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    private var allEvents = ArrayList<EventResponse>()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var topicsDropdown: Spinner
    private lateinit var distanceDropdown: Spinner
    private lateinit var notificationManager: NotificationManager
    private var NOTIFICATION_ID = 1

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventsViewModel = ViewModelProvider(this)[DiscoverViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val swipeRefreshLayout = binding.swiperefresh
        swipeRefreshLayout.isRefreshing = true
        binding.eventCardContainer.removeAllViews()
        val root: View = binding.root

        // Create two arrays to store all the possible options for the topics and locations filters
        val topicFilterDropdownItems = arrayOf("Select a Topic", "Near Earth Objects", "Donki", "Launches", "Eclipses", "All Topics")
        val distanceFilterDropdownItems = arrayOf("Location Range", ">10km", ">50km", ">100km", "Anywhere")

        // Find the searchBar, topicsDropdown, and distanceDropdown views
        val searchBar = root.findViewById<SearchView>(R.id.searchView)
        topicsDropdown = root.findViewById(R.id.topic_spinner)
        distanceDropdown = root.findViewById(R.id.distance_spinner)

        // Add the options to the topics dropdown
        val topicsAdapter = DropdownAdapter(requireContext(), android.R.layout.simple_spinner_item, topicFilterDropdownItems)
        topicsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        topicsDropdown.adapter = topicsAdapter

        // Add the options to the distance dropdown
        val distanceAdapter = DropdownAdapter(requireContext(), android.R.layout.simple_spinner_item, distanceFilterDropdownItems)
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        distanceDropdown.adapter = distanceAdapter

        // When the fragment is initially created get all the events without applying any filtering
        val queryParams: ArrayList<EventType> = ArrayList()
        queryParams.addAll(listOf(EventType.NEO,EventType.DONKI,EventType.LAUNCH,EventType.SOLAR))
        getEvents(queryParams, eventsViewModel)

        // search logic for the search bar
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //Search on enter
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    search(it,eventsViewModel)
                }
                return true
            }

            //Search well text is being input
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    search(it,eventsViewModel)
                }
                return true
            }
        })

        // apply the topics filter when a filter option is selected
        topicsDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                // get the current selected filter option
                val selectedTopic = parent.getItemAtPosition(position) as String
                queryParams.clear()

                // set the queryParams for getting events based in selected filter
                when (selectedTopic) {
                    "Near Earth Objects" -> queryParams.add(EventType.NEO)
                    "Donki" -> queryParams.add(EventType.DONKI)
                    "Launches" -> queryParams.add(EventType.LAUNCH)
                    "Eclipses" -> queryParams.add(EventType.SOLAR)
                    else -> queryParams.addAll(listOf(EventType.NEO,EventType.DONKI,EventType.LAUNCH,EventType.SOLAR))
                }

                // Check if the location filter is being applied and apply that to the returned events as well if it is
                if (distanceDropdown.selectedItem == "Location Range" || distanceDropdown.selectedItem == "Anywhere") {
                    // If distance filter is not set just get the events like normal
                    getEvents(queryParams, eventsViewModel)
                } else {
                    // if the distance filter is selected manually filter Launch events and Eclipse events manually
                    // This is because these are the only two events with location data and if get events is called the location filter changes will be overwritten
                    val eventsInRange = ArrayList<EventResponse>()

                    eventsViewModel.events.observe(viewLifecycleOwner) { events ->
                        events.forEach { event ->
                            if ((selectedTopic == "Launches" && event is Launch) || (selectedTopic == "Eclipses" && event is Solar)) {
                                eventsInRange.add(event)
                            }
                        }
                    }
                    eventsViewModel.setEvents(java.util.ArrayList(eventsInRange.take(10)))
                }
            }

            // If the filter is not selected get all events
            override fun onNothingSelected(parent: AdapterView<*>) {
                queryParams.addAll(listOf(EventType.NEO,EventType.DONKI,EventType.LAUNCH,EventType.SOLAR))
                getEvents(queryParams, eventsViewModel)
            }
        }

        // apply the distance filter when a distance filter is selected
        distanceDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                // get the current selected filter option
                val selectedDistance = parent.getItemAtPosition(position) as String

                // apply the appropriate location filter based on the selected filter option
                when (selectedDistance) {
                    ">10km" -> locationFilter(10.0, eventsViewModel)
                    ">50km" -> locationFilter(50.0, eventsViewModel)
                    ">100km" -> locationFilter(100.0, eventsViewModel)
                    else -> {
                        if (allEvents.isNotEmpty()) eventsViewModel.setEvents(java.util.ArrayList(allEvents.take(10)))
                        else eventsViewModel.setEvents(arrayListOf())
                    }
                }
            }

            // if filter is not selected don't apply any distance filtering
            override fun onNothingSelected(parent: AdapterView<*>) {
                if (allEvents.isNotEmpty()) eventsViewModel.setEvents(java.util.ArrayList(allEvents.take(10)))
                else eventsViewModel.setEvents(arrayListOf())
            }
        }

        // logic to refresh and call get events if user triggers a swipeRefresh
        swipeRefreshLayout.setOnRefreshListener {
            if (distanceDropdown.selectedItem == "Location Range" || distanceDropdown.selectedItem == "Anywhere") {
                getEvents(queryParams, eventsViewModel)
            } else {
                val eventsInRange = ArrayList<EventResponse>()

                eventsViewModel.events.observe(viewLifecycleOwner) { events ->
                    events.forEach { event ->
                        if ((topicsDropdown.selectedItem == "Launches" && event is Launch) || (topicsDropdown.selectedItem == "Eclipses" && event is Solar)) {
                            eventsInRange.add(event)
                        }
                    }
                }
                eventsViewModel.setEvents(java.util.ArrayList(eventsInRange.take(10)))
            }
        }

        eventsViewModel.events.observe(viewLifecycleOwner) { newEvents ->
            updateEvents(newEvents) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    // clear the filters if a user clicks into a new fragment and then back into the discover fragment
    override fun onResume() {
        super.onResume()
        topicsDropdown.setSelection(0)
        distanceDropdown.setSelection(0)
    }

    // function to handle updating events
    private fun updateEvents(newEvents: ArrayList<EventResponse>, afterUpdate: () -> Unit = {}) {
        val inflater = LayoutInflater.from(context)
        val eventCardHelper =
            EventCardHelper(requireContext(), inflater, binding.eventCardContainer)
        binding.eventCardContainer.removeAllViews()

        // Display no events text if no events are found
        if (newEvents.isEmpty()) {
            binding.noEventsText.visibility = View.VISIBLE
            afterUpdate()
            return
        } else {
            binding.noEventsText.visibility = View.GONE
        }

        // create event card for each event
        // this logic is very similar to how event cards are created on the events fragment
        for (event in newEvents) {
            val eventCard = eventCardHelper.createEventCard(event)
            val subscribeButton = eventCard.findViewById<ToggleButton>(R.id.subscribe_button)
            subscribeButton.isSelected = event.is_subscribed

            // add subscribe button functionality
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
                    if (subscribeButton.isSelected) {
                        UserInteractionsManager.deleteUserSubscription(
                            event.event_id,
                        ) { success, response ->
                            if (success) {
                                Log.d("DiscoveryFragment", "User Subscriptions: $response")
                                Toast.makeText(requireContext(), "Unsubscribed from ${eventCardHelper.getEventName(event)}", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("DiscoveryFragment", "Error: Failed to subscribe")
                            }
                        }

                        setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_outline),
                            null,
                            null
                        )

                        subscribeButton.isSelected = false
                    } else {
                        UserInteractionsManager.putUserSubscriptions(
                            event.event_id
                        ) { success, subscriptions ->
                            if (success) {
                                Log.d("DiscoveryFragment", "Updated Subscriptions: $subscriptions")
                                Toast.makeText(requireContext(), "Subscribed to ${eventCardHelper.getEventName(event)}", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("DiscoveryFragment", "Error: Failed to put user subscriptions")
                            }
                        }

                        if (event is Solar){ // Only Solar Events have timestamps that can be set for notifications
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                notificationManager.scheduleNotification(context, event.timestamp, NOTIFICATION_ID, event.event_type)
                                NOTIFICATION_ID++
                            } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                            }
                        }

                        setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_filled),
                            null,
                            null
                        )

                        subscribeButton.isSelected = true
                    }
                }
            }
            binding.eventCardContainer.addView(eventCard, createLayoutParams())
        }
        afterUpdate()
    }

    // function to create layout params
    private fun createLayoutParams(): ViewGroup.MarginLayoutParams {
        val layoutParamWithMargin = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParamWithMargin.setMargins(8, 8, 8, 16)
        return layoutParamWithMargin
    }

    // function to get events from the backend and display them based on input queryParams
    private fun getEvents(queryParams: ArrayList<EventType>, eventsViewModel:DiscoverViewModel) {
        DataManager.getEvents(queryParams) { success, events ->
            if (success) {
                // If events where received successfully from the backend update them on the front end
                // the events received are shuffled before they are displayed
                Log.d("DiscoveryFragment", "Events: $events")
                if (events != null){
                    allEvents.clear()
                    allEvents.addAll(events)
                    val shuffledEvents = events.shuffled()
                    if (shuffledEvents.isNotEmpty())eventsViewModel.setEvents(java.util.ArrayList(shuffledEvents.take(10)))
                    else eventsViewModel.setEvents(arrayListOf())
                }
            } else {
                // Log an error message if events are not successfully retrieved from the backend
                Log.e("DiscoveryFragment", "Error: Failed to get events")
            }
        }
    }

    // function to calculate the distance in km between to lat/long coordinates
    // this method of calculating distance is called the Haversine formula
    private fun calculateDistance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val latDiff = Math.toRadians(lat2 - lat1)
        val longDiff = Math.toRadians(long2 - long1)

        val calc1 = sin(latDiff / 2) * sin(latDiff / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(longDiff / 2) * sin(longDiff / 2)

        return 6371.0 * 2 * atan2(sqrt(calc1), sqrt(1 - calc1))
    }

    // function to apply the location filter
    private fun locationFilter(radius: Double, eventsViewModel: DiscoverViewModel) {
        // check if the user has location permissions enabled and then apply filter if they are
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val eventsInRange = ArrayList<EventResponse>()

                    //Check the distance between phone and location of each event
                    for (event in allEvents) {
                        if (event is Launch || event is Solar) {
                            var eventLat = 0.0
                            var eventLong = 0.0

                            if (event is Launch) {
                                eventLat = event.latitude
                                eventLong = event.longitude
                            } else if (event is Solar) {
                                eventLat = event.latitude
                                eventLong = event.longitude
                            }

                            val distanceAway = calculateDistance(eventLat, eventLong, it.latitude, it.longitude)
                            if (distanceAway < radius) eventsInRange.add(event)
                        }
                    }

                    // update the events displayed
                    if (eventsInRange.isNotEmpty()) eventsViewModel.setEvents(java.util.ArrayList(eventsInRange.take(10)))
                    else eventsViewModel.setEvents(arrayListOf())
                }
            }.addOnFailureListener {
                // Tell the user if getting the location of there device was unsuccessful
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_LONG).show()
            }
        } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request locations permissions from the user if they are not enabled
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

    }

    // logic to apply search
    private fun search(query: String, eventsViewModel: DiscoverViewModel) {

        val filteredEvents = allEvents.filter {
            when(it) {
                is Launch -> {
                    it.name.contains(query, ignoreCase = true)
                }
                else -> {
                    it.event_type.contains(query, ignoreCase = true)
                }
            }
        }
        if (filteredEvents.isNotEmpty()) eventsViewModel.setEvents(java.util.ArrayList(filteredEvents.take(10)))
        else eventsViewModel.setEvents(arrayListOf())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}