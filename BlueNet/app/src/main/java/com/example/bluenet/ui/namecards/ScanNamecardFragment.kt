package com.example.bluenet.ui.namecards

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.findNavController
import com.example.bluenet.MainActivity
import com.example.bluenet.R
import com.example.bluenet.databinding.FragmentScanNamecardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import java.util.*
import kotlin.collections.ArrayList


class ScanNamecardFragment : Fragment() {
    private lateinit var fragmentScanNamecardBinding: FragmentScanNamecardBinding
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 101
    private var textExtracted = ArrayList<String>()
    private lateinit var role : String
    private var selectedImage: Uri? = null
    private lateinit var user: FirebaseUser


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan_namecard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentScanNamecardBinding = FragmentScanNamecardBinding.bind(view)

        // set event listeners
        fragmentScanNamecardBinding.buttonScan.setOnClickListener {
            scanNamecardButtonOnClick(it)
        }
        fragmentScanNamecardBinding.buttonSave.setOnClickListener {
            createOnClick(it)
        }

        initialiseSpinner()
    }

    private fun initialiseSpinner() {
        // TODO: Make Industry a spinner also like filter

        val spinnerRole = fragmentScanNamecardBinding.spinnerRole

        // Initialise role spinner
        this.activity?.let {
            ArrayAdapter.createFromResource(
                    it,
                R.array.roles,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerRole?.adapter = adapter
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


    private fun scanNamecardButtonOnClick(view: View) {

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
                startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), 1)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val imageView = fragmentScanNamecardBinding.imageViewNamecardScan

        if (resultCode != AppCompatActivity.RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    imageView.setImageBitmap(selectedImage)
                    val image = InputImage.fromBitmap(selectedImage, 0)
                    analyze(image)
                }
                1 -> if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val selectedImage = data.data
                    if (selectedImage != null) {
                        val source = ImageDecoder.createSource(this.requireActivity().contentResolver, selectedImage)
                        imageView.setImageBitmap(ImageDecoder.decodeBitmap(source))
                        val inputImage = InputImage.fromFilePath(this.activity, selectedImage)
                        analyze(inputImage)
                    }
                }
            }
            Toast.makeText(this.activity, "Analyzed!", Toast.LENGTH_SHORT)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun analyze(image: InputImage){
        val recognizer = TextRecognition.getClient()
        Log.i("test", "hi")
        val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    extractText(visionText)
                    extractInfo()

                    Toast.makeText(this.activity, "Success", Toast.LENGTH_SHORT)
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                    Toast.makeText(this.activity, "Error in analyzing", Toast.LENGTH_SHORT)
                }

    }

    private fun extractText(result: Text){
        textExtracted = ArrayList<String>()
        for (block in result.textBlocks) {
            for (line in block.lines) {
                val lineText = line.text
                Log.i("added to array", lineText)
                textExtracted.add(lineText)
            }
        }
    }

    private fun requestForPermission(){

        var activity = this.activity

        if (activity != null){

            if (ContextCompat.checkSelfPermission(activity,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
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

    private fun extractInfo(){
        var extraction = NamecardDetailsExtraction(textExtracted)
        extraction.extractName()?.let { Log.i("name", it) }
        extraction.extractLinkedin()?.let { Log.i("linkedin", it) }

        fragmentScanNamecardBinding.name.setText(extraction.extractName())
        fragmentScanNamecardBinding.linkedin.setText(extraction.extractLinkedin())
    }

    private fun createOnClick(view: View){
        val name = fragmentScanNamecardBinding.name.text.toString().trim()
        val industry = fragmentScanNamecardBinding.industry.text.toString().trim()
        val company = fragmentScanNamecardBinding.company.text.toString().trim()
        var image = ""
        val linkedin = fragmentScanNamecardBinding.linkedin.text.toString().trim()

        if (selectedImage != null) {
            image = selectedImage.toString()
        }

        if (name != "" && company != ""){
            // save to db
            val namecard = Namecard(name, company, image, industry, role, linkedin)
            // TODO: need to add the namecard list
            saveToDb(namecard)

            view.findNavController().navigate(R.id.navigation_namecards)

        } else if (name.isBlank()){
            fragmentScanNamecardBinding.name.error = "Name is required!"
            fragmentScanNamecardBinding.name.requestFocus()
        }  else if (company.isBlank()){
            fragmentScanNamecardBinding.company.error = "Company is required!"
            fragmentScanNamecardBinding.company.requestFocus()
        }
    }

    private fun saveToDb(namecard: Namecard) {
        val filename = UUID.randomUUID().toString()
        var imageRef = FirebaseStorage.getInstance().getReference("/images/user/$filename")

//        imageRef.putFile(selectedImage!!)
//                .addOnSuccessListener {
//                    Log.d("RegisterActivity", "Image uploaded ${it.metadata?.path}")
//
//                    imageRef.downloadUrl.addOnSuccessListener {
//                        it.toString()
//                        Log.d("RegisterActivity", "File location $it")
//
//                    }
//                }

        user = FirebaseAuth.getInstance().currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("namecards")
        val namecardId = ref.push().key.toString()

        ref.child(namecardId).setValue(namecard).addOnCompleteListener {
            Toast.makeText(this.activity, "Saved!", Toast.LENGTH_SHORT).show()
        }
    }
}