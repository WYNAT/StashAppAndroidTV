package com.github.damontecres.stashapp.ui.pages

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.github.damontecres.stashapp.StashApplication
import com.github.damontecres.stashapp.api.type.SortDirectionEnum
import com.github.damontecres.stashapp.data.DataType
import com.github.damontecres.stashapp.data.SortAndDirection
import com.github.damontecres.stashapp.data.SortOption
import com.github.damontecres.stashapp.data.StashFindFilter
import com.github.damontecres.stashapp.navigation.FilterAndPosition
import com.github.damontecres.stashapp.navigation.NavigationManager
import com.github.damontecres.stashapp.suppliers.FilterArgs
import com.github.damontecres.stashapp.ui.ComposeUiConfig
import com.github.damontecres.stashapp.ui.cards.StashCard
import com.github.damontecres.stashapp.ui.components.ItemOnClicker
import com.github.damontecres.stashapp.ui.components.LongClicker
import com.github.damontecres.stashapp.ui.components.RowColumn
import com.github.damontecres.stashapp.ui.components.SearchEditTextBox
import com.github.damontecres.stashapp.ui.tryRequestFocus
import com.github.damontecres.stashapp.ui.util.OneTimeLaunchedEffect
import com.github.damontecres.stashapp.ui.util.ifElse
import com.github.damontecres.stashapp.util.FrontPageParser
import com.github.damontecres.stashapp.util.LoggingCoroutineExceptionHandler
import com.github.damontecres.stashapp.util.QueryEngine
import com.github.damontecres.stashapp.util.StashCoroutineExceptionHandler
import com.github.damontecres.stashapp.util.StashServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private lateinit var server: StashServer
    private var currentQuery = ""
    private var searchJob: Job? = null

    val scenes = MutableLiveData<List<Any>>(listOf())
    val groups = MutableLiveData<List<Any>>(listOf())
    val markers = MutableLiveData<List<Any>>(listOf())
    val performers = MutableLiveData<List<Any>>(listOf())
    val studios = MutableLiveData<List<Any>>(listOf())
    val tags = MutableLiveData<List<Any>>(listOf())
    val images = MutableLiveData<List<Any>>(listOf())
    val galleries = MutableLiveData<List<Any>>(listOf())

    val mapping =
        mapOf(
            DataType.SCENE to scenes,
            DataType.GROUP to groups,
            DataType.MARKER to markers,
            DataType.PERFORMER to performers,
            DataType.STUDIO to studios,
            DataType.TAG to tags,
            DataType.IMAGE to images,
            DataType.GALLERY to galleries,
        )

    fun init(
        server: StashServer,
        initialQuery: String,
        perPage: Int,
    ) {
        this.server = server
        if (initialQuery.isNotBlank()) {
            search(initialQuery, perPage)
        }
    }

    fun clear() {
        searchJob?.cancel()
        searchJob = null
        currentQuery = ""
        mapping.values.forEach { it.value = listOf() }
    }

    fun search(
        query: String,
        perPage: Int,
    ) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            clear()
            return
        }
        if (trimmedQuery == this.currentQuery && searchJob?.isActive == true) {
            return
        }
        this.currentQuery = trimmedQuery
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch(
                LoggingCoroutineExceptionHandler(
                    server,
                    viewModelScope,
                    toastMessage = "Search failed",
                ),
            ) {
                val queryEngine = QueryEngine(server)
                DataType.entries.forEach { dataType ->
                    val data = mapping[dataType] ?: return@forEach

                    val stashFindFilter =
                        StashFindFilter(
                            q = trimmedQuery,
                            sortAndDirection =
                                SortAndDirection(
                                    SortOption.sortByName(dataType),
                                    SortDirectionEnum.ASC,
                                ),
                        )
                    val findFilter =
                        stashFindFilter.toFindFilterType(
                            perPage = perPage,
                            page = 1,
                        )

                    launch(
                        LoggingCoroutineExceptionHandler(
                            server,
                            viewModelScope,
                            toastMessage = "Search for ${
                                StashApplication.getApplication().getString(dataType.pluralStringId)
                            } failed",
                        ),
                    ) {
                        val results = queryEngine.find(dataType, findFilter)
                        data.value = results
                    }
                }
            }
    }
}

@Composable
fun SearchPage(
    server: StashServer,
    navigationManager: NavigationManager,
    uiConfig: ComposeUiConfig,
    itemOnClick: ItemOnClicker<Any>,
    longClicker: LongClicker<Any>,
    modifier: Modifier = Modifier,
    initialQuery: String = "",
    viewModel: SearchViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val searchFocusRequester = remember { FocusRequester() }

    var searchQuery by rememberSaveable { mutableStateOf(initialQuery) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val perPage = uiConfig.preferences.searchPreferences.maxResults
    val searchDelay = uiConfig.preferences.searchPreferences.searchDelayMs

    val scenes by viewModel.scenes.observeAsState(listOf())
    val groups by viewModel.groups.observeAsState(listOf())
    val markers by viewModel.markers.observeAsState(listOf())
    val performers by viewModel.performers.observeAsState(listOf())
    val studios by viewModel.studios.observeAsState(listOf())
    val tags by viewModel.tags.observeAsState(listOf())
    val images by viewModel.images.observeAsState(listOf())
    val galleries by viewModel.galleries.observeAsState(listOf())

    val itemLists =
        mapOf(
            DataType.SCENE to scenes,
            DataType.GROUP to groups,
            DataType.MARKER to markers,
            DataType.PERFORMER to performers,
            DataType.STUDIO to studios,
            DataType.TAG to tags,
            DataType.IMAGE to images,
            DataType.GALLERY to galleries,
        )

    OneTimeLaunchedEffect {
        viewModel.init(server, initialQuery, perPage)
    }

    LaunchedEffect(Unit) {
        searchFocusRequester.tryRequestFocus()
    }

    val listState = rememberLazyListState()
    var focusedIndex by rememberSaveable { mutableStateOf(RowColumn(0, 0)) }
    var focusedRow by rememberSaveable { mutableIntStateOf(-1) }

    LazyColumn(
        state = listState,
        modifier =
            modifier
                .focusGroup()
                .focusRestorer(focusRequester),
        contentPadding = PaddingValues(16.dp),
    ) {
        stickyHeader {
            SearchEditTextBox(
                modifier = Modifier.focusRequester(searchFocusRequester),
                value = searchQuery,
                onValueChange = { newQuery ->
                    searchQuery = newQuery
                    searchJob?.cancel()
                    if (newQuery.isBlank()) {
                        viewModel.clear()
                    } else {
                        searchJob =
                            scope.launch(StashCoroutineExceptionHandler()) {
                                delay(searchDelay)
                                viewModel.search(newQuery, perPage)
                            }
                    }
                },
                onSearchClick = {
                    searchJob?.cancel()
                    viewModel.search(searchQuery, perPage)
                },
            )
        }

        DataType.entries.forEachIndexed { index, dataType ->
            val data = itemLists[dataType]!!
            if (data.isNotEmpty()) {
                item {
                    HomePageRow(
                        uiConfig = uiConfig,
                        row =
                            FrontPageParser.FrontPageRow.Success(
                                name = stringResource(dataType.pluralStringId),
                                filter =
                                    FilterArgs(
                                        dataType = dataType,
                                        findFilter =
                                            StashFindFilter(
                                                q = searchQuery,
                                                sortAndDirection =
                                                    SortAndDirection(
                                                        SortOption.sortByName(dataType),
                                                        SortDirectionEnum.ASC,
                                                    ),
                                            ),
                                    ),
                                data = data,
                            ),
                        itemOnClick = itemOnClick,
                        longClicker = longClicker,
                        onFocus = { idx, item ->
                            focusedIndex = RowColumn(index, idx)
//                            focusedItem = item
                            focusedRow = index
                        },
                        rowFocusRequester = if (index == focusedIndex.row) focusRequester else null,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}

@Composable
fun SearchItemsRow(
    title: String,
    items: List<Any>,
    uiConfig: ComposeUiConfig,
    itemOnClick: ItemOnClicker<Any>,
    longClicker: LongClicker<Any>,
    filterArgs: FilterArgs,
    modifier: Modifier = Modifier,
) {
    val firstFocus = remember { FocusRequester() }
    val listState = rememberLazyListState()
    Column(
        modifier = modifier,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyRow(
            modifier =
                Modifier
                    .padding(top = 8.dp)
                    .focusGroup()
                    .focusRestorer(firstFocus),
            state = listState,
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(items) { index, item ->
                StashCard(
                    modifier = Modifier.ifElse(index == 0, Modifier.focusRequester(firstFocus)),
                    uiConfig = uiConfig,
                    item = item,
                    itemOnClick = {
                        itemOnClick.onClick(
                            item,
                            FilterAndPosition(filterArgs, index),
                        )
                    },
                    longClicker = longClicker,
                    getFilterAndPosition = null,
                )
            }
        }
    }
}
