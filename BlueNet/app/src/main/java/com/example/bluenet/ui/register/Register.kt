package com.example.bluenet.ui.register

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
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
    private var userType = "Visitor"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


    }

    fun register(view: View) {

        // Get Username & Password inputs
        val inputPassword = findViewById<EditText>(R.id.password).text.trim()
        val inputConfirmPassword = findViewById<EditText>(R.id.confirmPassword).text.trim()
        val inputEmail = findViewById<EditText>(R.id.email).text.trim()
        val inputBoothCode = findViewById<EditText>(R.id.boothCode).text.trim()
        var hasError = false

        if (inputEmail.isEmpty()){
            findViewById<EditText>(R.id.email).error = "Please input your email!"
            findViewById<EditText>(R.id.email).requestFocus()
            hasError = true
        }
        // Check format of email input
        else if (!inputEmail.isValidEmail()){
            findViewById<EditText>(R.id.email).error = "Please input a valid email!"
            findViewById<EditText>(R.id.email).requestFocus()
            hasError = true
        }

        if (inputPassword.isEmpty()){
            findViewById<EditText>(R.id.password).error = "Please input your password!"
            findViewById<EditText>(R.id.password).requestFocus()
            hasError = true
        }
        // Check length requirement for password
        else if (inputPassword.length < 9){
            findViewById<EditText>(R.id.password).error = "Please input a password of 9 or more characters!"
            findViewById<EditText>(R.id.password).requestFocus()
            hasError = true
        }

        if (inputConfirmPassword.isEmpty()){
            findViewById<EditText>(R.id.confirmPassword).error = "Please input confirm password!"
            findViewById<EditText>(R.id.confirmPassword).requestFocus()
            hasError = true
        }
        // Check if Password & Confirm Password is the same
        else if (inputPassword != inputConfirmPassword){
            findViewById<EditText>(R.id.confirmPassword).error = "Confirm Password is not the same!"
            findViewById<EditText>(R.id.confirmPassword).requestFocus()
            hasError = true
        }

        if (userType == "Booth" && inputBoothCode.isEmpty()){
            findViewById<EditText>(R.id.boothCode).error = "Please input your booth code!"
            findViewById<EditText>(R.id.boothCode).requestFocus()
            hasError = true
        }

        // Check if there's errors
        if (!hasError){
            // TODO: Allow input for Booth Code!
            savetodb(inputEmail.toString(), inputPassword.toString(), userType)

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

    fun userTypeOnClick(view: View) {
        var boothCode = findViewById<EditText>(R.id.boothCode)

        when (view.id) {
            R.id.visitorType -> {
                userType = "Visitor"
                boothCode.visibility = View.GONE
                findViewById<RadioButton>(R.id.boothType).isChecked = false
            }
            R.id.boothType -> {
                userType = "Booth"
                boothCode.visibility = View.VISIBLE
                findViewById<RadioButton>(R.id.visitorType).isChecked = false
            }

        }
    }
}