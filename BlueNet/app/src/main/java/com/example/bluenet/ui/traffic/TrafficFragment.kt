package com.example.bluenet.ui.traffic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bluenet.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_traffic.*


class TrafficFragment : Fragment() {
    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_traffic, container, false)
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        Log.d("Traffic", "View Created")
        super.onViewCreated(itemView, savedInstanceState)
        getData()

    }


    private fun trafficCount(boothTraffic: HashMap<String, Int>) {

        Log.d("Traffic", "-----------TrafficCount---------")

        var zoneTraffic = HashMap<String, Int>()
        val modeList = mutableListOf<Traffic>()
        var trafficTotal = 0
        for ((k, v) in boothTraffic) {

            var zone = k[0].toString()
            if (zoneTraffic.containsKey(zone)) {
                var sum = zoneTraffic[zone]!! + v

                zoneTraffic[zone] = sum
            } else {
                zoneTraffic[zone] = v
            }
        }
        for ((k, v) in zoneTraffic) {
            trafficTotal += v
        }

        for ((k, v) in zoneTraffic) {
            Log.i("Zone", "$k -> $v")
            var name = "Others"

            if (k == "T") {
                name = "Technology"
            } else if (k == "A") {
                name = "Art"
            } else if (k == "C") {
                name = "Craft"
            }

            Log.d("Traffic", "$v")

            val trafficPercent = v.toFloat() / trafficTotal.toFloat()
            Log.d("Traffic", "$trafficTotal")

            val trafficPercentDisplay = "%.2f".format((trafficPercent * 100))

            var traffic = "Not Crowded ($trafficPercentDisplay%)"

            if (trafficTotal < 50) {
                traffic = "Not Crowded"
            } else if (trafficPercent >= 0.6) {
                traffic = "Very Crowded ($trafficPercentDisplay%)"
            } else if (trafficPercent >= 0.3) {
                traffic = "Crowded ($trafficPercentDisplay%)"
            } else if (trafficPercent >= 0.1) {
                traffic = "Less Crowded ($trafficPercentDisplay%)"
            }

            val model = Traffic(
                name,
                traffic
            )
            modeList.add(model)
        }

        Log.d("Traffic", modeList.toString())

        val adapter = activity?.let { TrafficAdapter(modeList, it) }

        rcv.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        rcv.adapter = adapter

        adapter?.setOnClickListener(object : TrafficAdapter.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                Toast.makeText(activity, modeList.get(pos).name, Toast.LENGTH_LONG).show()
                Log.d("traffic", "clicked")
            }
        })
    }


    private fun getData() {

        Log.d("Traffic", "------Get Data------")
        val ref = FirebaseDatabase.getInstance().getReference("traffic")

        val trafficListener = object : ValueEventListener {
            var boothTraffic = HashMap<String, Int>()

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (j in dataSnapshot.children) {
                        val boothCodeRef = FirebaseDatabase.getInstance().getReference("users")
                            .child(j.key.toString())

                        val userListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (n in dataSnapshot.children) {
                                        if (dataSnapshot.exists()) {
                                            if (n.key.toString() == "boothCode") {
                                                boothTraffic.put(
                                                    n.value.toString(),
                                                    j.value.toString().toInt()
                                                )
                                                trafficCount(boothTraffic)
                                            }
                                        }
                                    }
                                }
                            }


                            override fun onCancelled(databaseError: DatabaseError) {
                                // Getting Post failed, log a message
                                Log.w("test", "loadPost:onCancelled", databaseError.toException())
                            }
                        }

                        boothCodeRef.addValueEventListener(userListener)
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("test", "loadPost:onCancelled", databaseError.toException())
            }
        }


        ref.addValueEventListener(trafficListener)
    }

}