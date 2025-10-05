// (c) 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
// licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
// based on https://github.com/google-developer-training/basic-android-kotlin-compose-training-inventory-app

package com.example.mysqldataapp.ui.parts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mysqldataapp.ui.theme.MySQLDataAppTheme
import kotlinx.coroutines.launch

import com.example.mysqldataapp.AppViewModelProvider
import com.example.mysqldataapp.data.source.PersonEntity // TODO: refactor?
import com.example.mysqldataapp.ui.state.Person
import com.example.mysqldataapp.ui.state.PersonDetailViewModel
import com.example.mysqldataapp.ui.state.PersonEditViewModel
import com.example.mysqldataapp.ui.state.PersonEntryViewModel
import com.example.mysqldataapp.ui.state.PersonListViewModel
import com.example.mysqldataapp.ui.state.toPerson

// Navigation

enum class Screen { LIST, ENTRY, DETAIL, EDIT }

@Composable
fun MyNavigation(
    //viewModel: NavigationViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    // or https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail
    //var personId by rememberSaveable { mutableStateOf(null) }
    var screen by rememberSaveable { mutableStateOf(Screen.LIST) }
    when (screen) {
        Screen.LIST -> ListScreen(
            onAdd = { screen = Screen.ENTRY },
            onOpen = { it -> AppViewModelProvider.personId = it; screen = Screen.DETAIL }, // TODO
            modifier)
        Screen.ENTRY -> EntryScreen(
            onBack = { screen = Screen.LIST },
            //onCreated = { personId = it; screen = ... }
            modifier)
        Screen.DETAIL -> DetailScreen(
            //personId,
            onBack = { screen = Screen.LIST },
            onEdit = { screen = Screen.EDIT },
            modifier)
        Screen.EDIT -> EditScreen(
            //personId,
            onBack = { screen = Screen.DETAIL },
            onSave = { screen = Screen.LIST },
            modifier)
    }
}

// List

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
            ) { personEntity: PersonEntity ->
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

// Entry

@Composable
fun EntryScreen( // TODO: reuse EditScreen?
    //personId: Int,
    onBack: () -> Unit,
    //onCreate: (personId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val state = viewModel.state
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

// Detail

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

// Edit

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

// Components

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