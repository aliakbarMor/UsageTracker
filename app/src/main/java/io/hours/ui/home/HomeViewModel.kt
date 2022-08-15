package io.hours.ui.home


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.hours.model.UsageRepository
import io.hours.model.modules.UsageCell
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(repository: UsageRepository) : ViewModel() {

    val dailyUsage = MutableLiveData<List<UsageCell>>()
    val weeklyUsage = MutableLiveData<List<UsageCell>>()
    val monthlyUsage = MutableLiveData<List<UsageCell>>()

    init {
        viewModelScope.launch {
            dailyUsage.value = repository.getDailyUsageCell()
            weeklyUsage.value = repository.getWeeklyUsageCell()
            monthlyUsage.value = repository.getMonthlyUsageCell()
        }
    }

}