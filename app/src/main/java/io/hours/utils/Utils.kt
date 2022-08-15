package io.hours.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Utils {

    @SuppressLint("SimpleDateFormat")
    fun formatDate(timeStamp: Long): String {
        val timeFormat = "yyyy-MM-dd HH:mm:ss"
        val formatter = SimpleDateFormat(timeFormat)
        return formatter.format(Date(timeStamp))
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDateHour(timeStamp: Long): String {
        val timeFormat = "HH:mm"
        val formatter = SimpleDateFormat(timeFormat)
        return formatter.format(Date(timeStamp))
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDate(date: Date): String {
        val timeFormat = "yyyy-MM-dd"
        val formatter = SimpleDateFormat(timeFormat)
        return formatter.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getHour(timeStamp: Long): String {
        val timeFormat = "HH:mm a"
        val formatter = SimpleDateFormat(timeFormat)
        return formatter.format(Date(timeStamp))
    }

    fun getDurationBreakdown(timeStamp: Long): String {
        return try {
            var millis = timeStamp
            require(millis >= 0) { "Duration must be greater than zero!" }
            val hours = TimeUnit.MILLISECONDS.toHours(millis)
            millis -= TimeUnit.HOURS.toMillis(hours)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
            millis -= TimeUnit.MINUTES.toMillis(minutes)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
            if (hours > 0)
                hours.toString() + "h " + minutes.toString() + "m " + seconds.toString() + "s"
            else
                minutes.toString() + "m " + seconds.toString() + "s"

        } catch (e: Exception) {
            ""
        }
    }

    fun dayNameInWeek(dayInWeek: Int): String {
        return when (dayInWeek) {
            1 -> "sun"
            2 -> "mon"
            3 -> "tue"
            4 -> "wed"
            5 -> "thu"
            6 -> "fri"
            7 -> "sat"
            else -> "never"
        }
    }

    fun milliSecondToMinutes(milliSeconds: Long): String {
        return (milliSeconds / 60_000).toString()
    }

    fun dailyDateFormat(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = monthName(calendar.get(Calendar.MONTH))
        val year = calendar.get(Calendar.YEAR)
        return "$day $month, $year"
    }

    fun weeklyDateFormat(fromDate: Long, toDate: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fromDate
        val fromDay = calendar.get(Calendar.DAY_OF_MONTH)
        val fromMonth = monthName(calendar.get(Calendar.MONTH))
        calendar.timeInMillis = toDate
        val toDay = calendar.get(Calendar.DAY_OF_MONTH)
        val toMonth = monthName(calendar.get(Calendar.MONTH))
        val year = calendar.get(Calendar.YEAR)
        return "$fromDay $fromMonth, $toDay $toMonth, $year"
    }

    fun monthlyDateFormat(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val month = monthName(calendar.get(Calendar.MONTH))
        val year = calendar.get(Calendar.YEAR)
        return "$month, $year"
    }


    private fun monthName(monthInYear: Int): String {
        return when (monthInYear) {
            0 -> "Jan"
            1 -> "Feb"
            2 -> "Mar"
            3 -> "Apr"
            4 -> "May"
            5 -> "jun"
            6 -> "Jul"
            7 -> "Aug"
            8 -> "Sep"
            9 -> "Oct"
            10 -> "Nov"
            11 -> "Dec"
            else -> "never"
        }
    }

    fun getTodayFirstMoment(): Long {
        val todayCalendar = Calendar.getInstance()
        val passedHoursInMilliSeconds = todayCalendar.get(Calendar.HOUR_OF_DAY) * 3_600_000
        val passedMinutesInMilliSeconds = todayCalendar.get(Calendar.MINUTE) * 60_000
        val passedSecondsInMilliSeconds = todayCalendar.get(Calendar.SECOND) * 1_000
        return System.currentTimeMillis() - passedHoursInMilliSeconds - passedMinutesInMilliSeconds - passedSecondsInMilliSeconds
    }

    fun getFirstMomentOfWeek(): Long {
        val todayCalendar = Calendar.getInstance()
        val passedDaysInMilliSeconds = todayCalendar.get(Calendar.DAY_OF_WEEK) * 3_600_000 * 24
        val passedHoursInMilliSeconds = todayCalendar.get(Calendar.HOUR_OF_DAY) * 3_600_000
        val passedMinutesInMilliSeconds = todayCalendar.get(Calendar.MINUTE) * 60_000
        val passedSecondsInMilliSeconds = todayCalendar.get(Calendar.SECOND) * 1_000
        return System.currentTimeMillis() - passedDaysInMilliSeconds - passedHoursInMilliSeconds - passedMinutesInMilliSeconds - passedSecondsInMilliSeconds
    }


}