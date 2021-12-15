package com.example.shopperbeta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.shopperbeta.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var confirmPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        buttonRegister.setOnClickListener {
            email = editRegisterEmail.text.toString()
            password = editRegisterPassword.text.toString()
            confirmPassword = editRegisterConfirmPassword.text.toString()

            // Fields validation and if its correct then register the user
            if(email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                errorRegisterText.text = getString(R.string.emptyText)
                if(password == confirmPassword){
                    registerUser(email,password)
                }else{
                    errorRegisterText.text = getString(R.string.emptyText)
                    errorRegisterText.text = getString(R.string.errorPasswordDontMatch)
                }
            }else{
                errorRegisterText.text = getString(R.string.errorEmptyFields)
            }
        }
        buttonRegisterCancel.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(email: String, password: String){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            val db = Firebase.firestore
             val newUser = User("","")


            if(it.isSuccessful){
                Toast.makeText(this, "User registered",
                    Toast.LENGTH_SHORT).show()

                // Get current user created and assign his values to User model
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    newUser.userid = firebaseUser.uid
                    newUser.userEmail = email
                }

                // Put User in hashmap and create a new user in Firestore database
                val addUser = hashMapOf(
                    "userid" to newUser.userid,
                    "userEmail" to newUser.userEmail,
                )
                db.collection("users").document(newUser.userEmail).set(addUser)

            }else{
                Toast.makeText(this, "Registration Failed",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }
}