package com.rcsi.wellby.toolkitTab.toDoList
// view model for managing the functions associated with the to do list managment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class ToDoViewModel(private val repository: ToDoRepository): ViewModel() {

    private val _toDos = MutableLiveData<List<ToDoItemEntity>>()
    val toDos: LiveData<List<ToDoItemEntity>> = _toDos

    private val _selectedDate = MutableLiveData<Date>()
    val selectedDate: LiveData<Date> = _selectedDate

    // Call this method when the selected date changes
    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
        getToDosForDate(date) // Refresh todos for the selected date
    }


    fun addToDoItem(title: String, date: Date) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addToDo(title, date, false)
                getToDosForDate(date)
            } catch (e: Exception) {
                Log.e("ToDoViewModel", "Error adding todo item", e)
            }
        }
    }

    fun getToDosForDate(date: Date) {
        viewModelScope.launch(Dispatchers.IO) { // Offload to background thread
            try {
                val calendar = Calendar.getInstance()
                calendar.time = date
                // Set the calendar to the start of the day
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time

                // Move the calendar to the end of the day
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val endOfDay = calendar.time

                val todos = repository.getToDosForDate(startOfDay, endOfDay)
                withContext(Dispatchers.Main) { // Switch back to Main Thread to update LiveData
                    _toDos.value = todos
                }
            } catch (e: Exception) {
                Log.e("ToDoViewModel", "Error fetching todos", e)
                // Optionally update some error LiveData here as well
            }
        }
    }

    fun toggleToDoComplete(item: ToDoItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedItem = item.copy(isComplete = !item.isComplete)
            repository.updateToDo(updatedItem)
            selectedDate.value?.let { getToDosForDate(it) }
        }
    }

    fun deleteToDoItem(item: ToDoItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteToDo(item)
            _selectedDate.value?.let { getToDosForDate(it) } // Refresh the list after deletion
        }
    }

    fun getCompletedTasksCount(date: Date): LiveData<Int> {
        val completed = MutableLiveData<Int>()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endOfDay = calendar.time

        viewModelScope.launch {
            val count = repository.getCompletedTasksCount(startOfDay, endOfDay)
            completed.postValue(count)
        }

        return completed
    }

    fun getTotalTasksCount(date: Date): LiveData<Int> {
        val total = MutableLiveData<Int>()

        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.time

            calendar.apply {
                add(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }
            val endOfDay = calendar.time

            val count = repository.getTotalTasksCount(startOfDay, endOfDay)
            total.postValue(count)
        }

        return total
    }


}
