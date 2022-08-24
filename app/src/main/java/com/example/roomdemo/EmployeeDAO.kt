package com.example.roomdemo

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface EmployeeDAO {

    @Insert
    suspend fun insert(employeeEntity: EmployeeEntity)

    @Update
    suspend fun update(employeeEntity: EmployeeEntity)

    @Delete
    suspend fun delete(employeeEntity: EmployeeEntity)

    @Query("SELECT * FROM `employee-table`")
    fun fetchALLEmployees():Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM `employee-table` where id=:id")
    fun fetchEmployeesById(id:Int):Flow<EmployeeEntity>

}





