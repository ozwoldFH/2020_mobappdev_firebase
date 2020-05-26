package com.example.mobiletrainspotter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.models.Train

class recyclerViewAdapter(val trainList: ArrayList<Train>) : RecyclerView.Adapter<recyclerViewAdapter.ViewHolder>() {



    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item){
        var title: TextView = item.findViewById(R.id.textViewTitle)
        var description: TextView = item.findViewById(R.id.textViewDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trainList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var train:Train = trainList[position]
        holder.title.text = train.name
        holder.description.text = train.description
    }
}