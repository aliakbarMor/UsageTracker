package io.hours.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.hours.model.modules.UsageCell
import io.hours.model.modules.UsageType
import io.hours.utils.Utils
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import java.util.*


enum class HomeScreenTabs {
    Daily, Weekly, Monthly
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun HomeScreen(
    onDayItemsClick: (UsageCell) -> Unit,
    onWeekAndMonthItemsClick: (UsageCell, Boolean) -> Unit,
    onTimelineClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    tabSelected: HomeScreenTabs,
    onTabClick: (HomeScreenTabs) -> Unit,
) {
    val dailyUsage = viewModel.dailyUsage.observeAsState()
    val weeklyUsage = viewModel.weeklyUsage.observeAsState()
    val monthlyUsage = viewModel.monthlyUsage.observeAsState()

    HomeScreenView(
        onDayItemsClick,
        onWeekAndMonthItemsClick,
        onTimelineClick,
        dailyUsage.value.orEmpty(),
        weeklyUsage.value.orEmpty(),
        monthlyUsage.value.orEmpty(),
        tabSelected,
        onTabClick
    )
}

@ExperimentalFoundationApi
@Composable
fun HomeScreenView(
    onDayItemsClick: (UsageCell) -> Unit,
    onWeekAndMonthItemsClick: (UsageCell, Boolean) -> Unit,
    onTimelineClick: () -> Unit,
    dailyUsage: List<UsageCell>,
    weeklyUsage: List<UsageCell>,
    monthlyUsage: List<UsageCell>,
    tabSelected: HomeScreenTabs,
    onTabClick: (HomeScreenTabs) -> Unit,
) {

    CollapsingToolbarScaffold(
        modifier = Modifier,
        scrollStrategy = ScrollStrategy.EnterAlwaysCollapsed,
        state = rememberCollapsingToolbarScaffoldState(),
        toolbar = {
            UsageStateTopAppBar(onTimelineClick)
        }
    ) {
        Column {
            UsageStateTabBar(tabSelected, onTabClick = onTabClick)

            when (tabSelected) {
                HomeScreenTabs.Daily ->
                    DayItems(dailyUsage, onDayItemsClick)
                HomeScreenTabs.Weekly ->
                    WeekItems(weeklyUsage, onWeekAndMonthItemsClick)
                HomeScreenTabs.Monthly ->
                    MonthItems(monthlyUsage, onWeekAndMonthItemsClick)
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun DayItems(dailyUsage: List<UsageCell>, onItemsClick: (UsageCell) -> Unit) {
    val listState = rememberLazyListState()
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    LazyColumn(state = listState) {
        dailyUsage.forEachIndexed { index, dailyUsage ->
            if ((index + (7 - today)) % 7 == 0 || index == 0) {
                stickyHeader {
                    if ((index + (7 - today)) / 7 != 0) ItemHeader("${(index + (7 - today)) / 7} week ago")
                    else ItemHeader("this week")
                }
            }
            item {
                Box(modifier = Modifier.clickable { onItemsClick.invoke(dailyUsage) }) {
                    ListItem(usageCell = dailyUsage)
                }
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun WeekItems(weeklyActivities: List<UsageCell>, onItemsClick: (UsageCell, Boolean) -> Unit) {
    val listState = rememberLazyListState()
    LazyColumn(state = listState) {
        weeklyActivities.forEachIndexed { index, weeklyUsage ->
            if (index % 4 == 0) {
                stickyHeader {
                    ItemHeader("this month")
                }
            }
            item {
                Box(modifier = Modifier.clickable { onItemsClick.invoke(weeklyUsage, true) }) {
                    ListItem(usageCell = weeklyUsage)
                }
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun MonthItems(monthlyUsage: List<UsageCell>, onItemsClick: (UsageCell, Boolean) -> Unit) {
    val listState = rememberLazyListState()
    LazyColumn(state = listState) {
        monthlyUsage.forEachIndexed { index, monthlyUsage ->
            if (index % 12 == 0) {
                stickyHeader {
                    ItemHeader("this year")
                }
            }
            item {
                Box(modifier = Modifier.clickable { onItemsClick.invoke(monthlyUsage, false) }) {
                    ListItem(usageCell = monthlyUsage)
                }
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
            }
        }
    }
}

@Composable
fun ItemHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .fillMaxWidth()
            .padding(8.dp)
    )
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    usageCell: UsageCell,
    textColor: Color = MaterialTheme.colors.onBackground,
) {
    Row(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colors.surface
        ) {
            Text(
                text = usageCell.usageAvatar,
                Modifier
                    .size(40.dp)
                    .padding(vertical = 10.dp)
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.primary
            )
        }
        Column(Modifier.padding(start = 16.dp)) {
            Text(
                text = Utils.getDurationBreakdown(usageCell.totalUsage),
                style = MaterialTheme.typography.body2,
                color = textColor
            )
            Text(
                text = when (usageCell.type) {
                    UsageType.DAY -> Utils.dailyDateFormat(usageCell.fromDate)
                    UsageType.WEEK -> Utils.weeklyDateFormat(usageCell.fromDate, usageCell.toDate)
                    UsageType.MONTH -> Utils.monthlyDateFormat(usageCell.fromDate)
                },
                style = MaterialTheme.typography.overline,
                color = textColor
            )
        }
    }
}

@Composable
fun UsageStateTopAppBar(onTimelineClick: () -> Unit) {
    TopAppBar(
        title = {
            Text("Usage")
        },
        modifier = Modifier,
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = "Icon Account"
                )
            }
            IconButton(onClick = { onTimelineClick.invoke() }) {
                Icon(
                    imageVector = Icons.Rounded.Timeline,
                    contentDescription = "Icon Timeline",
                )
            }
//            IconButton(onClick = {}) {
//                Icon(
//                    imageVector = Icons.Rounded.MoreVert,
//                    contentDescription = "Icon More"
//                )
//            }
        },
        elevation = 0.dp
    )
}

@Composable
fun UsageStateTabBar(
    tabSelected: HomeScreenTabs,
    onTabClick: (HomeScreenTabs) -> Unit,
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
        HomeScreenTabs.values().map { it.name }.forEachIndexed { index, title ->
            val selected = index == tabSelected.ordinal
            Tab(
                selected = selected,
                onClick = { onTabClick(HomeScreenTabs.values()[index]) }
            ) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

