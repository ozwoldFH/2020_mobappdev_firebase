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
import com.example.mobiletrainspotter.models.Trains
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    private val LOGIN_REQUEST_CODE: Int = 123
    private val ADD_TRAIN_REQUEST_CODE: Int = 124

    private val trainsAdapter = RecyclerViewTrainsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // call authentication activity first
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onShowLoginFirebaseUI()
            return
        } else {
            Trains.init(DataBaseHelper.getTrainsReference(user))
            mainMenuCoordinatorLayout.visibility = View.VISIBLE
        }

        recyclerViewTrains.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerViewTrains.adapter = trainsAdapter

        fab.setOnClickListener { _ ->
            val intent: Intent = Intent(this, AddTrainActivity::class.java)
            startActivityForResult(intent, ADD_TRAIN_REQUEST_CODE)
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
                Trains.dispose()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOGIN_REQUEST_CODE) {
            val user = FirebaseAuth.getInstance().currentUser

            if (resultCode == Activity.RESULT_OK && user != null) {
                // Successfully signed in
                mainMenuCoordinatorLayout.visibility = View.VISIBLE
                Trains.init(DataBaseHelper.getTrainsReference(user))
            } else {
                onShowLoginFirebaseUI()
            }
        }
    }
}
