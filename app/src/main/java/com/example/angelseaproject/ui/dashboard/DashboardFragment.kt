package com.example.angelseaproject.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.angelseaproject.R
import com.example.angelseaproject.databinding.FragmentDashboardBinding
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class DrugInfo(
    val traceableID: String,
    val name: String,
    val storageLocation: String,
    val expiredDate: String,
    val type: String,
    val security: String,
    var isSelected: Boolean = false,
    var taged: Boolean = false
)


class DrugInfoAdapter(private var drugList: MutableList<DrugInfo>, private val showCheckbox: Boolean) : RecyclerView.Adapter<DrugInfoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val itemNumber: TextView = itemView.findViewById(R.id.itemNumber)
        val nameTextView: TextView = itemView.findViewById(R.id.drugName)  // 新添加的 TextView 变量
        val traceIDTextView: TextView = itemView.findViewById(R.id.drugTraceID)
        val locationTextView: TextView = itemView.findViewById(R.id.drugLocation)
        val expiredDateTextView: TextView = itemView.findViewById(R.id.drugExpiredDate)
        val typeTextView: TextView = itemView.findViewById(R.id.drugType)
        val securityTextView: TextView = itemView.findViewById(R.id.drugSecurity)
    }

    // 以下为原有的 onCreateViewHolder 和 onBindViewHolder 方法，这部分不需要更改
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.drug_item, parent, false))
    }

    fun getSelectedDrugs(): List<DrugInfo> {
        return drugList.filter { it.isSelected }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentDrugInfo = drugList[position]

        //holder.itemNumber.text = "${position + 1}."
        holder.nameTextView.text = "${currentDrugInfo.name}"  // 新增的代码，用于显示 Drug Name
        holder.traceIDTextView.text = "${currentDrugInfo.traceableID}"
        holder.locationTextView.text = "${currentDrugInfo.storageLocation}"
        holder.typeTextView.text = "${currentDrugInfo.type}"
        holder.securityTextView.text = "${currentDrugInfo.security}"
        holder.itemView.findViewById<ImageView>(R.id.tagIconGreen).visibility = View.GONE
        holder.itemView.findViewById<ImageView>(R.id.tagIconRed).visibility = View.GONE
        holder.itemView.findViewById<ImageView>(R.id.tagIconOrange).visibility = View.GONE

        // 获取复选框，并设置它的状态
        val checkbox = holder.itemView.findViewById<CheckBox>(R.id.selectCheckBox)
        checkbox.visibility = if (showCheckbox) View.VISIBLE else View.GONE

        // 当复选框状态改变时，更新 isSelected 字段
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            currentDrugInfo.isSelected = isChecked
        }

        // 设置过期日期，并进行颜色更改
        val expiredDateString = "${currentDrugInfo.expiredDate}"
        holder.expiredDateTextView.text = expiredDateString

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expiredDate: Date = sdf.parse(currentDrugInfo.expiredDate)!!
        val currentDate: Date = sdf.parse(sdf.format(Date()))!!

        if (expiredDate.before(currentDate)) {
            // 如果药品已经过期，显示红色角标
            holder.expiredDateTextView.setTextColor(android.graphics.Color.RED)
            holder.itemView.findViewById<ImageView>(R.id.tagIconRed).visibility = View.VISIBLE
        } else {
            if (currentDrugInfo.taged) {
                // 如果taged为true，显示绿色角标
                holder.itemView.findViewById<ImageView>(R.id.tagIconGreen).visibility = View.VISIBLE
            } else {
                // 如果taged为false，显示黄色角标
                holder.itemView.findViewById<ImageView>(R.id.tagIconOrange).visibility = View.VISIBLE
            }
            holder.expiredDateTextView.setTextColor(android.graphics.Color.GRAY)
        }
    }


    fun updateList(newList: List<DrugInfo>) {
        drugList.clear()
        drugList.addAll(newList)
        notifyDataSetChanged()
    }


    override fun getItemCount() = drugList.size
}


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private val drugList = mutableListOf<DrugInfo>()
    private lateinit var adapter: DrugInfoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        database = FirebaseDatabase.getInstance().getReference("Drugs")

        adapter = DrugInfoAdapter(drugList, showCheckbox = false) // 对于Dashboard界面

        binding.drugListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.drugListRecyclerView.adapter = adapter

        binding.addDrugButton.setOnClickListener {
            // 添加新药品的逻辑
        }

        binding.searchDrug.setOnClickListener {
            val drugName = binding.searchBar.text.toString()
            if (drugName.isNotEmpty()) {
                readDrugData(drugName)
            } else {
                readAllDrugs()
            }
        }

        val addButton: Button = binding.root.findViewById(R.id.addDrugButton)  // 注意这里是 binding.root.findViewById
        addButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_addDrugFragment)
        }


        return root
    }

    private fun readAllDrugs() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                drugList.clear()
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val traceID = snapshot.child("traceableID").value.toString()
                        val name = snapshot.child("name").value.toString()
                        val location = snapshot.child("storageLocation").value.toString()
                        val expiredDate = snapshot.child("expiredDate").value.toString()
                        val type = snapshot.child("type").value.toString()
                        val security = snapshot.child("security").value.toString()
                        val taged = snapshot.child("taged").getValue(Boolean::class.java) ?: false
                        drugList.add(DrugInfo(traceID, name, location, expiredDate, type, security, false, taged))
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to Read Data", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun readDrugData(searchQuery: String) {
        // 这个标志用来检查是否已经找到了匹配的数据
        var isFound = false

        // 第一个查询：根据名字搜索
        database.orderByChild("name")
            .startAt(searchQuery)
            .endAt("$searchQuery\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (isFound) return

                    drugList.clear()
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            // 从 snapshot 中读取数据并添加到 drugList
                            val traceID = snapshot.child("traceableID").value.toString()
                            val name = snapshot.child("name").value.toString()
                            val location = snapshot.child("storageLocation").value.toString()
                            val expiredDate = snapshot.child("expiredDate").value.toString()
                            val type = snapshot.child("type").value.toString()
                            val security = snapshot.child("security").value.toString()
                            val taged = snapshot.child("taged").getValue(Boolean::class.java) ?: false
                            drugList.add(DrugInfo(traceID, name, location, expiredDate, type, security,false, taged))
                        }
                        isFound = true
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to Read Data", Toast.LENGTH_SHORT).show()
                }
            })

        // 第二个查询：根据 traceableID 搜索
        database.orderByChild("traceableID")
            .equalTo(searchQuery)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (isFound) return

                    drugList.clear()
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            // 从 snapshot 中读取数据并添加到 drugList
                            val traceID = snapshot.child("traceableID").value.toString()
                            val name = snapshot.child("name").value.toString()
                            val location = snapshot.child("storageLocation").value.toString()
                            val expiredDate = snapshot.child("expiredDate").value.toString()
                            val type = snapshot.child("type").value.toString()
                            val security = snapshot.child("security").value.toString()
                            drugList.add(DrugInfo(traceID, name, location, expiredDate, type, security))
                        }
                        isFound = true
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to Read Data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
