package com.example.mobiletrainspotter

import android.app.Activity
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.models.Train
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

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




        // Initialize Firebase Auth
        auth = Firebase.auth


        // adding test data
        addTestdata()


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
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
                val user = FirebaseAuth.getInstance().currentUser
                var test = true
            }
    }

    private fun onShowLoginFirebaseUI() {

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            123)
    }

    // firebase ui
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                mainMenuCoordinatorLayout.visibility = View.VISIBLE
                var test = true
            } else {
                onShowLoginFirebaseUI()
            }
        }
    }

    private fun addTestdata() {
        var recycler:RecyclerView = recyclerView
        recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        var trainList = ArrayList<Train>()

        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))
        trainList.add(Train("", "Zug 4849", "Ein Zug der bei uns faehrt", "no comment", LocalDateTime.MAX  ))

        var adapter = recyclerViewAdapter(trainList)
        recycler.adapter = adapter
    }
}
