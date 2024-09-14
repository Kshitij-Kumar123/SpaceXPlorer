package com.ece452.spacexplorer.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ece452.spacexplorer.networking.models.data.EventResponse

class EventsViewModel : ViewModel() {

    private var _events = MutableLiveData<ArrayList<EventResponse>>()
    val events: LiveData<ArrayList<EventResponse>> = _events

    fun setEvents(events: ArrayList<EventResponse>) {
        _events.value = events
    }
}