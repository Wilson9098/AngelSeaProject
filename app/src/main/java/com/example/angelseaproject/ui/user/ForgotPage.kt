package com.example.angelseaproject.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.angelseaproject.R
import com.example.angelseaproject.databinding.FragmentForgotBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ForgotPage : AppCompatActivity() {
    private lateinit var binding: FragmentForgotBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentForgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.myFindButtonConfirm.setOnClickListener {
            val firstName = binding.myFindFirstname.text.toString()
            val lastName = binding.myFindLastname.text.toString()
            val nHI = binding.myFindNHI.text.toString()

            findPassword(firstName, lastName, nHI)
        }

//        binding.myFindButtonBack.setOnClickListener {
//            finish()
//        }
    }

    private fun findPassword(firstName: String, lastName: String, nHI: String) {
        database = FirebaseDatabase.getInstance().getReference("User")

        database.child(firstName).get().addOnSuccessListener {
            if (it.exists()) {
                val storedLastName = it.child("lastName").value as? String ?: ""
                val storedNHI = it.child("nhi").value as? String ?: ""

                if (storedLastName.equals(lastName, ignoreCase = true) && storedNHI.equals(nHI, ignoreCase = true)) {
                    val password = it.child("passWord").value as? String ?: ""
                    showPasswordDialog(password)
                } else {
                    showToast("Last name or NHI does not match.")
                }
            } else {
                showToast("User does not exist.")
            }
        }.addOnFailureListener {
            showToast("Failed to find password.")
        }
    }


    private fun showPasswordDialog(password: String) {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.find_password_alert_dialog, null)
        val passwordTextView = dialogView.findViewById<TextView>(R.id.yourPassword)
        val logInButton = dialogView.findViewById<Button>(R.id.LogInButton)

        // Set the retrieved password to the TextView
        passwordTextView.text = password

        // Create the AlertDialog using the Builder
        val passwordDialog = AlertDialog.Builder(this).create()
        passwordDialog.setView(dialogView)
        passwordDialog.show()

        // Set the click listener for the login button
        logInButton.setOnClickListener {
            // Handle the login action here if necessary
            passwordDialog.dismiss()


            val intent = Intent(this, loginPage::class.java)
            startActivity(intent)
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
