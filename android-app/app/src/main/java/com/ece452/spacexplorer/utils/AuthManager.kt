package com.ece452.spacexplorer.utils

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast

// IMPORTS FOR API CALLS
import com.ece452.spacexplorer.networking.api.auth.*
import com.ece452.spacexplorer.networking.models.auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// To abstract away the API calls and provide a simpler interface for the rest of the app to use
object AuthManager {

    // Create an implementation of the API endpoints defined by the apiService interface
    private var authApiService: AuthApiService =
        AuthApiClient.createService(AuthApiService::class.java)

    fun login(
        context: Context,
        username: String,
        password: String,
        loginCallback: (Boolean) -> Unit
    ) {
        val loginRequest = LoginRequest(
            username = username,
            password = Hasher.toSHA256(password)
        )
        authApiService.postLogin(loginRequest).enqueue(object : Callback<LoginReturn> {
            override fun onResponse(call: Call<LoginReturn>, response: Response<LoginReturn>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("AuthManager", "Data: $data")

                    // Save session id to local storage
                    data?.detail?.session_id?.let { sessionID ->
                        SessionIDManager.setSessionID(context, sessionID)
                    }
                    Log.d(
                        "AuthManager",
                        "Session ID from storage: ${SessionIDManager.getSessionID(context)}"
                    )

                    // Save username to local storage
                    data?.detail?.username?.let { username ->
                        UsernameManager.setUsername(context, username)
                    }
                    Log.d(
                        "AuthManager",
                        "Username from storage: ${UsernameManager.getUsername(context)}"
                    )

                    // On a successful login, initialize DataManager and UserInteractionsManager
                    // These managers need context to access saved username and session_id so we don't need to explicitly pass them all the time
                    DataManager.init(context)
                    UserInteractionsManager.init(context)

                    loginCallback(true)
                } else {
                    Log.e("AuthManager", "Error: $response")
                    loginCallback(false)
                }
            }

            override fun onFailure(call: Call<LoginReturn>, t: Throwable) {
                Log.e("AuthManager", "Failure: ${t.message}")
                loginCallback(false)
            }
        })
    }

    fun validateCredentials(
        context: Context,
        username: String,
        email: String,
        phone_number: String,
        password: String,
        confirmPassword: String,
        skipPassword: Boolean = false
    ): Boolean {

        if (username == "" || email == "" || phone_number == "" || (password == "" && !skipPassword)) {
            Toast.makeText(context, "Fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        // Email validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Enter a Valid Email Address", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!skipPassword) {
            // Check passwords are the same
            if (password != confirmPassword) {
                Toast.makeText(context, "Passwords must match!", Toast.LENGTH_SHORT).show()
                return false
            }

            // Password Strength checker
            if (password.length < 8 || password.contains(Regex("^[a-zA-Z0-9]*\$"))) {
                Toast.makeText(
                    context,
                    "Password must have 8+ characters and a special character.",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }


        // Phone Number validation
        if (!Patterns.PHONE.matcher(phone_number).matches()) {
            Toast.makeText(context, "Enter a Valid Phone Number", Toast.LENGTH_SHORT).show()
            return false
        }

        return true

    }

    fun register(
        context: Context,
        username: String,
        email: String,
        phone_number: String,
        password: String,
        confirmPassword: String,
        registerCallback: (Boolean) -> Unit
    ) {


        if (!validateCredentials(
                context,
                username,
                email,
                phone_number,
                password,
                confirmPassword
            )
        ) {
            registerCallback(false)
            return
        }


        val registerAccountRequest = RegisterAccountRequest(
            username = username,
            email = email,
            password = Hasher.toSHA256(password),
            phone_number = phone_number
        )
        authApiService.postRegisterAccount(registerAccountRequest).enqueue(object :
            Callback<RegisterAccountReturn> {
            override fun onResponse(
                call: Call<RegisterAccountReturn>,
                response: Response<RegisterAccountReturn>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("AuthManager", "Data: $data")

                    registerCallback(true)
                } else {
                    if (response.code() == 409) {
                        // If the username exists inform user
                        Toast.makeText(context, "Username already in use!", Toast.LENGTH_SHORT)
                            .show()
                    }
                    Log.e("AuthManager", "Error: $response")
                    registerCallback(false)
                }
            }

            override fun onFailure(call: Call<RegisterAccountReturn>, t: Throwable) {
                Log.e("AuthManager", "Failure: ${t.message}")
                registerCallback(false)
            }
        })
    }

    fun logout(
        context: Context,
        username: String,
        session_id: String,
        logoutCallback: (Boolean) -> Unit
    ) {
        authApiService.postLogout(username, session_id).enqueue(object : Callback<LogoutReturn> {
            override fun onResponse(call: Call<LogoutReturn>, response: Response<LogoutReturn>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("AuthManager", "Data: $data")

                    // Clear session id from local storage
                    SessionIDManager.clearSessionID(context)

                    logoutCallback(true)
                } else {
                    Log.e("AuthManger", "Error: $response")
                    logoutCallback(false)
                }
            }

            override fun onFailure(call: Call<LogoutReturn>, t: Throwable) {
                Log.e("AuthManager", "Failure: ${t.message}")
                logoutCallback(false)
            }
        })
    }

    fun isLoggedIn(context: Context): Boolean {
        return SessionIDManager.getSessionID(context) != null
    }

    fun getProfileInfo(
        context: Context,
        username: String,
        session_id: String,
        profileCallback: (Boolean, ProfileReturnDetail?) -> Unit
    ) {
        authApiService.getProfile(username, session_id).enqueue(object : Callback<ProfileReturn> {
            override fun onResponse(call: Call<ProfileReturn>, response: Response<ProfileReturn>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("AuthManager", "Data: $data")
                    profileCallback(true, data?.detail)

                } else {
                    Log.e("AuthManager", "Error: $response")
                    profileCallback(false, null)
                }
            }

            override fun onFailure(call: Call<ProfileReturn>, t: Throwable) {
                Log.e("AuthManager", "Failure: ${t.message}")
                profileCallback(false, null)
            }
        })
    }

    fun updateAccount(
        context: Context,
        new_username: String,
        new_email: String,
        old_hashed_password: String,
        new_password: String,
        new_phone_number: String,
        new_subscriptions: List<String>?,
        username: String,
        session_id: String,
        updateAccountCallback: (Boolean, UpdateAccountReturn?) -> Unit
    ) {

        // Should we add a confirm password box here?
        if (!validateCredentials(
                context,
                new_username,
                new_email,
                new_phone_number,
                new_password,
                new_password,
                new_password.isEmpty()
            )
        ) {
            updateAccountCallback(false, null)
            return
        }

        val user_profile = UserProfile(
            username = new_username,
            email = new_email,
            password = if (new_password.isEmpty() == true) old_hashed_password else Hasher.toSHA256(
                new_password
            ),
            phone_number = new_phone_number,
            subscriptions = new_subscriptions
        )

        Log.d("password", "${user_profile.password}")

        authApiService.postUpdateAccount(username, session_id, user_profile)
            .enqueue(object : Callback<UpdateAccountReturn> {
                override fun onResponse(
                    call: Call<UpdateAccountReturn>,
                    response: Response<UpdateAccountReturn>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        Log.d("AuthManager", "Data: $data")
                        Toast.makeText(context, "Updated Profile", Toast.LENGTH_SHORT).show()
                        updateAccountCallback(true, data)
                    } else {
                        Log.e("AuthManager", "Error: $response")
                        updateAccountCallback(false, null)
                    }
                }

                override fun onFailure(call: Call<UpdateAccountReturn>, t: Throwable) {
                    Log.e("AuthManager", "Failure: ${t.message}")
                    updateAccountCallback(false, null)
                }
            })
    }
}