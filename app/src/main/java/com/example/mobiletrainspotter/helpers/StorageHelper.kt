package com.example.mobiletrainspotter.helpers

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*


object StorageHelper {
    fun getImageReference(filename: String): StorageReference {
        return Firebase.storage.reference.child("images").child(filename)
    }

    fun getImageDownloadUrl(imageId: String): Task<Uri> {
        return getImageReference(imageId).downloadUrl
    }

    fun putImage(bitmap: Bitmap, imageId: String = UUID.randomUUID().toString()): Task<String> {
        val filename = "$imageId.jpg"
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val data: ByteArray = stream.toByteArray()

        return getImageReference(filename).putBytes(data).continueWith { filename }
    }

    fun deleteImage(filename: String): Task<Void> {
        return getImageReference(filename).delete()
    }
}