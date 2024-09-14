package com.ece452.spacexplorer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.ToggleButton
import com.ece452.spacexplorer.databinding.ActivityWelcomeBinding
import com.ece452.spacexplorer.networking.models.userinteractions.TopicsType
import com.ece452.spacexplorer.utils.UserInteractionsManager

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val inflater = LayoutInflater.from(this)

        // Create an empty list to store selected topics
        val selectedTopics = mutableListOf<TopicsType>()

        // Get all the different types of topics from the backend
        UserInteractionsManager.getTopics() { success, topics ->
            if (success && topics != null) {
                // Loop through all the different topics and create a toggle button for each
                for (topic in topics) {
                    val topicToggleParent = inflater.inflate(R.layout.topic_toggle, null)
                    val topicToggle = topicToggleParent.findViewById<ToggleButton>(R.id.topic_toggle)
                    topicToggle.textOn = topic
                    topicToggle.textOff = topic
                    topicToggle.text = topic

                    // Add topic to the selected topic list if it is selected
                    topicToggle.setOnCheckedChangeListener { buttonView, isChecked ->
                        val topicType = TopicsType.fromString(topic)
                        if (isChecked) {
                            if (topicType != null) selectedTopics.add(topicType)
                        } else {
                            if (topicType != null) selectedTopics.remove(topicType)
                        }
                    }

                    binding.toggleButtons.addView(topicToggleParent)
                }
            } else {
                // Log an error if the getTopics() call was unsuccessful
                Log.e("WelcomeActivity", "Error: Failed to get topics")
            }
        }

        val nextButton = findViewById<Button>(R.id.next_button)

        // Save the users selected topics in the backend when the next button is clicked
        // These topics will be used on the news feed to determine which news articles to show
        // Switch the user to the main activity if they have selected at least one topic when the next button is clicked
        nextButton.setOnClickListener{
            if (selectedTopics.isNotEmpty()) {

                // function to save the users topics in the backend
                UserInteractionsManager.putUserTopics(selectedTopics.toList()) { success, response ->
                    if (success) {
                        Log.d("WelcomeActivity", "Success: $response")
                    } else {
                        Log.e("WelcomeActivity", "Error: Failed to update user topics")
                    }
                }

                // Switch to the main activity after saving topics
                val intent = Intent(
                    this@WelcomeActivity,
                    MainActivity::class.java
                )
                startActivity(intent)
            } else {
                // Let the user know they can't click next unless at least one newsfeed topic is selected
                Toast.makeText(this@WelcomeActivity,"Please select at least one topic", Toast.LENGTH_SHORT).show()
            }
        }
    }
}