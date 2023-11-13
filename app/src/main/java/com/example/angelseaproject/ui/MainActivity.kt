package com.example.angelseaproject.ui

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.angelseaproject.R
import com.example.angelseaproject.data.LoaclData
import com.example.angelseaproject.data.Operation
import com.example.angelseaproject.databinding.ActivityMainBinding
import com.example.angelseaproject.ui.dashboard.DashboardFragment
import com.example.angelseaproject.ui.home.HomeFragment
import com.example.angelseaproject.ui.notifications.NotificationsFragment
import com.example.angelseaproject.ui.operation.ExampleAdapter
import com.example.angelseaproject.ui.operation.ExampleItem
import com.example.angelseaproject.ui.user.SettingsPage
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

//        navView.setOnNavigationItemSelectedListener { item ->
//            val navOptions = NavOptions.Builder()
//                .setPopUpTo(R.id.nav_host_fragment_activity_main, true)
//                .build()
//
//            when (item.itemId) {
//                R.id.navigation_home -> {
//                    navController.navigate(R.id.navigation_home, null, navOptions)
//                }
//                R.id.navigation_dashboard -> {
//                    navController.navigate(R.id.navigation_dashboard, null, navOptions)
//                }
//                R.id.navigation_notifications -> {
//                    navController.navigate(R.id.navigation_notifications, null, navOptions)
//                }
//                R.id.navigation_settings -> {
//                    navController.navigate(R.id.navigation_settings, null, navOptions)
//                }
//            }
//            true
//        }

        navView.setOnItemSelectedListener { item ->
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_host_fragment_activity_main, true)
                .build()

            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home, null, navOptions)
                    true
                }
                R.id.navigation_dashboard -> {
                    navController.navigate(R.id.navigation_dashboard, null, navOptions)
                    true
                }
                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications, null, navOptions)
                    true
                }
                R.id.navigation_settings -> {
                    navController.navigate(R.id.navigation_settings, null, navOptions)
                    true
                }
                else -> false
            }
        }


        // 设置未选中项的颜色
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked), // 未选中状态
                intArrayOf(android.R.attr.state_checked) // 选中状态
            ),
            intArrayOf(
                Color.parseColor("#696969"), // 未选中项的颜色
                Color.parseColor("#009688") // 选中项的颜色
            )
        )

        navView.itemIconTintList = colorStateList
        navView.itemTextColor = colorStateList


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
