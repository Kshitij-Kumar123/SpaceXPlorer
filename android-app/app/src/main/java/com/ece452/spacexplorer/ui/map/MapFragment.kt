package com.ece452.spacexplorer.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.ece452.spacexplorer.R
import com.ece452.spacexplorer.databinding.FragmentMapBinding
import com.ece452.spacexplorer.networking.models.data.EventResponse
import com.ece452.spacexplorer.networking.models.data.Launch
import com.ece452.spacexplorer.networking.models.data.Solar
import com.ece452.spacexplorer.utils.DataManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.time.Duration
import java.time.format.DateTimeParseException

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mapMarkers = ArrayList<Pair<String, LatLng>>()
    private val markerEventMap = mutableMapOf<Marker, EventResponse>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        placeEventMarkers(googleMap)
        moveToCurrentLocation(googleMap)
        googleMap.setOnMarkerClickListener { marker ->
            showEventMapCards(marker)
            true
        }
    }

    private fun placeEventMarkers(googleMap: GoogleMap) {
        DataManager.getUserEvents { success, res ->
            if (success && res!=null){
                for (event in res) {
                    when (event) {
                        is Launch -> {
                            mapMarkers.add(Pair(
                            ("Launch for " + event.mission_name + " mission from pad: " + event.pad_name),
                            LatLng(event.latitude, event.longitude)
                        ))
                            val title = "Launch for " + event.mission_name + " mission from pad: " + event.pad_name
                            val pos = LatLng(event.latitude, event.longitude)
                            val markerObj = googleMap.addMarker(MarkerOptions().position(pos).title(title))
                            if (markerObj != null){markerEventMap[markerObj] = event}
                        }
                        is Solar -> {
                            mapMarkers.add(Pair(
                            (event.eclipse_type),
                            LatLng(event.latitude, event.longitude)
                        ))
                            val title = event.eclipse_type
                            val pos = LatLng(event.latitude, event.longitude)
                            val markerObj = googleMap.addMarker(MarkerOptions().position(pos).title(title))
                            if (markerObj != null){markerEventMap[markerObj] = event}
                        }
                        else -> {} // horrible coding practice
                    }
                }
            }else{
                Log.e("MapFragment", "ERROR: Failed to get user events")
            }
        }
    }

    private fun moveToCurrentLocation(googleMap: GoogleMap) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    Log.d("MapFragment", "Got Device Location")
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude),5f))
                }
            }.addOnFailureListener {
                Log.e("MapFragment", "ERROR: Get device Location Failed")
            }
        } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showEventMapCards(marker: Marker){
        val dialog = Dialog(requireContext(),R.style.CustomDialog)
        val view = layoutInflater.inflate(R.layout.map_event_info_card, null)
        dialog.setContentView(view)

        val title = view.findViewById<TextView>(R.id.title)
        val descriptionTop = view.findViewById<TextView>(R.id.description)
        val descriptionBottom = view.findViewById<TextView>(R.id.description1)


        when (val event = markerEventMap[marker]) {
            is Launch -> {
                title.text = (event.mission_name + " Launch")
                descriptionTop.text = ("A " + event.rocket_name + "is launching from pad " + event.pad_name + " from " + event.country)
                descriptionBottom.text = ("Mission info: " + event.mission_info)
            }

            is Solar -> {
                title.text = ("Solar " + event.eclipse_type)
                descriptionTop.text = ("Length of eclipse is " + formatTime(event.central_duration))
                descriptionBottom.text = ("Path width of eclipse is " + event.path_width.toString())
            }

            else -> {
                title.text = "No Event Title"
                descriptionTop.text = ("No Event Description")
                descriptionBottom.text = ("lNo Event Description1")
            }
        }

        dialog.window?.attributes?.gravity = Gravity.TOP
        dialog.window?.attributes?.y = 100
        dialog.show()
    }

    private fun formatTime(time: String): String {
        try {
            val duration = Duration.parse(time)
            return "${duration.toMinutes()}m ${duration.seconds % 60}s"
        } catch (e: DateTimeParseException) {
            return "Invalid Duration"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}