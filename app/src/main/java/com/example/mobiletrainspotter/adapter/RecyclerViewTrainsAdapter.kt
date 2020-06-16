package com.example.mobiletrainspotter.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.R
import com.example.mobiletrainspotter.helpers.StorageHelper
import com.example.mobiletrainspotter.models.Train
import com.squareup.picasso.Picasso
import java.time.format.DateTimeFormatter

class RecyclerViewTrainsAdapter(
    private val trainList: ArrayList<Train>,
    private val context: Context
) :
    RecyclerView.Adapter<RecyclerViewTrainsAdapter.ViewHolder>() {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.train_cardview_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return trainList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val train: Train = trainList[position]
        setThumbnail(holder, train)
        holder.location.text = train.location
        holder.timestamp.text = train.timestamp.format(formatter)
        holder.trainParts.text = train.parts.map { it.model + " " + it.no }.joinToString(" | ")
    }

    private fun setThumbnail(item: ViewHolder, train: Train) {
        if (train.imageFilenames.size > 0) {
            val filename = train.imageFilenames[0]
            StorageHelper.getImageDownloadUrl(filename).addOnSuccessListener {
                Picasso.with(context).load(it!!).into(item.thumbImage)
            }
        }
    }
}