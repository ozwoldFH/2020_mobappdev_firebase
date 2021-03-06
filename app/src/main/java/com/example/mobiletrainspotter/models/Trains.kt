package com.example.mobiletrainspotter.models

import com.example.mobiletrainspotter.helpers.StorageHelper
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

object Trains : ValueEventListener {
    private var dbTrains: DatabaseReference? = null
    private var isLoaded: Boolean = false
    private val map: HashMap<String, Train> = HashMap()
    private val onLoadedListeners: ArrayList<OnLoadedListener> = arrayListOf()
    private val onAddTrainListeners: ArrayList<OnAddTrainListener> = arrayListOf()
    private val onRemoveTrainListeners: ArrayList<OnRemoveTrainListener> = arrayListOf()
    private val onChangeTrainListeners: ArrayList<OnChangeTrainListener> = arrayListOf()

    val size get() = map.size

    operator fun get(key: String?): Train? = map[key]

    fun set(key: String, train: Train): Task<Void>? {
        return dbTrains?.child(key)?.setValue(train)
    }

    fun add(train: Train): Task<Void>? {
        return dbTrains?.push()?.setValue(train)
    }

    fun remove(train: Train): Task<Void>? {
        val key = map.entries.find { it.value == train }?.key ?: return null
        return dbTrains?.child(key)?.removeValue()?.addOnSuccessListener {
            train.imageFilenames.forEach { StorageHelper.deleteImage(it) }
        }
    }

    fun init(dbTrains: DatabaseReference) {
        this.dispose()
        this.dbTrains = dbTrains
        dbTrains.addValueEventListener(this)
    }

    fun dispose() {
        dbTrains?.removeEventListener(this)
        dbTrains = null
        for (key in ArrayList(map.keys)) {
            removeItem(key)
        }
        isLoaded = false
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        val removeTrainKey = ArrayList(map.keys)
        for (messageSnapshot in dataSnapshot.children) {
            val train: Train = messageSnapshot.getValue(Train::class.java) ?: continue

            if (map.containsKey(messageSnapshot.key)) {
                removeTrainKey.remove(messageSnapshot.key)
                setItem(messageSnapshot.key!!, train)
            } else {
                addItem(messageSnapshot.key!!, train)
            }
        }

        for (key in removeTrainKey) {
            removeItem(key)
        }

        if (!isLoaded) {
            isLoaded = true
            onLoadedListeners.forEach { it.onLoaded(this) }
        }
    }

    override fun onCancelled(databaseError: DatabaseError) {
        println("loadTrains:onCancelled ${databaseError.toException().message}")
    }

    private fun addItem(key: String, train: Train) {
        map[key] = train
        onAddTrainListeners.forEach { it.onAdd(train, key, this) }
    }

    private fun setItem(key: String, train: Train) {
        val oldTrain = map[key] ?: return
        if (oldTrain == train) return
        map[key] = train
        onChangeTrainListeners.forEach { it.onChange(train, oldTrain, key, this) }
    }

    private fun removeItem(key: String) {
        val train = map[key] ?: return
        map.remove(key)
        onRemoveTrainListeners.forEach { it.onRemove(train, key, this) }
    }

    fun addOnLoadedListener(listener: OnLoadedListener) {
        onLoadedListeners.add(listener)
    }

    fun removeOnLoadedListener(listener: OnLoadedListener) {
        onLoadedListeners.remove(listener)
    }

    fun addOnAddListener(listener: OnAddTrainListener) {
        onAddTrainListeners.add(listener)
    }

    fun removeOnAddListener(listener: OnAddTrainListener) {
        onAddTrainListeners.remove(listener)
    }

    fun addOnRemoveListener(listener: OnRemoveTrainListener) {
        onRemoveTrainListeners.add(listener)
    }

    fun removeOnRemoveListener(listener: OnRemoveTrainListener) {
        onRemoveTrainListeners.remove(listener)
    }

    fun addOnChangeListener(listener: OnChangeTrainListener) {
        onChangeTrainListeners.add(listener)
    }

    fun removeOnChangeListener(listener: OnChangeTrainListener) {
        onChangeTrainListeners.remove(listener)
    }
}

interface OnLoadedListener {
    fun onLoaded(trains: Trains)
}

interface OnAddTrainListener {
    fun onAdd(train: Train, key: String, trains: Trains): Unit
}

interface OnRemoveTrainListener {
    fun onRemove(train: Train, key: String, trains: Trains): Unit
}

interface OnChangeTrainListener {
    fun onChange(newTrain: Train, oldTrain: Train, key: String, trains: Trains): Unit
}