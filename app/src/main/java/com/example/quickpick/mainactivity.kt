package com.example.quickpick

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.quickpick.EndUserLayouts.HomeENdUser
import com.example.quickpick.HomeLayoutforDrivers.homefordriandowner
import com.example.quickpick.Model.QuickpickdataModel
import com.example.quickpick.Utils.UserUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import dmax.dialog.SpotsDialog

class mainactivity : AppCompatActivity() {
    override fun onStart() {
        super.onStart()
        val alertDialog=SpotsDialog.Builder().setMessage("Please wait!!")
            .setContext(this).setCancelable(false)
            .build()
        alertDialog.show()
        firebaseUser= FirebaseAuth.getInstance().currentUser
        if (firebaseUser==null){
            startActivity(Intent(this,loginactivity::class.java))
            alertDialog.dismiss()
        }
//
        else{

            val database=FirebaseDatabase.getInstance().reference.child(Commmon.DRIVER_INFO_REFERENCE).child(FirebaseAuth.getInstance().currentUser!!.uid)
            database.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val model=snapshot.getValue(QuickpickdataModel::class.java)
                    if (firebaseUser?.uid.toString()==model?.uid){

                        FirebaseInstanceId.getInstance().
                            instanceId
                            .addOnFailureListener{e-> Toast.makeText(this@mainactivity,
                            e.message,Toast.LENGTH_SHORT
                                ).show()}
                            .addOnSuccessListener { instaceResult->
                                Log.d("Token",instaceResult.token)
                                UserUtils.updateToken(this@mainactivity,instaceResult.token)


                            }


                        if (model.isEnduser){
                            startActivity(Intent(this@mainactivity,HomeENdUser::class.java))
                            alertDialog.dismiss()
                            finish()
                        }
                        else{
                            startActivity(Intent(this@mainactivity,homefordriandowner::class.java))
                            alertDialog.dismiss()
                            finish()
                        }
                    }
                }


            })

        }


    }



    private var firebaseauth:FirebaseAuth?=null
    private  var firebaseUser:FirebaseUser?=null
    companion object{
        private  val Long_inCode=7171;
    }
    private lateinit var providers:List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var listeener:FirebaseAuth.AuthStateListener
    private lateinit var firebasedatabase: FirebaseDatabase
    private lateinit var driverinfo: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainactivity)
        firebaseUser= FirebaseAuth.getInstance().currentUser
//        if (firebaseUser!=null){
//              if (Commmon.currentuser!!.isEnduser){
//                startActivity(Intent(this,HomeENdUser::class.java))
//                finish()
//            }
//            else{
//                startActivity(Intent(this,homefordriandowner::class.java))
//                finish()
//            }
//        }



    }







}