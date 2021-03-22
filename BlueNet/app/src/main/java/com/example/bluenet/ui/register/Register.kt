package com.example.bluenet.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.bluenet.MainActivity
import com.example.bluenet.R
import com.example.bluenet.ui.home.HomeViewModel
import com.example.bluenet.ui.login.Login
import com.example.bluenet.ui.namecards.CreateNamecard
import com.example.bluenet.ui.namecards.MyNamecardFragment
import com.example.bluenet.ui.namecards.Namecard
import java.io.PrintStream
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    fun register(view: View) {

        // Get Username & Password inputs
        val inputPassword = findViewById<TextView>(R.id.password).text.trim()
        val inputConfirmPassword = findViewById<TextView>(R.id.confirmPassword).text.trim()
        val inputEmail = findViewById<TextView>(R.id.email).text.trim()
        var hasError = false

        // Check if email is valid ?
        if (inputEmail.isEmpty()){
            findViewById<TextView>(R.id.email).error = "Please input your email!"
            findViewById<TextView>(R.id.email).requestFocus()
            hasError = true
        } else if (!inputEmail.isValidEmail()){
            findViewById<TextView>(R.id.email).error = "Please input a valid email!"
            findViewById<TextView>(R.id.email).requestFocus()
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
            // TODO: dk why if password is too short its giving the error message confirm password is not the same
            findViewById<TextView>(R.id.confirmPassword).error = "Confirm Password is not the same!"
            findViewById<TextView>(R.id.confirmPassword).requestFocus()
            hasError = true
        }

        // Check if there's errors
        if (!hasError){
            // TODO: dropdown for visitor/booth
            savetodb(inputEmail.toString(), inputPassword.toString(), "Visitor")

        }
    }

    // Email Validation Function
    fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    // Return to Login page
    fun back(view: View) {
        val int = Intent(this, Login::class.java)
        startActivity(int)
    }

    fun savetodb(email:String,  password:String, type: String){
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success
                    Log.d("TAG", "createUserWithEmail:success")
                    val firebaseUser = auth.currentUser


                    // Save more details into "users" db
                    val user = User(type)
                    val ref = FirebaseDatabase.getInstance().getReference("users")
                    ref.child(firebaseUser!!.uid).setValue(user).addOnCompleteListener {
                        Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show()
                    }
                    // Go to CreateNamecard page
                    val intent = Intent(this, CreateNamecard::class.java)
                    startActivity(intent)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }

    }
}