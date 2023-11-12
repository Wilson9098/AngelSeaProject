package com.example.angelseaproject.ui.dashboard

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.angelseaproject.R
import com.example.angelseaproject.data.Drug
import com.example.angelseaproject.data.LoaclData
import com.example.angelseaproject.data.Operation
import com.example.angelseaproject.data.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AddDrugFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var database_Operations: DatabaseReference
    private lateinit var nameSpinner: Spinner
    private lateinit var drugTypeSpinner: Spinner
    private lateinit var securitySpinner: Spinner
    private lateinit var storageLocationSpinner: Spinner


    var isFirstTimeName = true
    var isFirstTimeDrugType = true
    var isFirstTimeSecurity = true
    var isFirstTimeStorage = true

    var drug: Drug? = null
    var user: User? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        val view = inflater.inflate(R.layout.fragment_adddrug, container, false)

        database = FirebaseDatabase.getInstance().getReference("Drugs")
        database_Operations = FirebaseDatabase.getInstance().getReference("Operations")

        nameSpinner = view.findViewById(R.id.nameSpinner)
        drugTypeSpinner = view.findViewById(R.id.drugTypeSpinner)
        securitySpinner = view.findViewById(R.id.securitySpinner)
        storageLocationSpinner = view.findViewById(R.id.storageLocationSpinner)
        val saveButton: Button = view.findViewById(R.id.addDrugButton)
        val expiredDateTextView: TextView = view.findViewById(R.id.expiredDateTextView)
        val pickDateButton: Button = view.findViewById(R.id.pickDateButton)

        val drugTraceableIDTextInput: EditText = view.findViewById(R.id.drugTraceableIDTextInput)


        val actualNameList = arrayOf("Oxycodone", "Oxynorm", "Tramadol", "Codeine", "Panadol", "Ibuprofen", "Gabapentin", "Celebrex")
        val drugTypeList = arrayOf("Controlled", "Simple")
        val securityList = arrayOf("Safe", "Cupboard")
        val emptyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, emptyList<String>())

        val controlledList = arrayOf("Knox Wing Drugs Room")
        val simpleList = arrayOf("Ward Office Nurses Station", "Day Stay Nurses Station")
        val controlledAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, controlledList)
        val simpleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, simpleList)

        val actualNameAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, actualNameList)
        val drugTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, drugTypeList)
        val securityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, securityList)

        drugTypeSpinner.setSelection(0)

        nameSpinner.adapter = emptyAdapter
        drugTypeSpinner.adapter = emptyAdapter
        securitySpinner.adapter = emptyAdapter
        storageLocationSpinner.adapter = emptyAdapter
        
        nameSpinner.setOnTouchListener { _, _ ->
            if (isFirstTimeName) {
                nameSpinner.adapter = actualNameAdapter
                isFirstTimeName = false
            }
            false
        }

        drugTypeSpinner.setOnTouchListener { _, _ ->
            if (isFirstTimeDrugType) {
                drugTypeSpinner.adapter = drugTypeAdapter
                isFirstTimeDrugType = false
            }
            false
        }

        securitySpinner.setOnTouchListener { _, _ ->
            if (isFirstTimeSecurity) {
                securitySpinner.adapter = securityAdapter
                isFirstTimeSecurity = false
            }
            false
        }

        storageLocationSpinner.setOnTouchListener { _, _ ->
            if (isFirstTimeStorage) {
                val currentDrugType = drugTypeSpinner.selectedItem?.toString() ?: ""
                storageLocationSpinner.adapter = if (currentDrugType == "Controlled") controlledAdapter else simpleAdapter
                isFirstTimeStorage = false
            }
            false
        }

        drugTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isFirstTimeDrugType) {
                    val selectedType = drugTypeList[position]
                    storageLocationSpinner.adapter = if (selectedType == "Controlled") controlledAdapter else simpleAdapter
                    (storageLocationSpinner.adapter as ArrayAdapter<String>).notifyDataSetChanged()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 无操作
            }
        }

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        pickDateButton.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                expiredDateTextView.text = dateFormat.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        saveButton.setOnClickListener {
            val drugTraceableID = drugTraceableIDTextInput.text.toString()
            val drugType = drugTypeSpinner.selectedItem?.toString() ?: ""
            val name = nameSpinner.selectedItem?.toString() ?: ""
            val security = securitySpinner.selectedItem?.toString() ?: ""
            val storageLocation = storageLocationSpinner.selectedItem?.toString() ?: ""
            val expiredDate = expiredDateTextView.text.toString()

            if (name.isBlank() || drugType.isBlank() || security.isBlank() || storageLocation.isBlank() || expiredDate.isBlank() || drugTraceableID.isBlank()) {
                Toast.makeText(requireContext(), "All fields must be filled out!", Toast.LENGTH_SHORT).show()
            } else {
                // 当创建新药品时，'taged' 字段应设置为 false
                drug = Drug(
                    traceableID = drugTraceableID,  // 可追踪 ID
                    type = drugType,  // 药品类型
                    name = name,  // 名称
                    security = security,  // 安全等级
                    storageLocation = storageLocation,  // 存储位置
                    expiredDate = expiredDate,  // 过期日期
                    taged = false  // 标记为未过期
                )
                database.child(drugTraceableID).setValue(drug).addOnSuccessListener {
                    val currentTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val formattedTime = currentTime.format(formatter)
                    val newOperation:Operation = Operation(drug!!, LoaclData.User,formattedTime,
                        "Add the new drug to the drug list management interface.")

                    database_Operations.child(LoaclData.User.firstName.toString()).child(formattedTime).setValue(newOperation)
                    LoaclData.Operations.add(newOperation)

                    Toast.makeText(requireContext(), "Successfully saved", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val backButton: Button = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_addDrugFragment_to_navigation_dashboard)
        }

// 初始化时设置为 false，这样用户触摸时才会更新
        isFirstTimeStorage = false

        return view

    }
}
