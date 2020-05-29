package com.example.mobiletrainspotter.helpers

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AlertDialogHelper {
    suspend fun showAsync(
        context: Context,
        message: String? = null,
        title: String? = null,
        positiveButtonText: String? = null,
        negativeButtonText: String? = null,
        neutralButtonText: String? = null
    ): Int {
        return suspendCoroutine {
            val dialogBuilder = AlertDialog.Builder(context)
            if (message != null) {
                dialogBuilder.setMessage(message)
            }
            if (title != null) {
                dialogBuilder.setTitle(title)
            }
            if (positiveButtonText != null) {
                dialogBuilder.setPositiveButton(positiveButtonText) { _, button -> it.resume(button) }
            }
            if (negativeButtonText != null) {
                dialogBuilder.setNegativeButton(negativeButtonText) { _, button -> it.resume(button) }
            }
            if (neutralButtonText != null) {
                dialogBuilder.setNeutralButton(neutralButtonText) { _, button -> it.resume(button) }
            }
            dialogBuilder.setOnCancelListener { _ -> it.resume(-4) }
            dialogBuilder.setOnDismissListener { _ -> it.resume(-5) }
            dialogBuilder.create().show()
        }
    }

    fun show(
        context: Context,
        message: String? = null,
        title: String? = null,
        positiveButtonText: String? = null,
        onPositiveClickListener: DialogInterface.OnClickListener? = null,
        negativeButtonText: String? = null,
        onNegativeClickListener: DialogInterface.OnClickListener? = null,
        neutralButtonText: String? = null,
        onNeutralClickListener: DialogInterface.OnClickListener? = null
    ) {
        val dialogBuilder = AlertDialog.Builder(context)
        if (message != null) {
            dialogBuilder.setMessage(message)
        }
        if (title != null) {
            dialogBuilder.setTitle(title)
        }
        if (positiveButtonText != null) {
            if (onPositiveClickListener != null) {
                dialogBuilder.setPositiveButton(positiveButtonText, onPositiveClickListener)
            } else {
                dialogBuilder.setPositiveButton(positiveButtonText) { _, _ -> }
            }
        }
        if (negativeButtonText != null) {
            if (onNegativeClickListener != null) {
                dialogBuilder.setNegativeButton(negativeButtonText, onNegativeClickListener)
            } else {
                dialogBuilder.setNegativeButton(negativeButtonText) { _, _ -> }
            }
        }
        if (neutralButtonText != null) {
            if (onNeutralClickListener != null) {
                dialogBuilder.setNeutralButton(neutralButtonText, onNeutralClickListener)
            } else {
                dialogBuilder.setNeutralButton(neutralButtonText) { _, _ -> }
            }
        }
        dialogBuilder.create().show()
    }
}