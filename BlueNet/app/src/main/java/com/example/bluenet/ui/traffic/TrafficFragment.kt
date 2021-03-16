package com.example.bluenet.ui.traffic

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bluenet.R
import kotlinx.android.synthetic.main.fragment_traffic.*


class TrafficFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<TrafficAdapter.ViewHolder>? = null
    private var boothTraffic =  mapOf("T1" to 100, "T2" to 251, "A3" to 300, "C1" to 20, "C2" to 400)
    private var zoneTraffic = HashMap<String, Int>();

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_traffic, container, false)
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        val model = readFromAsset();

        val adapter = activity?.let { TrafficAdapter(model, it) }

        rcv.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        rcv.adapter = adapter;

        adapter?.setOnClickListener(object : TrafficAdapter.ClickListener{
            override fun onClick(pos: Int, aView: View) {
                Toast.makeText(activity, model.get(pos).name, Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun readFromAsset(): List<Traffic> {

        val modeList = mutableListOf<Traffic>()

        for((k, v) in boothTraffic) {

            var zone = k[0].toString();

            if (zoneTraffic.containsKey(zone)){
                var sum = zoneTraffic[zone]!! + v;
                zoneTraffic[zone] = sum;
            }else{
                zoneTraffic[zone] = v;
            }
        }


        for((k, v) in zoneTraffic) {
            Log.i("Zone", "$k -> $v")
            var name = "Others"

            if(k == "T"){
                name = "Technology"
            }else if (k == "A"){
                name = "Art"
            }else if (k == "C"){
                name = "Craft"
            }

            val model = Traffic(
                name,
                v
            )
            modeList.add(model)
        }

        return modeList
    }
}