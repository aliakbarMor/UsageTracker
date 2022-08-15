package io.hours.model

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.hours.model.modules.*
import io.hours.utils.Utils
import io.hours.utils.Utils.getFirstMomentOfWeek
import io.hours.utils.Utils.getTodayFirstMoment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class UsageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageCellDao: UsageCellDao,
    private val appCellDao: AppCellDao,
) {
    private lateinit var packages: MutableList<ApplicationInfo>
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    lateinit var todayAppCells: List<AppCell>
    lateinit var thisWeekAppCells: List<AppCell>
    lateinit var thisMonthAppCells: List<AppCell>
    var todayUsageCell: UsageCell? = null
    var thisWeekUsageCell: UsageCell? = null
    var thisMonthUsageCell: UsageCell? = null

    init {
        calculateUsageCellsAppCells()
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun calculateUsageCellsAppCells() {
        packages = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val oneDaysInMilliseconds = 86_400_000L

        val firstMomentOfDay = getTodayFirstMoment()

        for (i in 0..9) {
            val firstMomentInDay = firstMomentOfDay - (oneDaysInMilliseconds * i)
            val lastMomentInDay = firstMomentInDay + oneDaysInMilliseconds
            val exist: Boolean
            runBlocking {
                exist = isExist(firstMomentInDay, lastMomentInDay, UsageType.DAY)
            }
            if (!exist) {
                val appCells = ArrayList<AppCell>()
                timelineUsage(firstMomentInDay, lastMomentInDay).forEach { timeline ->
                    val appCell =
                        appCells.find { it.pack.packageName == timeline.pack.packageName }
                    if (appCell != null) {
                        appCell.visit++
                        appCell.usageTime += timeline.duration
                    } else {
                        val appInfo =
                            packages.find { it.packageName == timeline.pack.packageName }
                        if (appInfo != null) {
                            appCells.add(AppCell(Package(timeline.pack.packageName).apply {
                                this.appName =
                                    context.packageManager.getApplicationLabel(appInfo)
                                        .toString()
                                this.icon = context.packageManager.getApplicationIcon(appInfo)
                            }, timeline.duration, 1))
                        }
                    }
                }

                runBlocking {
                    var usage = 0L
                    appCells.forEach {
                        usage += it.usageTime
                    }
                    val usageCell = UsageCell(
                        Utils.dayNameInWeek(Calendar.getInstance().apply {
                            timeInMillis = firstMomentInDay
                        }.get(Calendar.DAY_OF_WEEK)),
                        usage,
                        firstMomentInDay + 1000L,
                        lastMomentInDay - 1000L,
                    )

                    if (i == 0) {
                        todayAppCells = appCells.sortedByDescending { it.usageTime }
                        todayUsageCell = usageCell

                    } else {
                        val usageCellId = addUsageCell(usageCell)
                        addAppCells(appCells.onEach { it.usageCellId = usageCellId })
                    }
                }
            }
        }
    }

    private fun timelineUsage(
        beginTime: Long, endTime: Long
    ): List<TimeLineUsageCell> {

        var temp: TimeLineUsageCell? = null

        val timeLineUsageCells: ArrayList<TimeLineUsageCell> = ArrayList()
        val events = usageStatsManager.queryEvents(beginTime, endTime)

        val event = UsageEvents.Event()
        var start = beginTime
        var end: Long

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val eventType = event.eventType
            val packageName = event.packageName

            if (eventType == UsageEvents.Event.ACTIVITY_RESUMED)
                start = event.timeStamp

            if (eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                end = event.timeStamp

                if (temp == null)
                    temp = TimeLineUsageCell(Package(packageName), start, end, end - start)
                else
                    if (temp.pack.packageName == event.packageName && (start - (temp.launchTime + temp.duration)) <= 1000)
                        temp = TimeLineUsageCell(
                            Package(packageName), temp.launchTime, end, end - temp.launchTime
                        )
                    else {
                        if (temp.duration > 500) {
                            val timelineCell = TimeLineUsageCell(
                                temp.pack,
                                temp.launchTime,
                                temp.exitTime,
                                temp.exitTime - temp.launchTime
                            )
                            timeLineUsageCells.add(timelineCell)
                        }
                        temp = TimeLineUsageCell(
                            Package(packageName), start, end, end - start
                        )
                    }
            }
        }

        timeLineUsageCells.reverse()
        return timeLineUsageCells
    }

    fun getTimeline2day(): List<TimeLineUsageCell> {
        val thisMoment = System.currentTimeMillis()
        return timelineUsage(getTodayFirstMoment() - 86_400_000L, thisMoment).onEach { timeline ->
            val appInfo =
                packages.find { it.packageName == timeline.pack.packageName }
            if (appInfo != null) {
                timeline.pack = Package(timeline.pack.packageName).apply {
                    this.appName =
                        context.packageManager.getApplicationLabel(appInfo)
                            .toString()
                    this.icon = context.packageManager.getApplicationIcon(appInfo)
                }
            }
        }
    }

    suspend fun getAppCells(id: Long): List<AppCell> {
        return withContext(Dispatchers.IO) {
            appCellDao.getAll(id).onEach { appCell ->
                val appInfo =
                    packages.find { it.packageName == appCell.pack.packageName }
                if (appInfo != null) {
                    val appName =
                        context.packageManager.getApplicationLabel(appInfo)
                            .toString()
                    val icon = context.packageManager.getApplicationIcon(appInfo)
                    appCell.pack = Package(appCell.pack.packageName).apply {
                        this.appName = appName
                        this.icon = icon
                    }
                }
            }
        }
    }

    suspend fun getDailyUsageCell(): List<UsageCell> {
        return withContext(Dispatchers.IO) {
            val usageCellList =
                (usageCellDao.getAll(UsageType.DAY) as ArrayList)
                    .apply { add(0, todayUsageCell!!) }
            calculateWeeklyUsage(usageCellList)
            calculateMonthlyUsage(usageCellList)
            usageCellList
        }
    }

    suspend fun getWeeklyUsageCell(): List<UsageCell> {
        return withContext(Dispatchers.IO) {
            (usageCellDao.getAll(UsageType.WEEK) as ArrayList)
                .apply { thisWeekUsageCell?.let { add(0, it) } }
        }
    }

    suspend fun getMonthlyUsageCell(): List<UsageCell> {
        return withContext(Dispatchers.IO) {
            (usageCellDao.getAll(UsageType.MONTH) as ArrayList)
                .apply { add(0, thisMonthUsageCell!!) }
        }
    }

    suspend fun getUsageCellWithId(usageCellId: Long): UsageCell {
        return withContext(Dispatchers.IO) { usageCellDao.getWithId(usageCellId) }
    }

    suspend fun getUsageCellWithDate(fromDate: Long, toDate: Long): List<UsageCell> {
        return withContext(Dispatchers.IO) { usageCellDao.getWithDate(fromDate, toDate) }
    }

    suspend fun isExist(startDate: Long, endDate: Long, type: UsageType): Boolean {
        return withContext(Dispatchers.IO) { usageCellDao.exist(startDate, endDate, type) }
    }

    suspend fun addAppCells(list: List<AppCell>) {
        withContext(Dispatchers.IO) { appCellDao.addAll(list) }
    }

    suspend fun addUsageCell(usageCell: UsageCell): Long {
        return withContext(Dispatchers.IO) { usageCellDao.add(usageCell) }
    }

    private suspend fun calculateWeeklyUsage(usageCellList: List<UsageCell>) {
        val oneWeekInMilliseconds = 604_800_000L
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        loop@ for (i in 0..2) {
            val firstMomentInWeek = getFirstMomentOfWeek() - (oneWeekInMilliseconds * i)
            val lastMomentInWeek = firstMomentInWeek + oneWeekInMilliseconds
            val exist = isExist(firstMomentInWeek, lastMomentInWeek, UsageType.WEEK)
            if (!exist) {
                val usageCellsOfWeek = usageCellList.filterIndexed { index, _ ->
                    index in i * 7 - (7 - dayOfWeek) until i * 7 + dayOfWeek
                }
                if (usageCellsOfWeek.isEmpty())
                    continue@loop
                var usage = 0L
                val appCellsWeekly = ArrayList<AppCell>()
                usageCellsOfWeek.forEach { usageCellOfWeek ->
                    val appCells = getAppCells(usageCellOfWeek.id)
                    appCells.forEach { appCell ->
                        val find =
                            appCellsWeekly.find { it.pack.packageName == appCell.pack.packageName }
                        if (find == null) {
                            appCellsWeekly.add(appCell)
                        } else {
                            find.usageTime += appCell.usageTime
                            find.visit += appCell.visit
                        }
                    }
                    usage += usageCellOfWeek.totalUsage
                }
                val usageCell = UsageCell(
                    Calendar.getInstance().apply {
                        timeInMillis = firstMomentInWeek
                    }.get(Calendar.WEEK_OF_YEAR).toString(),
                    usage,
                    usageCellsOfWeek.last().fromDate,
                    usageCellsOfWeek.first().toDate,
                ).apply { type = UsageType.WEEK }
                if (i == 0) {
                    thisWeekUsageCell = usageCell
                    thisWeekAppCells = appCellsWeekly
                } else {
                    val usageCellId = addUsageCell(usageCell)
                    addAppCells(appCellsWeekly.onEach { it.usageCellId = usageCellId })
                }
            }
        }
    }

    private suspend fun calculateMonthlyUsage(usageCellList: List<UsageCell>) {
        val oneDayInMilliseconds = 86_400_000L

        val calender = Calendar.getInstance()
        val daysOfLastMonth = calender.apply { set(Calendar.MONTH, calender.get(Calendar.MONTH)) }
            .getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        loop@ for (i in 0..1) {
            val firstMomentInMonth =
                getTodayFirstMoment() - (oneDayInMilliseconds * (daysOfLastMonth - 1) * i)
            val lastMomentInWeek = firstMomentInMonth + oneDayInMilliseconds * daysOfLastMonth
            val exist = isExist(firstMomentInMonth, lastMomentInWeek, UsageType.MONTH)

            if (!exist) {
                val usageCellsOfMonth = usageCellList.filterIndexed { index, _ ->
                    index in i * daysOfLastMonth - (daysOfLastMonth - dayOfMonth) until i * daysOfLastMonth + dayOfMonth
                }
                if (usageCellsOfMonth.isEmpty())
                    continue@loop
                var usage = 0L
                val appCellsMonthly = ArrayList<AppCell>()
                usageCellsOfMonth.forEach { usageCellOfWeek ->
                    val appCells = getAppCells(usageCellOfWeek.id)
                    appCells.forEach { appCell ->
                        val find =
                            appCellsMonthly.find { it.pack.packageName == appCell.pack.packageName }
                        if (find == null) {
                            appCellsMonthly.add(appCell)
                        } else {
                            find.usageTime += appCell.usageTime
                            find.visit += appCell.visit
                        }
                    }
                    usage += usageCellOfWeek.totalUsage
                }
                usageCellsOfMonth.forEach { usage += it.totalUsage }

                val usageCell = UsageCell(
                    (Calendar.getInstance().apply {
                        timeInMillis = firstMomentInMonth
                    }.get(Calendar.MONTH) + 1).toString(),
                    usage,
                    usageCellsOfMonth.last().fromDate,
                    usageCellsOfMonth.first().toDate,
                ).apply { type = UsageType.MONTH }
                if (i == 0) {
                    thisMonthUsageCell = usageCell
                    thisMonthAppCells = appCellsMonthly
                } else {
                    val usageCellId = addUsageCell(usageCell)
                    addAppCells(appCellsMonthly.onEach { it.usageCellId = usageCellId })
                }
            }
        }
    }
}
