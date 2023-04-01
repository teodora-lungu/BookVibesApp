package com.example.bookvibes

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class MainActivity : AppCompatActivity() {

    val mAuth = FirebaseAuth.getInstance()
    val currentUser = mAuth.currentUser
    val database = FirebaseDatabase.getInstance()
    val userRef = database.reference.child("users")
        //.child(currentUser?.uid!!)
    val uid = currentUser?.uid.toString()


    private lateinit var adapter: MainAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var booksArrayList: ArrayList<Books>

    lateinit var imageURL : Array<String>
    lateinit var title : Array<String>
    lateinit var author : Array<String>
    lateinit var prefGen : ArrayList<String>

    lateinit var toggle : ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout


    private lateinit var calendar: Calendar
    private lateinit var alarmManager : AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var nicknameEditText : EditText
    //private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.activity_main, null)
        setContentView(view)
        //setContentView(binding.navView)
        /** set instance for ModelView **/
        val sharedViewModel: SharedViewModel by viewModels()

        /**Notification Book of the Day**/
        createNotificationChannel()
        setNotification()

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)

        /** get user image **/
        //binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(binding.root)
        val userImage = headerView.findViewById<CircleImageView>(R.id.user_image)
        getUserImageFromDatabase(uid, userImage)
        setUserImage(userImage)

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

        replaceFragment(RecommendationsFragment(), title = "Recommended Books")
        navView.setNavigationItemSelectedListener {

            //highlight the selected item
            it.isChecked = true

            when(it.itemId) {
                R.id.nav_home -> replaceFragment(RecommendationsFragment(), it.title.toString())

                R.id.nav_book_of_the_day ->
                    //replaceFragment(BookOfTheDayFragment(), it.title.toString())
                               {
                    val intent = Intent(this, BookOfTheDayActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_fav_books -> Toast.makeText(applicationContext, "Clicked Fav Book", Toast.LENGTH_SHORT).show()
                R.id.nav_books_to_read -> {
                    replaceFragment(BooksToReadFragment(), it.title.toString())
                    Toast.makeText(applicationContext, "Clicked Books to read", Toast.LENGTH_SHORT)
                        .show()
                }
                R.id.nav_stopped_reading -> {
                    replaceFragment(StoppedReadingFragment(), it.title.toString())
                    Toast.makeText(applicationContext, "Clicked Stopped reading", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_book_taste -> replaceFragment(BookGenresFragment(), it.title.toString())
                R.id.nav_my_books -> replaceFragment(MyBooksFragment(), it.title.toString())
                R.id.nav_logout -> logout(navView)
            }
            true
        }

//        /** Get menu View references **/
//        val menuView = inflater.inflate(R.layout.book_list, null)
//        val bookRecmImg = menuView.findViewById<ImageView>(R.id.book_image)
//        val bookRecmTitle = menuView.findViewById<TextView>(R.id.book_title)
//        val bookRecmAuthor = menuView.findViewById<TextView>(R.id.book_author)


        /** Create Book Recommendation **/

        //set RecyclerView
//        val layoutManager = LinearLayoutManager(this)
//        recyclerView = findViewById(R.id.recycler_view_main)
//        recyclerView.layoutManager = layoutManager
//        booksArrayList = ArrayList()
//        adapter = MainAdapter(booksArrayList)
//        recyclerView.adapter = adapter

        /** Get pref genres from Firebase **/
        prefGen = ArrayList()
        //getPrefGenresFromFirebase(prefGen)
        //println("PREF GN ON CREATE:" + prefGen)

        //setRecommendation(bookRecmTitle, bookRecmAuthor, bookRecmImg)

    }

    private fun setNotification() {

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        println(System.currentTimeMillis())
        calendar.set(Calendar.HOUR_OF_DAY, 18)
        calendar.set(Calendar.MINUTE, 58)
        alarmManager.setRepeating(

            AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY, pendingIntent
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmManager.cancel(pendingIntent)
    }
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name : CharSequence = "BookofTheDayChannel"
            val description = "Channel for Book of the Day"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("bookoftheday", name, importance)
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getUserImageFromDatabase(uid: String, userImage: ImageView) {
        if (currentUser != null) {
            userRef.child(uid).child("image")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userImageFromDatabse = snapshot.getValue()
                        if (userImageFromDatabse != null)
                            /** Load image from Firebase **/
                            Glide.with(this@MainActivity)
                                .load(userImageFromDatabse)
                                .circleCrop()
                                .into(userImage)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Failed to get image from database", error.toException())
                    }
                })
        }
    }

    private fun setUserImage(userImage: CircleImageView?) {

        val pickImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                /** handle image**/
                userRef.child(uid).child("image").setValue(it.toString())

                /**TODO( Check if image was selected )**/
                userImage?.setImageURI(it)
                Toast.makeText(this, "Image change succesfully", Toast.LENGTH_SHORT).show()
            }
        )
        userImage?.setOnClickListener {
            pickImage.launch("image/*")
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
        fragmentTransaction.addToBackStack(null)
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