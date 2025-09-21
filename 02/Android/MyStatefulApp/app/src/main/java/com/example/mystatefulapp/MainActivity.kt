package com.example.mystatefulapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { innerPadding ->
                    MultiPage(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MultiPage(modifier: Modifier = Modifier) {
    TODO("Fix state and logic, add lambdas for next/back")
    var page = 0
    when (page) {
        0 -> Screen(
            label = "A",
            modifier = modifier
        )
        1 -> Screen (
            label = "B",
            modifier = modifier
        )
        else -> Screen(
            label = "C",
            modifier = modifier
        )
    }
}

@Composable
fun Screen(
    label: String,
    onBack: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier.size(0.dp))
        Text("Screen $label", style = MaterialTheme.typography.headlineLarge)
        Row(
            modifier = Modifier.padding(horizontal = 36.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onBack != null) {
                Button(onClick = onBack) {
                    Text(text = "Back")
                }
            }
            Spacer(modifier.size(0.dp))
            if (onNext != null) {
                Button(onClick = onNext) {
                    Text(text = "Next")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    MaterialTheme {
        Screen(
            label = "Preview",
            onBack = null,
            onNext = { TODO("Implement logic") }
        )
    }
}