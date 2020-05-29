package com.example.mobiletrainspotter.helpers

import com.google.android.gms.tasks.Task
import java.lang.Exception
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


fun <TResult> Continuation<TResult>.await(task: Task<TResult>) {
    task.addOnCompleteListener { _ ->
        when {
            task.result != null -> {
                this.resume(task.result!!)
            }
            task.isSuccessful -> {
                (this as Continuation<Void?>).resume(null)
            }
            task.isCanceled -> {
                this.resumeWithException(task.exception!!)
            }
            else -> {
                this.resumeWithException(Exception("Task is not successful or cancel"))
            }
        }
    }
}

fun <TResult> Continuation<TResult>.awaitSuccessful(task: Task<TResult>, errorResult: TResult) {
    task.addOnCompleteListener { _ ->
        if (task.isSuccessful) {
            this.resume(task.result!!)
        } else {
            this.resume(errorResult)
        }
    }
}

suspend fun <TResult> Task<TResult>.await(): TResult {
    return suspendCoroutine { it.await(this) }
}

suspend fun Task<Void>.awaitVoid() {
    val task = this.continueWith { _ -> }
    return suspendCoroutine { it.await(task) }
}

suspend fun <TResult> Task<TResult>.awaitSuccessful(errorResult: TResult): TResult {
    return suspendCoroutine { it.awaitSuccessful(this, errorResult) }
}

suspend fun Task<Unit>.awaitSuccessfulUnit() {
    return suspendCoroutine { it.awaitSuccessful(this, Unit) }
}

suspend fun Task<Void>.awaitSuccessfulVoid() {
    val task = this.continueWith { _ -> }
    return suspendCoroutine { it.awaitSuccessful(task, Unit) }
}