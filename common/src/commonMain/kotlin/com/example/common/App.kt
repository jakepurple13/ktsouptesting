package com.example.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ktsoup.KtSoupParser
import ktsoup.parseRemote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun App() {
    val getting = remember { Getting() }

    Surface {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("KtSoup Testing") }
                )
            }
        ) { padding ->
            when (val viewState = getting.viewState) {
                is ViewState.Content -> {
                    LazyColumn(
                        contentPadding = padding,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(viewState.items) {
                            OutlinedCard {
                                ListItem(
                                    headlineContent = { Text(it.title) },
                                    supportingContent = { Text(it.description) },
                                    overlineContent = { Text(it.url) }
                                )
                            }
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

internal class Getting {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var viewState: ViewState by mutableStateOf(ViewState.Loading)

    private val baseUrl = "https://www.nineanime.com"

    init {
        viewModelScope.launch { getItems() }
    }

    private suspend fun getItems() {
        viewState = KtSoupParser.parseRemote("$baseUrl/category/index_1.html?sort=updated")
            .use { doc ->
                doc
                    .querySelectorAll("div.post")
                    .mapNotNull {
                        runCatching {
                            ItemModel(
                                title = it.querySelector("p.title a")!!.textContent(),
                                description = "",
                                url = it.querySelector("p.title a")!!.attr("href")!!,
                                imageUrl = it.querySelector("img")!!.attr("src")!!,
                            )
                        }
                            .onFailure { t ->
                                println(it.html())
                                t.printStackTrace()
                            }
                            .getOrNull()
                    }
                    .filter { it.title.isNotEmpty() }
            }
            .let(ViewState::Content)
    }
}

internal data class ItemModel(
    val title: String,
    val description: String,
    val url: String,
    val imageUrl: String,
)

internal sealed class ViewState {
    data object Loading : ViewState()
    data class Content(val items: List<ItemModel>) : ViewState()
}