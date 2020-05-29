package com.example.mobiletrainspotter

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.adapter.RecyclerViewTrainPartsAdapter
import com.example.mobiletrainspotter.helpers.DataBaseHelper
import com.example.mobiletrainspotter.models.Train
import com.example.mobiletrainspotter.models.TrainPart
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_train.*
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AddTrainActivity : AppCompatActivity() {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val parts: ArrayList<TrainPart> = arrayListOf(TrainPart("", ""))
    private val partsAdapter = RecyclerViewTrainPartsAdapter(parts)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_train)

        val now = LocalDateTime.now()
        editTextDate.setText(now.format(dateFormatter))
        editTextTime.setText(now.format(timeFormatter))

        recyclerViewParts.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerViewParts.adapter = partsAdapter

        buttonAddPart.setOnClickListener { _ ->
            parts.add(TrainPart())
            partsAdapter.notifyItemInserted(parts.size - 1)
        }

        fabAddImage.setOnClickListener { _ ->
            // TODO: implement taking a photo
            println("add image")
        }

        fabSave.setOnClickListener { _ ->
            val timestamp = getTimestamp()
            if (timestamp == null) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage("Given date or time is not having the right format")
                    .setTitle("Error")
                    .setNeutralButton("OK", DialogInterface.OnClickListener { dialog, id -> })
                    .create()
                    .show()
                return@setOnClickListener
            }

            val train = Train(
                arrayListOf(),
                parts,
                editTextLocation.text.toString(),
                editTextNo.text.toString(),
                editTextComment.text.toString(),
                timestamp
            )

            saveTrain(train)
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

    private fun saveTrain(train: Train) {
        DataBaseHelper.addTrain(train)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                val message = "${task.exception?.message ?: "<no message>"}\n\nTry again?"
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage(message)
                    .setTitle("Error")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id -> saveTrain(train) })
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id -> })
                    .create()
                    .show()
            }
        }
    }
}
