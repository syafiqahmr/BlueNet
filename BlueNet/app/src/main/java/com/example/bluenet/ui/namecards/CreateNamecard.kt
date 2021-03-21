package com.example.bluenet.ui.namecards

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.example.assignment3.ImagePopup
import com.example.bluenet.R
import java.io.File

class CreateNamecard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_namecard)

        initialiseSpinner()
    }

    companion object {
        private const val IMAGE_PICK_CODE = 900
    }

    private fun initialiseSpinner(){
        val spinnerRole = findViewById<Spinner>(R.id.roleSpinner)

        // Initialise role spinner
        ArrayAdapter.createFromResource(
                this,
                R.array.roles,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerRole?.adapter = adapter
        }

    }

    fun createOnClick(view: View) {
        val name = findViewById<EditText>(R.id.name).text.toString()
        val company = findViewById<EditText>(R.id.company).text.toString()

        if (name != "" && company != ""){

        } else if (name == ""){
            Toast.makeText(this, "Please Enter Your Name!", Toast.LENGTH_SHORT).show()
        }  else if (name == ""){
            Toast.makeText(this, "Please Enter Your Name!", Toast.LENGTH_SHORT).show()
        }

    }

    fun imageButtonOnClick(view: View) {
        // go to imagePopup page
        val intent = Intent(this, ImagePopup::class.java)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            // handle response after returning from imagePopup page
            // bind image button with selected image
            val file = File(getExternalFilesDir(null), "ProfileImageFile.jpg")
            val uri = Uri.fromFile(file)
            findViewById<ImageButton>(R.id.namecardPhoto).setImageURI(uri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}

