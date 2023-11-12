package com.example.angelseaproject.ui.user

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.angelseaproject.R
import com.example.angelseaproject.data.User
import com.example.angelseaproject.databinding.FragmentRegistrationBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegistrationFragment : AppCompatActivity() {

    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("User")

        binding.myRegButtonConfirm.setOnClickListener {
            val firstname = binding.myRegTextInputName.text.toString()
            val lastname = binding.myRegTextInputSurname.text.toString()
            val password = binding.myRegTextInputPassword.text.toString()
            val NHI = binding.myRegTextInputNHI.text.toString()
            val isTermsChecked = binding.agreeCheckBox.isChecked

            if (isTermsChecked) {
                if (firstname.isNotEmpty() && lastname.isNotEmpty() && password.isNotEmpty() && NHI.isNotEmpty()) {
                    val user = User(firstname, lastname, password, NHI)
                    database.child(firstname).setValue(user).addOnSuccessListener {
                        clearInputFields()
                        showSuccessDialog(firstname, password) // 使用用户名和密码调用弹窗显示
                    }.addOnFailureListener {
                        showCustomDialog("Registration Failed", "Failed to save user data") // 使用自定义对话框显示错误
                    }
                } else {
                    showCustomDialog("Registration Incomplete!", "All fields are required to be filled in to proceed with the registration.") // 如果信息不完整也使用自定义对话框
                }
            } else {
                showCustomDialog("Agreement Required", "You must agree to the terms and privacy policy to register.") // 如果未勾选条款，使用自定义对话框
            }
        }


        binding.myRegButtonHome.setOnClickListener {
            navigateToLoginPage()
        }
    }

    private fun clearInputFields() {
        binding.myRegTextInputName.text?.clear()
        binding.myRegTextInputSurname.text?.clear()
        binding.myRegTextInputPassword.text?.clear()
        binding.myRegTextInputNHI.text?.clear()
    }

    private fun showSuccessDialog(username: String, password: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.registration_success_dialog)

        // 获取自定义布局中的文本视图并设置用户名和密码
        val usernameView = dialog.findViewById<TextView>(R.id.yourUsername)
        val passwordView = dialog.findViewById<TextView>(R.id.yourPassword)
        usernameView.text = username
        passwordView.text = password

        // 初始化登录按钮并设置点击事件
        val logInButton = dialog.findViewById<MaterialButton>(R.id.LogInButton)
        logInButton.setOnClickListener {
            dialog.dismiss() // 关闭对话框
            navigateToLoginPage() // 导航到登录页面
        }

        dialog.show()
    }


    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK", null)
            show()
        }
    }

    private fun navigateToLoginPage() {
        val intent = Intent(this, loginPage::class.java)
        startActivity(intent)
        finish()
    }

    fun onTermsClick(view: View) {
        // Intent to start Terms of Service Activity or navigate to the fragment
        val intent = Intent(this, TermsOfServiceActivity::class.java)
        startActivity(intent)
    }

    fun onPolicyClick(view: View) {
        // Intent to start Privacy Policy Activity or navigate to the fragment
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    private fun showTermsDialog() {
        // Create a Dialog instance
        val dialog = Dialog(this)
        // Set the custom layout
        dialog.setContentView(R.layout.terms_alert_dialog)

        // Initialize the button from the custom layout
        val yesButton = dialog.findViewById<MaterialButton>(R.id.yesButton)

        // Set the button click listener
        yesButton.setOnClickListener {
            dialog.dismiss() // Close the dialog
            binding.agreeCheckBox.isChecked = true // Optionally set the checkbox to checked
        }

        // Display the custom dialog
        dialog.show()
    }

    private fun showCustomDialog(title: String, message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.terms_alert_dialog)

        // 获取并设置标题和消息
        val titleView = dialog.findViewById<TextView>(R.id.requestText)
        val messageView = dialog.findViewById<TextView>(R.id.signUpInfo)
        titleView.text = title
        messageView.text = message

        val yesButton = dialog.findViewById<MaterialButton>(R.id.yesButton)
        yesButton.setOnClickListener {
            dialog.dismiss() // 关闭对话框
            // 如果需要，可以在这里添加更多逻辑
        }

        dialog.show()
    }





}
