package com.example.angelseaproject.ui.notifications

import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.angelseaproject.data.Drug
import com.example.angelseaproject.data.LoaclData
import com.example.angelseaproject.data.Operation
import com.example.angelseaproject.databinding.FragmentNotificationsBinding
import com.example.angelseaproject.ui.dashboard.DrugInfo
import com.example.angelseaproject.ui.dashboard.DrugInfoAdapter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.example.angelseaproject.R
import com.example.angelseaproject.data.User

enum class FilterType {
    ALL_PENDING_MEDICATIONS, EXPIRED_MEDICATIONS, SOON_TO_EXPIRE_MEDICATIONS
}

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var database_Operations: DatabaseReference
    private val drugList = mutableListOf<DrugInfo>()
    private lateinit var adapter: DrugInfoAdapter
    private var currentFilter: FilterType = FilterType.ALL_PENDING_MEDICATIONS
    private var isControlledChecked = false


    // 新添加的列表来保存原始数据
    private val originalDrugList = mutableListOf<DrugInfo>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize the database references
        database = FirebaseDatabase.getInstance().getReference("Drugs")
        database_Operations = FirebaseDatabase.getInstance().getReference("Operations")

        // Initialize the adapter with the drugList
        adapter = DrugInfoAdapter(drugList, showCheckbox = true) // 对于Notifications界面


        // Set the LinearLayoutManager and adapter for the RecyclerView
        // This is where you define the RecyclerView's layout behavior
        binding.expiredDrugListRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Setting the adapter to the RecyclerView
        binding.expiredDrugListRecyclerView.adapter = adapter

        val controlledCheckbox = binding.root.findViewById<CheckBox>(R.id.controlledCheckbox)
        controlledCheckbox.setOnCheckedChangeListener { _, isChecked ->
            // 更新复选框状态
            isControlledChecked = isChecked
            // 重新筛选药品列表
            filterDrugList()
        }


        // Setting up date views and other UI components
        setupDateViews()

        // Spinner for selecting filter types
        val spinner = binding.filterSpinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Handling selection of filter types
                when (position) {
                    0 -> currentFilter = FilterType.ALL_PENDING_MEDICATIONS
                    1 -> currentFilter = FilterType.EXPIRED_MEDICATIONS
                    2 -> currentFilter = FilterType.SOON_TO_EXPIRE_MEDICATIONS
                }
                // Applying the selected filter to the drug list
                filterDrugList()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing here
            }
        }

        // Button click listeners
        binding.deleteButton.setOnClickListener {
            deleteSelectedDrugs()
        }

        binding.labelButton.setOnClickListener {
            tagSelectedExpiringDrugs()
        }

        // Read and load expired drug data
        readExpiredDrugData()

        // Return the root view
        return root
    }

    private fun tagSelectedExpiringDrugs() {
        // 获取选中的药品
        val selectedDrugs = adapter.getSelectedDrugs()

        // 日期格式化
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Date()

        // 标记是否存在已经过期的药品
        var hasExpiredDrugs = false

        // 遍历选中的药品
        for (drugInfo in selectedDrugs) {
            // 解析过期日期
            val expiredDate = sdf.parse(drugInfo.expiredDate)

            // 检查药品是否已过期
            if (expiredDate.before(currentDate)) {
                hasExpiredDrugs = true
                break
            }
        }

        // 如果存在已过期的药品
        if (hasExpiredDrugs) {
            // 弹出禁止操作的对话框
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Forbidden Operation")
                setMessage("Operation not allowed. The selection contains expired drugs. Please proceed to deletion.")
                setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                show()
            }
        } else {
            // 所有选中的药品都未过期
            val builder2 = AlertDialog.Builder(requireContext())
            val inflater2 = requireActivity().layoutInflater
            val dialogView2 = inflater2.inflate(R.layout.tag_drugs_alert_dialog, null)
            val checkbox11 = dialogView2.findViewById<CheckBox>(R.id.checkbox11)
            val yesButton = dialogView2.findViewById<Button>(R.id.yesButton)
            val noButton = dialogView2.findViewById<Button>(R.id.noButton)

            builder2.setView(dialogView2)

            val dialog = builder2.create()
            dialog.show()

            yesButton.setOnClickListener {
                if (checkbox11.isChecked) {
                    selectedDrugs.forEach { drugInfo ->
                        if (!drugInfo.taged) {
                            val drugUpdate = mapOf("taged" to true)
                            database.child(drugInfo.traceableID).updateChildren(drugUpdate).addOnSuccessListener {
                                drugInfo.taged = true
                                adapter.notifyDataSetChanged()

                                // save and upload operation
                                var drug = Drug(drugInfo.traceableID, drugInfo.type, drugInfo.name, drugInfo.security,
                                    drugInfo.storageLocation, drugInfo.expiredDate, drugInfo.taged)
                                val currentTime = LocalDateTime.now()
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                val formattedTime = currentTime.format(formatter)
                                val newOperation = Operation(drug, LoaclData.User, formattedTime,
                                    "Apply red stickers to the drugs nearing expiration and clearly mark the expiration date with a bold marker.")

                                database_Operations.child(LoaclData.User.firstName.toString()).child(formattedTime).setValue(newOperation)
                                LoaclData.Operations.add(newOperation)

                            }.addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to update tag for ${drugInfo.name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "You must confirm the declaration before proceeding", Toast.LENGTH_LONG).show()
                }
            }

            noButton.setOnClickListener {
                // 当“NO”按钮被点击时，关闭对话框
                dialog.dismiss()
            }
        }

    }

    private fun deleteSelectedDrugs() {
        val selectedDrugs = adapter.getSelectedDrugs()

        if (selectedDrugs.isEmpty()) {
            Toast.makeText(requireContext(), "No drugs selected for deletion", Toast.LENGTH_SHORT).show()
            return
        }

        // 检查是否有未过期或即将过期的药品
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate: Date = sdf.parse(sdf.format(Date()))!!
        var hasSoonToExpire = false

        for (drug in selectedDrugs) {
            val drugDate = sdf.parse(drug.expiredDate)!!
            if (drugDate.after(currentDate)) {
                hasSoonToExpire = true
                break
            }
        }

        // 如果有未过期或即将过期的药品，显示警告对话框
        if (hasSoonToExpire) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Warning")
            builder.setMessage("The selected drugs have not expired yet. Are you sure you want to delete?")
            builder.setPositiveButton("YES") { _, _ -> performDeletion(selectedDrugs) }
            builder.setNegativeButton("NO") { dialog, _ -> dialog.dismiss() }
            builder.show()
        } else {
            performDeletion(selectedDrugs)
        }
    }


    private fun performDeletion(selectedDrugs: List<DrugInfo>) {
        for (drug in selectedDrugs) {
            // Check if the drug is a controlled substance.
            if (drug.type == "Controlled") {
                val builder = AlertDialog.Builder(requireContext())
                val inflater = layoutInflater
                val dialogLayout = inflater.inflate(R.layout.delete_drugs_alert_dialog, null)
                val checkbox1 = dialogLayout.findViewById<CheckBox>(R.id.deleteConfirmCheckbox1)
                val checkbox2 = dialogLayout.findViewById<CheckBox>(R.id.deleteConfirmCheckbox2)
                val checkbox5 = dialogLayout.findViewById<CheckBox>(R.id.deleteConfirmCheckbox5)
                val yesButton = dialogLayout.findViewById<Button>(R.id.yesButton)
                val noButton = dialogLayout.findViewById<Button>(R.id.noButton)

                val dialog = builder.create()
                dialog.setView(dialogLayout)
                dialog.show()

                yesButton.setOnClickListener {
                    if (checkbox1.isChecked && checkbox2.isChecked && checkbox5.isChecked) {
                        deleteDrug(drug)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "You must confirm all conditions before deleting a controlled substance", Toast.LENGTH_LONG).show()
                    }
                }

                noButton.setOnClickListener {
                    dialog.cancel()
                }
            } else {
                // If the drug is not controlled, delete as usual.
                deleteDrug(drug)
            }
        }
    }

    private fun deleteDrug(drug: DrugInfo) {
        database.child(drug.traceableID).removeValue().addOnSuccessListener {
            drugList.remove(drug)
            originalDrugList.remove(drug)
            filterDrugList()


            var _drug : Drug = Drug(drug.traceableID, drug.type, drug.name, drug.security, drug.storageLocation, drug.expiredDate)

            val currentTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedTime = currentTime.format(formatter)

            var operationContent = ""
            if (drug.type == "Controlled"){
                operationContent = "Remove expired controlled drugs from the safe and transfer them to the PACU."
            } else {
                operationContent = "Dispose of expired non-controlled drugs"
            }
            val newOperation = Operation(_drug, LoaclData.User, formattedTime, operationContent)

            database_Operations.child(LoaclData.User.firstName.toString()).child(formattedTime).setValue(newOperation)
            LoaclData.Operations.add(newOperation)

            Toast.makeText(requireContext(), "Successfully deleted ${drug.name}", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to delete ${drug.name}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun filterDrugList() {
        val filteredList = mutableListOf<DrugInfo>()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate: Date = sdf.parse(sdf.format(Date()))!!
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val futureDate: Date = calendar.time

        for (drug in originalDrugList) {
            val drugDate = sdf.parse(drug.expiredDate)!!

            val shouldAdd = when {
                isControlledChecked && drug.type == "Controlled" -> true
                !isControlledChecked -> true  // 当复选框未勾选时显示所有药品
                else -> false
            }
            if (shouldAdd){
                when (currentFilter) {
                    FilterType.ALL_PENDING_MEDICATIONS -> {
                        // 如果药品已经过期，无论 taged 值如何都添加到列表中
                        if (drugDate.before(currentDate)) {
                            filteredList.add(drug)
                        }
                        // 如果药品未过期但即将在30天内过期，确保 taged 为 false 才添加到列表中
                        else if (drugDate.before(futureDate) && !drug.taged) {
                            filteredList.add(drug)
                        }
                    }
                    FilterType.EXPIRED_MEDICATIONS -> {
                        if (drugDate.before(currentDate)) {
                            filteredList.add(drug)
                        }
                    }
                    FilterType.SOON_TO_EXPIRE_MEDICATIONS -> {
                        // Check if the drug is expiring soon and not tagged
                        if (drugDate.after(currentDate) && drugDate.before(futureDate) && !drug.taged) {
                            filteredList.add(drug)
                        }
                    }
                }
            }
        }
        adapter.updateList(filteredList)
    }


    private fun setupDateViews() {
        val currentDate = LocalDate.now()
        val nextCheckDate = currentDate.plusDays(30)

        binding.todaysCheckDateValue.text = currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        binding.nextCheckDateValue.text = nextCheckDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    private fun readExpiredDrugData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                drugList.clear()
                originalDrugList.clear()
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val currentDate: Date = sdf.parse(sdf.format(Date()))!!
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.add(Calendar.DAY_OF_YEAR, 30)
                val futureDate: Date = calendar.time

                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val expiredDateStr = snapshot.child("expiredDate").value.toString()
                        val expiredDate: Date = sdf.parse(expiredDateStr)!!
                        val traceID = snapshot.child("traceableID").value.toString()
                        val name = snapshot.child("name").value.toString()
                        val location = snapshot.child("storageLocation").value.toString()
                        val type = snapshot.child("type").value.toString()
                        val security = snapshot.child("security").value.toString()
                        val taged = snapshot.child("taged").getValue(Boolean::class.java) ?: false

                        originalDrugList.add(DrugInfo(traceID, name, location, expiredDateStr, type, security, false, taged))
                    }
                    filterDrugList()
                } else {
                    Toast.makeText(requireContext(), "No Drugs Info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



