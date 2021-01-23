package com.example.quickpick

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var imageView:ImageView=findViewById(R.id.imageView);
        var textView:TextView=findViewById(R.id.toptext);
        var bootomtext:TextView=findViewById(R.id.botomtext)

        var topanimation:Animation=AnimationUtils.loadAnimation(this,R.anim.top_animation)
        var bottomanm:Animation=AnimationUtils.loadAnimation(this,R.anim.bottomanim)

        imageView.animation=topanimation
        textView.animation=bottomanm
        bootomtext.animation=bottomanm
        //inti()
Handler().postDelayed({
    startActivity(Intent(this, mainactivity::class.java))
    finish()
},3000)

    }








    }




