package com.example.angelseaproject.ui.user

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.angelseaproject.R
import com.example.angelseaproject.data.LoaclData
import com.example.angelseaproject.data.Operation
import com.example.angelseaproject.data.User
import com.example.angelseaproject.databinding.AllNurseHistoryBinding
import com.example.angelseaproject.ui.operation.ExampleAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class OperationFragment : Fragment(R.layout.all_nurse_history) {
    private lateinit var adapter_rv: ExampleAdapter
    private lateinit var database_allOperations: DatabaseReference
    private lateinit var database_users: DatabaseReference
    private var _binding: AllNurseHistoryBinding? = null
    private val binding get() = _binding!!
    private var startDateCalendar = Calendar.getInstance()
    private var endDateCalendar = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = AllNurseHistoryBinding.bind(view)
        database_allOperations = FirebaseDatabase.getInstance().getReference("Operations")
        database_users = FirebaseDatabase.getInstance().getReference("User")
        adapter_rv = ExampleAdapter(LoaclData.allOperations, requireContext(), true)
        val spinner: Spinner = view.findViewById(R.id.operationNurseFilterSpinner)
        startDateCalendar = Calendar.getInstance()
        endDateCalendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        // Spinner data set
        var useList = ArrayList<User>(21)
        var spinnerContent = ArrayList<String>(21)
        var selected: String = "ALL"
//        Log.d("test1", selected)

        val adapter_spinner =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerContent)
        spinner.adapter = adapter_spinner

        database_users.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    useList.clear()
                    useList.add(0, User())
                    spinnerContent.clear() // 清空旧数据
                    spinnerContent.add(0, "ALL")
                    for (item in snapshot.children) {
                        val user = item.getValue(User::class.java)
                        if (user != null) {
                            useList.add(user)
                            spinnerContent.add(user.firstName.toString())
                        }
                    }
                    adapter_spinner.notifyDataSetChanged() // 通知适配器数据已更新
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // handle errors
            }
        })


        // set spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // 获取选中的项
                if (useList.isNotEmpty()) {
                    if (useList[position] != null) {
//                        Log.d("test2", selected)
                        if (position > 0) {
                            selected = useList[position].firstName.toString()
//                            Log.d("test3", selected)
                        }
                    } else {
                        selected = "ALL"
                    }
                }
                // 可以在这里根据选中的项进行操作
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 可选实现，当没有选项被选择时的行为
                selected = "ALL"
            }
        }
        adapter_spinner.notifyDataSetChanged()

        //search btn click
        binding.buttonSearch.setOnClickListener {
            searchByName(selected, startDateCalendar, endDateCalendar)
        }

        //reset btn click
        binding.buttonReset.setOnClickListener {
            reset(spinner, startDateCalendar, endDateCalendar)
        }


        // set start date
        startDateCalendar.add(Calendar.DATE, -7)
        binding.historyStartDateButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    startDateCalendar.set(year, month, dayOfMonth)
                    startDateCalendar.stripTime()
                    binding.historyStartDateValue.text = dateFormat.format(startDateCalendar.time)
                },
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // set end date
        endDateCalendar.time = Calendar.getInstance().time
        binding.historyEndDateButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    endDateCalendar.set(year, month, dayOfMonth)
                    endDateCalendar.stripTime()
                    binding.historyEndDateValue.text = dateFormat.format(endDateCalendar.time)
                },
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }


        binding.apply {
            recyclerViewAllOperations.setHasFixedSize(true)
            recyclerViewAllOperations.itemAnimator = null
            recyclerViewAllOperations.adapter = adapter_rv
            operationNurseFilterSpinner.adapter = adapter_spinner
            historyEndDateValue.text = "-/-/-"
            historyStartDateValue.text = "-/-/-"
        }

        // creating page with searching
        searchByName("ALL", startDateCalendar, endDateCalendar)
    }

    private fun searchByName(name: String, start: Calendar, end: Calendar) {
        // RecyclerView loading
        database_allOperations.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    LoaclData.allOperations.clear()
                    for (item in snapshot.children) {
                        if (name == "ALL" || item.key.toString() == name) {
                            for (data in item.children) {
                                val operationData = data.getValue(Operation::class.java)

                                if (operationData != null) {
                                    val formatter =
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                    val Date = LocalDateTime.parse(operationData.date, formatter)

                                    val operationCalendar: Calendar = Calendar.getInstance()

                                    operationCalendar.clear()
                                    operationCalendar.set(Calendar.YEAR, Date.year)
                                    operationCalendar.set(Calendar.MONTH, Date.monthValue - 1)
                                    operationCalendar.set(Calendar.DAY_OF_MONTH, Date.dayOfMonth)

                                    // 创建一个 SimpleDateFormat 用于输出
                                    val dateFormat =
                                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    val formattedDate = dateFormat.format(operationCalendar.time)
                                    val formattedStart = dateFormat.format(start.time)
//                                    Log.d("op", "$formattedDate")
//                                    Log.d("start", "$formattedStart")

                                    val isBetween =
                                        !operationCalendar.before(start)
                                                && !operationCalendar.after(end)
//                                    Log.d("TAG", "$isBetween")

                                    if (isBetween) {
                                        LoaclData.allOperations.add(0, operationData)
                                    }
                                }
                            }
                        }
                    }
                    adapter_rv.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // handle errors
            }

        })
    }

    private fun reset(spinner: Spinner, start: Calendar, end: Calendar) {
        spinner.setSelection(0)
        searchByName("ALL", start, end)

        startDateCalendar = Calendar.getInstance()
        endDateCalendar = Calendar.getInstance()
        binding.historyEndDateValue.text = "-/-/-"
        binding.historyStartDateValue.text = "-/-/-"
    }

    private fun Calendar.stripTime(): Calendar = apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
