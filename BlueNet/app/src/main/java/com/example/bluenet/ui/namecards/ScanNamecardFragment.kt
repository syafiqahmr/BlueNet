package com.example.bluenet.ui.namecards

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.bluenet.R
import com.example.bluenet.databinding.FragmentScanNamecardBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition


class ScanNamecardFragment : Fragment() {
    private lateinit var fragmentScanNamecardBinding: FragmentScanNamecardBinding
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 101




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

        test()
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
                    analyze()
                }
                1 -> if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val selectedImage = data.data
                    Log.i("selectedImage", selectedImage.toString())
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor: Cursor? = this.activity?.contentResolver?.query(selectedImage,
                                filePathColumn, null, null, null)
                        imageView.setImageURI(selectedImage)
                        val source = ImageDecoder.createSource(this.requireActivity().contentResolver, selectedImage)
                        imageView.setImageBitmap(ImageDecoder.decodeBitmap(source))
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun analyze(){
        val recognizer = TextRecognition.getClient()

        val bitmap = fragmentScanNamecardBinding.imageViewNamecardScan.drawable.toBitmap()
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    extractText(visionText)
                    Toast.makeText(this.activity, "Success", Toast.LENGTH_SHORT)
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                    Toast.makeText(this.activity, "Error in analyzing", Toast.LENGTH_SHORT)
                }

    }

    private fun extractText(result: Text){
        val resultText = result.text
        for (block in result.textBlocks) {
            val blockText = block.text
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            for (line in block.lines) {
                val lineText = line.text
                fragmentScanNamecardBinding.textviewResult.text = lineText
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

    private fun test(){

        Log.i("test", "start test")
        var textarr = ArrayList<String> ()

        textarr.add("Ong Kai Min")
        textarr.add("linkedin.com/kaimin")
        textarr.add("PhoneNumber: 91131622")

        var extraction = NamecardDetailsExtraction(textarr)
        extraction.extractName()?.let { Log.i("name", it) }
        extraction.extractLinkedin()?.let { Log.i("linkedin", it) }
        extraction.extractPhoneNumber()?.let { Log.i("phone", it) }
    }


}