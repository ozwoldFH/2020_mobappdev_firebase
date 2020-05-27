package com.example.mobiletrainspotter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.models.Train
import com.squareup.picasso.Picasso
import java.time.format.DateTimeFormatter

class recyclerViewAdapter(val trainList: ArrayList<Train>, val context: Context) :
    RecyclerView.Adapter<recyclerViewAdapter.ViewHolder>() {

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var thumbImage: ImageView = item.findViewById(R.id.imageViewPicture)
        var location: TextView = item.findViewById(R.id.textViewLocation)
        var timestamp: TextView = item.findViewById(R.id.textViewTimestamp)
        var trainParts: TextView = item.findViewById(R.id.textViewTrainparts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trainList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var train: Train = trainList[position]
        setThumbnail(holder, train)
        holder.location.text = train.location
        holder.timestamp.text = train.timestamp.format(formatter)
        holder.trainParts.text = train.parts.map { it.model + " " + it.no }.joinToString(" | ")
    }

    private fun setThumbnail(item: ViewHolder, train: Train) {
        if (train.imageUrls.size == 0) {
            val resId = context.resources.getIdentifier("no_image_icon", "drawable", context.packageName)
            item.thumbImage.setImageResource(resId)
        } else {
            Picasso.with(context).load(train.imageUrls[0]).into(item.thumbImage)
            println("set uri")
        }
    }
}