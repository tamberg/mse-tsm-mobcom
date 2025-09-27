package com.example.myviewmodelapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myviewmodelapp.ui.theme.MyViewModelAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyViewModelAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greetings(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class Person(val name: String) // TODO: add surname

class MyViewModel : ViewModel() {
    //val recipients = (List(1000) {
    //    i -> Person("Name $i")
    //}).toMutableStateList()
    val recipients = listOf(
        Person("Ada"),
        Person("Grace"),
        Person("Niklaus")).toMutableStateList()
}

@Composable
fun Greetings(
    modifier: Modifier = Modifier,
    myViewModel: MyViewModel = viewModel()
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(
            items = myViewModel.recipients,
            key = { person -> person.name }
        ) { person ->
            Greeting(
                person = person,
                onClose = {}, // TODO: remove person
                modifier = Modifier.height(96.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingsPreview() {
    MyViewModelAppTheme {
        Greetings()
    }
}

@Composable
fun Greeting(
    person: Person = Person("Name"),
    onClose: () -> Unit,
    modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(12.dp).fillMaxWidth()
        ) {
            var expanded = rememberSaveable { mutableStateOf(false) }
            Text(
                text = "Hello ${person.name}!", // TODO: surname if expanded
                modifier = Modifier.weight(3.0f)
            )
            ElevatedButton(
                onClick = { expanded.value = !expanded.value },
                modifier = Modifier.weight(1.0f)
            ) {
                Text(
                    text = if (expanded.value) "Less" else "More",
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyViewModelAppTheme {
        Greeting(onClose = {})
    }
}