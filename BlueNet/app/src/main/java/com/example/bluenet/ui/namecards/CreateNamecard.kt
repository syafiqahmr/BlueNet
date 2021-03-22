package com.example.bluenet.ui.namecards

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.bluenet.MainActivity
import com.example.bluenet.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


class CreateNamecard() : AppCompatActivity() {

    private var role = "Entrepreneur"
    private lateinit var imageBtn: ImageButton
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_namecard)
        imageBtn = findViewById<ImageButton>(R.id.namecardPhoto)

        initialiseSpinner()
    }

    companion object {
        private const val IMAGE_PICK_CODE = 900
    }

    private fun initialiseSpinner() {
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

        // set event listener for industry spinner
        spinnerRole.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                role = parentView?.getItemAtPosition(position).toString()
            }
        })

    }


    fun imageButtonOnClick(view: View) {

        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose your profile picture")

        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            if (options[item] == "Take Photo") {
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePicture, 0)
            } else if (options[item] == "Choose from Gallery") {
                val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhoto, 1)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    imageBtn.setImageBitmap(selectedImage)
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor: Cursor? = contentResolver.query(selectedImage,
                                filePathColumn, null, null, null)
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath: String = cursor.getString(columnIndex)
                            imageBtn.setImageBitmap(BitmapFactory.decodeFile(picturePath))
                            cursor.close()
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun saveToDb(namecard: Namecard){
        val user = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().getReference("namecards")

        // namecard id == uid
        if (user != null) {
            ref.child(user.uid).setValue(namecard).addOnCompleteListener {
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun createOnClick(view: View) {
        val name = findViewById<EditText>(R.id.name).text.toString().trim()
        val industry = findViewById<EditText>(R.id.industry).text.toString().trim()
        val company = findViewById<EditText>(R.id.company).text.toString().trim()
        val image = findViewById<ImageButton>(R.id.namecardPhoto)

        if (name != "" && company != ""){
            // save to db
            val namecard = Namecard(name, company, null, industry, role)

            saveToDb(namecard)

        } else if (name.isBlank()){
            findViewById<EditText>(R.id.name).error = "Name is required!"
            findViewById<EditText>(R.id.name).requestFocus()
        }  else if (company.isBlank()){
            findViewById<EditText>(R.id.company).error = "Company is required!"
            findViewById<EditText>(R.id.company).requestFocus()
        }
    }

}




