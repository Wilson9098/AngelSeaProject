package com.example.angelseaproject.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.angelseaproject.R
import com.example.angelseaproject.data.LoaclData
import com.example.angelseaproject.data.Operation
import com.example.angelseaproject.databinding.ActivityMainBinding
import com.example.angelseaproject.ui.operation.ExampleAdapter
import com.example.angelseaproject.ui.operation.ExampleItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

var Oadsad = 1

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var database_Operations : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取 BottomNavigationView
        val navView: BottomNavigationView = binding.navView

        // 从Intent中获取用户名，并存储到 SharedPreferences
        val username = intent.getStringExtra("username")
        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", username)
            apply()
        }

        // 设置 Navigation
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        /* val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration) */
        navView.setupWithNavController(navController)

        // 初始化 SharedPreferences 和它的监听器
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "expiredDrugCount" || key == "upcomingDrugCount" || key == "totalDrugsToHandle") {
                updateBadge()
            }
        }
        sharedPref.registerOnSharedPreferenceChangeListener(listener)

        // 初始设置 Badge
        updateBadge()
    }


    override fun onDestroy() {
        super.onDestroy()
        // 反注册 SharedPreferences 监听器
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
    }

    // 更新 Badge 的方法
    private fun updateBadge() {
        val navView: BottomNavigationView = binding.navView
        val expiredDrugCount = sharedPref.getInt("expiredDrugCount", 0)
        val upcomingDrugCount = sharedPref.getInt("upcomingDrugCount", 0)
        val totalDrugsToHandle = expiredDrugCount + upcomingDrugCount // 计算总数

        if (totalDrugsToHandle > 0) {
            val badge = navView.getOrCreateBadge(R.id.navigation_notifications)
            badge.number = totalDrugsToHandle
            badge.isVisible = true
        } else {
            navView.removeBadge(R.id.navigation_notifications)
        }
    }


}
