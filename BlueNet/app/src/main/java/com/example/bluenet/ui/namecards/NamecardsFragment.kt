package com.example.bluenet.ui.namecards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.bluenet.Namecards
import com.example.bluenet.R
import com.example.bluenet.databinding.FragmentNamecardsBinding

class NamecardsFragment : Fragment() {

    private lateinit var namecardsViewModel: NamecardsViewModel

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private lateinit var fragmentNamecardsBinding: FragmentNamecardsBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        namecardsViewModel =
                ViewModelProvider(this).get(NamecardsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_namecards, container, false)
//        val textView: TextView = root.findViewById(R.id.text_notifications)
//        namecardsViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNamecardsBinding.bind(view)
        fragmentNamecardsBinding = binding
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val spinnerIndustry = fragmentNamecardsBinding.spinnerIndustry
        val spinnerRole = fragmentNamecardsBinding.spinnerRole

        // Initialise spinner
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

    }


}