package com.example.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun App() {
    val getting = remember { Getting() }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Surface {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AndroidX Release Notes") },
                    scrollBehavior = scrollBehavior
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

    var viewState: ViewState by mutableStateOf(ViewState.Loading)

    init {
        viewModelScope.launch { getItems() }
    }

    private suspend fun getItems() {
        viewState = ViewState.Content(Network.getReleaseNotes())
    }
}

internal sealed class ViewState {
    data object Loading : ViewState()
    data class Content(val items: List<ReleaseNotes>) : ViewState()
}
