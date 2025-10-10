package com.example.mycsvexportapp

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycsvexportapp.ui.theme.MyCSVExportAppTheme
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCSVExportAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ListScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class Person(
    val id: Int,
    val name: String,
    val surname: String,
    val language: String,
)

class ListViewModel(app: Application): AndroidViewModel(app) {
    val list = listOf(
        Person(0, "Adele", "Goldberg", "Smalltalk"),
        Person(1, "Niklaus", "Wirth", "Pascal"),
        Person(2, "Dennis", "Ritchie", "C")
    ).toMutableList()

    val exportToCsvIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/csv" // mime type
        putExtra(Intent.EXTRA_TITLE, "export.csv")
    }

    var exportToCsvStatus: String? by mutableStateOf(null)

    fun exportToCsv(uri: Uri) {
        val resolver = getApplication<Application>().contentResolver
        try {
            resolver.openOutputStream(uri)?.use { out ->
                // https://www.rfc-editor.org/rfc/rfc4180
                out.bufferedWriter(charset = Charsets.US_ASCII).use { writer ->
                    writer.write("Name, Surname\n")
                    for (person in list) {
                        writer.write("${person.name}, ${person.surname}\n")
                    }
                    exportToCsvStatus = "CSV exported"
                }
            }
        } catch (e: FileNotFoundException) {
            exportToCsvStatus = e.message
        } catch (e: IOException) {
            exportToCsvStatus = e.message
        }
    }

    fun processExportToCsvIntentActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) exportToCsv(uri)
        }
    }
}

@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = viewModel()
) {
    Column(modifier = modifier) {
        TopBar()
        LazyColumn(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = viewModel.list,
                key = { person: Person -> person.id }
            ) { person: Person -> ListItem(person = person) }
        }
        // TODO: move to viewModel?
        var launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            viewModel.processExportToCsvIntentActivityResult(result)
        }
        Button(onClick = {
             launcher.launch(viewModel.exportToCsvIntent)
        }) { Text("Export") }
        if (viewModel.exportToCsvStatus != null) {
            val context = LocalContext.current
            val text = viewModel.exportToCsvStatus
            val duration = Toast.LENGTH_SHORT
            Toast.makeText(context, text, duration).show()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListScreenPreview() {
    MyCSVExportAppTheme {
        ListScreen()
    }
}

@Composable
fun ListItem(person: Person, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Text(text = "${person.name} ${person.surname}",
                modifier.padding(horizontal = 8.dp))
            Spacer(modifier = Modifier
                .weight(1.0f)
                .height(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PersonListItemPreview(
    person: Person = Person(0, "Name", "Surname", "Language")
) {
    MyCSVExportAppTheme {
        ListItem(person)
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row() {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Home, contentDescription = "Home")
            }
            Spacer(modifier = Modifier.weight(1.0f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    MyCSVExportAppTheme {
        TopBar()
    }
}