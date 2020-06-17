package com.example.mobiletrainspotter.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.R

class RecyclerViewImagesAdapter(private val images: ArrayList<Bitmap>) :
    RecyclerView.Adapter<RecyclerViewImagesAdapter.ViewHolder>() {

    var textViewNoImages: TextView? = null

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        private var image: ImageView = item.findViewById(R.id.imageViewTrainImage)
        val delete: ImageButton = item.findViewById(R.id.buttonDeleteTrainImage)

        var bitmap: Bitmap? = null
            set(value) {
                field = value

                if (value != null) {
                    image.setImageBitmap(value)
                }
            }

        init {
            image.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                if (bitmap != null) {
                    val height = bitmap!!.height.toDouble() / bitmap!!.width * image.width
                    image.maxHeight = height.toInt()
                    image.minimumHeight = height.toInt()
                } else {
                    image.maxHeight = 0
                    image.maxHeight = 0
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.train_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bitmap = images[position]
        holder.delete.setOnClickListener { _ ->
            images.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)

            if (images.size == 0) {
                textViewNoImages!!.visibility = View.VISIBLE
            }
        }
    }
}