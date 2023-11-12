package com.example.angelseaproject.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.angelseaproject.data.LoaclData

import com.example.angelseaproject.databinding.FragmentLoginBinding
import com.example.angelseaproject.ui.MainActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class loginPage : AppCompatActivity() {

    private lateinit var binding:FragmentLoginBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("User")

        binding.myLoginButtonLogin.setOnClickListener {
            val userName = binding.myLoginTextInputUsername.text.toString()
            val passWord = binding.myLoginTextInputPassword.text.toString()

            if (userName.isNotEmpty() && passWord.isNotEmpty()) {
                // 从Firebase获取用户数据
                // Fetch the user data from Firebase
                database.child(userName).get().addOnSuccessListener {
                    if (it.exists()) {
                        // 用户存在，现在检查密码
                        // User exists, now check the password
                        val retrievedPassword = it.child("passWord").value.toString()
                        if (retrievedPassword == passWord) {
                            // get the user
                            LoaclData.User.firstName = it.child("firstName").value.toString()
                            LoaclData.User.lastName = it.child("lastName").value.toString()
                            LoaclData.User.passWord = it.child("passWord").value.toString()
                            LoaclData.User.NHI = it.child("nhi").value.toString()

                            // 密码正确，导航到主页
                            // Password is correct, navigate to Homepage
                            val intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("username", userName)
                            startActivity(intent)
                            finish()
                        } else {
                            // 密码不正确
                            // Password is incorrect
                            Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 用户不存在
                        // User doesn't exist
                        Toast.makeText(this, "User doesn't exist", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    // 数据库获取失败
                    // Database fetch failed
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            } else {
                // 用户名或密码字段为空
                // Username or Password field is empty
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.myLoginButtonSignUp.setOnClickListener {
            // 导航到注册页面
            // Navigate to RegisterPage
            val intent = Intent(this, RegistrationFragment::class.java)
            startActivity(intent)
        }

        binding.myLoginButtonForgot.setOnClickListener {
            // 导航到注册页面
            // Navigate to RegisterPage
            val intent = Intent(this, ForgotPage::class.java)
            startActivity(intent)
        }

        }
    }

