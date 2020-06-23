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
import com.example.mobiletrainspotter.adapter.RecyclerViewFileImagesAdapter
import com.example.mobiletrainspotter.adapter.RecyclerViewImagesAdapter
import com.example.mobiletrainspotter.adapter.RecyclerViewTrainPartsAdapter
import com.example.mobiletrainspotter.helpers.*
import com.example.mobiletrainspotter.models.*
import kotlinx.android.synthetic.main.activity_add_train.*
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AddTrainActivity : AppCompatActivity(), CoroutineScope by MainScope(), OnChangeTrainListener,
    OnRemoveTrainListener {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val trainId: String? get() = intent.getStringExtra("trainId")

    private val parts: ArrayList<TrainPart> = arrayListOf(TrainPart())
    private val partsAdapter = RecyclerViewTrainPartsAdapter(parts)

    private val newImages: ArrayList<Bitmap> = arrayListOf()
    private val newImagesAdapter = RecyclerViewImagesAdapter(newImages, this)

    private val oldImages: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_train)

        val train = Trains[trainId]
        if (train != null) {
            val oldImagesAdapter = RecyclerViewFileImagesAdapter(oldImages, this)
            oldImagesAdapter.textViewNoImages = textViewNewNoImages
            recyclerViewOldImages.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
            recyclerViewOldImages.adapter = oldImagesAdapter

            Trains.addOnChangeListener(this)
            Trains.addOnRemoveListener(this)

            setTrain(train)
        } else {
            val timestamp = LocalDateTime.now()
            editTextDate.setText(timestamp.format(dateFormatter))
            editTextTime.setText(timestamp.format(timeFormatter))
        }

        recyclerViewParts.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerViewParts.adapter = partsAdapter

        newImagesAdapter.textViewNoImages = textViewNewNoImages
        recyclerViewNewImages.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        recyclerViewNewImages.adapter = newImagesAdapter

        buttonAddPart.setOnClickListener { _ ->
            parts.add(TrainPart())
            partsAdapter.notifyItemInserted(parts.size - 1)
        }

        val hasCameraFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if (!hasCameraFeature) {
            fabAddImage.hide()
            textViewNewImages.visibility = View.GONE
            recyclerViewNewImages.visibility = View.GONE
        }

        fabAddImage.setOnClickListener { _ ->
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }

        fabSave.setOnClickListener { _ ->
            launch {
                trySave()
            }
        }
    }

    override fun onBackPressed() {
        if (!hasUnsavedChanges()) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        val alertContext = this;
        launch {
            val result = AlertDialogHelper.showAsync(
                alertContext,
                "All changes get lost!",
                "Unsaved changes",
                "Save",
                "Don't Save",
                "Cancel"
            )
            when (result) {
                AlertDialog.BUTTON_POSITIVE -> {
                    trySave()
                }
                AlertDialog.BUTTON_NEGATIVE -> {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        if (trainId == null) {
            return newImages.size > 0 ||
                    editTextLocation.text.toString() != "" ||
                    editTextNo.text.toString() != "" ||
                    editTextComment.text.toString() != ""
        }

        val timestamp = getTimestamp()
        val train = Trains[trainId]!!
        return newImages.size > 0 ||
                train.imageFilenames.size != oldImages.size ||
                train.location != editTextLocation.text.toString() ||
                train.no != editTextNo.text.toString() ||
                train.comment != editTextComment.text.toString() ||
                train.rawTimestamp != timestamp?.toString()
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

    private suspend fun trySave() {
        val timestamp = getTimestamp()
        if (timestamp == null) {
            AlertDialogHelper.show(
                this,
                "Given date or time is not having the right format",
                "Error",
                "OK"
            )
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            pbrSpinner.visibility = View.VISIBLE

            saveTrain(timestamp)

            pbrSpinner.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private suspend fun saveTrain(timestamp: LocalDateTime) {
        val uploadedFilenames: ArrayList<String> = arrayListOf()
        val allFilenames = ArrayList(oldImages)
        var deletedFilenames: List<String>? = null

        if (trainId != null) {
            val train = Trains[trainId]
            if (train != null) deletedFilenames = train.imageFilenames.filter { !allFilenames.contains(it) }
        }
        try {
            for (image in newImages) {
                val filename = uploadImage(image)
                if (filename != null) {
                    uploadedFilenames.add(filename)
                    allFilenames.add(filename)
                }
            }

            val train = Train(
                allFilenames,
                parts,
                editTextLocation.text.toString(),
                editTextNo.text.toString(),
                editTextComment.text.toString(),
                timestamp.toString()
            )

            saveTrainInDatabase(trainId, train)
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

        if (deletedFilenames != null) deleteImages(deletedFilenames)

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

    private suspend fun deleteImages(filenames: List<String>) {
        for (filename in filenames) {
            StorageHelper.deleteImage(filename).awaitSuccessfulVoid()
        }
    }

    private suspend fun saveTrainInDatabase(trainId: String?, train: Train) {
        while (true) {
            try {
                if (trainId != null) Trains.set(trainId, train)?.await()
                else Trains.add(train)?.await()
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
                newImages.add(imageBitmap)
                newImagesAdapter.notifyItemInserted(newImages.size - 1)
                textViewNewNoImages.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Trains.removeOnChangeListener(this)
        Trains.removeOnRemoveListener(this)
    }

    override fun onChange(newTrain: Train, oldTrain: Train, key: String, index: Int, trains: Trains) {
        if (key == trainId) setTrain(newTrain)
    }

    override fun onRemove(train: Train, key: String, index: Int, trains: Trains) {
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

    private fun setTrain(train: Train) {
        editTextLocation.setText(train.location)
        editTextNo.setText(train.no)
        editTextComment.setText(train.comment)

        val timestamp = train.timestamp
        editTextDate.setText(timestamp.format(dateFormatter))
        editTextTime.setText(timestamp.format(timeFormatter))

        parts.clear()
        train.parts.forEach { parts.add(it) }
        partsAdapter.notifyDataSetChanged()

        oldImages.clear()
        train.imageFilenames.forEach { oldImages.add(it) }
        partsAdapter.notifyDataSetChanged()

        textViewNewImages.text = "New Images"
        textViewOldImages.visibility = View.VISIBLE
        textViewOldNoImages.visibility = if (train.imageFilenames.size == 0) View.VISIBLE else View.GONE
        recyclerViewOldImages.visibility = View.VISIBLE
    }
}
