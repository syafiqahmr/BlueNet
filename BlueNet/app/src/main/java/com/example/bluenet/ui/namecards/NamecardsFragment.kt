package com.example.bluenet.ui.namecards

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.ArraySet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.bluenet.R
import com.example.bluenet.databinding.FragmentNamecardsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_namecards.*


class NamecardsFragment : Fragment() {

    private lateinit var lvNamecards: ListView
    private var role = "All roles"
    private var industry = "All industries"

    // TODO: Link to actual data
    private var arrNamecard: ArrayList<Namecard> = ArrayList()

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private lateinit var fragmentNamecardsBinding: FragmentNamecardsBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_namecards, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentNamecardsBinding = FragmentNamecardsBinding.bind(view)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initialiseSpinner()
        retrieveDataFromDB()

        // Initalise fake namecards data
        lvNamecards = fragmentNamecardsBinding.lvNamecards
        refreshList()

    }

    private fun initialiseSpinner(){
        val spinnerIndustry = fragmentNamecardsBinding.spinnerIndustry
        val spinnerRole = fragmentNamecardsBinding.spinnerRole

        this.activity?.let {
            // Initialise industry spinner
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

            // Initialise role spinner
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
        spinnerRole.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                role = parentView?.getItemAtPosition(position).toString()
                (parentView!!.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                (parentView!!.getChildAt(0) as TextView).textSize = 16f
                applyFilter()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                role = "All roles"
                (parentView!!.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                (parentView!!.getChildAt(0) as TextView).textSize = 16f
                applyFilter()
            }
        })

        // set event listener for role spinner
        spinnerIndustry.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                industry = parentView?.getItemAtPosition(position).toString()
                (parentView!!.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                (parentView!!.getChildAt(0) as TextView).textSize = 16f
                applyFilter()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                role = "All industries"
                (parentView!!.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                (parentView!!.getChildAt(0) as TextView).textSize = 16f
                applyFilter()
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun retrieveDataFromDB() {
        val user = FirebaseAuth.getInstance().currentUser!!
        Log.i("user", user.uid)
        var arrNamecardId: ArraySet<String> = ArraySet()


        // retrieve list of namecards saved by the users from the db
        val listOfNamecard = FirebaseDatabase.getInstance().getReference("listOfNamecards").child(user.uid)

        val postListenerListOfNamecard = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (n in dataSnapshot.children) {
                        val namecard = n.key.toString()
                        if (namecard != null) {
                            arrNamecardId.add(namecard)
                        }
                    }
                    refreshList()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("test", "loadPost:onCancelled", databaseError.toException())
            }
        }

        listOfNamecard.addValueEventListener(postListenerListOfNamecard)

        Log.i("namecards", arrNamecardId.toString())


        // retrieve namecard info
        val ref = FirebaseDatabase.getInstance().getReference("namecards")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (n in dataSnapshot.children) {
                        val namecard = n.getValue(Namecard::class.java)
                        if (namecard != null && n.key in arrNamecardId) {
                            arrNamecard.add(namecard)
                        }
                    }

                    refreshList()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("test", "loadPost:onCancelled", databaseError.toException())
            }
        }

        ref.addValueEventListener(postListener)
    }

    private fun refreshList(){
        lvNamecards = fragmentNamecardsBinding.lvNamecards
        lvNamecards.adapter = this.activity?.let { NamecardAdapter(it, arrNamecard) }
    }

    private fun applyFilter(){
        if (role == "All roles" && industry == "All industries"){
            lvNamecards.adapter = this.activity?.let { NamecardAdapter(it, arrNamecard) }
        } else {
            var arrNamecardFiltered: ArrayList<Namecard> = ArrayList()
            for (namecard in arrNamecard){
                if (namecard.role == role && namecard.industry == industry){
                    arrNamecardFiltered.add(namecard)
                } else if (namecard.role == role && industry == "All industries"){
                    arrNamecardFiltered.add(namecard)
                } else if (namecard.industry == industry && role == "All roles"){
                    arrNamecardFiltered.add(namecard)
                }
            }
            lvNamecards.adapter = this.activity?.let { NamecardAdapter(it, arrNamecardFiltered) }
        }
    }
}



