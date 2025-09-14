// Modified NoteListScreen.kt
// Changes: Renamed 'notes' to 'noteList', reordered functions (moved NoteEditScreen and getRandomColor before NoteListScreen, NoteDetailScreen after)

package simplenote.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.yourorg.simplenote.data.db.DatabaseProvider
import com.yourorg.simplenote.data.db.NoteEntity
import com.yourorg.simplenote.data.guest.GuestDatabaseProvider
import com.yourorg.simplenote.data.guest.GuestNoteEntity
import com.yourorg.simplenote.data.guest.GuestNoteRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import simplenote.AuthRepository
import simplenote.Red40
import simplenote.UserInfo
import java.util.Date
import kotlin.random.Random

fun getRandomColor(): Color {
    val colors = listOf(
        Color(0xFFFFF9C4), // Light yellow
        Color(0xFFFFECB3), // Light amber
        Color(0xFFFFF3E0), // Light orange
        Color(0xFFE1F5FE), // Light blue
        Color(0xFFE8F5E9) ,
        Color(0xFFFFC1CC),
        Color(0xFFE6E6FA),
        Color(0xFF9370DB)// Light green
    )
    return colors[Random.nextInt(colors.size)]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(navController: NavHostController, noteId: Int? = null) {

    val context = LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    val isGuest = remember { authRepo.getAccessToken().isNullOrBlank() && authRepo.getRefreshToken().isNullOrBlank() }
    val dao = remember { DatabaseProvider.get(context).noteDao() }
    val repo = remember { NoteRepository(dao) }
    val guestRepo = remember { GuestNoteRepository(GuestDatabaseProvider.get(context).guestNoteDao()) }

    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val isNew = noteId == null

    LaunchedEffect(noteId) {
        if (!isNew) {
            isLoading = true
            try {
                val n = if (isGuest) guestRepo.retrieveNote(noteId!!) else repo.retrieveNote(noteId!!)
                title = n.title; description = n.description
            } catch (_: Exception) {} finally { isLoading = false }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(if (isNew) "Create Note" else "Edit Note") },
            navigationIcon = { TextButton(onClick = { navController.popBackStack() }) { Text("<", fontSize = 28.sp) } }
        )
    }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("write your note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                minLines = 8,
                maxLines = 20
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        if (isNew) {
                            if (isGuest) guestRepo.createNote(NoteRequest(title, description)) else repo.createNote(NoteRequest(title, description))
                        } else {
                            if (isGuest) guestRepo.updateNote(noteId!!, NoteRequest(title, description)) else repo.updateNote(noteId!!, NoteRequest(title, description))
                        }
                        navController.popBackStack()
                    } catch (_: Exception) {} finally { isLoading = false }
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) { Text(if (isNew) "Create" else "Save") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    val isGuest = remember { authRepo.getAccessToken().isNullOrBlank() && authRepo.getRefreshToken().isNullOrBlank() }

    val dao = remember { DatabaseProvider.get(context).noteDao() }
    val repo = remember { NoteRepository(dao) }
    val guestDao = remember { GuestDatabaseProvider.get(context).guestNoteDao() }
    val guestRepo = remember { GuestNoteRepository(guestDao) }

    var headerUser by remember { mutableStateOf<UserInfo?>(null) }
    LaunchedEffect(isGuest) {
        if (isGuest) {
            try {
                val noteDao = DatabaseProvider.get(context).noteDao()
                noteDao.clear()
                val guestDao = GuestDatabaseProvider.get(context).guestNoteDao()
                val toSync = guestDao.getAll()
                if (toSync.isNotEmpty()) {
                    val remoteRepo = NoteRepository(noteDao)
                    toSync.forEach { g ->
                        remoteRepo.createNote(NoteRequest(g.title, g.description))
                    }
                    guestDao.clear()
                }
            } catch (e: Exception) {
                println("Guest note sync failed: ${e.message}")
            }
        }
        if (!isGuest) {
            try {
                headerUser = authRepo.fetchUserInfo()
            } catch (e: Exception) {
                println("Fetch user info failed: ${e.message}")
            }
        }
    }

    var noteList by remember { mutableStateOf(listOf<Note>()) }
    var currentPage by remember { mutableStateOf(1) }
    var totalCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var isSearchLoading by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf(listOf<Note>()) }
    var searchPage by remember { mutableStateOf(1) }
    var searchTotalCount by remember { mutableStateOf(0) }
    var searchError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val notesFlow: Flow<List<NoteEntity>> = remember(search) {
        if (search.isBlank()) dao.observeAll() else dao.observeByLike("%$search%")
    }
    val notesLocal by notesFlow.collectAsState(initial = emptyList())

    val guestFlow: Flow<List<GuestNoteEntity>> = remember(search) {
        if (search.isBlank()) guestDao.observeAll() else guestDao.observeByLike("%$search%")
    }
    val notesGuest by guestFlow.collectAsState(initial = emptyList())

    fun performSearch() {
        if (search.isBlank()) {
            isSearching = false
            searchResults = emptyList()
            searchPage = 1
            searchTotalCount = 0
            searchError = null
            return
        }
        isSearching = true
        isSearchLoading = true
        searchError = null
        scope.launch {
            try {
                if (!isGuest) {
                    val resp = repo.filterNotes(
                        title = search.trim(),
                        description = search.trim(),
                        page = searchPage,
                        page_size = 40
                    )
                    searchResults = if (searchPage == 1) resp.results else (searchResults + resp.results).distinctBy { it.id }
                    searchTotalCount = resp.count
                }
            } catch (e: HttpException) {
                searchError = when (e.code()) {
                    404 -> "Search endpoint not found. Please contact support."
                    401 -> "Authentication failed. Please log in again."
                    else -> "Search failed: ${e.response()?.errorBody()?.string() ?: e.message()}"
                }
                searchResults = emptyList()
            } catch (e: Exception) {
                searchError = "Search failed: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
                searchResults = emptyList()
            } finally {
                isSearchLoading = false
            }
        }
    }

    LaunchedEffect(search) {
        delay(350)
        performSearch()
    }

    fun loadNotes(page: Int) {
        scope.launch {
            isLoading = true
            try {
                val resp = repo.listNotes(page = page, page_size = 20)
                if (page == 1) noteList = resp.results else noteList = noteList + resp.results
                totalCount = resp.count
                currentPage = page
            } catch (e: Exception) {
                println("Load notes failed: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(isGuest) {
        if (!isGuest) loadNotes(1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                actions = {
                    if (!isGuest) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .clickable { navController.navigate("profile") }
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                val label = (headerUser?.email ?: headerUser?.username ?: "U").firstOrNull()?.uppercase() ?: "U"
                                Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 18.sp)
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("note_edit") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Note")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search") },
                placeholder = { Text("Search notes...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (search.isNotBlank()) IconButton(onClick = { search = "" }) { Icon(Icons.Filled.Close, null) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { performSearch() }),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val displayList = if (isGuest) notesGuest.map { Note(it.id, it.title, it.description, it.createdAt, it.updatedAt) } else if (isSearching) searchResults else noteList
            val localList = if (isGuest) notesGuest else notesLocal

            val gridState = rememberLazyGridState()
            BoxWithConstraints {
                val spanCount = maxOf(2, (maxWidth / 200.dp).toInt())
                LazyVerticalGrid(
                    columns = GridCells.Fixed(spanCount),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayList) { note ->
                        val randomColor = getRandomColor()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clickable { navController.navigate("note_detail/${note.id}") },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = randomColor),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(note.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                Spacer(Modifier.height(4.dp))
                                Text(note.description, style = MaterialTheme.typography.bodySmall, maxLines = 4)
                            }
                        }
                    }

                    if (isLoading || isSearchLoading) {
                        item(span = { GridItemSpan(spanCount) }) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
                        }
                    }

                    if (searchError != null) {
                        item(span = { GridItemSpan(spanCount) }) {
                            Text(searchError!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(navController: NavHostController, noteId: Int) {
    val context = LocalContext.current

    val authRepo = remember { AuthRepository(context) }
    val isGuest = remember { authRepo.getAccessToken().isNullOrBlank() && authRepo.getRefreshToken().isNullOrBlank() }
    val dao = remember { DatabaseProvider.get(context).noteDao() }
    val repo = remember { NoteRepository(dao) }
    val guestRepo = remember { GuestNoteRepository(GuestDatabaseProvider.get(context).guestNoteDao()) }

    var note by remember { mutableStateOf<Note?>(null) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }


    var showDeleteSheet by remember { mutableStateOf(false) }



    LaunchedEffect(noteId, isGuest) {
        scope.launch {
            isLoading = true
            try {
                note = if (isGuest) guestRepo.retrieveNote(noteId) else repo.retrieveNote(noteId)
            } catch (_: Exception) {}
            finally { isLoading = false }
        }
    }


    var titleText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var saveMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(note) {
        note?.let { n ->
            titleText = n.title
            descText = n.description
        }
    }


    Scaffold(
        topBar = {

            TopAppBar(
                title = { Text(note?.title ?: "Loading...") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("<", fontSize = 28.sp) }
                },
                actions = {
                    val dirty = note != null && (titleText != note!!.title || descText != note!!.description)
                    if (dirty) {
                        TextButton(enabled = !saving, onClick = {
                            val current = note ?: return@TextButton
                            saving = true
                            saveMsg = null
                            scope.launch {
                                try {
                                    val updated = if (isGuest)
                                        guestRepo.updateNote(current.id, NoteRequest(titleText, descText))
                                    else
                                        repo.updateNote(current.id, NoteRequest(titleText, descText))
                                    note = updated
                                    saveMsg = "Saved"
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    saveMsg = e.localizedMessage ?: e.message ?: "Save failed"
                                } finally { saving = false }
                            }
                        }) { Text(if (saving) "Saving..." else "Save") }
                    }
                }
            )


        },
        bottomBar = {

            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {

                    Text("Created on ${note?.created ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Text("Last edited on ${note?.updated ?: "-"}", style = MaterialTheme.typography.bodySmall)

                }
                FilledIconButton(
                    onClick = { showDeleteSheet = true },
                    modifier = Modifier
                        .offset(y = (-100).dp)  // Adjusted to move higher
                        .size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Red40,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (note != null) {

                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Title") },
                    placeholder = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("Description") },
                    placeholder = { Text("write your note") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    minLines = 8,
                    maxLines = 20
                )
                saveMsg?.let { Spacer(Modifier.height(8.dp)); Text(it) }

            }
        }

        if (showDeleteSheet) {
            ModalBottomSheet(onDismissRequest = { showDeleteSheet = false }) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Want to Delete this Note?")
                    Spacer(Modifier.height(12.dp))
                    Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            val id = note?.id ?: return@Button
                            scope.launch {
                                if (isGuest) guestRepo.deleteNote(id) else repo.deleteNote(id)
                                showDeleteSheet = false
                                navController.popBackStack()
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Note")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}