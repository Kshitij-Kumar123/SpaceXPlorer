package com.ece452.spacexplorer.ui.newsfeed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ece452.spacexplorer.R
import com.ece452.spacexplorer.networking.models.data.NewsResponse
import com.ece452.spacexplorer.utils.DataManager

class NewsfeedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsfeedAdapter: NewsfeedAdapter
    private lateinit var searchView: SearchView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var events: ArrayList<NewsResponse> = arrayListOf()
    private var filteredEvents: ArrayList<NewsResponse> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_newsfeed, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_newsfeed)
        searchView = view.findViewById(R.id.search_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        recyclerView.layoutManager = LinearLayoutManager(context)
        fetchNews()

        newsfeedAdapter = NewsfeedAdapter(
            context = requireContext(),
            events = filteredEvents,
            parentRecyclerView = recyclerView
        )

        recyclerView.adapter = newsfeedAdapter

        setupSearchView()

        swipeRefreshLayout.setOnRefreshListener {
            refreshEvents()
        }

        return view
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterEvents(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterEvents(it) }
                return true
            }
        })
    }

    private fun filterEvents(query: String) {
        filteredEvents.clear()
        if (query.isEmpty()) {
            filteredEvents.addAll(events)
        } else {
            filteredEvents.addAll(events.filter { event ->
                event.title.contains(query, ignoreCase = true) ||
                        event.description.contains(query, ignoreCase = true)
            })
        }
        newsfeedAdapter.notifyDataSetChanged()
    }

    private fun refreshEvents() {
        fetchNews()
    }

    private fun fetchNews() {
        DataManager.getNews { success, newsList ->
            if (success) {
                newsList?.let {
                    events.clear()
                    events.addAll(it)
                    filterEvents(searchView.query.toString())
                }
            } else {
                Toast.makeText(context, "Failed to load news", Toast.LENGTH_SHORT).show()
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }
}
