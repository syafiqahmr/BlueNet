package com.example.bluenet.ui.namecards

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.annotation.RequiresApi
import androidx.core.view.get
import com.example.assignment3.ImagePopup
import com.example.bluenet.MainActivity
import com.example.bluenet.R
import com.example.bluenet.databinding.FragmentMyNamecardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.PrintStream
import java.util.*

class MyNamecardFragment : Fragment() {

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private lateinit var fragmentMyNamecardBinding: FragmentMyNamecardBinding

    private var role = "Entrepreneur"
    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        initialiseSpinner()

        // set button event listener
        val saveButton = fragmentMyNamecardBinding.buttonSave
        saveButton.setOnClickListener {
            saveNamecard()
        }

        // Retrieve namecard data and display it
        getData()

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
        user = FirebaseAuth.getInstance().currentUser
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

            }

        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }


    }

    fun imageButtonOnClick(view: View) {
        // go to imagePopup page
        val intent = Intent(this.activity, ImagePopup::class.java)
        startActivityForResult(intent, MyNamecardFragment.IMAGE_PICK_CODE)
    }

    private fun saveNamecard(){

        Log.d("saved", "clicked!")

        // TODO: Industry should be spinner

        val name = fragmentMyNamecardBinding.name.text.toString().trim()
        val industry = fragmentMyNamecardBinding.industry.text.toString().trim()
        val company = fragmentMyNamecardBinding.company.text.toString().trim()
        val image = fragmentMyNamecardBinding.namecardPhoto

        if (name != "" && company != ""){
            // update db
            val ref = FirebaseDatabase.getInstance().getReference("namecards")
            val namecard = Namecard(name, company, null, industry, role)

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