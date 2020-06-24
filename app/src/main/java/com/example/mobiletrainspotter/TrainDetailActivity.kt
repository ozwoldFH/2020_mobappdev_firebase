package com.example.mobiletrainspotter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.adapter.RecyclerViewFileImagesAdapter
import com.example.mobiletrainspotter.adapter.RecyclerViewTrainPartsAdapter
import com.example.mobiletrainspotter.helpers.AlertDialogHelper
import com.example.mobiletrainspotter.models.*
import kotlinx.android.synthetic.main.activity_train_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


class TrainDetailActivity : AppCompatActivity(), CoroutineScope by MainScope(), OnRemoveTrainListener,
    OnChangeTrainListener {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private var trainId: String? = null

    private val parts: ArrayList<TrainPart> = arrayListOf()
    private val partsAdapter = RecyclerViewTrainPartsAdapter(parts, true)

    private val images: ArrayList<String> = arrayListOf()
    private val imagesAdapter = RecyclerViewFileImagesAdapter(images, this, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_detail)

        trainId = intent.getStringExtra("trainId")
        val train = Trains[trainId]

        if (train != null) setTrain(train)

        recyclerViewPartsDetail.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerViewPartsDetail.adapter = partsAdapter

        recyclerViewImagesDetail.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        recyclerViewImagesDetail.adapter = imagesAdapter

        Trains.addOnRemoveListener(this)
        Trains.addOnChangeListener(this)

        fabEdit.setOnClickListener { _ ->
            val intent: Intent = Intent(this, AddTrainActivity::class.java)
            intent.putExtra("trainId", trainId)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Trains.removeOnChangeListener(this)
        Trains.removeOnRemoveListener(this)
    }

    override fun onRemove(train: Train, key: String, trains: Trains) {
        if (key != trainId) return

        val alertContext = this
        launch {
            AlertDialogHelper.showAsync(
                alertContext,
                "Train got delete somewhere else",
                "Info",
                "OK"
            )
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onChange(newTrain: Train, oldTrain: Train, key: String, trains: Trains) {
        if (key == trainId) setTrain(newTrain)
    }

    private fun setTrain(train: Train) {
        val locationDetail = findViewById<TextView>(R.id.detailTextLocation)
        val commentDetail = findViewById<TextView>(R.id.detailTextComment)
        val dateDetail = findViewById<TextView>(R.id.detailTextDate)
        val numberDetail = findViewById<TextView>(R.id.detailTextNo)
        val timeDetail = findViewById<TextView>(R.id.detailTextTime)

        locationDetail.text = train.location
        numberDetail.text = train.no
        commentDetail.text = train.comment

        val timestamp = train.timestamp
        dateDetail.text = timestamp.format(dateFormatter)
        timeDetail.text = timestamp.format(timeFormatter)

        parts.clear()
        train.parts.forEach { parts.add(it) }
        partsAdapter.notifyDataSetChanged()

        images.clear()
        train.imageFilenames.forEach { images.add(it) }
        imagesAdapter.notifyDataSetChanged()

        textViewNoImagesDetail.visibility = if (train.imageFilenames.size == 0) View.VISIBLE else View.GONE
    }


}
