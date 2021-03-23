package com.example.bluenet.ui.namecards

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
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
import com.example.bluenet.R
import com.example.bluenet.databinding.FragmentMyNamecardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.File
import java.util.*

class MyNamecardFragment : Fragment() {

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private lateinit var fragmentMyNamecardBinding: FragmentMyNamecardBinding

    private var role = "Entrepreneur"
    private lateinit var user: FirebaseUser
    private lateinit var imageBtn: ImageButton

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
        imageBtn = fragmentMyNamecardBinding.namecardPhoto


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initialiseSpinner()

        // set button event listener
        val saveButton = fragmentMyNamecardBinding.buttonSave
        saveButton.setOnClickListener {
            saveNamecard()
        }

        // Retrieve namecard data and display it
        getData()

        imageBtn.setOnClickListener(View.OnClickListener() {
                val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

                val builder: AlertDialog.Builder = AlertDialog.Builder(this.activity)
                builder.setTitle("Choose your profile picture")

                builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
                    if (options[item] == "Take Photo") {
                        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(takePicture, 0)
                    } else if (options[item] == "Choose from Gallery") {
                        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(pickPhoto, 1)

//                        val galleryIntent = Intent(Intent.ACTION_PICK)
//                        galleryIntent.type = "image/"
//                        startActivityForResult(galleryIntent, 1)

                    } else if (options[item] == "Cancel") {
                        dialog.dismiss()
                    }
                })
                builder.show()
        })

    }

    private fun initialiseSpinner(){
        val spinnerRole = fragmentMyNamecardBinding.spinnerRole

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

    private fun getData(){
        user = FirebaseAuth.getInstance().currentUser!!
        val ref = Firebase.database.reference

        ref.child("namecards").child(user.uid).get().addOnSuccessListener {
            var name = fragmentMyNamecardBinding.name
            val industry = fragmentMyNamecardBinding.industry
            val company = fragmentMyNamecardBinding.company
            val position = fragmentMyNamecardBinding.spinnerRole

            val namecard = it.getValue(Namecard::class.java)
            Log.d("name1", namecard.toString())
            if (namecard != null) {
                // TODO: Need display those with spinner
                fragmentMyNamecardBinding.name.setText(namecard.name)
                fragmentMyNamecardBinding.industry.setText(namecard.industry)
                fragmentMyNamecardBinding.company.setText(namecard.company)
                fragmentMyNamecardBinding.linkedin.setText(namecard.linkedin)

            }

        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }


    }


    private fun saveNamecard(){

        Log.d("saved", "clicked!")

        // TODO: Industry should be spinner

        val name = fragmentMyNamecardBinding.name.text.toString().trim()
        val industry = fragmentMyNamecardBinding.industry.text.toString().trim()
        val company = fragmentMyNamecardBinding.company.text.toString().trim()
        val image = fragmentMyNamecardBinding.namecardPhoto
        val linkedin = fragmentMyNamecardBinding.linkedin.text.toString().trim()

        if (name != "" && company != ""){
            // update db
            val ref = FirebaseDatabase.getInstance().getReference("namecards")
            val namecard = Namecard(name, company, null, industry, role, linkedin)

            ref.child(user.uid).setValue(namecard).addOnCompleteListener {
                Toast.makeText(this.activity, "Saved!", Toast.LENGTH_SHORT).show()
            }
        } else if (name == ""){
            fragmentMyNamecardBinding.name.error = "Name is required!"
            fragmentMyNamecardBinding.name.requestFocus()
        }  else if (company == ""){
            fragmentMyNamecardBinding.company.error = "Name is required!"
            fragmentMyNamecardBinding.company.requestFocus()
        }
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != AppCompatActivity.RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    imageBtn.setImageBitmap(selectedImage)
                }
                1 -> if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
//                        val cursor: Cursor? = this.requireActivity().contentResolver.query(selectedImage,
//                                filePathColumn, null, null, null)
//                        if (cursor != null) {
//                            cursor.moveToFirst()
//                            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
//                            val picturePath: String = cursor.getString(columnIndex)
//                            imageBtn.setImageBitmap(BitmapFactory.decodeFile(picturePath))
//                            cursor.close()
                        imageBtn.setImageURI(selectedImage)
                        val source = ImageDecoder.createSource(this.requireActivity().contentResolver, selectedImage)
                        imageBtn.setImageBitmap(ImageDecoder.decodeBitmap(source))

//                        }
                    }
                }
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}