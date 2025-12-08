package com.rcsi.wellby.toolkitTab.toDoList
// functions for updating the to do list
import java.util.Date

class ToDoRepository(private val toDoDao: ToDoDao) {
    fun getToDosForDate(startOfDay: Date, endOfDay: Date): List<ToDoItemEntity> =
        toDoDao.getToDosForDate(startOfDay, endOfDay)

    fun addToDo(title: String, date: Date, isComplete: Boolean) {
        val newToDo = ToDoItemEntity(title = title, date = date, isComplete = isComplete)
        toDoDao.addToDo(newToDo)
    }

    fun updateToDo(toDoItemEntity: ToDoItemEntity) {
        toDoDao.updateToDo(toDoItemEntity)
    }

    fun deleteToDo(toDoItemEntity: ToDoItemEntity) {
        toDoDao.deleteToDo(toDoItemEntity)
    }

    suspend fun getCompletedTasksCount(startOfDay: Date, endOfDay: Date): Int =
        toDoDao.getCompletedTasksCount(startOfDay, endOfDay)

    suspend fun getTotalTasksCount(startOfDay: Date, endOfDay: Date): Int =
        toDoDao.getTotalTasksCount(startOfDay, endOfDay)
}
