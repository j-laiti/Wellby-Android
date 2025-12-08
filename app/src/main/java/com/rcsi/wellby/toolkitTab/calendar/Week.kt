package com.rcsi.wellby.toolkitTab.calendar
// model for the week associated with a specific date

import java.util.Date

data class Week(val dates: List<Date>, val referenceDate: Date)