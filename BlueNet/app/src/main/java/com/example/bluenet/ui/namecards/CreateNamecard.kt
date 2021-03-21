package com.example.bluenet.ui.namecards

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.example.assignment3.ImagePopup
import com.example.bluenet.MainActivity
import com.example.bluenet.R
import com.google.firebase.database.FirebaseDatabase
import java.io.File

class CreateNamecard() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_namecard)

        initialiseSpinner()
    }

    companion object {
        private const val IMAGE_PICK_CODE = 900
    }

    fun initialiseSpinner() {
        val spinnerRole = findViewById<Spinner>(R.id.role)

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

    fun createOnClick(view: View) {

        Log.d("saved", "clicked!")

        val name = findViewById<EditText>(R.id.name).text.toString().trim()
        val industry = findViewById<EditText>(R.id.industry).toString().trim()
        val company = findViewById<EditText>(R.id.company).toString().trim()
        val position = findViewById<Spinner>(R.id.role).toString().trim()
        val image = findViewById<ImageButton>(R.id.namecardPhoto)

        // TODO: set input checks

        if (name == "") {
            findViewById<EditText>(R.id.name).error = "Name is required!"
            findViewById<EditText>(R.id.name).requestFocus()
        } else if (company == "") {
            findViewById<EditText>(R.id.company).error = "Name is required!"
            findViewById<EditText>(R.id.company).requestFocus()
        } else if (name != "" && company != "") {
            val ref = FirebaseDatabase.getInstance().getReference("namecards")
            val namecardId = ref.push().key.toString()

            val namecard = Namecard(namecardId, name, company, null, industry, position)

            ref.child(namecardId).setValue(namecard).addOnCompleteListener {
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
                Log.d("namecardId", namecardId)
            }

            val it = Intent(this, MainActivity::class.java)
            startActivity(it)

        }
    }
}




