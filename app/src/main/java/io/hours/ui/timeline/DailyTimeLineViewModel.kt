package io.hours.ui.timeline

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.hours.model.UsageRepository
import io.hours.model.modules.Package
import io.hours.model.modules.TimeLineUsageCell
import io.hours.model.modules.TimelineCell
import io.hours.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class DailyTimeLineViewModel @Inject constructor(
    usageRepository: UsageRepository
) : ViewModel() {

    val timeLineUsageCells = MutableLiveData<List<TimeLineUsageCell>>()
    val timelineCells = MutableLiveData<ArrayList<TimelineCell>>()

    init {
        viewModelScope.launch {
            timeLineUsageCells.value = usageRepository.getTimeline2day()
            withContext(Dispatchers.IO) {
                getTimelineCells()
            }
        }
    }

    private fun getTimelineCells() {
        val calender = Calendar.getInstance()
        var hourTimeStamp = calender.timeInMillis - calender.get(Calendar.MINUTE) * 60_000
        val oneHourMilliseconds = 3600000
        val timelineCellsArray = ArrayList<TimelineCell>()

        while (timeLineUsageCells.value?.last()!!.launchTime <= hourTimeStamp) {
            hourTimeStamp -= oneHourMilliseconds
            var totalUsage = 0L
            val listPackage = ArrayList<Package>()
            val apps = ArrayList<TimeLineUsageCell>()
            timeLineUsageCells.value
                ?.filter { timeLineUsageCell ->
                    timeLineUsageCell.exitTime >= hourTimeStamp && timeLineUsageCell.launchTime <= (hourTimeStamp + oneHourMilliseconds)
                }?.onEach { timeLineUsageCell ->
                    if (timeLineUsageCell.launchTime <= hourTimeStamp) {
                        timeLineUsageCell.launchTime = hourTimeStamp
                        timeLineUsageCell.duration =
                            timeLineUsageCell.exitTime - timeLineUsageCell.launchTime
                    }
                    if (timeLineUsageCell.exitTime >= (hourTimeStamp + oneHourMilliseconds)) {
                        timeLineUsageCell.launchTime = (hourTimeStamp + oneHourMilliseconds)
                        timeLineUsageCell.duration =
                            timeLineUsageCell.exitTime - timeLineUsageCell.launchTime
                    }
                    totalUsage += timeLineUsageCell.duration

                    if (listPackage.find { it.packageName == timeLineUsageCell.pack.packageName } == null)
                        listPackage.add(timeLineUsageCell.pack)
                    apps.add(timeLineUsageCell)
                }

            timelineCellsArray.add(
                TimelineCell(Utils.getHour(hourTimeStamp), totalUsage, listPackage, apps)
            )

        }
        timelineCells.postValue(timelineCellsArray)
    }

}
