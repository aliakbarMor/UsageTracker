package io.hours.ui.dailydetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.hours.model.UsageRepository
import io.hours.model.modules.AppCell
import io.hours.model.modules.UsageCell
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class DailyDetailViewModel @Inject constructor(
    state: SavedStateHandle,
    private val repository: UsageRepository
) : ViewModel() {

    val apps = MutableLiveData<List<AppCell>>()
    val usageCell = MutableLiveData<UsageCell>()

    init {
        val usageCellId = state.get<Long>("usageCellId")!!
        viewModelScope.launch {
            if (usageCellId != 0L) {
                usageCell.value = repository.getUsageCellWithId(usageCellId)
                apps.value = repository.getAppCells(usageCellId)
            } else {
                val obj = JSONObject(state.get<String>("usageCell")!!)
                usageCell.value = UsageCell(
                    obj.getString("usageAvatar"),
                    obj.getLong("totalUsage"),
                    obj.getString("fromDate").toLong(),
                    obj.getString("toDate").toLong(),
                )
                apps.value = repository.todayAppCells

            }
        }
    }
}