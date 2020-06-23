package com.example.mobiletrainspotter

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mobiletrainspotter.adapter.PageViewImageAdapter
import com.example.mobiletrainspotter.helpers.StorageHelper
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_train_images.*


class ViewTrainImagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_train_images)

        when (intent.getStringExtra("type")) {
            "imageFilenames" -> {
                val imageFilenames = intent.getStringArrayListExtra("imageFilenames")
                viewPagerImages.adapter = PageViewImageAdapter(imageFilenames!!, this) { filename, _, image ->
                    StorageHelper.getImageDownloadUrl(filename).addOnSuccessListener {
                        Picasso.with(this).load(it).into(image)
                    }
                    return@PageViewImageAdapter filename
                }
            }

            "bitmaps" -> {
                val size = intent.getIntExtra("size", 0)
                val dataArrays: ArrayList<ByteArray> = arrayListOf()
                for (i in 0 until size) {
                    val data = intent.getByteArrayExtra("bitmap$i")
                    dataArrays.add(data)
                }
                viewPagerImages.adapter = PageViewImageAdapter(dataArrays, this) { data, index, image ->
                    val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
                    image.setImageBitmap(bmp)

                    return@PageViewImageAdapter index.toString()
                }
            }
        }

        viewPagerImages.currentItem = intent.getIntExtra("index", 0)
    }
}
