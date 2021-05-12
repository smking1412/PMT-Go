package com.shingetsu.mitago

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.shingetsu.mitago.Models.RiderMiTa
import com.shingetsu.mitago.Utils.Common
import com.shingetsu.mitago.Utils.UserUtils
import java.util.*

class FirstScreenActivity : AppCompatActivity() {

    companion object {
        private val LOGIN_REQUEST_CODE = 6868;
    }

    private lateinit var mGoogleSignClient: GoogleSignInClient
    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var btnSignIn: AppCompatButton
    private lateinit var btnSignUp: TextView

    private lateinit var database: FirebaseDatabase
    private lateinit var riderMiTaRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_screen)
        init()
        checkPermission()
        evenClick()
    }

    private fun init() {
        btnSignIn = findViewById(R.id.btn_sign_in)
        btnSignUp = findViewById(R.id.btn_sign_up)
        database = FirebaseDatabase.getInstance()
        riderMiTaRef = database.getReference(Common.RIDER_INFO_REFERENCE)
    }

    private fun checkPermission() {
        //request Permission
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    Toast.makeText(
                        this@FirstScreenActivity,
                        "Permission " + p0!!.permissionName + " was granted ",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@FirstScreenActivity,
                        "Permission " + p0!!.permissionName + " was denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }).check()
    }

    private fun evenClick() {
        btnSignIn.setOnClickListener(View.OnClickListener {
            showLoginLayout()
        })

        btnSignUp.setOnClickListener(View.OnClickListener {
            showRegisterLayout()
        })
    }

    private fun showRegisterLayout() {
        val builder = AlertDialog.Builder(this, R.style.AppTheme)
        val itemView = LayoutInflater.from(this).inflate(R.layout.activity_sign_up, null)

        val edtFirstName: EditText = itemView.findViewById(R.id.edt_register_first_name)
        val edtLastName: EditText = itemView.findViewById(R.id.edt_register_last_name)
        val edtPhoneNumber: EditText = itemView.findViewById(R.id.edt_register_phone_number)
        val btnRegister: AppCompatButton = itemView.findViewById(R.id.btn_register)
        val edtEmail: EditText = itemView.findViewById(R.id.edt_email)
        val btnLogin: TextView = itemView.findViewById(R.id.btn_login)

        //View
        builder.setView(itemView)
        val dialog = builder.create()
        dialog.show()

        //evenclick
        btnRegister.setOnClickListener {
            if (TextUtils.isDigitsOnly(edtFirstName.text.toString())) {
                Toast.makeText(this, "Vui lòng nhập Họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (TextUtils.isDigitsOnly(edtLastName.text.toString())) {
                Toast.makeText(this, "Vui lòng nhập Họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (TextUtils.isEmpty(edtPhoneNumber.text.toString())) {
                Toast.makeText(this, "Vui lòng nhập Số điện thoại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (TextUtils.isEmpty(edtEmail.text.toString())) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                val riderMiTa = RiderMiTa()
                riderMiTa.firstName = edtFirstName.text.toString()
                riderMiTa.lastName = edtLastName.text.toString()
                riderMiTa.phoneNumber = "0" + edtPhoneNumber.text.toString()
                riderMiTa.email = edtEmail.text.toString()
                riderMiTa.gender = "default"
                riderMiTa.avatar = ""
                riderMiTa.dateOfBirth="01/01/2000"
                riderMiTa.address = "default"
                riderMiTa.point = 0

                //add to database firebase
                riderMiTaRef
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(riderMiTa)
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        gotoHomeActivity(riderMiTa)
                    }
            }
        }
    }

    private fun showLoginLayout() {
        providers = Arrays.asList(
            AuthUI.IdpConfig.PhoneBuilder().build(),
//            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.activity_sign_in)
            .setPhoneButtonId(R.id.btn_phone_sign_in)
            .setGoogleButtonId(R.id.btn_gg_sign_in)
//            .setFacebookButtonId(R.id.btn_fb_sign_in)
            .build()

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.LoginTheme)
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(), LOGIN_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
//                val user = FirebaseAuth.getInstance().currentUser
                checkUserFromFirebase()
            } else
                Toast.makeText(this, response!!.error!!.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkUserFromFirebase() {
        riderMiTaRef
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(
                            this@FirstScreenActivity, "User already register!", Toast.LENGTH_SHORT).show()
                        Handler().postDelayed({
                            var model = snapshot.getValue(RiderMiTa::class.java)
                            gotoHomeActivity(model)
                        }, 1000)
                    } else {
                        showRegisterLayout()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@FirstScreenActivity, error.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun gotoHomeActivity(model: RiderMiTa?) {
        Common.currentUser = model
        val intent = Intent(this@FirstScreenActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}