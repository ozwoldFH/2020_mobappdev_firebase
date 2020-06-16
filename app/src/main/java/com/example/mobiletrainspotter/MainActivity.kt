package com.example.mobiletrainspotter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.adapter.RecyclerViewTrainsAdapter
import com.example.mobiletrainspotter.helpers.DataBaseHelper
import com.example.mobiletrainspotter.helpers.StorageHelper
import com.example.mobiletrainspotter.helpers.await
import com.example.mobiletrainspotter.models.Train
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity() {
    private val LOGIN_REQUEST_CODE: Int = 123
    private val ADD_TRAIN_REQUEST_CODE: Int = 124

    private lateinit var auth: FirebaseAuth

    private val trains: ArrayList<Train> = arrayListOf()
    private val trainsAdapter = RecyclerViewTrainsAdapter(trains, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // call authentication activity first
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null)
            onShowLoginFirebaseUI()
        else
            mainMenuCoordinatorLayout.visibility = View.VISIBLE

        recyclerViewTrains.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerViewTrains.adapter = trainsAdapter


        // Initialize Firebase Auth
        auth = Firebase.auth

        // Load data from firebase
        loadData()

        fab.setOnClickListener { view ->
            val intent: Intent = Intent(this, AddTrainActivity::class.java)
            startActivityForResult(intent, ADD_TRAIN_REQUEST_CODE)
        }

        val scope = CoroutineScope(Dispatchers.Default + Job())
        scope.launch {
            try {
                val url = StorageHelper.getImageDownloadUrl("filenaWme").await();
            } catch (e: Exception) {
                println("Test error2: ${e.message}")
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                return true
            }
            R.id.action_logout -> {
                onLogoutFirebaseUI()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onLogoutFirebaseUI() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                // show login screen again
                onShowLoginFirebaseUI()
            }
    }

    private fun onShowLoginFirebaseUI() {

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            LOGIN_REQUEST_CODE
        )
    }

    // firebase ui
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOGIN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                mainMenuCoordinatorLayout.visibility = View.VISIBLE
                loadData()
            } else {
                onShowLoginFirebaseUI()
            }
        } else if (requestCode == ADD_TRAIN_REQUEST_CODE) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                val json = data.getStringExtra("trainData")
                if (json != null) {
                    val train = Gson().fromJson(json, Train::class.java)
                    addTrainToList(train);
                }
            }
        }
    }

    private fun loadData() {
        trains.clear()
        trainsAdapter.notifyDataSetChanged()
        DataBaseHelper.getTrainsReference()?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                for (messageSnapshot in dataSnapshot.children) {
                    val train: Train? = messageSnapshot.getValue(Train::class.java)
                    if (train != null) addTrainToList(train)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("loadTrains:onCancelled ${databaseError.toException().message}")
            }
        })
    }

    private fun addTrainToList(train: Train) {
        trains.add(train)
        trainsAdapter.notifyItemInserted(trains.size - 1)
    }
}
