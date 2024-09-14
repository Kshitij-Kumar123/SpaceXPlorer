package com.ece452.spacexplorer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ece452.spacexplorer.utils.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val loginButton = findViewById<TextView>(R.id.login_button)
        val registerButton = findViewById<Button>(R.id.register_button)
        val username = findViewById<EditText>(R.id.register_username)
        val email = findViewById<EditText>(R.id.register_email)
        val phoneNumber = findViewById<EditText>(R.id.register_phone_number)
        val password = findViewById<EditText>(R.id.register_password)
        val confirmPassword = findViewById<EditText>(R.id.confirm_password)

        // Attempt to register the user when the register button is clicked
        // If the registration is successful log the user in
        registerButton.setOnClickListener {
            AuthManager.register(this, username.text.toString(), email.text.toString(), phoneNumber.text.toString(), password.text.toString(), confirmPassword.text.toString()) { success ->
                if (success) {
                    Log.d("RegisterActivity", "Registration Successful")

                    // Run login because register doesn't return session_id to save
                    AuthManager.login(this, username.text.toString(), password.text.toString()) { loginSuccess ->
                        if (loginSuccess) {
                            Log.d("RegisterActivity", "Login Successful")
                        } else {
                            Log.e("RegisterActivity", "Error: Login Failed")
                        }
                    }

                    // Switch to the welcome activity on successful registration
                    // The welcome activity is where users are able to pick the newsfeed topics they are intersted in
                    val intent = Intent(
                        this@RegisterActivity,
                        WelcomeActivity::class.java
                    )
                    Toast.makeText(this@RegisterActivity,"Registration Successful", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                } else {
                    // Log an error message if the registration fails
                    Log.e("RegisterActivity", "Error: Registration Failed")
                }
            }
        }

        // Switch to the login activity if the user clicks the login button
        loginButton.setOnClickListener {
            val intent = Intent(
                this@RegisterActivity,
                LoginActivity::class.java
            )
            startActivity(intent)
        }
    }
}