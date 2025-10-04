// (c) 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
// licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
// based on https://github.com/google-developer-training/basic-android-kotlin-compose-training-inventory-app

package com.example.mysqldataapp

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.mysqldataapp.ui.theme.MySQLDataAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.assert

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MySQLDataAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// # App Container

interface AppContainer {
    val personRepository: PersonRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val personRepository: PersonRepository by lazy {
        OfflinePersonRepository(PersonDatabase.getDatabase(context).personDao())
    }
}

class MySQLDataApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

// # Data Layer

// ## Entity

// https://stackoverflow.com/questions/338156/table-naming-dilemma-singular-vs-plural-names
@Entity(tableName = "person")
data class PersonEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "surname") val surname: String?,
    @ColumnInfo(name = "language") val language: String?
)

// ## DAO

@Dao
interface PersonDao { // see PersonDao_Impl.kt (generated)
    @Query("SELECT * from person WHERE id = :id") fun getPerson(id: Int): Flow<PersonEntity>
    @Query("SELECT * from person ORDER BY name ASC") fun getPersonList(): Flow<List<PersonEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(person: PersonEntity)
    @Update suspend fun update(person: PersonEntity)
    @Delete suspend fun delete(person: PersonEntity)
}

// ## DB

@Database(entities = [PersonEntity::class], version = 1, exportSchema = false)
abstract class PersonDatabase : RoomDatabase() { // see PersonDatabase_Impl.kt (generated)
    abstract fun personDao(): PersonDao
    companion object {
        @Volatile
        private var Instance: PersonDatabase? = null

        fun getDatabase(context: Context): PersonDatabase {
            return Instance ?: synchronized(this) { // singleton pattern
                Room.databaseBuilder(context, PersonDatabase::class.java, "person_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

// # Domain Layer
// ## Repository

interface PersonRepository {
    fun getPersonListFlow(): Flow<List<PersonEntity>>
    fun getPersonFlow(id: Int): Flow<PersonEntity?>
    suspend fun insertPerson(person: PersonEntity)
    suspend fun updatePerson(person: PersonEntity)
    suspend fun deletePerson(person: PersonEntity)
}

class OfflinePersonRepository(private val personDao: PersonDao) : PersonRepository {
    override fun getPersonListFlow(): Flow<List<PersonEntity>> = personDao.getPersonList()
    override fun getPersonFlow(id: Int): Flow<PersonEntity?> = personDao.getPerson(id)
    override suspend fun insertPerson(person: PersonEntity) = personDao.insert(person)
    override suspend fun updatePerson(person: PersonEntity) = personDao.update(person)
    override suspend fun deletePerson(person: PersonEntity) = personDao.delete(person)
}

// # UI State Layer
// ## ViewModelProvider

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { NavigationViewModel(this.createSavedStateHandle()) }
        initializer { PersonListViewModel(mySqlDataApp().container.personRepository) }
        initializer { PersonEntryViewModel(mySqlDataApp().container.personRepository) }
        initializer { PersonDetailViewModel(this.createSavedStateHandle(),
            mySqlDataApp().container.personRepository) }
        initializer { PersonEditViewModel(this.createSavedStateHandle(),
            mySqlDataApp().container.personRepository) }
    }
}

fun CreationExtras.mySqlDataApp(): MySQLDataApp =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MySQLDataApp)

/*
class MyViewModel: ViewModel() {
    val list = listOf(
        PersonState("ag", "Adele", "Goldberg", "Smalltalk"),
        PersonState("nw", "Niklaus", "Wirth", "Pascal"),
        PersonState("dr", "Dennis", "Ritchie", "C")
    ).toMutableStateList()

    fun findPersonById(id: String): PersonState? =
        list.find { person -> person.id == id }
}
*/

// ## State

data class Person(
    val id: Int = 0,
    val name: String = "",
    val surname: String = "",
    val language: String = "",
)

fun Person.toEntity() = PersonEntity(
    // if needed, convert from UI to DB types
    id = id,
    name = name,
    surname = surname,
    language = language)

fun PersonEntity.toPerson() = Person(
    id = id,
    name = name!!, // TODO null safety
    surname = surname!!,
    language = language!!)

// ## Navigation ViewModel

class NavigationViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    fun setPersonId(personId: Int) {
        savedStateHandle["personId"] = personId
    }
}

// ## List ViewModel

class PersonListViewModel(personRepository: PersonRepository) : ViewModel() {
    val state: StateFlow<PersonListState> =
        personRepository.getPersonListFlow() // single, ongoing call to the DB
            .map { PersonListState(it) } // it = personList updates on change
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = PersonListState()
            )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class PersonListState(val personList: List<PersonEntity> = listOf()) // TODO: List<PersonState>?

// ## Entry ViewModel

class PersonEntryViewModel(private val personRepository: PersonRepository) : ViewModel() {
    var state by mutableStateOf(PersonEntryState())
        private set

    fun updateState(person: Person) {
        state = PersonEntryState(person = person, isValid = validateInput(person))
    }

    suspend fun createPerson() {
        if (validateInput(state.person)) {
            personRepository.insertPerson(state.person.toEntity())
        }
    }

    private fun validateInput(person: Person): Boolean {
        return with(person) {
            name.isNotBlank() && surname.isNotBlank() && language.isNotBlank()
        }
    }
}

data class PersonEntryState(val person: Person = Person(), val isValid: Boolean = false)

// ## Detail ViewModel

class PersonDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val personRepository: PersonRepository,
) : ViewModel() {
    private val personId: Int = checkNotNull(savedStateHandle["personId"])
    val state: StateFlow<PersonDetailState> =
        personRepository.getPersonFlow(personId)
            .filterNotNull()
            .map {
                it.toDetailState()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = PersonDetailState()
            )

//    suspend fun deletePerson() {
//        personRepository.deletePerson(
//            state.person.toEntity())
//    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class PersonDetailState(val person: Person = Person())

fun PersonEntity.toDetailState(): PersonDetailState = PersonDetailState(person = this.toPerson())

// ## Edit ViewModel

class PersonEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val personRepository: PersonRepository
) : ViewModel() {
    var state by mutableStateOf(PersonEditState())
        private set

    private val personId: Int = checkNotNull(savedStateHandle["personId"])

    init {
        viewModelScope.launch {
            state = personRepository.getPersonFlow(personId)
                .filterNotNull()
                .first()
                .toEditState()
        }
    }

    fun updateState(person: Person) {
        state = PersonEditState(person = person, isValid = validateInput(person))
    }

    suspend fun updatePerson() {
        if (validateInput(state.person)) {
            personRepository.updatePerson(state.person.toEntity())
        }
    }

    private fun validateInput(person: Person = state.person): Boolean {
        return with(person) {
            name.isNotBlank() && surname.isNotBlank() && language.isNotBlank()
        }
    }
}

data class PersonEditState(val person: Person = Person(), val isValid: Boolean = false)

fun PersonEntity.toEditState(): PersonEditState = PersonEditState(person = this.toPerson())

// # UI Components Layer

// ## Navigation

enum class Screen { LIST, ENTRY, DETAIL, EDIT }

@Composable
fun MyNavigation(
    viewModel: NavigationViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    // or https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail
    //var personId by rememberSaveable { mutableStateOf(null) }
    var screen by rememberSaveable { mutableStateOf(Screen.LIST) }
    when (screen) {
        Screen.LIST -> ListScreen(
            onAdd = { screen = Screen.ENTRY },
            onOpen = { it -> viewModel.setPersonId(it); screen = Screen.DETAIL },
            modifier)
        Screen.ENTRY -> EntryScreen(
            onBack = { screen = Screen.LIST },
            //onCreated = { personId = it; screen = ... }
            modifier)
        Screen.DETAIL -> DetailScreen(
            onBack = { screen = Screen.LIST },
            onEdit = { screen = Screen.EDIT },
            modifier)
        Screen.EDIT -> EditScreen(
            onBack = { screen = Screen.DETAIL },
            onSave = { screen = Screen.LIST },
            modifier)
    }
}

// ## List

@Composable
fun ListScreen(
    onAdd: () -> Unit,
    onOpen: (personId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsState()
    Column(modifier = modifier) {
        TopBar()
        LazyColumn(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = state.personList,
                key = { personEntity -> personEntity.id } // TODO: move entity down
            ) { personEntity ->
                PersonListItem(
                    person = personEntity.toPerson(),
                    onClick = { onOpen(personEntity.id) })
            }
        }
        Button(onClick = onAdd) { Text("Add") }
    }
}

@Preview(showBackground = true)
@Composable
fun ListScreenPreview() {
    MySQLDataAppTheme {
        ListScreen(onAdd = {}, onOpen = {})
    }
}

@Composable
fun PersonListItem(person: Person, onClick: (Person) -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = Modifier.clickable(onClick = { onClick(person) })) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .padding(8.dp).fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Text(text = "${person.name} ${person.surname}",
                modifier.padding(horizontal = 8.dp))
            Spacer(modifier = Modifier.weight(1.0f).height(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PersonListItemPreview(
    person: Person = Person(0, "Name", "Surname", "Language")
) {
    MySQLDataAppTheme {
        PersonListItem(person, onClick = {})
    }
}

// ## Enter

@Composable
fun EntryScreen( // TODO: reuse EditScreen?
    //personId: Int,
    onBack: () -> Unit,
    //onCreate: (personId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val state = viewModel.state //.collectAsState()
    val person = state.person
    Column(modifier = modifier) {
        TopBar(onBack = onBack)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1.0f).padding(8.dp).fillMaxWidth()
        ) {
            OutlinedTextField(
                value = person.name,
                onValueChange = { viewModel.updateState(person.copy(name = it)) },
                singleLine = true,
                label = { Text("Name") }
            )
            OutlinedTextField(
                value = person.surname,
                onValueChange = { viewModel.updateState(person.copy(surname = it)) },
                singleLine = true,
                label = { Text("Surname") }
            )
            OutlinedTextField(
                value = person.language,
                onValueChange = { viewModel.updateState(person.copy(language = it)) },
                singleLine = true,
                label = { Text("Language") }
            )
            Button(enabled = state.isValid, onClick = {
                coroutineScope.launch {
                    viewModel.createPerson()
                    onBack() // TODO: onCreated, to show spinner, detail once created?
                }
            }) {
                Text(text = "Create")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EntryScreenPreview() {
    MySQLDataAppTheme {
        EntryScreen(onBack = {}) //, onCreate = {})
    }
}

// ## Detail

@Composable
fun DetailScreen(
    //personId: Int,
    onBack: () -> Unit,
    onEdit: (personId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsState()
    val person = state.person
    Column(modifier = modifier) {
        TopBar(onBack = onBack)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1.0f).padding(8.dp).fillMaxWidth()
        ) {
            OutlinedTextField(value = person.name, onValueChange = {}, readOnly = true)
            OutlinedTextField(value = person.surname, onValueChange = {}, readOnly = true)
            OutlinedTextField(value = person.language, onValueChange = {}, readOnly = true)
            Button(onClick = { onEdit(person.id) }) {
                Text(text = "Edit")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    MySQLDataAppTheme {
        DetailScreen(onBack = {}, onEdit = {})
    }
}

// ## Edit

@Composable
fun EditScreen(
    //personId: Int,
    onBack: () -> Unit,
    onSave: (personId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.state //.collectAsState()
    val person = state.person
    Column(modifier = modifier) {
        TopBar(onBack = onBack)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1.0f).padding(8.dp).fillMaxWidth()
        ) {
            OutlinedTextField(
                value = person.name,
                onValueChange = { viewModel.updateState(person.copy(name = it)) },
                singleLine = true,
                label = { Text("Name") }
            )
            OutlinedTextField(
                value = person.surname,
                onValueChange = { viewModel.updateState(person.copy(surname = it)) },
                singleLine = true,
                label = { Text("Surname") }
            )
            OutlinedTextField(
                value = person.language,
                onValueChange = { viewModel.updateState(person.copy(language = it)) },
                singleLine = true,
                label = { Text("Language") }
            )
            Button(onClick = { onSave(person.id) }) {
                Text(text = "Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditScreenPreview() {
    MySQLDataAppTheme {
        EditScreen(onBack = {}, onSave = {})
    }
}

// ## Components

@Composable
fun TopBar(onBack: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    // or https://developer.android.com/develop/ui/compose/quick-guides/content/create-scaffold
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row() {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Home, contentDescription = "Home")
                }
            }
            Spacer(modifier = Modifier.weight(1.0f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    MySQLDataAppTheme {
        TopBar(onBack = {})
    }
}