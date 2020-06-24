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
    private val trains: ArrayList<Train> = arrayListOf()
    private val keys: ArrayList<String> = arrayListOf()

    var defaultThumbnailResId: Int? = null
    var textViewNoTrain: TextView? = null

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
        val key = keys[position]
        val train = trains[position]
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
            val intent: Intent = Intent(context, TrainDetailActivity::class.java)
            intent.putExtra("trainId", key)
            context.startActivity(intent)
        }
    }

    private fun setThumbnail(item: ViewHolder, train: Train) {
        if (train.imageFilenames.size > 0) {
            val filename = train.imageFilenames[0]
            StorageHelper.getImageDownloadUrl(filename).addOnSuccessListener {
                Picasso.with(context).load(it).into(item.thumbImage)
            }.addOnFailureListener {
                setDefaultThumbnail(item)
            }
        } else {
            setDefaultThumbnail(item)
        }
    }

    private fun setDefaultThumbnail(item: ViewHolder) {
        if (defaultThumbnailResId != null) item.thumbImage.setImageResource(defaultThumbnailResId!!)
        else item.thumbImage.setImageDrawable(null)
    }

    override fun onAdd(train: Train, key: String, trains: Trains) {
        val index = this.trains.indexOfFirst { it.timestamp <= train.timestamp }
        if (index != -1) {
            this.trains.add(index, train)
            keys.add(index, key)
            notifyItemInserted(index)
        } else {
            this.trains.add(train)
            keys.add(key)
            notifyItemInserted(this.trains.size - 1)
        }

        if (this.trains.size > 0 && textViewNoTrain != null) textViewNoTrain!!.visibility = View.GONE
    }

    override fun onRemove(train: Train, key: String, trains: Trains) {
        val index = keys.indexOfFirst { it == key }

        this.trains.removeAt(index)
        keys.removeAt(index)
        notifyItemRemoved(index)

        if (this.trains.size == 0 && textViewNoTrain != null) textViewNoTrain!!.visibility = View.VISIBLE
    }

    override fun onChange(newTrain: Train, oldTrain: Train, key: String, trains: Trains) {
        val oldIndex = keys.indexOfFirst { it == key }
        if (newTrain.timestamp == oldTrain.timestamp) {
            this.trains[oldIndex] = newTrain
            notifyItemChanged(oldIndex)
        } else {
            var newIndex = this.trains.indexOfFirst { it.timestamp <= newTrain.timestamp }
            if (newIndex == -1) newIndex = this.trains.size - 1
            if (newIndex > oldIndex) newIndex--

            if (newIndex != oldIndex) {
                this.trains.removeAt(oldIndex)
                keys.removeAt(oldIndex)
                this.trains.add(newIndex, newTrain)
                keys.add(newIndex, key)

                notifyItemMoved(oldIndex, newIndex)
                notifyItemChanged(newIndex)
            }
        }
    }
}