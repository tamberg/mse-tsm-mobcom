package com.example.myresourcefulapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.myresourcefulapp.ui.theme.MyResourcefulAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyResourcefulAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Screen(
                        label = "Step by step", // or another saying
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Screen(
    label: String,
    modifier: Modifier = Modifier
) {
    Box() {
        Image(
            alpha = 0.8f,
            painter = painterResource(id = R.drawable.path),
            contentScale = ContentScale.Crop,
            contentDescription = stringResource(R.string.path),
            modifier = modifier.zIndex(-1f).fillMaxSize())
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.size(0.dp))
            Text("$label.", style = MaterialTheme.typography.headlineLarge,
                color = Color.White.copy(alpha = 0.9f))
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {}) {
                    Text("Back")
                }
                Spacer(Modifier.size(0.dp))
                Button(onClick = {}) {
                    Text("Next")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    MyResourcefulAppTheme {
        Screen(label = "Preview")
    }
}