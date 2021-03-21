package com.example.bluenet.ui.namecards

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.assignment3.ImagePopup
import com.example.bluenet.R
import com.example.bluenet.databinding.FragmentMyNamecardBinding
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.PrintStream
import java.util.*

class MyNamecardFragment : Fragment() {

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private lateinit var fragmentMyNamecardBinding: FragmentMyNamecardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initialiseSpinner()
        return inflater.inflate(R.layout.fragment_my_namecard, container, false)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 900
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentMyNamecardBinding = FragmentMyNamecardBinding.bind(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // set button event listener
        val saveButton = fragmentMyNamecardBinding.buttonSave
        saveButton.setOnClickListener {
            saveNamecard()
        }

        //TODO: Retrieve current namecard data and display it

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

    }

    fun imageButtonOnClick(view: View) {
        // go to imagePopup page
        val intent = Intent(this.activity, ImagePopup::class.java)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun saveNamecard(){

        Log.d("saved", "clicked!")

        val name = fragmentMyNamecardBinding.name.text.toString().trim()
        val industry = fragmentMyNamecardBinding.industry.text.toString().trim()
        val company = fragmentMyNamecardBinding.company.text.toString().trim()
        val position = fragmentMyNamecardBinding.spinnerRole.toString().trim()
        val image = fragmentMyNamecardBinding.namecardPhoto

        // TODO: set input checks

        if (name != "" && company != ""){
            val ref = FirebaseDatabase.getInstance().getReference("namecards")
            val namecardId = ref.push().key.toString()

            val namecard = Namecard(namecardId, name, company, null, industry, position)

            ref.child(namecardId).setValue(namecard).addOnCompleteListener {
                Toast.makeText(this.activity, "Saved!", Toast.LENGTH_SHORT).show()
                Log.d("namecardId", namecardId)
            }
        } else if (name == ""){
            fragmentMyNamecardBinding.name.error = "Name is required!"
            fragmentMyNamecardBinding.name.requestFocus()
        }  else if (company == ""){
            fragmentMyNamecardBinding.company.error = "Name is required!"
            fragmentMyNamecardBinding.company.requestFocus()
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            // handle response after returning from imagePopup page
            // bind image button with selected image
            val file = File(this.activity?.getExternalFilesDir(null), "ProfileImageFile.jpg")
            val uri = Uri.fromFile(file)
            fragmentMyNamecardBinding.namecardPhoto.setImageURI(uri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}