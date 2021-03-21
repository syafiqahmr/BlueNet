package com.example.bluenet.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.bluenet.MainActivity
import com.example.bluenet.R
import com.example.bluenet.ui.home.HomeViewModel
import com.example.bluenet.ui.login.Login
import com.example.bluenet.ui.namecards.CreateNamecard
import com.example.bluenet.ui.namecards.MyNamecardFragment
import java.io.PrintStream

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    fun register(view: View) {

        // Get Username & Password inputs
        val inputUsername = findViewById<TextView>(R.id.name).text.trim()
        val inputPassword = findViewById<TextView>(R.id.password).text.trim()
        val inputConfirmPassword = findViewById<TextView>(R.id.confirmPassword).text.trim()
        val inputEmail = findViewById<TextView>(R.id.email).text.trim()
        var hasError = false

        // Check if Username exists in DB
        if (inputUsername.isEmpty()){
            findViewById<TextView>(R.id.name).error = "Please input your username!"
            findViewById<TextView>(R.id.name).requestFocus()
            hasError = true
        }
        if (inputPassword.isEmpty()){
            findViewById<TextView>(R.id.password).error = "Please input your password!"
            findViewById<TextView>(R.id.password).requestFocus()
            hasError = true
        }
        if (inputConfirmPassword.isEmpty()){
            findViewById<TextView>(R.id.confirmPassword).error = "Please input confirm password!"
            findViewById<TextView>(R.id.confirmPassword).requestFocus()
            hasError = true
        }
        // Check if Password & Confirm Password is the same
        else if (inputPassword != inputConfirmPassword){
            findViewById<TextView>(R.id.confirmPassword).error = "Confirm Password is not the same!"
            findViewById<TextView>(R.id.confirmPassword).requestFocus()
            hasError = true
        }

        // Check if email is valid ?
        if (inputEmail.isEmpty()){
            findViewById<TextView>(R.id.email).error = "Please input your email!"
            findViewById<TextView>(R.id.email).requestFocus()
            hasError = true
        }

        // Check if there's errors
        if (!hasError){
            // Create Account
            val account = PrintStream(openFileOutput("account.txt", MODE_PRIVATE))
            account.println(inputUsername)
            account.println(inputPassword)
            account.println(inputEmail)

            // Go to Homepage
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, CreateNamecard::class.java)
            startActivity(intent)

        } else{

        }
    }

    // Return to Login page
    fun back(view: View) {
        val int = Intent(this, Login::class.java)
        startActivity(int)
    }
}