package com.example.mobiletrainspotter.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.get
import androidx.viewpager.widget.PagerAdapter
import com.example.mobiletrainspotter.R

class PageViewImageAdapter<T>(
    private val list: ArrayList<T>,
    private val context: Context,
    private val setValueFunc: (T, Int, ImageView) -> String
) :
    PagerAdapter() {
    override fun isViewFromObject(view: View, obj: Any): Boolean {
        val text: TextView = view.findViewById(R.id.textViewViewTrainImages)
        return text.text == obj.toString()
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val obj = list[position]

        val item = LayoutInflater.from(context).inflate(R.layout.train_view_images_item, container, false)
        val image: ImageView = item.findViewById(R.id.imageViewViewTrainImages)
        val text: TextView = item.findViewById(R.id.textViewViewTrainImages)

        val key = setValueFunc(obj, position, image)
        text.text = key

        container.addView(item)

        return key
    }

    override fun destroyItem(container: ViewGroup, position: Int, key: Any) {
        for (i in (container.childCount - 1) downTo 0) {
            val item = container[i]
            val text: TextView = item.findViewById(R.id.textViewViewTrainImages)
            if (text.text == key) container.removeViewAt(i)
        }
    }
}