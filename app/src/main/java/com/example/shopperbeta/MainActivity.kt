package com.example.shopperbeta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        // Login Edit Text
        val email = editLoginEmail
        val password = editLoginPassword

        buttonLogin.setOnClickListener{
            loginUser(email.text.toString(), password.text.toString())
        }
        buttonLoginRegisterLbl.setOnClickListener {
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loginUser(email: String, password: String){
        if(email.isNotEmpty() && password.isNotEmpty()) {
            errorLogin.text = getString(R.string.emptyText)

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if(it.isSuccessful){
                    // Navigate to Home Screen
                        val loginIntent = Intent(this,HomeActivity::class.java)
                        Toast.makeText(this, "Authentication Succeed", Toast.LENGTH_SHORT).show()
                    startActivity(loginIntent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            errorLogin.text = getString(R.string.errorLoginMessage)
        }

    }
}