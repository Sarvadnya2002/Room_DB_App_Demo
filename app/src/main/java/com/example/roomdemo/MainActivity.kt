package com.example.roomdemo

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomdemo.databinding.ActivityMainBinding
import com.example.roomdemo.databinding.DialogUpdateBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val employeeDAO = (application as EmployeeApp).db.employeeDao()
        binding?.btnAdd?.setOnClickListener {
            addRecord(employeeDAO)
        }
        lifecycleScope.launch {
            employeeDAO.fetchALLEmployees().collect {
                val list = ArrayList(it)
                setUpListOfDataIntoRecyclerView(list, employeeDAO)
            }

        }

    }


    fun addRecord(employeeDAO: EmployeeDAO) {
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmailId?.text.toString()

        if (name.isNotEmpty() && email.isNotEmpty()) {
            lifecycleScope.launch {
                employeeDAO.insert(EmployeeEntity(name = name, email = email))
                Toast.makeText(applicationContext, "Record Saved", Toast.LENGTH_SHORT).show()
                binding?.etName?.text?.clear()
                binding?.etEmailId?.text?.clear()
            }
        } else {
            Toast.makeText(
                this,
                "Name or email cannot be empty",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpListOfDataIntoRecyclerView(
        employeeList: ArrayList<EmployeeEntity>,
        employeeDao: EmployeeDAO
    ) {
        if (employeeList.isNotEmpty()) {
            val itemAdapter = ItemAdapter(employeeList,
                { updateId ->
                    updateRecordDialog(updateId, employeeDao)
                },
                { deleteId ->
                    deleteRecordAlertDialog(deleteId, employeeDao)
                })

            binding?.rvItemsList?.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding?.rvItemsList?.adapter=itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun updateRecordDialog(id: Int, employeeDao: EmployeeDAO) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDao.fetchEmployeesById(id).collect {
                if(it!=null) {
                    binding.etUpdateName.setText(it.name)
                    binding.etUpdateEmailId.setText(it.email)
                }
            }
        }

        binding.tvUpdate.setOnClickListener {
            val name = binding.etUpdateName.text.toString()
            val email = binding.etUpdateEmailId.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                lifecycleScope.launch {
                    employeeDao.update(EmployeeEntity(id, name, email))
                    Toast.makeText(applicationContext, "Record Updated", Toast.LENGTH_SHORT).show()
                    updateDialog.dismiss()
                }
            } else {
                Toast.makeText(
                    this,
                    "Name or email cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    private fun deleteRecordAlertDialog(id: Int, employeeDao: EmployeeDAO) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(
                    applicationContext,
                    "Record deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

}