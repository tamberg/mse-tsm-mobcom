package com.example.mysqldataapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mysqldataapp.ui.theme.MySQLDataAppTheme

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

data class Person( // TODO: val
    var id: String,
    var name: String,
    var surname: String,
    var language: String)

class MyViewModel: ViewModel() {
    val list = listOf(
        Person("ag", "Adele", "Goldberg", "Smalltalk"),
        Person("nw", "Niklaus", "Wirth", "Pascal"),
        Person("dr", "Dennis", "Ritchie", "C")
    ).toMutableStateList()

    fun findItemById(id: String): Person? =
        list.find { person -> person.id == id }
}

@Composable
fun MyNavigation(modifier: Modifier = Modifier, viewModel: MyViewModel = viewModel()) {
    // or use https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail
    val activity = LocalActivity.current;
    var id: MutableState<String?> = rememberSaveable { mutableStateOf(null) }
    if (id.value == null) {
        ListScreen(
            viewModel.list,
            onBack = { activity?.finish() },
            onOpen = { it -> id.value = it },
            modifier)
    } else {
        DetailScreen(
            viewModel.findItemById(id.value!!)!!, // TODO?
            onBack = { id.value = null },
            modifier)
    }
}

@Composable
fun ListScreen(
    list: List<Person>,
    onBack: () -> Unit,
    onOpen: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        TopBar(onBack = onBack, modifier = modifier)
        LazyColumn(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = list,
                key = { person -> person.name }
            ) { person ->
                PersonItem(
                    person = person,
                    onClick = { onOpen(person.id) })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListScreenPreview(viewModel: MyViewModel = viewModel()) {
    MySQLDataAppTheme {
        ListScreen(viewModel.list, onBack = {}, onOpen = {})
    }
}

@Composable
fun DetailScreen(
    person: Person,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        TopBar(onBack = onBack, modifier)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1.0f).padding(8.dp).fillMaxWidth()
                //.background(color = Color.Red)
        ) {
            OutlinedTextField(
                value = person.name,
                onValueChange = { person.name = it },
                singleLine = true,
                label = { Text("Name") }
            )
            OutlinedTextField(
                value = person.surname,
                onValueChange = { person.surname = it },
                singleLine = true,
                label = { Text("Surname") }
            )
            OutlinedTextField(
                value = person.language,
                onValueChange = { person.language = it },
                singleLine = true,
                label = { Text("Language") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview(viewModel: MyViewModel = viewModel()) {
    MySQLDataAppTheme {
        DetailScreen(viewModel.findItemById("ag")!!, onBack = {})
    }
}

@Composable
fun PersonItem(person: Person, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = Modifier.clickable(onClick = { onClick() })) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .padding(8.dp).fillMaxWidth()
                .height(IntrinsicSize.Min)
                //.background(color = Color.Red)
        ) {
            Text(text = "${person.name} ${person.surname}",
                modifier.padding(horizontal = 8.dp))//.background(color = Color.Yellow))
            Spacer(modifier = Modifier.weight(1.0f).height(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PersonItemPreview(viewModel: MyViewModel = viewModel()) {
    MySQLDataAppTheme {
        PersonItem(viewModel.findItemById("ag")!!, onClick = {})
    }
}

@Composable
fun TopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier.fillMaxWidth()
    ) {
        Row() {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
