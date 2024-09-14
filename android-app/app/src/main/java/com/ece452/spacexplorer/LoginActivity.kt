package com.ece452.spacexplorer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ece452.spacexplorer.utils.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val registerButton = findViewById<TextView>(R.id.register_button)
        val loginButton = findViewById<Button>(R.id.login_button)
        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)

        // Attempt to login when the login button is clicked
        loginButton.setOnClickListener{
            AuthManager.login(this, username.text.toString(), password.text.toString()) { success ->
                if (success) {
                    // Switch to the main activity it the login is successful
                    val intent = Intent(
                        this@LoginActivity,
                        MainActivity::class.java
                    )
                    Toast.makeText(this@LoginActivity,"Login Successful",Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                } else {
                    // Login failed, handle error or show error message
                    Log.e("LoginActivity", "Error: Login Failed")
                    Toast.makeText(this@LoginActivity,"Login Failed",Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Navigate to RegisterActivity on click
        registerButton.setOnClickListener {
            val intent = Intent(
                this@LoginActivity,
                RegisterActivity::class.java
            )
            startActivity(intent)
        }
    }
}