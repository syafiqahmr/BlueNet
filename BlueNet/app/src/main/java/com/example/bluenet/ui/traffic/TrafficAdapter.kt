package com.example.bluenet.ui.traffic

import android.content.Context
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bluenet.R
import kotlinx.android.synthetic.main.traffic_item_list.view.*


class TrafficAdapter(private val modelList: List<Traffic>, val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(modelList.get(position));
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.traffic_item_list, parent, false))
    }

    override fun getItemCount(): Int {
        return modelList.size;
    }

    lateinit var mClickListener: ClickListener

    fun setOnClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(model: Traffic): Unit {
            itemView.txt.text = model.name

            itemView.sub_txt.text = model.traffic
            var color = R.color.red

            Log.d("Traffic", "${model.traffic}")
            if (model.traffic.contains("Very Crowded")){
                color = R.color.red
            }else if (model.traffic.contains("Not Crowded")){
                color = R.color.green
            }else if (model.traffic.contains("Less Crowded")) {
                color = R.color.yellow
            }
                else if (model.traffic.contains("Crowded")){
                color = R.color.red
            }

            Log.d("Traffic Color", "${color}")



            itemView.statuscircle.setColorFilter(ContextCompat.getColor(context, color));

            val id = context.resources.getIdentifier(
                model.name.toLowerCase(),
                "drawable",
                context.packageName
            )
            itemView.img.setBackgroundResource(id)
        }

        override fun onClick(p0: View?) {
            mClickListener.onClick(adapterPosition, itemView)
        }
    }
}