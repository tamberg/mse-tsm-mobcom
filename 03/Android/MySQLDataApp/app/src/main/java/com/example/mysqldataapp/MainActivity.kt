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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

import com.example.mysqldataapp.data.source.PersonDatabase
import com.example.mysqldataapp.data.repos.PersonRepo
import com.example.mysqldataapp.data.repos.LocalPersonRepo
import com.example.mysqldataapp.ui.parts.MyNavigation
import com.example.mysqldataapp.ui.state.PersonDetailViewModel
import com.example.mysqldataapp.ui.state.PersonEditViewModel
import com.example.mysqldataapp.ui.state.PersonEntryViewModel
import com.example.mysqldataapp.ui.state.PersonListViewModel
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

// App ViewModel provider

object AppViewModelProvider {
    var personId: Int = 0 // TODO
    val Factory = viewModelFactory {
        //initializer { NavigationViewModel(this.createSavedStateHandle()) }
        initializer { PersonListViewModel(mySqlDataApp().container.personRepository) }
        initializer { PersonEntryViewModel(mySqlDataApp().container.personRepository) }
        initializer { PersonDetailViewModel(//this.createSavedStateHandle(),
            personId, mySqlDataApp().container.personRepository) }
        initializer { PersonEditViewModel(//this.createSavedStateHandle(),
            personId, mySqlDataApp().container.personRepository) }
    }
}

fun CreationExtras.mySqlDataApp(): MySQLDataApp =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MySQLDataApp)

// App container

interface AppContainer {
    val personRepository: PersonRepo
}

class DbAppContainer(private val context: Context) : AppContainer {
    override val personRepository: PersonRepo by lazy {
        LocalPersonRepo(PersonDatabase.getInstance(context).personDao())
    }
}

// App

class MySQLDataApp : Application() { // see manifest.xml
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DbAppContainer(this)
    }
}
