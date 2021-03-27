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
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.bluenet.R
import com.example.bluenet.databinding.FragmentMyNamecardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*

class MyNamecardFragment : Fragment() {

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private lateinit var fragmentMyNamecardBinding: FragmentMyNamecardBinding

    private var role = "All roles"
    private var industry = "All industries"
    private lateinit var user: FirebaseUser
    private lateinit var userImage: ImageView
    private var selectedImage: Uri? = null
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 101

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_namecard, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentMyNamecardBinding = FragmentMyNamecardBinding.bind(view)
        userImage = fragmentMyNamecardBinding.namecardPhoto


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i("updateProfile","Activity created")
        super.onActivityCreated(savedInstanceState)

        initialiseSpinner()

        val spinnerRole = fragmentMyNamecardBinding.spinnerRole
        val spinnerIndustry = fragmentMyNamecardBinding.spinnerIndustry

        // set event listener for role spinner
        spinnerRole.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                role = parentView?.getItemAtPosition(position).toString()
                Log.i("role",role)
            }
        })

        // set event listener for industry spinner
        spinnerIndustry.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                industry = parentView?.getItemAtPosition(position).toString()
                Log.i("industry", industry)
            }
        })

        // set button event listener
        val saveButton = fragmentMyNamecardBinding.buttonSave
        saveButton.setOnClickListener {
            saveNamecard()
        }

        // Retrieve namecard data and display it
        getData()

        userImage.setOnClickListener(View.OnClickListener() {

                Log.i("updateProfile","Image button clicked")
                val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

                val builder: AlertDialog.Builder = AlertDialog.Builder(this.activity)
                builder.setTitle("Choose your profile picture")

                builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
                    if (options[item] == "Take Photo") {
                        while (!isCameraPermissionGranted()){
                            requestForPermission()
                        }
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
        })

    }

    private fun requestForPermission(){

        var activity = this.activity

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
        return this.activity?.let {
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

    private fun initialiseSpinner(){
        val spinnerRole = fragmentMyNamecardBinding.spinnerRole
        val spinnerIndustry = fragmentMyNamecardBinding.spinnerIndustry

        this.activity?.let {

            // Initialise role spinner
            ArrayAdapter.createFromResource(
                it,
                R.array.roles,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinnerRole.adapter = adapter
            }

            // Initialise role spinner
            ArrayAdapter.createFromResource(
                it,
                R.array.industries,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinnerIndustry.adapter = adapter
            }
        }


    }

    private fun getData(){
        user = FirebaseAuth.getInstance().currentUser!!
        val ref = Firebase.database.reference

        ref.child("namecards").child(user.uid).get().addOnSuccessListener {

            val namecard = it.getValue(Namecard::class.java)
            

            if (namecard != null) {
                Log.d("updateProfile", namecard?.image.toString())
                // TODO: Need display those with spinner
                fragmentMyNamecardBinding.name.setText(namecard.name)
//                fragmentMyNamecardBinding.industry.setText(namecard.industry)
                industry = namecard.industry
                role = namecard.role
                fragmentMyNamecardBinding.company.setText(namecard.company)
                fragmentMyNamecardBinding.linkedin.setText(namecard.linkedin)
//                if (namecard.image != ""){
//                val bitmap = MediaStore.Images.Media.getBitmap(this.requireActivity().contentResolver, namecard.image.toUri())
//                val bitmapDrawable = BitmapDrawable(bitmap)
//                userImage.setBackgroundDrawable(bitmapDrawable)
//                }

                Glide.with(this)
                        .load(namecard.image)
                        .into(fragmentMyNamecardBinding.namecardPhoto)
//                userImage.setImageURI(namecard.image.toUri())
            }

        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Log.d("updateProfile","image received")
        if (resultCode != AppCompatActivity.RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    userImage.setImageBitmap(selectedImage)
//                    Glide.with(this)
//                            .load(selectedImage.toUri)
//                            .into(fragmentMyNamecardBinding.namecardPhoto)
                }
                1 -> if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    selectedImage = data.data
//                    val bitmap = MediaStore.Images.Media.getBitmap(this.requireActivity().contentResolver, selectedImage)
//                    val bitmapDrawable = BitmapDrawable(bitmap)
//                    userImage.setBackgroundDrawable(bitmapDrawable)
                    Log.i("URI", selectedImage.toString())
                    Glide.with(this)
                            .load(selectedImage)
                            .into(fragmentMyNamecardBinding.namecardPhoto)
                }
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun saveNamecard(){

        Log.d("updateProfile", "Save NameCard")
        if (selectedImage == null) return
        saveImageToFirebase()
    }


    private fun saveImageToFirebase(){
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/user/$filename")

        ref.putFile(selectedImage!!)
                .addOnSuccessListener {
                    Log.d("updateProfile", "Image uploaded ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        it.toString()
                        Log.d("updateProfile", "File location $it")

                        sameDetailsToFirebase(it.toString())
                    }
                }
    }

    private fun sameDetailsToFirebase(image: String) {
        val name = fragmentMyNamecardBinding.name.text.toString().trim()
        val company = fragmentMyNamecardBinding.company.text.toString().trim()
        val linkedin = fragmentMyNamecardBinding.linkedin.text.toString().trim()
        var image = image


        if (name != "" && company != "" && role != "All roles" && industry != "All industries"){
            // update db
                Log.d("First If", "Am here")
            val ref = FirebaseDatabase.getInstance().getReference("namecards")
            val namecard = Namecard(name, company,  image, industry, role, linkedin)

            ref.child(user.uid).setValue(namecard).addOnCompleteListener {
                Toast.makeText(this.activity, "Saved!", Toast.LENGTH_SHORT).show()
            }
        } else if (name == ""){
            Log.d("First El If", "Am here")
            fragmentMyNamecardBinding.name.error = "Name is required!"
            fragmentMyNamecardBinding.name.requestFocus()
        }  else if (company == ""){
            Log.d("Second El If", "Am here")
            fragmentMyNamecardBinding.company.error = "Name is required!"
            fragmentMyNamecardBinding.company.requestFocus()

        } else if(role == "All roles"){
            Log.d("Third El If", "Am here")
            Toast.makeText(this.activity, "Please Select A Role!", Toast.LENGTH_SHORT).show()
        } else{
            Log.d("Fourth El If", "Am here")
            Toast.makeText(this.activity, "Please Select An Industry!", Toast.LENGTH_SHORT).show()
        }
    }


//    @Suppress("DEPRECATION")
//    @RequiresApi(Build.VERSION_CODES.R)


}
