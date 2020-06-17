package com.example.mobiletrainspotter

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.adapter.RecyclerViewImagesAdapter
import com.example.mobiletrainspotter.adapter.RecyclerViewTrainPartsAdapter
import com.example.mobiletrainspotter.helpers.*
import com.example.mobiletrainspotter.models.Train
import com.example.mobiletrainspotter.models.TrainPart
import com.example.mobiletrainspotter.models.Trains
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_add_train.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AddTrainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val parts: ArrayList<TrainPart> = arrayListOf(TrainPart())
    private val partsAdapter = RecyclerViewTrainPartsAdapter(parts)

    private val images: ArrayList<Bitmap> = arrayListOf()
    private val imagesAdapter = RecyclerViewImagesAdapter(images)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_train)

        val now = LocalDateTime.now()
        editTextDate.setText(now.format(dateFormatter))
        editTextTime.setText(now.format(timeFormatter))

        recyclerViewParts.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerViewParts.adapter = partsAdapter

        imagesAdapter.textViewNoImages = textViewNoImages
        recyclerViewImages.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        recyclerViewImages.adapter = imagesAdapter

        buttonAddPart.setOnClickListener { _ ->
            parts.add(TrainPart())
            partsAdapter.notifyItemInserted(parts.size - 1)
        }

        val hasCameraFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if (!hasCameraFeature) {
            fabAddImage.hide()
            textViewImages.visibility = View.GONE
            recyclerViewImages.visibility = View.GONE
        }

        fabAddImage.setOnClickListener { _ ->
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }

        fabSave.setOnClickListener { fab ->
            val timestamp = getTimestamp()
            if (timestamp == null) {
                AlertDialogHelper.show(
                    this,
                    "Given date or time is not having the right format",
                    "Error",
                    "OK"
                )
            } else {
                launch {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    pbrSpinner.visibility = View.VISIBLE

                    saveTrain(timestamp)

                    pbrSpinner.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        }
    }

    private fun getTimestamp(): LocalDateTime? {
        val dateParts = editTextDate.text.toString().split(".")
        if (dateParts.size != 3) {
            return null
        }
        val day = dateParts[0]
        val month = dateParts[1]
        val year = dateParts[2]

        val timeParts = editTextTime.text.toString().split(":")
        if (timeParts.size != 2) {
            return null
        }
        val hour = timeParts[0]
        val minute = timeParts[1]

        return try {
            LocalDateTime.parse("${year}-${month}-${day}T${hour}:${minute}:00")
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveTrain(timestamp: LocalDateTime) {
        val train: Train;
        val uploadedFilenames: ArrayList<String> = arrayListOf()
        try {
            for (image in images) {
                val filename = uploadImage(image)
                if (filename != null) uploadedFilenames.add(filename)
            }

            train = Train(
                uploadedFilenames,
                parts,
                editTextLocation.text.toString(),
                editTextNo.text.toString(),
                editTextComment.text.toString(),
                timestamp.toString()
            )

            saveTrainInDatabase(train)
        } catch (e: Exception) {
            deleteImages(uploadedFilenames)
            AlertDialogHelper.show(
                this,
                e.message,
                "Save train error",
                "OK"
            )
            return
        }

        setResult(Activity.RESULT_OK)
        finish()
    }

    private suspend fun uploadImage(image: Bitmap): String? {
        while (true) {
            try {
                return StorageHelper.putImage(image).await()
            } catch (e: Exception) {
                val message = "${e.message}\n\nTry again?"
                val result = AlertDialogHelper.showAsync(
                    this,
                    message,
                    "Upload image error",
                    "Yes",
                    "Cancel",
                    "Skip"
                )
                if (result == AlertDialog.BUTTON_POSITIVE) {
                    continue
                }
                if (result == AlertDialog.BUTTON_NEUTRAL) {
                    return null
                }
                throw e
            }
        }
    }

    private suspend fun deleteImages(filenames: ArrayList<String>) {
        for (filename in filenames) {
            StorageHelper.deleteImage(filename).awaitSuccessfulVoid()
        }
    }

    private suspend fun saveTrainInDatabase(train: Train) {
        while (true) {
            try {
                Trains.add(train)?.await()
                return
            } catch (e: Exception) {
                val message = "${e.message ?: "<no message>"}\n\nTry again?"
                println(message)
                val result = AlertDialogHelper.showAsync(
                    this,
                    message,
                    "Save train error",
                    "Yes",
                    "No"
                )
                if (result == AlertDialog.BUTTON_POSITIVE) {
                    continue
                }
                throw e
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?
            if (imageBitmap != null) {
                images.add(imageBitmap)
                imagesAdapter.notifyItemInserted(images.size - 1)
                textViewNoImages.visibility = View.GONE
            }
        }
    }
}
