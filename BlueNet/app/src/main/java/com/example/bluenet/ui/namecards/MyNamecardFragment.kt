package com.example.bluenet.ui.namecards

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bluenet.R
import com.example.bluenet.databinding.FragmentMyNamecardBinding
import com.google.firebase.database.FirebaseDatabase

class MyNamecardFragment : Fragment() {

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private lateinit var fragmentMyNamecardBinding: FragmentMyNamecardBinding

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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // set button event listener
        val saveButton = fragmentMyNamecardBinding.buttonSave
        saveButton.setOnClickListener(){
            saveNamecard()
        }

        //TODO: Retrieve current namecard data and display it

    }

    private fun saveNamecard(){

        Log.d("saved", "clicked!")

        val name = fragmentMyNamecardBinding.name.text.toString()
        val industry = fragmentMyNamecardBinding.industry.text.toString()
        val company = fragmentMyNamecardBinding.company.text.toString()
        val position = fragmentMyNamecardBinding.position.text.toString()
        val image = fragmentMyNamecardBinding.imageView5

        // TODO: set input checks

        // TODO: figure out how to send image

        val ref = FirebaseDatabase.getInstance().getReference("namecards")
        val namecardId = ref.push().key.toString()

        val namecard = Namecard(namecardId, name, company, null, industry, position)
        
        ref.child(namecardId).setValue(namecard).addOnCompleteListener(){
            Toast.makeText(this.activity, "Saved!", Toast.LENGTH_SHORT).show()
            Log.d("namecardId", namecardId)
        }


    }

}