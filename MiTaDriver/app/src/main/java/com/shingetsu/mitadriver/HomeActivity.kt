package com.shingetsu.mitadriver

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shingetsu.mitadriver.Utils.Common
import com.shingetsu.mitadriver.Utils.UserUtils
import java.lang.StringBuilder
import java.lang.ref.Reference

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var waitingDialog: AlertDialog
    private lateinit var storageReference: StorageReference

    private lateinit var img_avatar: ImageView
    private var imageUri: Uri? = null

    companion object {
        val PICK_IMAGE_REQUEST = 141
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        init()
    }

    private fun init() {

        storageReference = FirebaseStorage.getInstance().getReference()

        waitingDialog = AlertDialog.Builder(this)
            .setMessage(resources.getString(R.string.waiting))
            .setCancelable(false)
            .create()

        navView.setNavigationItemSelectedListener { it ->
            if (it.itemId == R.id.nav_logout) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(resources.getString(R.string.log_out))
                    .setMessage(resources.getString(R.string.log_out_title))
                    .setNegativeButton(
                        resources.getString(R.string.cancel),
                        { dialogInterface, i -> dialogInterface.dismiss() })
                    .setPositiveButton(resources.getString(R.string.log_out)) { dialogInterface, i ->
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, FirstScreenActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }
                    .setCancelable(false)

                val dialog = builder.create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.black))
                }
                dialog.show()
            }
            true
        }

        val headerView = navView.getHeaderView(0)
        val txtName = headerView.findViewById<View>(R.id.tv_fullname) as TextView
        val txtPhone = headerView.findViewById<View>(R.id.tv_phone) as TextView
        val txtRateStar = headerView.findViewById<View>(R.id.tv_rate_star) as TextView
        img_avatar = headerView.findViewById(R.id.img_avatar)


        txtName.setText(Common.buildWelcomeMessage())
        txtPhone.setText(Common.currentUser!!.phoneNumber)
        txtRateStar.setText(StringBuilder().append(Common.currentUser!!.rating))
        if (Common.currentUser != null && Common.currentUser!!.avatar != null && !TextUtils.isEmpty(
                Common.currentUser!!.avatar
            )
        ) {
            Glide.with(this)
                .load(Common.currentUser!!.avatar)
                .into(img_avatar)
        }

        img_avatar.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    resources.getString(R.string.select_pics)
                ), PICK_IMAGE_REQUEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.data != null) {
                imageUri = data.data
                img_avatar.setImageURI(imageUri)

                showDialogUpload()
            }
        }
    }

    private fun showDialogUpload() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.change_avatar))
            .setMessage(resources.getString(R.string.change_avatar_title))
            .setNegativeButton(
                resources.getString(R.string.cancel),
                { dialogInterface, i -> dialogInterface.dismiss() })
            .setPositiveButton(resources.getString(R.string.change)) { dialogInterface, i ->
                if (imageUri != null) {
                    waitingDialog.show()
                    val avatarFolder =
                        storageReference.child("avatar/" + FirebaseAuth.getInstance().currentUser!!.uid)

                    avatarFolder.putFile(imageUri!!)
                        .addOnFailureListener { e ->
                            Snackbar.make(drawerLayout, e.message!!, Snackbar.LENGTH_LONG).show()
                            waitingDialog.dismiss()
                        }
                        .addOnCompleteListener { task ->
                            if  (task.isSuccessful){
                                avatarFolder.downloadUrl.addOnSuccessListener { uri ->
                                    val update_data = HashMap<String, Any>()
                                    update_data.put("avatar", uri.toString())

                                    UserUtils.updateUser(drawerLayout, update_data)
                                }
                            }
                            waitingDialog.dismiss()
                        }
                        .addOnProgressListener { taskSnapShot ->
                            val progress = (100.0 * taskSnapShot.bytesTransferred/ taskSnapShot.totalByteCount)
                            waitingDialog.setMessage(StringBuilder("Uploading: ").append(progress).append("%"))
                        }
                }
            }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.black))
        }
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}