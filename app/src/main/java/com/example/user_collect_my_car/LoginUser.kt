package com.example.user_collect_my_car

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.UserModel
import com.example.user_collect_my_car.Utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_login_user.*

class LoginUser : AppCompatActivity() {

    private lateinit var database : FirebaseDatabase
    private lateinit var userInfoRef : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth

    private lateinit var listener: FirebaseAuth.AuthStateListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_user)

        login_button_login.setOnClickListener {

            signIn()

        }

        not_signed_up.setOnClickListener {

            Log.d("`Login Activity`", "Show Register Activity")

            val intent = Intent(this, RegisterUser::class.java)

            startActivity(intent)

        }


    }

    private fun signIn(){

        val email = email_editText_login.text.toString()
        val password = password_editText_login.text.toString()

        Log.d("Login", "Attempt Login with Email / Password : $email/***")

        if(email.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Email Address", Toast.LENGTH_SHORT).show()

            return

        }

        else if(password.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Password", Toast.LENGTH_SHORT).show()

            return

        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)

            .addOnCompleteListener{

                if(!it.isSuccessful) return@addOnCompleteListener

                Log.d("Main", "Successfully Signed in with User with UID: ${it.result?.user!!.uid}")

                Toast.makeText(this, "Successfully Signed in", Toast.LENGTH_SHORT).show()

              /*  FirebaseInstanceId.getInstance().instanceId
                    .addOnFailureListener { e->


                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()

                    }

                    .addOnSuccessListener { instanceIdResult ->


                        Log.d("TOKEN", instanceIdResult.token)
                        UserUtils.updateToken(this@LoginUser, instanceIdResult.token)



                    }*/

                //getUserFromFirebase()
                firebaseAuth.addAuthStateListener(listener)
               /* val intent = Intent(this, MapsActivity::class.java)

                //intent.putExtra(MESSGAE, email)

                startActivity(intent)*/

            }

            .addOnFailureListener{

                Log.d("Main", "ERROR - Failed to Sign in with User : ${it.message}")

                Toast.makeText(this, "ERROR - Failed to Sign in with User : ${it.message}", Toast.LENGTH_SHORT).show()

            }

        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->

            val user = myFirebaseAuth.currentUser

            if(user != null){

                FirebaseMessaging.getInstance().token
                        .addOnFailureListener { e->


                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()

                        }

                        .addOnSuccessListener { token ->


                            Log.d("TOKEN", token)
                            UserUtils.updateToken(this@LoginUser, token)



                        }


                getUserFromFirebase()

            }

        }
    }

    private fun getUserFromFirebase(){

        database = FirebaseDatabase.getInstance()
        userInfoRef = database.getReference(Common.USER_INFO_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()

        userInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener{

                    override fun onDataChange(p0: DataSnapshot) {

                        if(p0.exists()){

                            val model = p0.getValue(UserModel::class.java)

                            goToMapsActivity(model)

                        }

                    }

                    override fun onCancelled(p0: DatabaseError) {

                        Toast.makeText(this@LoginUser, p0.message, Toast.LENGTH_SHORT).show()

                    }


                })

    }

    private fun goToMapsActivity(model: UserModel?) {

        Common.currentUser = model

        val intent = Intent(this, MapsActivity::class.java)

        //intent.putExtra(MESSGAE, email)

        startActivity(intent)

        finish()



    }


}