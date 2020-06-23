package com.example.mobiletrainspotter.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.R
import com.example.mobiletrainspotter.ViewTrainImagesActivity
import java.io.ByteArrayOutputStream

class RecyclerViewImagesAdapter(private val images: ArrayList<Bitmap>, private val activity: Activity) :
    RecyclerView.Adapter<RecyclerViewImagesAdapter.ViewHolder>() {

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
        holder.image.setImageBitmap(images[position])
        holder.image.setOnClickListener {
            val intent: Intent = Intent(activity, ViewTrainImagesActivity::class.java)
            intent.putExtra("type", "bitmaps")
            for (i in images.indices) {
                val stream = ByteArrayOutputStream()
                images[i].compress(Bitmap.CompressFormat.PNG, 100, stream)
                val data: ByteArray = stream.toByteArray()
                intent.putExtra("bitmap$i", data)
            }
            intent.putExtra("size", images.size)
            intent.putExtra("index", holder.adapterPosition)
            activity.startActivity(intent)
        }
        holder.delete.setOnClickListener { _ ->
            images.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)

            if (images.size == 0) {
                textViewNoImages!!.visibility = View.VISIBLE
            }
        }
    }
}