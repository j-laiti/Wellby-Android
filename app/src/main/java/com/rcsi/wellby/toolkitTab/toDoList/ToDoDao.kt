package com.rcsi.wellby.toolkitTab.toDoList
// methods to store and manage to do items on the users phone
import androidx.room.*
import java.util.Date

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todo_items WHERE date BETWEEN :startOfDay AND :endOfDay")
    fun getToDosForDate(startOfDay: Date, endOfDay: Date): List<ToDoItemEntity>

    @Insert
    fun addToDo(toDoItemEntity: ToDoItemEntity)

    @Update
    fun updateToDo(toDoItemEntity: ToDoItemEntity)

    @Delete
    fun deleteToDo(toDoItemEntity: ToDoItemEntity)

    @Query("SELECT COUNT(*) FROM todo_items WHERE isComplete = 1 AND date BETWEEN :startOfDay AND :endOfDay")
    suspend fun getCompletedTasksCount(startOfDay: Date, endOfDay: Date): Int

    @Query("SELECT COUNT(*) FROM todo_items WHERE date BETWEEN :startOfDay AND :endOfDay")
    suspend fun getTotalTasksCount(startOfDay: Date, endOfDay: Date): Int
}