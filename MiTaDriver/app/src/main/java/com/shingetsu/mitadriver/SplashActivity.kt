package com.shingetsu.mitadriver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.shingetsu.mitadriver.Models.DriverMiTa
import com.shingetsu.mitadriver.Utils.Common
import com.shingetsu.mitadriver.Utils.UserUtils
import java.util.*

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var database: FirebaseDatabase
    private lateinit var driverInfoRef: DatabaseReference

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        if (firebaseAuth != null && listener != null) {
            firebaseAuth.removeAuthStateListener(listener)
        }
        super.onStop()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //hide status bar and make full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val logoImage: ImageView = findViewById(R.id.img_logo_splash)
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        logoImage.startAnimation(logoAnimation)

        val logoName: TextView = findViewById(R.id.tv_logo_name)
        val logoName2: TextView = findViewById(R.id.tv_logo_name_2)
        val logoNameAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)
        logoName.startAnimation(logoNameAnimation)
        logoName2.startAnimation(logoNameAnimation)
        init()
    }

    private fun init() {
        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            Handler().postDelayed({
                if (user != null) {
                    FirebaseInstanceId.getInstance()
                        .instanceId
                        .addOnFailureListener { e ->
                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                        }
                        .addOnSuccessListener { instanceIdResult ->
                            Log.d("Token", instanceIdResult.token)
                            UserUtils.updateToken(this,instanceIdResult.token)
                        }

                    driverInfoRef
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    Common.currentUser = snapshot.getValue(DriverMiTa::class.java)
                                }
                                val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@SplashActivity,
                                    "Error connect to data user",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        })

                } else {
                    val intent = Intent(this, FirstScreenActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }, 1000)
        }
    }
}