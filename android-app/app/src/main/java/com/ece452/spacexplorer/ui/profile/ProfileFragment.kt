package com.ece452.spacexplorer.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.ece452.spacexplorer.GettingStartedActivity
import com.ece452.spacexplorer.R
import com.ece452.spacexplorer.databinding.FragmentProfileBinding
import com.ece452.spacexplorer.utils.AuthManager
import com.ece452.spacexplorer.utils.SessionIDManager
import com.ece452.spacexplorer.utils.UsernameManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import com.ece452.spacexplorer.networking.models.userinteractions.TopicsType
import com.ece452.spacexplorer.utils.UserInteractionsManager

// class to store/group all data for edit field elements
data class EditFieldElement(
    val editText: EditText?,
    val editButton: Button,
    val textView: TextView,
    val cardView: CardView?,
    val isPassword: Boolean = false,
    var isEditable: Boolean = false,
)

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val logoutButton = root.findViewById<Button>(R.id.logout_button)
        var hashedPassword = ""

        // create a list of all the profile edit fields and initials them
        val profileFields = listOf(
            EditFieldElement(
                root.findViewById(R.id.edit_username),
                root.findViewById(R.id.edit_username_button),
                root.findViewById(R.id.text_username),
                null
            ),
            EditFieldElement(
                root.findViewById(R.id.edit_email_address),
                root.findViewById(R.id.edit_email_address_button),
                root.findViewById(R.id.text_email_address),
                null
            ),
            EditFieldElement(
                root.findViewById(R.id.edit_phone),
                root.findViewById(R.id.edit_phone_button),
                root.findViewById(R.id.text_phone),
                null
            ),
            EditFieldElement(
                root.findViewById(R.id.edit_password),
                root.findViewById(R.id.edit_password_button),
                root.findViewById(R.id.header_password),
                null,
                true
            )
        )

        // get all the views for the topics field
        val topicsField = EditFieldElement(
            null,
            root.findViewById(R.id.edit_topics),
            root.findViewById(R.id.header_topics),
            root.findViewById(R.id.topics_card)
        )

        // Get profile info from the backend
        AuthManager.getProfileInfo(
            requireContext(),
            UsernameManager.getUsername(requireContext()).toString(),
            SessionIDManager.getSessionID(requireContext()).toString()
        ) { success, profileData ->
            if (success) {
                if (profileData != null) {
                    profileFields[0].textView.text = profileData.username
                    profileFields[0].editText?.setText(profileData.username)
                    profileFields[1].textView.text = profileData.email
                    profileFields[1].editText?.setText(profileData.email)
                    profileFields[2].textView.text = profileData.phone_number
                    profileFields[2].editText?.setText(profileData.phone_number)
                    hashedPassword = profileData.password
                }
            }
        }

        // Add edit button functionality for each profile field
        for (field in profileFields) {

            val constraintLayout =
                root.findViewById<ConstraintLayout>(R.id.constraint_layout_password)
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)

            field.editButton.setOnClickListener {
                if (field.isEditable) {
                    if (field.isPassword) {
                        constraintSet.connect(field.textView.id, TOP, PARENT_ID, TOP)
                        constraintSet.connect(field.textView.id, BOTTOM, PARENT_ID, BOTTOM)
                        constraintSet.setMargin(field.textView.id, TOP, 0)
                        constraintSet.applyTo(constraintLayout)
                    }
                    saveProfileData(
                        profileFields,
                        UsernameManager.getUsername(requireContext()).toString(),
                        SessionIDManager.getSessionID(requireContext()).toString(),
                        hashedPassword
                    )
                    switchToTextView(field)
                } else {
                    if (field.isPassword) {
                        constraintSet.connect(field.textView.id, TOP, PARENT_ID, TOP, 30)
                        constraintSet.clear(field.textView.id, BOTTOM)
                        constraintSet.applyTo(constraintLayout)
                    }
                    switchToEditText(field)
                }
            }
            field.editText?.setOnEditorActionListener { _, _, _ ->
                saveProfileData(
                    profileFields,
                    UsernameManager.getUsername(requireContext()).toString(),
                    SessionIDManager.getSessionID(requireContext()).toString(),
                    hashedPassword
                )
                switchToTextView(field)
                true
            }
        }

        // Custom logic for topics edit field
        // First create an empty list to store the current topic selections
        val selectedTopics = mutableListOf<TopicsType>()

        // get the topics options from the backend
        UserInteractionsManager.getTopics { success, topics ->
            // if topics are retrieved successfully create toggle buttons for each
            if (success && topics != null) {
                for (topic in topics) {
                    val topicToggleParent = inflater.inflate(R.layout.topic_toggle, null)
                    val topicToggle =
                        topicToggleParent.findViewById<ToggleButton>(R.id.topic_toggle)
                    topicToggle.textOn = topic
                    topicToggle.textOff = topic
                    topicToggle.text = topic

                    // add topic to selected topics list if it is clicked
                    topicToggle.setOnCheckedChangeListener { _, isChecked ->
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
                // log an error message if topics were not received successfully
                Log.e("ProfileFragment", "Error: Failed to get topics")
            }
        }

        val constraintLayout = root.findViewById<ConstraintLayout>(R.id.constraint_layout_topics)
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        // allow users to edit there topics selections when the edit button is clicked
        topicsField.editButton.setOnClickListener {
            // hide the topics select and make the field no longer editable if field is in edit mode when button is clicked
            // this is also when users topics changes are saved to the backend
            if (topicsField.isEditable) {

                UserInteractionsManager.putUserTopics(selectedTopics.toList()) { success, response ->
                    if (success) {
                        Log.d("ProfileFragment", "Success: $response")
                    } else {
                        Log.e("ProfileFragment", "Error: Failed to update user topics")
                    }
                }

                constraintSet.connect(topicsField.textView.id, TOP, PARENT_ID, TOP)
                constraintSet.connect(topicsField.textView.id, BOTTOM, PARENT_ID, BOTTOM)
                constraintSet.setMargin(topicsField.textView.id, TOP, 0)
                constraintSet.connect(topicsField.editButton.id, TOP, PARENT_ID, TOP)
                constraintSet.connect(topicsField.editButton.id, BOTTOM, PARENT_ID, BOTTOM)
                constraintSet.setMargin(topicsField.editButton.id, TOP, 40)
                constraintSet.setMargin(topicsField.editButton.id, BOTTOM, 40)
                constraintSet.applyTo(constraintLayout)

                topicsField.cardView?.visibility = View.GONE
                topicsField.isEditable = false

            } else {
                // show the topics select when the field it put into edit mode
                constraintSet.connect(topicsField.textView.id, TOP, PARENT_ID, TOP, 70)
                constraintSet.clear(topicsField.textView.id, BOTTOM)
                constraintSet.connect(topicsField.editButton.id, TOP, PARENT_ID, TOP, 30)
                constraintSet.clear(topicsField.editButton.id, BOTTOM)
                constraintSet.applyTo(constraintLayout)

                topicsField.cardView?.visibility = View.VISIBLE
                topicsField.isEditable = true
            }
        }

        // log user out when the logout button is clicked
        logoutButton.setOnClickListener {
            AuthManager.logout(
                requireContext(),
                UsernameManager.getUsername(requireContext()).toString(),
                SessionIDManager.getSessionID(requireContext()).toString()
            ) { success ->
                if (success) {
                    // Switch user to the GettingStarted activity on successful logout
                    val intent = Intent(
                        activity,
                        GettingStartedActivity::class.java
                    )
                    Toast.makeText(activity, "Logout Successful", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                } else {
                    // Login failed, handle error or show error message
                    Log.e("MainActivity", "Error: Logout Failed")
                    Toast.makeText(activity, "Logout Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return root
    }

    // function to switch from non-editable profile field to editable one
    // this is used for all profile fields but the topic field
    private fun switchToEditText(field: EditFieldElement) {
        if (!field.isPassword) {
            field.editText?.setText(field.textView.text)
            field.textView.visibility = View.GONE
        }
        field.editText?.visibility = View.VISIBLE
        field.isEditable = true
        field.editText?.requestFocus()
    }

    // function to switch from editable profile field to non-editable one
    // this is used for all profile fields but the topic field
    private fun switchToTextView(field: EditFieldElement) {

        if (!field.isPassword) {
            field.textView.text = field.editText?.text
            field.textView.visibility = View.VISIBLE
        }
        field.editText?.visibility = View.GONE
        field.isEditable = false
    }

    // function to save profile data in the backend when it is updated
    private fun saveProfileData(
        fields: List<EditFieldElement>,
        username: String,
        sessionId: String,
        hashedPassword: String
    ) {
        AuthManager.updateAccount(
            requireContext(),
            fields[0].editText?.text.toString(),
            fields[1].editText?.text.toString(),
            hashedPassword,
            fields[3].editText?.text.toString(),
            fields[2].editText?.text.toString(),
            null,
            username,
            sessionId
        ) { success, _ ->
            if (success) {
                if (fields[0].editText?.text.toString() != UsernameManager.getUsername(
                        requireContext()
                    )
                ) {
                    UsernameManager.setUsername(
                        requireContext(),
                        fields[0].editText?.text.toString()
                    )
                    UserInteractionsManager.init(requireContext())
                }
            } else {
                Log.e("ProfileFragment", "Error: Failed to update account")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}