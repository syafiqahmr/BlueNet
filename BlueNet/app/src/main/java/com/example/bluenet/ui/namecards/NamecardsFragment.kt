package com.example.bluenet.ui.namecards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bluenet.R
import com.example.bluenet.databinding.FragmentNamecardsBinding


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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initialiseSpinner()

        // Initalise fake namecards data
        lvNamecards = fragmentNamecardsBinding.lvNamecards
        arrNamecard.add(Namecard("1","Person1", "Company1", R.drawable.person1, "Finance", "Entrepreneur"))
        arrNamecard.add(Namecard("2", "Person2", "Company2", R.drawable.person2, "Technology", "Venture Capitalist"))
        lvNamecards.adapter = this.activity?.let { NamecardAdapter(it, arrNamecard) }

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
                applyFilter()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                role = "All roles"
                applyFilter()
            }
        })

        // set event listener for role spinner
        spinnerIndustry.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                industry = parentView?.getItemAtPosition(position).toString()
                applyFilter()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                role = "All industries"
                applyFilter()
            }
        })

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



