package com.example.angelseaproject.ui.operation

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.angelseaproject.R
import com.example.angelseaproject.data.LoaclData
import com.example.angelseaproject.data.Operation
import com.example.angelseaproject.data.User
import com.google.android.material.button.MaterialButton
import org.w3c.dom.Text
import java.time.format.DateTimeFormatter

class ExampleAdapter(
    private val operationList: ArrayList<Operation>,
    val context: Context,
    val has_BtnInfo: Boolean
) :
    RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.all_nurse_history_item,
            parent, false
        )

        return ExampleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        if (operationList[position] != null) {
            val currentItem = operationList[position]
            holder.drugName.text = currentItem.drug?.name + " " + currentItem.drug?.traceableID
//            holder.drugExpireDate.text = "ExpireDate: " + currentItem.drug?.expiredDate
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            holder.operationDate.text = currentItem.date
            holder.operationContent.text = currentItem.content
            holder.drugType.text = currentItem.drug?.type
            holder.operatorName.text = currentItem.user?.firstName

            if (has_BtnInfo) {
                holder.btn_info.visibility = View.VISIBLE
                holder.btn_info.setOnClickListener {
                    if (currentItem.user != null) {
                        showInfoDialog(currentItem.user!!)
                    }
                }
            } else {
                holder.btn_info.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = operationList.size
    class ExampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val drugName: TextView = itemView.findViewById(R.id.drugIdAndName)
        val drugType: TextView = itemView.findViewById(R.id.drugTypeLabel)

        //        val drugExpireDate: TextView = itemView.findViewById(R.id.operation_drug_expire_date)
        val operationContent: TextView = itemView.findViewById(R.id.operationValue)
        val operationDate: TextView = itemView.findViewById(R.id.checkDateValue)
        val operatorName: TextView = itemView.findViewById(R.id.nurseName)
        val btn_info: ImageView = itemView.findViewById(R.id.nurseInformation)
    }

    private fun showInfoDialog(user: User) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.nurse_personal_information_alert_dialog)

        val usernameView = dialog.findViewById<TextView>(R.id.yourUsername)
        val passwordView = dialog.findViewById<TextView>(R.id.yourPassword)
        dialog.findViewById<TextView>(R.id.nurseFullName).text =
            user.firstName + " " + user.lastName
        dialog.findViewById<TextView>(R.id.tv_RegNum).text = user.NHI

        val returnBtn = dialog.findViewById<MaterialButton>(R.id.backButton)
        returnBtn.setOnClickListener {
            dialog.dismiss() // 关闭对话框
        }

        dialog.show()
    }
}