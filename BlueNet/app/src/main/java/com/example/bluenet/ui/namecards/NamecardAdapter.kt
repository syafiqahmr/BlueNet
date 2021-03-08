package com.example.bluenet.ui.namecards

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.bluenet.R

//
//class NamecardAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val namecards: List<Namecard>):
//        ArrayAdapter<Namecard>(context, layoutResource, namecards) {
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        return createViewFromResource(position, convertView, parent)
//    }
//
//    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        return createViewFromResource(position, convertView, parent)
//    }
//
//    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(layoutResource, parent, false) as TextView
//        view.text = hotels[position].name
//        return view
//    }
//}

//class NamecardAdapter (context: Context, @LayoutRes private val layoutResource: Int, private val namecards: List<Namecard>):
//        ArrayAdapter<Namecard>(context, layoutResource, namecards) {
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        return createViewFromResource(position, convertView, parent)
//    }
//
//    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        return createViewFromResource(position, convertView, parent)
//    }
//
//    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(layoutResource, parent, false) as TextView
//        view.text = hotels[position].name
//        return view
//    }
//}

class NamecardAdapter (var context: Context, var namecards: List<Namecard>) : BaseAdapter(){

    private class ViewHolder(row: View?){
        var tvName: TextView
        var tvCompany: TextView
        var image: ImageView
        var tvIndustry: TextView
        var tvRole: TextView

        init{
            this.tvName = row?.findViewById(R.id.textViewName) as TextView
            this.tvCompany = row?.findViewById(R.id.textViewCompany) as TextView
            this.image = row?.findViewById(R.id.imageView) as ImageView
            this.tvRole = row?.findViewById(R.id.textViewRole) as TextView
            this.tvIndustry = row?.findViewById(R.id.textViewIndustry) as TextView
        }
    }
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        var view: View?
        var viewHolder: ViewHolder

        if (p1 == null){
            var layout = LayoutInflater.from(context)
            view = layout.inflate(R.layout.namecard_item_list, p2, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else{
            view = p1
            viewHolder = view.tag as ViewHolder
        }

        var namecard: Namecard = getItem(p0) as Namecard
        viewHolder.tvCompany.text = namecard.company
        viewHolder.tvName.text = namecard.name
        viewHolder.image.setImageResource(namecard.image)
        viewHolder.tvIndustry.text = namecard.industry
        viewHolder.tvRole.text = namecard.role

        return view as View
    }

    override fun getItem(p0: Int): Any {
        return namecards.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return namecards.count()
    }

}