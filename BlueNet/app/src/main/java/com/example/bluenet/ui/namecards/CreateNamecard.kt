package com.example.bluenet.ui.namecards

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.bluenet.MainActivity
import com.example.bluenet.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_my_namecard.*
import java.io.ByteArrayOutputStream
import java.util.*


@Suppress("DEPRECATION")
class CreateNamecard() : AppCompatActivity() {

    private var role = "All roles"
    private var industry = "All industries"
    private lateinit var userImage: ImageView
    private lateinit var user: FirebaseUser
    private var selectedImage: Uri? = null
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_namecard)
        userImage = findViewById(R.id.namecardPhoto)
        selectedImage = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.drawable.profile_avatar)
        Glide.with(this)
            .load(selectedImage)
            .into(userImage)

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
                (parentView!!.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                (parentView!!.getChildAt(0) as TextView).textSize = 16f
            }
        })

        // set event listener for role spinner
        spinnerIndustry.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                industry = parentView?.getItemAtPosition(position).toString()
                (parentView!!.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                (parentView!!.getChildAt(0) as TextView).textSize = 16f
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
            saveToDb(name, company, industry, role, linkedin)

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
                while (!isCameraPermissionGranted()){
                    requestForPermission()
                }

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

    private fun requestForPermission(){

        var activity = this

        if (activity != null){

            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)){
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.CAMERA
                        ),
                        MY_PERMISSIONS_REQUEST_CAMERA
                    )
                }
            }

        }

    }

    private fun isCameraPermissionGranted(): Boolean {
        return this.let {
            ActivityCompat.checkSelfPermission(
                it,
                Manifest.permission.CAMERA
            )
        } == PackageManager.PERMISSION_GRANTED

    }

    //for handling permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    requestForPermission()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("RegisterActivity","Image data received")
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                    val selected = data.extras!!["data"] as Bitmap?

                    val bytes = ByteArrayOutputStream()
                    if (selected != null) {
                        selected.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    }
                    val path: String = MediaStore.Images.Media.insertImage(
                        contentResolver,
                        selected,
                        "Title",
                        null
                    )
                    selectedImage = Uri.parse(path)
                    Glide.with(this)
                        .load(selectedImage)
                        .into(userImage)
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    selectedImage = data.data
//                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
//                    val bitmapDrawable = BitmapDrawable(bitmap)
//                    userImage.setBackgroundDrawable(bitmapDrawable)
                    Glide.with(this)
                        .load(selectedImage)
                        .into(userImage)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun saveToDb(name:String, company:String, industry:String, role:String, linkedin:String){
        val filename = UUID.randomUUID().toString()
        var imageRef = FirebaseStorage.getInstance().getReference("/images/user/$filename")

        imageRef.putFile(selectedImage!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Image uploaded ${it.metadata?.path}")

                imageRef.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity", "File location $it")
                    val namecard = Namecard(name, company, it.toString(), industry, role, linkedin)

                    user = FirebaseAuth.getInstance().currentUser!!
                    val ref = FirebaseDatabase.getInstance().getReference("namecards")


                    // namecard id == uid
                    if (user != null) {
                        ref.child(user.uid.substring(0, 16)).setValue(namecard).addOnCompleteListener {
                            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


    }




}




