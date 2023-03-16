package com.example.bookvibes

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    val mAuth = FirebaseAuth.getInstance()
    val currentUser = mAuth.currentUser
    val database = FirebaseDatabase.getInstance()
    val userRef = database.reference.child("users")
        //.child(currentUser?.uid!!)
    val uid = currentUser?.uid.toString()

    lateinit var toggle : ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout

    private lateinit var nicknameEditText : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)

        // get user email added by default
        val userEmailView = headerView.findViewById<TextView>(R.id.user_email)
        setEmail(userEmailView)

        // get user name added by default
        val userNicknameLabel = headerView.findViewById<TextView>(R.id.user_nickname)
        //userNicknameLabel.text
        // get user's current nickname from databse
        getNicknameFromDatabase(uid, userNicknameLabel)
        setNickname(userNicknameLabel)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {

            //highlight the selected item
            it.isChecked = true

            when(it.itemId) {
                R.id.nav_book_of_the_day -> replaceFragment(BookOfTheDayFragment(), it.title.toString())
                R.id.nav_fav_books -> Toast.makeText(applicationContext, "Clicked Fav Book", Toast.LENGTH_SHORT).show()
                //R.id.nav_logout -> Toast.makeText(applicationContext, "Clicked Logout", Toast.LENGTH_SHORT).show()
                R.id.nav_books_to_read -> Toast.makeText(applicationContext, "Clicked Books to read", Toast.LENGTH_SHORT).show()
                R.id.nav_stopped_reading -> Toast.makeText(applicationContext, "Clicked Stopped reading", Toast.LENGTH_SHORT).show()
                R.id.nav_my_books -> replaceFragment(MyBooksFragment(), it.title.toString())
                R.id.nav_logout -> logout(navView)
            }
            true
        }
    }

    private fun setNickname(userNicknameLabel: TextView) {
        userNicknameLabel.setOnClickListener {
            // create EditText dialog for the user to enter their new nickname
            nicknameEditText = EditText(this)
            nicknameEditText.setText(userNicknameLabel.text)
            AlertDialog.Builder(this)
                .setTitle("Change your nickname")
                .setMessage("Enter your new nickname")
                .setView(nicknameEditText)
                .setPositiveButton("Update") {_, _ ->
                    val newNickname = nicknameEditText.text.toString().trim()
                    updateNickname(newNickname, userNicknameLabel)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun updateNickname(newNickname: String, userNicknameLabel: TextView) {
        if (currentUser != null) {
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(newNickname)
                .build()

            currentUser.updateProfile(profileUpdate).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update nickname in Firebase
                    Firebase.database.reference.child("users").child(uid)
                        .child("nickname").setValue(newNickname)
                        .addOnCompleteListener { databaseTask ->
                            if (databaseTask.isSuccessful) {
                                Log.e(TAG, "User nickname update succefully")
                                userNicknameLabel.text = newNickname
                            } else {
                                Log.e(TAG, "Failed to update user nickname")
                            }
                        }
                } else {
                    Log.e(TAG, "Failed to update user nickname")
                }
            }
        }
    }

    fun getNicknameFromDatabase(nickname : String, userNicknameLabel: TextView) {
        if (currentUser != null) {
            database.reference.child("users").child(uid).child("nickname")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (nickname.isNotEmpty()) {
                            val nickname = snapshot.getValue(String::class.java).toString()
                            userNicknameLabel.text = nickname
                           // return nickname
                        } else {
                            userNicknameLabel.text = "Please add a nickname"
                            Log.e(TAG, "Nickname is empaty")
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Failed to get user nickname", error.toException())
                    }
                })
        }
    }

    private fun setEmail(userEmailView : TextView) {
        if (currentUser != null) {
            // user is already logged in
            val userEmail = currentUser.email.toString()
            println(userEmail)
            userEmailView.text = userEmail
        } else {
            // user is not logged in
        }
    }

    private fun replaceFragment(fragment : Fragment, title: String) {

        // get reference for the fragment manager
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers() //close drawers whenever user tabs it
        setTitle(title)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Logut implementation

    private fun logout(view: NavigationView) {
        logoutMenu(this@MainActivity)
    }

    private fun logoutMenu(mainActivity: MainActivity) {
        val builder = AlertDialog.Builder(mainActivity)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout ? ")
        builder.setPositiveButton("Yes") { _, _ ->
            finish()
        }
        builder.setNegativeButton("No") { _, _ ->
            //do nothing
            builder.create().dismiss()
        }
        builder.show()
    }


}