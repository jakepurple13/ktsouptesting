package com.example.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun App() {
    val getting = remember { Getting() }

    val lastUpdate by getting.lastUpdate.collectAsState("")

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Surface {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AndroidX Release Notes") },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                BottomAppBar(
                    actions = { Text("Last updated: $lastUpdate") },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = getting::getItems
                        ) { Icon(Icons.Default.Refresh, null) }
                    }
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { padding ->
            when (val viewState = getting.viewState) {
                is ViewState.Content -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        viewState.items.forEach {
                            stickyHeader {
                                TopAppBar(
                                    title = { Text(it.date) },
                                    scrollBehavior = scrollBehavior,
                                    windowInsets = WindowInsets(0)
                                )
                            }

                            item { ReleaseNoteItem(item = it) }
                        }
                    }
                }

                ViewState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReleaseNoteItem(item: ReleaseNotes) {
    val uriHandler = LocalUriHandler.current
    ElevatedCard(
        onClick = { uriHandler.openUri(item.link) },
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = {
                item.links.forEach {
                    Text(it.name)
                }
            },
        )
    }
}

internal class Getting {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val lastUpdate = AndroidXDataStore.lastUpdate
        .map { getFormattedDate(Instant.fromEpochMilliseconds(it)) }

    var viewState: ViewState by mutableStateOf(ViewState.Loading)

    init {
        getItems()
    }

    fun getItems() {
        viewState = ViewState.Loading
        viewModelScope.launch { viewState = ViewState.Content(Network.getReleaseNotes()) }
    }
}

internal sealed class ViewState {
    data object Loading : ViewState()
    data class Content(val items: List<ReleaseNotes>) : ViewState()
}

internal fun getFormattedDate(
    iso8601Timestamp: Instant,
): String {
    val localDateTime = iso8601TimestampToLocalDateTime(iso8601Timestamp)
    val date = localDateTime.date
    val day = date.dayOfMonth
    val month = date.monthNumber
    val year = date.year
    val dateTime = "${month.zeroPrefixed(2)}/${day.zeroPrefixed(2)}/${year}"
    val time = localDateTime.time
    val hour = time.hour
    val minute = time.minute
    val timeDate = "$hour:$minute"
    return "$timeDate - $dateTime"
}

private fun Int.zeroPrefixed(
    maxLength: Int,
): String {
    if (this < 0 || maxLength < 1) return ""

    val string = this.toString()
    val currentStringLength = string.length
    return if (maxLength <= currentStringLength) {
        string
    } else {
        val diff = maxLength - currentStringLength
        var prefixedZeros = ""
        repeat(diff) {
            prefixedZeros += "0"
        }
        "$prefixedZeros$string"
    }
}

private fun iso8601TimestampToLocalDateTime(timestamp: Instant): LocalDateTime {
    return timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
}