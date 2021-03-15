package com.example.bluenet.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bluenet.R
import com.example.bluenet.ui.home.HomeViewModel
import com.example.bluenet.ui.register.Register
import java.util.*

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

    }

    // Go register page
    fun toRegister(view: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    fun login(view: View) {
        var error = ""
        // Check username & password with database (file for now)
        try{
            val account = Scanner(openFileInput("account.txt"))
            // Get username & password from FILE/DATABASE
            val dbUsername = SpannableStringBuilder(account.nextLine())
            val dbPassword = SpannableStringBuilder(account.nextLine())

            // Get Username & Password inputs
            val inputUsername = findViewById<TextView>(R.id.name).text
            val inputPassword = findViewById<TextView>(R.id.password).text

            if (inputUsername != dbUsername || inputPassword != dbPassword){
                error = "Invalid Username/Password!"
            }

            // Maybe can make a banner instead
            if (error != ""){
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
            else{
                val intent = Intent(this, HomeViewModel::class.java)
                startActivity(intent)
            }

        } catch (e: Exception){
        }
    }

}