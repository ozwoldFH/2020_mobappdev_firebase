package com.example.mobiletrainspotter.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.R
import com.example.mobiletrainspotter.TrainDetailActivity
import com.example.mobiletrainspotter.helpers.StorageHelper
import com.example.mobiletrainspotter.models.*
import com.squareup.picasso.Picasso
import java.time.format.DateTimeFormatter

class RecyclerViewTrainsAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerViewTrainsAdapter.ViewHolder>(), OnAddTrainListener, OnRemoveTrainListener,
    OnChangeTrainListener {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    init {
        Trains.addOnAddListener(this)
        Trains.addOnRemoveListener(this)
        Trains.addOnChangeListener(this)
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val thumbImage: ImageView = item.findViewById(R.id.imageViewPicture)
        val location: TextView = item.findViewById(R.id.textViewLocation)
        val timestamp: TextView = item.findViewById(R.id.textViewTimestamp)
        val trainParts: TextView = item.findViewById(R.id.textViewTrainparts)
        val delete: ImageButton = item.findViewById(R.id.buttonDeleteTrain)
        val cardView: CardView = item.findViewById(R.id.cardViewTrain)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.train_cardview_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Trains.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val train: Train = Trains[holder.adapterPosition] ?: return
        setThumbnail(holder, train)
        holder.location.text = train.location
        holder.timestamp.text = train.timestamp.format(formatter)
        holder.trainParts.text = train.parts.joinToString(" | ") { it.model + " " + it.no }
        holder.delete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Train?")
                .setMessage("Are you sure you want to delete this train?")
                .setNegativeButton("Cancel") { _, _ -> }
                .setPositiveButton("Delete") { _, _ ->
                    Trains.remove(train)
                }
                .create()
                .show()
        }

        holder.cardView.setOnClickListener {
            //Log.d("TrainDetailIntent", "following position was clicked:" + holder.adapterPosition)
            val intent: Intent = Intent(context, TrainDetailActivity::class.java)
            intent.putExtra("trainIndex", holder.adapterPosition)
            context.startActivity(intent)
        }
    }

    private fun setThumbnail(item: ViewHolder, train: Train) {
        if (train.imageFilenames.size > 0) {
            val filename = train.imageFilenames[0]
            StorageHelper.getImageDownloadUrl(filename).addOnSuccessListener {
                Picasso.with(context).load(it).into(item.thumbImage)
            }
        }
    }

    override fun onAdd(train: Train, key: String, index: Int, trains: Trains) {
        notifyItemInserted(index)
    }

    override fun onRemove(train: Train, key: String, index: Int, trains: Trains) {
        notifyItemRemoved(index)
    }

    override fun onChange(newTrain: Train, oldTrain: Train, key: String, index: Int, trains: Trains) {
        notifyItemChanged(index)
    }
}