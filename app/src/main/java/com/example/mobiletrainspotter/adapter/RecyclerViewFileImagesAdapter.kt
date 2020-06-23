package com.example.mobiletrainspotter.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.R
import com.example.mobiletrainspotter.ViewTrainImagesActivity
import com.example.mobiletrainspotter.helpers.StorageHelper
import com.squareup.picasso.Picasso

class RecyclerViewFileImagesAdapter(
    private val images: ArrayList<String>,
    private val activity: Activity,
    private val withDeleteButton: Boolean = true
) :
    RecyclerView.Adapter<RecyclerViewFileImagesAdapter.ViewHolder>() {

    var textViewNoImages: TextView? = null

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val image: ImageView = item.findViewById(R.id.imageViewTrainImage)
        val delete: ImageButton = item.findViewById(R.id.buttonDeleteTrainImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.train_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filename = images[position]
        StorageHelper.getImageDownloadUrl(filename).addOnSuccessListener {
            Picasso.with(activity).load(it).into(holder.image)
        }
        holder.image.setOnClickListener {
            val intent: Intent = Intent(activity, ViewTrainImagesActivity::class.java)
            intent.putExtra("type", "imageFilenames")
            intent.putStringArrayListExtra("imageFilenames", images)
            intent.putExtra("index", holder.adapterPosition)
            activity.startActivity(intent)
        }
        if (withDeleteButton) {
            holder.delete.setOnClickListener { _ ->
                images.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)

                if (images.size == 0) {
                    textViewNoImages!!.visibility = View.VISIBLE
                }
            }
        } else {
            holder.delete.visibility = View.GONE
        }
    }
}