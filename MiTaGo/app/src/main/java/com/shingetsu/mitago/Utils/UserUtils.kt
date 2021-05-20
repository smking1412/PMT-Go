package com.shingetsu.mitago.Utils

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.shingetsu.mitago.Models.TokenModel
import com.shingetsu.mitago.R

/**
 * Created by Phạm Minh Tân - Shin on 5/9/2021.
 */
object UserUtils {
    fun updateUser(view : View?,  updateData : Map<String,Any>) {
        FirebaseDatabase.getInstance()
            .getReference(Common.RIDER_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .updateChildren(updateData)
            .addOnFailureListener{ e->
                Snackbar.make(view!!, e.message!!, Snackbar.LENGTH_LONG).show()
            }
            .addOnSuccessListener {
                Snackbar.make(view!!, R.string.update_success,Snackbar.LENGTH_LONG).show()
            }
    }

    fun updateToken(context : Context, token: String) {
        val tokenModel = TokenModel()
        tokenModel.token = token

        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(tokenModel)
            .addOnFailureListener{e->
                Toast.makeText(context,e.message,Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {  }
    }
}