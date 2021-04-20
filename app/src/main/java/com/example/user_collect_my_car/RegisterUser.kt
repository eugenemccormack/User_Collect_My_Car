package com.example.user_collect_my_car

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.user_collect_my_car.Common.Common
import com.example.user_collect_my_car.Model.UserModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register_user.*
import java.util.*


class RegisterUser : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var userInfoRef: DatabaseReference

    private var fileUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)


        iv_profileImage.setOnClickListener {

            selectImage()

        }


        register_button_register.setOnClickListener {

            register()

        }

        not_signed_up.setOnClickListener {

            Log.d("MainActivity", "Show Login Activity")

            val intent = Intent(this, LoginUser::class.java)

            startActivity(intent)

        }

    }

    private fun selectImage() {

        ImagePicker.with(this)
            .crop().compress(1024).maxResultSize(1080, 1080).start()


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Log.d("MainActivity", "Photo was Selected 1")

        super.onActivityResult(requestCode, resultCode, data)

        when(resultCode){
            Activity.RESULT_OK -> {

                fileUri = data?.data

                iv_profileImage.setImageURI(fileUri)

            }

            ImagePicker.RESULT_ERROR -> {

                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()

            }

            else -> {

                Toast.makeText(this, "Image Selection Cancelled", Toast.LENGTH_SHORT).show()

            }



        }

        /*if(resultCode == 1 && requestCode == Activity.RESULT_OK && data != null){

            Log.d("MainActivity", "Photo was Selected 2")

            val uri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            //val bitmapDrawable = BitmapDrawable(bitmap)
            val bitmapDrawable = BitmapDrawable(this.resources,bitmap)
            photo_button_user.setBackground(bitmapDrawable)

        }*/


    }



    private fun register(){

        database = FirebaseDatabase.getInstance()
        userInfoRef = database.getReference(Common.USER_INFO_REFERENCE)

        val model = UserModel()

        model.name = username_editText_register.text.toString()
        model.phone = phone_editText_register.text.toString()
        model.email = email_editText_register.text.toString()
        val password = password_editText_register.text.toString()

        if(model.name.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Name", Toast.LENGTH_SHORT).show()

            return

        }

        else if(model.phone.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Phone Number", Toast.LENGTH_SHORT).show()

            return

        }

        else if(model.email.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Email Address", Toast.LENGTH_SHORT).show()

            return

        }

        else if(password.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Password", Toast.LENGTH_SHORT).show()

            return

        }

        if(fileUri == null) {

            Toast.makeText(this, "ERROR - Please Select A Photo", Toast.LENGTH_SHORT).show()

            return

        }





        Log.d("MainActivity", "Email is : " + model.email)
        Log.d("MainActivity", "Password is : $password")

        //Firebase Authentication to Create a User with Email and Password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(model.email, password)
            .addOnCompleteListener{

                if(!it.isSuccessful) return@addOnCompleteListener

              /*  val filename = UUID.randomUUID().toString()

                val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

                ref.putFile(fileUri!!)
                    .addOnSuccessListener {

                        Log.d("Main", "Successfully Uploaded Image :  ${it.metadata?.path}")

                        ref.downloadUrl.addOnSuccessListener {

                            //it.toString()

                            Log.d("Main", "Image File Location : $it")

                            //saveUserInfo(it.toString())

                            //var profileImageUrl: String  = it.toString()

                            Log.d("Main", "Image File Location : $it")

                        }

                    }*/
/*
*//*
                driverInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .setValue(model)
                        .addOnFailureListener { e ->

                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()

                        }

                        .addOnSuccessListener {

                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

                        }*//*


                // val uid = FirebaseAuth.getInstance().uid ?: ""
                //val ref = FirebaseDatabase.getInstance().getReference("/DriverInfo/$uid")

                // val user = DriverInfo(uid, username_editText_register.text.toString(), phone_editText_register.text.toString(), email_editText_register.text.toString())

                //ref.setValue(user)
                userInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(model)

                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, LoginUser::class.java)

                startActivity(intent)

                finish()

              //  Common.currentUser = model*/



                Log.d("Main", "Successfully Create User with UID: ${it.result?.user!!.uid}")

              //  uploadImageToFireBaseStorage()

               // saveUserInfo(it.toString())

                uploadImageToFireBaseStorage()
            }

            .addOnFailureListener{

                Log.d("Main", "ERROR - Failed to Create User : ${it.message}")

                Toast.makeText(this, "ERROR - Fail to Create User : ${it.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun uploadImageToFireBaseStorage() {

        if(fileUri == null) {

            Toast.makeText(this, "ERROR - Please Select A Photo", Toast.LENGTH_SHORT).show()

            return

        }

        val filename = UUID.randomUUID().toString()

        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(fileUri!!)
            .addOnSuccessListener {

                Log.d("Main", "Successfully Uploaded Image :  ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {

                    //it.toString()

                    Log.d("Main", "Image File Location : $it")

                    saveUserInfo(it.toString())

                    //var profileImageUrl: String  = it.toString()

                    Log.d("Main", "Image File Location : $it")

                }

            }

    }

    private fun saveUserInfo(profileImageUrl: String){

        val model = UserModel()

        database = FirebaseDatabase.getInstance()
        userInfoRef = database.getReference(Common.USER_INFO_REFERENCE)

        model.image = profileImageUrl
        model.name = username_editText_register.text.toString()
        model.phone = phone_editText_register.text.toString()
        model.email = email_editText_register.text.toString()


        userInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(model)

        val intent = Intent(this, LoginUser::class.java)

        startActivity(intent)

        finish()

    }


}