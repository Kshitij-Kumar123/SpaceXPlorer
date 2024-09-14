package com.ece452.spacexplorer

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ece452.spacexplorer.utils.AuthManager
import com.ece452.spacexplorer.utils.Hasher
import com.ece452.spacexplorer.utils.SessionIDManager
import java.util.UUID

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

// These tests are for the AuthManager API calls
@RunWith(AndroidJUnit4::class)
class AuthInstrumentedTest {

    private val TAG = "AuthInstrumentedTest"

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    // testing variables - every test should be a new username since we're doing the entire flow starting from register
    private val username = "auth test" + UUID.randomUUID().toString()
    private val email = "test@test.com"
    private val phone_number = "1234567890"
    private val password = "Test#1234"
    private val confirm_password = "Test#1234"

    @Test
    fun main() {
        // ---------- Register ----------
        val registerLatch = CountDownLatch(1)
        Log.d(TAG, "Registering account")
        AuthManager.register(appContext, username, email, phone_number, password, confirm_password) { success ->
            Log.d(TAG, "Register success: $success")
            assertTrue(success)
            registerLatch.countDown() // decrease count by 1
        }
        registerLatch.await(5, TimeUnit.SECONDS) // wait for registration to complete

        // ---------- Login ----------
        val loginLatch = CountDownLatch(1)
        Log.d(TAG, "Logging in")
        AuthManager.login(appContext, username, password) { success ->
            Log.d(TAG, "Login success: $success")
            assertTrue(success)
            loginLatch.countDown()
        }
        loginLatch.await(5, TimeUnit.SECONDS) // wait for login to complete

        val session_id = SessionIDManager.getSessionID(appContext).toString()

        // ---------- Get user profile ----------
        val profileLatch = CountDownLatch(1)
        Log.d(TAG, "Getting user profile")
        AuthManager.getProfileInfo(appContext, username, session_id) { success, profileData ->
            Log.d(TAG, "Get user profile success: $success")
            Log.d(TAG, "Profile data: $profileData")
            assertTrue(success)
            profileLatch.countDown()
        }
        profileLatch.await(5, TimeUnit.SECONDS) // wait for get profile to complete

        // ---------- Update account ----------
        val updateLatch = CountDownLatch(1)
        Log.d(TAG, "Updating account")
        val new_username = username + "_updated"
        AuthManager.updateAccount(appContext, new_username, email, Hasher.toSHA256(password), password, phone_number, null, username, session_id) { success, response ->
            Log.d(TAG, "Update account success: $success")
            Log.d(TAG, "Update account response: $response")
            assertTrue(success)
            updateLatch.countDown()
        }
        updateLatch.await(5, TimeUnit.SECONDS) // wait for update to complete

        // ---------- Logout ---------- (after username is updated)
        val logoutLatch = CountDownLatch(1)
        Log.d(TAG, "Logging out")
        AuthManager.logout(appContext, new_username, session_id) { success ->
            Log.d(TAG, "Logout success: $success")
            assertTrue(success)
            logoutLatch.countDown()
        }
        logoutLatch.await(5, TimeUnit.SECONDS) // wait for logout to complete
    }
}
