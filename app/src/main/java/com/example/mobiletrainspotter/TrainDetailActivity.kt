package com.example.mobiletrainspotter

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.adapter.RecyclerViewFileImagesAdapter
import com.example.mobiletrainspotter.adapter.RecyclerViewImagesAdapter
import com.example.mobiletrainspotter.adapter.RecyclerViewTrainPartsAdapter
import com.example.mobiletrainspotter.helpers.StorageHelper
import com.example.mobiletrainspotter.models.TrainPart
import com.example.mobiletrainspotter.models.Trains
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import kotlinx.android.synthetic.main.activity_add_train.*
import kotlinx.android.synthetic.main.activity_train_detail.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TrainDetailActivity : AppCompatActivity() {

    private var parts: ArrayList<TrainPart> = arrayListOf()
    private val images: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_detail)

        val index = intent.extras?.getInt("trainIndex") ?: return
        val train = Trains[index]

        val locationDetail = findViewById<TextView>(R.id.detailTextLocation)
        val commentDetail = findViewById<TextView>(R.id.detailTextComment)
        val dateDetail = findViewById<TextView>(R.id.detailTextDate)
        val numberDetail = findViewById<TextView>(R.id.detailTextNo)
        val timeDetail = findViewById<TextView>(R.id.detailTextTime)


        if (train != null) {
            locationDetail.text = train.location
            commentDetail.text = train.comment
            dateDetail.text = convertLocalDateTimeToDate(train.timestamp)
            numberDetail.text = train.no
            timeDetail.text = convertLocalDateTimeToTime(train.timestamp)

            if (train.imageFilenames.size != 0) textViewNoImagesDetail.visibility = View.GONE

            recyclerViewPartsDetail.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            recyclerViewPartsDetail.adapter = RecyclerViewTrainPartsAdapter(train.parts, false)

            recyclerViewImagesDetail.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
            recyclerViewImagesDetail.adapter = RecyclerViewFileImagesAdapter(train.imageFilenames, this)
        }

        fabEdit.setOnClickListener { _ ->
            val intent: Intent = Intent(this, AddTrainActivity::class.java)
            intent.putExtra("trainId", Trains.getKey(index))
            startActivity(intent)

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun convertLocalDateTimeToDate(timestamp: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return formatter.format(timestamp)
    }

    private fun convertLocalDateTimeToTime(timestamp: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm")
        return formatter.format(timestamp)
    }


}
