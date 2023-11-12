package com.example.angelseaproject.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.angelseaproject.R
import com.example.angelseaproject.databinding.FragmentHomeBinding
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import com.example.angelseaproject.data.Drug
import com.example.angelseaproject.data.LoaclData
import com.example.angelseaproject.data.Operation
import com.example.angelseaproject.databinding.FragmentOperationsBinding
import com.example.angelseaproject.ui.operation.ExampleAdapter


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var drugDatabase: DatabaseReference
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ExampleAdapter
    private lateinit var database_Operations: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().getReference("User")
        drugDatabase = FirebaseDatabase.getInstance().getReference("Drugs")
        database_Operations = FirebaseDatabase.getInstance().getReference("Operations").child(LoaclData.User.firstName.toString())

        getCurrentUser()
        checkExpiredDrugs()

        binding.allNursesHistoryButton.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToAdminLoginPage()
            findNavController().navigate(action)
        }

        adapter = ExampleAdapter(LoaclData.Operations, requireContext(), false)

        binding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
        }

        // Attach a listener to read the data at our Operations reference
        database_Operations.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    LoaclData.Operations.clear()
                    for (item in snapshot.children){
                        var operationData = item.getValue(Operation::class.java)

                        if (operationData != null) {
                            LoaclData.Operations.add(0,operationData)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // handle errors
            }

        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getCurrentUser()
        checkExpiredDrugs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrentUser() {
        val sharedPref = activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPref?.getString("username", "默认用户名") ?: "默认用户名"

        database.child(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstName").value.toString()
                    val lastName = snapshot.child("lastName").value.toString()
                    val nhiNumber = snapshot.child("nhi").value.toString()
                    binding.nurseDynamicName.text = "$firstName"
                    binding.nurseFirstName.text = "$firstName"
                    binding.nurseLastName.text = "$lastName"
                    binding.nurseRegNum.text = "$nhiNumber"
                } else {
                    // 当用户不存在时可能的操作
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error, such as through logging or a Toast message
            }
        })
    }

    private fun checkExpiredDrugs() {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // 设置 currentDate 时间部分为0
        val currentCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentDate = currentCalendar.time
        // 设置 upcomingDate 时间部分为0
        val upcomingCalendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val upcomingDate = upcomingCalendar.time

        drugDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var expiredCount = 0
                    var upcomingCount = 0

                    for (drugSnapshot in snapshot.children) {
                        val expiredDateString = drugSnapshot.child("expiredDate").value.toString()
                        val expiredCalendar = Calendar.getInstance()
                        expiredCalendar.time = formatter.parse(expiredDateString) ?: continue
                        expiredCalendar.set(Calendar.HOUR_OF_DAY, 0)
                        expiredCalendar.set(Calendar.MINUTE, 0)
                        expiredCalendar.set(Calendar.SECOND, 0)
                        expiredCalendar.set(Calendar.MILLISECOND, 0)
                        val expiredDate = expiredCalendar.time
                        val taged = drugSnapshot.child("taged").getValue(Boolean::class.java) ?: false

                        if (expiredDate.before(currentDate)) {
                            expiredCount++
                        } else if (expiredDate.after(currentDate) && expiredDate.before(upcomingDate) && !taged) {
                            upcomingCount++
                        }
                    }

                    val totalDrugsToHandle = expiredCount + upcomingCount
                    binding.expiredWarning.text =
                        "$totalDrugsToHandle Medications for Expiry Review"
                    if (totalDrugsToHandle > 0) {
                        binding.nurseNotificationIcon.visibility = View.VISIBLE
                        binding.expiredWarning.visibility = View.VISIBLE
                        binding.expiredWarning.text =
                            "$totalDrugsToHandle Drugs for Expiry Review"
                    } else {
                        binding.nurseNotificationIcon.visibility = View.GONE
                        binding.expiredWarning.visibility = View.GONE
                    }

                    // 更新SharedPreferences
                    val sharedPref = activity?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    sharedPref?.edit()?.apply {
                        putInt("totalDrugsToHandle", totalDrugsToHandle)
                        putInt("upcomingDrugCount", upcomingCount)
                        putInt("expiredDrugCount", expiredCount)
                        Log.d(
                            "Debug",
                            "checkExpiredDrugs - Expired: $expiredCount, Upcoming: $upcomingCount"
                        )
                        apply()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error?
            }
        })
    }
}







