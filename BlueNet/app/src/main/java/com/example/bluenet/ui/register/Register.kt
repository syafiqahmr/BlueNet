package com.example.bluenet.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.bluenet.R
import com.example.bluenet.ui.home.HomeViewModel
import com.example.bluenet.ui.login.Login
import java.io.PrintStream

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    fun register(view: View) {
        // initialise error list
        var error = ArrayList<String>()

        // Get Username & Password inputs
        val inputUsername = findViewById<TextView>(R.id.name).text
        val inputPassword = findViewById<TextView>(R.id.password).text
        val inputConfirmPassword = findViewById<TextView>(R.id.confirmPassword).text
        val inputEmail = findViewById<TextView>(R.id.email).text

        // Check if Username exists in DB

        // Check if Password & Confirm Password is the same
        if (inputPassword != inputConfirmPassword){
            error.add("Confirm Password is not the same as Password")
        }

        // Check if email is valid ?

        // Check if there's errors
        if (error.isNotEmpty()){
            // Create Account
            val account = PrintStream(openFileOutput("account.txt", MODE_PRIVATE))
            account.println(inputUsername)
            account.println(inputPassword)
            account.println(inputEmail)

            // Go to Homepage
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeViewModel::class.java)
            startActivity(intent)

        } else{
            // Show First Error
            Toast.makeText(this, error[0], Toast.LENGTH_SHORT).show()
        }
    }

    // Return to Login page
    fun back(view: View) {
        val int = Intent(this, Login::class.java)
        startActivity(int)
    }
}