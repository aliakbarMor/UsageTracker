package io.hours.ui.dailydetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scaleMatrix
import androidx.hilt.navigation.compose.hiltViewModel
import io.hours.model.modules.AppCell
import io.hours.model.modules.UsageCell
import io.hours.utils.Utils
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@Composable
fun DailyDetailScreen(
    viewModel: DailyDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    usageCellString: String,
    usageCellId: Long
) {
    val apps = viewModel.apps.observeAsState()
    val usageCell = viewModel.usageCell.observeAsState()

    usageCell.value?.let { DailyDetailView(apps.value, onBackClick, it) }
}

@Composable
fun DailyDetailView(apps: List<AppCell>?, onBackClick: () -> Unit, usageCell: UsageCell) {
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

            io.hours.ui.home.ListItem(
                usageCell = usageCell,
                modifier = Modifier
                    .road(Alignment.CenterStart, Alignment.BottomStart)
                    .padding(paddingStart, 16.dp, 16.dp, 16.dp),
                textColor = MaterialTheme.colors.background
            )

        }
    ) {
        AppItems(apps.orEmpty())
    }
}

@Composable
fun AppItems(apps: List<AppCell>) {
    val listState = rememberLazyListState()
    LazyColumn(state = listState) {
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

@Composable
fun AppItem(appCell: AppCell) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            bitmap = appCell.pack.icon.toBitmap().asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(40.dp)
        )
        Column(Modifier.padding(start = 16.dp)) {
            Text(
                text = appCell.pack.appName,
                style = MaterialTheme.typography.body2
            )
            Text(
                text = "${Utils.getDurationBreakdown(appCell.usageTime)}, ${appCell.visit} time visit",
                style = MaterialTheme.typography.overline
            )
        }
    }
}