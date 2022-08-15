package io.hours.ui.weeklydetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.hours.model.UsageRepository
import io.hours.model.modules.AppCell
import io.hours.model.modules.UsageCell
import io.hours.model.modules.UsageType
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class WeeklyDetailViewModel @Inject constructor(
    state: SavedStateHandle,
    repository: UsageRepository
) : ViewModel() {

    val apps = MutableLiveData<List<AppCell>>()
    val usageCell = MutableLiveData<UsageCell>()
    val dailyUsageCells = MutableLiveData<List<UsageCell>>()

    init {
        val usageCellId = state.get<Long>("usageCellId")!!
        viewModelScope.launch {
            if (usageCellId != 0L) {
                usageCell.value = repository.getUsageCellWithId(usageCellId)
                dailyUsageCells.value = repository.getUsageCellWithDate(
                    usageCell.value!!.fromDate, usageCell.value!!.toDate
                )
                apps.value = repository.getAppCells(usageCellId)
            } else {
                val obj = JSONObject(state.get<String>("usageCell")!!)
                val isWeekly = obj.getBoolean("isWeekly")

                usageCell.value = UsageCell(
                    obj.getString("usageAvatar"),
                    obj.getLong("totalUsage"),
                    obj.getString("fromDate").toLong(),
                    obj.getString("toDate").toLong(),
                )
                dailyUsageCells.value = repository.getUsageCellWithDate(
                    usageCell.value!!.fromDate, usageCell.value!!.toDate
                )

                if (isWeekly) {
                    usageCell.value?.type = UsageType.WEEK
                    apps.value = repository.thisWeekAppCells
                    (dailyUsageCells.value as ArrayList).add(0, repository.todayUsageCell!!)
                } else {
                    usageCell.value?.type = UsageType.MONTH
                    apps.value = repository.thisMonthAppCells
                }

            }
        }
    }

}