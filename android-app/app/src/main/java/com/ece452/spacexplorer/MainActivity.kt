package com.ece452.spacexplorer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ece452.spacexplorer.databinding.ActivityMainBinding
import com.ece452.spacexplorer.utils.DataManager
import com.ece452.spacexplorer.utils.SessionIDManager
import com.ece452.spacexplorer.utils.UserInteractionsManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val NOFTIF_CHANNEL_ID = "n_ch_1"
    private val NOFTIF_CHANNEL_NAME = "n_Event_Subs"
    private val NOFTIF_CHANNEL_DESC = ("ID: $NOFTIF_CHANNEL_ID, NAME: $NOFTIF_CHANNEL_NAME, handles Subscribed Event notifications")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UserInteractionsManager and DataManager so that sessionId is not null if users is
        // already logged in
        UserInteractionsManager.init(this)
        DataManager.init(this)

        // If the session ID is null kick the user back to the login page
        if (SessionIDManager.getSessionID(this) == null) {
            val intent = Intent(
                this@MainActivity,
                LoginActivity::class.java
            )
            startActivity(intent)
        }

        // handle the nav bar at the bottom of the screen
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // The Up button will not be displayed when on these destinations. (back button in top left)
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_newsfeed,
            R.id.navigation_discover,
            R.id.navigation_map,
            R.id.navigation_events,
            R.id.navigation_profile
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        notificationChannelBuilder()
    }

    private fun notificationChannelBuilder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOFTIF_CHANNEL_ID, NOFTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = NOFTIF_CHANNEL_DESC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}