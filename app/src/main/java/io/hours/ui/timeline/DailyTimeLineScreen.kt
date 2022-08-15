package io.hours.ui.timeline

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.TagFaces
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import io.hours.model.modules.TimeLineUsageCell
import io.hours.model.modules.TimelineCell
import io.hours.utils.Utils

@Composable
fun DailyTimeLineScreen(
    viewModel: DailyTimeLineViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val timeLineUsageCells = viewModel.timeLineUsageCells.observeAsState()
    val timeLineCells = viewModel.timelineCells.observeAsState()
    timeLineCells.value.orEmpty().forEach {
        Log.d("BBBBBBBBB", it.hour)
        Log.d("BBBBBBBBB", Utils.getDurationBreakdown(it.usage))
    }
    DailyTimeLineView(timeLineUsageCells.value, onBackClick, timeLineCells.value.orEmpty())
}

@Composable
fun DailyTimeLineView(
    timeLineUsageCells: List<TimeLineUsageCell>?,
    onBackClick: () -> Unit,
    timeLineCells: List<TimelineCell>
) {
    var expandedTopic by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = { DailyTimeLineTopBar(onBackClick) },
    ) {

        LazyColumn(
            contentPadding = PaddingValues(top = 4.dp)
        ) {

            itemsIndexed(timeLineCells) { index, timelineCell ->
                TimelineRow(
                    timeLineCell = timelineCell,
                    expanded = expandedTopic == index,
                    onClick = {
                        expandedTopic = if (expandedTopic == index) -1 else index
                    })
            }
        }

    }
}

@Composable
fun TimelineRow(timeLineCell: TimelineCell, expanded: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .clickable { if (timeLineCell.apps.isNotEmpty()) onClick.invoke() }
            .animateContentSize(),
    ) {
        if (!expanded) {
            Divider(
                modifier = Modifier.size(width = 2.dp, height = 26.dp),
                color = MaterialTheme.colors.primary
            )
            Surface(
                color = MaterialTheme.colors.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = timeLineCell.hour,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.wrapContentWidth(Alignment.Start)
                )
                Row(Modifier.wrapContentWidth(Alignment.CenterHorizontally)) {
                    if (timeLineCell.pack.isEmpty())
                        Image(
                            colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Rounded.TagFaces,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                        )
                    else
                        for (i in 0..4)
                            if (i < timeLineCell.pack.size)
                                Image(
                                    modifier = Modifier.size(24.dp),
                                    bitmap = timeLineCell.pack[i].icon.toBitmap().asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                )
                }
                Text(
                    text = if (timeLineCell.usage != 0L) Utils.getDurationBreakdown(timeLineCell.usage)
                    else "Happy Hour",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.wrapContentWidth(Alignment.End)
                )
            }
            Divider(
                modifier = Modifier.size(width = 2.dp, height = 26.dp),
                color = MaterialTheme.colors.primary
            )

        } else
            Column {
                timeLineCell.apps.forEach {
                    ItemTimeLine(timeLineUsageCell = it)
                }
            }
    }

}

@Composable
fun ItemTimeLine(timeLineUsageCell: TimeLineUsageCell) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Divider(
                modifier = Modifier.size(width = 1.dp, height = 30.dp),
                color = MaterialTheme.colors.primary
            )
            Text(
                modifier = Modifier.padding(vertical = 4.dp),
                text = Utils.formatDateHour(timeLineUsageCell.launchTime),
                style = TextStyle(fontSize = 10.sp, color = MaterialTheme.colors.primary)
            )
            Divider(
                modifier = Modifier.size(width = 1.dp, height = 30.dp),
                color = MaterialTheme.colors.primary
            )
        }
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .padding(end = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colors.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    modifier = Modifier.size(40.dp),
                    bitmap = timeLineUsageCell.pack.icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                )
                Column(Modifier.padding(start = 16.dp)) {
                    Text(
                        text = timeLineUsageCell.pack.appName,
                        style = MaterialTheme.typography.body2
                    )
                    Text(
                        text = "${Utils.formatDateHour(timeLineUsageCell.launchTime)} - ${
                            Utils.formatDateHour(timeLineUsageCell.exitTime)
                        } (${
                            Utils.getDurationBreakdown(timeLineUsageCell.duration)
                        }) ",
                        style = MaterialTheme.typography.overline
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTimeLineTopBar(onBackClick: () -> Unit) {
    TopAppBar {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.primary)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBackClick.invoke() }) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Icon Back"
                )
            }
//            Icon(
//                imageVector = Icons.Rounded.ArrowBack,
//                contentDescription = null,
//                modifier = Modifier
//                    .clickable {  }
//                    .padding(start = 16.dp)
//                    .size(size = 24.dp)
//            )
            Text(
                text = "Daily Timeline",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

}
