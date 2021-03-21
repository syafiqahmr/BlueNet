package com.example.assignment3

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.bluenet.R
import java.io.*
import android.net.Uri


class ImagePopup : AppCompatActivity() {
    private lateinit var imageBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image_popup)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 999
        private const val IMAGE_CAMERA_CODE = 998
        private const val FILENAME = "ProfileImageFile.jpg"
    }

    fun buttonGalleryOnClick (view: View) {
        // go to pick image intent
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    fun cameraButtonOnClick(view:View){
        // go to camera intent
        var pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(pictureIntent, IMAGE_CAMERA_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun saveButtonOnClick(view:View){
        // save image to file
        saveImage()

        // go back to main page
        val goBack = Intent()
        setResult(RESULT_OK, goBack)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            // handle result from pick image intent
            val uri = data?.data
            if (uri != null) {
                // bind imageView to image and save image bitmap to variable
                findViewById<ImageView>(R.id.imageView).setImageURI(uri)
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                imageBitmap = ImageDecoder.decodeBitmap(source)
                findViewById<Button>(R.id.sendButton).isEnabled = true
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_CAMERA_CODE){
            // handle result from camera intent
            if (data != null && data.extras != null) {
                // bind imageView to image and save image bitmap to variable
                val imageBitmapData = data.extras!!["data"] as Bitmap
                findViewById<ImageView>(R.id.imageView).setImageBitmap(imageBitmapData)
                imageBitmap = imageBitmapData
                findViewById<Button>(R.id.sendButton).isEnabled = true
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun saveImage() {
        // converts image bitmap stored in variable to file
        val bitmap = imageBitmap
        val file = File(getExternalFilesDir(null), FILENAME)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}