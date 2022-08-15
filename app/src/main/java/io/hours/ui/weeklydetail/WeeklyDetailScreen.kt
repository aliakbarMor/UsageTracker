package io.hours.ui.weeklydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.hours.model.modules.AppCell
import io.hours.model.modules.UsageCell
import io.hours.ui.home.*
import me.onebone.toolbar.*
import io.hours.ui.dailydetail.AppItem


enum class WeeklyDetailTabs {
    Days, Apps
}

@Composable
fun WeeklyDetailScreen(
    viewModel: WeeklyDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    usageCellString: String?,
    usageCellId: Long,
    onDayClick: (UsageCell) -> Unit
) {
    val apps = viewModel.apps.observeAsState()
    val usageCell = viewModel.usageCell.observeAsState()
    val dailyUsageCells = viewModel.dailyUsageCells.observeAsState()

    usageCell.value?.let {
        WeeklyDetailView(
            onBackClick,
            it,
            dailyUsageCells.value.orEmpty(),
            apps.value.orEmpty(),
            onDayClick
        )
    }

}

@Composable
fun WeeklyDetailView(
    onBackClick: () -> Unit,
    usageCell: UsageCell,
    dailUsageCells: List<UsageCell>,
    apps: List<AppCell>,
    onDayClick: (UsageCell) -> Unit
) {
    var tabSelected by remember { mutableStateOf(WeeklyDetailTabs.Days) }
    val collapsingState = rememberCollapsingToolbarScaffoldState()
    CollapsingToolbarScaffold(
        modifier = Modifier,
        scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
        state = collapsingState,
        toolbar = {
            val paddingStart = (36 - 36 * collapsingState.toolbarState.progress).dp

            Box(
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .fillMaxWidth()
                    .height(140.dp)
                    .pin()
            )
            IconButton(onClick = { onBackClick.invoke() }) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    tint = MaterialTheme.colors.onPrimary,
                    contentDescription = "Icon Back",
                )
            }
            ListItem(
                usageCell = usageCell,
                modifier = Modifier
                    .road(Alignment.CenterStart, Alignment.BottomStart)
                    .padding(paddingStart, 16.dp, 16.dp, 16.dp),

                textColor = MaterialTheme.colors.background
            )
        }
    ) {
        Column {
            WeeklyDetailTabBar(tabSelected, onTabSelected = { tabSelected = it })

            when (tabSelected) {
                WeeklyDetailTabs.Days -> {
                    LazyColumn() {
                        items(dailUsageCells) {
                            Box(modifier = Modifier.clickable { onDayClick.invoke(it) }) {
                                ListItem(usageCell = it)
                            }
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                            )
                        }
                    }
                }
                WeeklyDetailTabs.Apps -> {
                    LazyColumn {
                        items(apps) {
                            AppItem(it)
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun WeeklyDetailTabBar(
    tabSelected: WeeklyDetailTabs,
    onTabSelected: (WeeklyDetailTabs) -> Unit
) {
    TabRow(
        selectedTabIndex = tabSelected.ordinal,
        modifier = Modifier
            .height(42.dp),
        divider = {},
        indicator = {
            Box(
                Modifier
                    .tabIndicatorOffset(it[tabSelected.ordinal])
                    .height(2.dp)
                    .padding(horizontal = 32.dp)
                    .background(color = Color.White)
            )
        }
    ) {
        WeeklyDetailTabs.values().map { it.name }.forEachIndexed { index, title ->
            val selected = index == tabSelected.ordinal
            Tab(
                selected = selected,
                onClick = { onTabSelected(WeeklyDetailTabs.values()[index]) }
            ) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
