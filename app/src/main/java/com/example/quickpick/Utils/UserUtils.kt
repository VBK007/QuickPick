package com.example.quickpick.Utils

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.example.quickpick.Commmon
import com.example.quickpick.Model.TokenModel
import com.example.quickpick.Services.MyFirebaseMessagingSertives
import com.google.android.gms.common.internal.service.Common
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UserUtils {
    fun updateToken(context: Context, token: String) {
        val tokenModel= TokenModel()
        tokenModel.token=token

        FirebaseDatabase.getInstance()
            .getReference(Commmon.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(token)
            .addOnFailureListener{
                e->
                Toast.makeText(context,e.message,Toast.LENGTH_LONG).show()
            }

            .addOnSuccessListener {


            }

    }


}