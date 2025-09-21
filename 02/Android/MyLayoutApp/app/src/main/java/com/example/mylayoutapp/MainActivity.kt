package com.example.mylayoutapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mylayoutapp.ui.theme.MyLayoutAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyLayoutAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greetings(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Greetings(
    names: List<String> = listOf("Ada", "Grace", "Niklaus"),
    modifier: Modifier = Modifier
) {
    Column() { // TODO: try Row or Box instead
        for (name in names) { // this is fine
            // TODO: try modifier variants
            //Greeting(name, Modifier.fillMaxWidth())
            //Greeting(name, Modifier.fillMaxHeight())
            //Greeting(name, modifier)
            Greeting(name)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingsPreview() {
    MyLayoutAppTheme {
        Greetings()
    }
}

@Composable
fun ColumnGreetings(
    names: List<String> = listOf("Ada", "Grace", "Niklaus"),
    modifier: Modifier = Modifier
) {
    Column(
        // TODO: try arrangement and alignment variants
        verticalArrangement = Arrangement.spacedBy(8.dp), // or Top, Center, Bottom, etc.
        horizontalAlignment = Alignment.CenterHorizontally, // or Start, End
        modifier = modifier.background(Color.Yellow).padding(8.dp).fillMaxSize()
    ) {
        for (name in names) {
            Greeting(name, Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ColumnGreetingsPreview() {
    MyLayoutAppTheme {
        ColumnGreetings()
    }
}

@Composable
fun RowGreetings(
    names: List<String> = listOf("Ada", "Grace", "Niklaus"),
    modifier: Modifier = Modifier
) {
    Row(
        // TODO: try arrangement and alignment variants
        horizontalArrangement = Arrangement.spacedBy(8.dp), // or Start, Center, End, etc.
        verticalAlignment = Alignment.Top, // or CenterVertically, Bottom
        modifier = modifier.background(Color.Yellow).padding(8.dp).fillMaxWidth()
    ) {
        for (name in names) {
            Greeting(name, Modifier.fillMaxHeight())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RowGreetingsPreview() {
    MyLayoutAppTheme {
        RowGreetings()
    }
}

@Composable
fun BoxGreetings(
    names: List<String> = listOf("Ada", "Grace", "Niklaus"),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Color.Yellow).padding(8.dp).fillMaxSize()
    ) {
        var x = 0.dp
        var y = 0.dp
        for (name in names) {
            Greeting(name, Modifier.offset(x, y))
            x += 15.dp
            y += 15.dp
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BoxGreetingsPreview() {
    MyLayoutAppTheme {
        BoxGreetings()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val modifier = modifier.background(Color.Red)
    // TODO: try these padding modifiers, one at a time
    //    .padding(24.dp)
    //    .padding(24.dp).fillMaxWidth()
    //    .padding(24.dp).fillMaxHeight()
    //    .padding(vertical = 24.dp)
    //    .padding(horizontal = 24.dp)
    //    .padding(
    //        start = 24.dp, top = 8.dp,
    //        end = 8.dp, bottom = 24.dp)
    Surface(color = MaterialTheme.colorScheme.primary, modifier = modifier) {
        Text(
            text = "Hello $name!",
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyLayoutAppTheme {
        Greeting(name = "MSE")
    }
}