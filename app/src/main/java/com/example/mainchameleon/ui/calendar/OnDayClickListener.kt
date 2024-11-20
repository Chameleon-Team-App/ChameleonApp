package com.example.mainchameleon.ui.calendar

import java.time.LocalDate

interface OnDayClickListener {
    fun onItemClick(position: Int, day: LocalDate?)
}
