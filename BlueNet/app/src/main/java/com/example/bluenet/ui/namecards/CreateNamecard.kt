package com.example.bluenet.ui.namecards

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_my_namecard.*
import java.util.*


@Suppress("DEPRECATION")
class CreateNamecard() : AppCompatActivity() {

    private var role = "All roles"
    private var industry = "All industries"
    private lateinit var userImage: ImageView
    private lateinit var user: FirebaseUser
    private var selectedImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_namecard)
        userImage = findViewById(R.id.namecardPhoto)

        //initialise default image
        userImage.setImageResource(R.drawable.profile_avatar)

        initialiseSpinner()
    }

//    companion object {
//        private const val IMAGE_PICK_CODE = 900
//    }

    private fun initialiseSpinner() {
        // TODO: Make Industry a spinner also like filter

        val spinnerRole = findViewById<Spinner>(R.id.role)
        val spinnerIndustry = findViewById<Spinner>(R.id.industry)

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

        // Initialise role spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.industries,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerIndustry?.adapter = adapter
        }

        // set event listener for role spinner
        spinnerRole.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                role = parentView?.getItemAtPosition(position).toString()
            }
        })

        // set event listener for role spinner
        spinnerIndustry.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                industry = parentView?.getItemAtPosition(position).toString()
            }
        })

    }

    fun createOnClick(view: View) {
        val name = findViewById<EditText>(R.id.name).text.toString().trim()
        val company = findViewById<EditText>(R.id.company).text.toString().trim()
        var image = ""
        val linkedin = findViewById<EditText>(R.id.linkedin).text.toString().trim()

        if (selectedImage != null) {
            image = selectedImage.toString()
        }

        if (name != "" && company != "" && role != "All roles" && industry != "All industries"){
            // save to db
            val namecard = Namecard(name, company, image, industry, role, linkedin)
            saveToDb(namecard)

            val it = Intent(this, MainActivity::class.java)
            startActivity(it)

        } else if (name.isBlank()){
            findViewById<EditText>(R.id.name).error = "Name is required!"
            findViewById<EditText>(R.id.name).requestFocus()
        }  else if (company.isBlank()){
            findViewById<EditText>(R.id.company).error = "Company is required!"
            findViewById<EditText>(R.id.company).requestFocus()
        } else if(role == "All roles"){
            Toast.makeText(this, "Please Select A Role!", Toast.LENGTH_SHORT).show()
        } else{
            Toast.makeText(this, "Please Select An Industry!", Toast.LENGTH_SHORT).show()
        }
    }


    fun imageButtonOnClick(view: View) {

        Log.d("RegisterActivity","Image button clicked")
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose your profile picture")

        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            if (options[item] == "Take Photo") {
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePicture, 0)
            } else if (options[item] == "Choose from Gallery") {
                startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), 1)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("RegisterActivity","Image data received")
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                2 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    userImage.setImageBitmap(selectedImage)
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    selectedImage = data.data
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                    val bitmapDrawable = BitmapDrawable(bitmap)
                    userImage.setBackgroundDrawable(bitmapDrawable)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun saveToDb(namecard: Namecard){
        val filename = UUID.randomUUID().toString()
        var imageRef = FirebaseStorage.getInstance().getReference("/images/user/$filename")

        imageRef.putFile(selectedImage!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Image uploaded ${it.metadata?.path}")

                imageRef.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity", "File location $it")

                }
            }

        user = FirebaseAuth.getInstance().currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("namecards")


        // namecard id == uid
        if (user != null) {
            ref.child(user.uid).setValue(namecard).addOnCompleteListener {
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }




}




