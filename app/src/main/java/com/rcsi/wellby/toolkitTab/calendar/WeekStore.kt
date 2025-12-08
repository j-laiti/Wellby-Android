package com.rcsi.wellby.toolkitTab.calendar
// view model for controlling the week of dates displayed in the calendar view and handling navigation
// to previous and future weeks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date


class WeekStore : ViewModel() {
    private val _currentWeek = MutableStateFlow<Week?>(null)
    val currentWeek: StateFlow<Week?> = _currentWeek

    init {
        setCurrentWeek(Date()) // Initialize with the current week
    }

    private fun setCurrentWeek(date: Date) {
        viewModelScope.launch {
            _currentWeek.value = weekFromDate(date)
        }
    }

    fun adjustWeek(by: Int) {
        viewModelScope.launch {
            _currentWeek.value?.referenceDate?.let { referenceDate ->
                val calendar = Calendar.getInstance().apply {
                    time = referenceDate
                    add(Calendar.WEEK_OF_YEAR, by)
                }
                _currentWeek.value = weekFromDate(calendar.time)
            }
        }
    }

    private fun weekFromDate(date: Date): Week {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        val dates = List(7) { index ->
            calendar.run {
                add(Calendar.DATE, if (index == 0) 0 else 1) // Add 1 day for each index after the first
                time
            }
        }
        return Week(dates, calendar.time)
    }
}
