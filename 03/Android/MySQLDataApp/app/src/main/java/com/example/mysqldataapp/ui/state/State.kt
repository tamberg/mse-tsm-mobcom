// (c) 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
// licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
// based on https://github.com/google-developer-training/basic-android-kotlin-compose-training-inventory-app

package com.example.mysqldataapp.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.mysqldataapp.data.source.PersonEntity // TODO: refactor?
import com.example.mysqldataapp.data.repos.PersonRepo

// State

data class Person(
    val id: Int = 0,
    val name: String = "",
    val surname: String = "",
    val language: String = "",
)

fun Person.toEntity() = PersonEntity(
    id = id,
    name = name,
    surname = surname)

fun PersonEntity.toPerson() = Person(
    id = id,
    name = name!!, // TODO null safety
    surname = surname!!)

// List ViewModel

class PersonListViewModel(personRepository: PersonRepo) : ViewModel() {
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

data class PersonListState(val personList: List<PersonEntity> = listOf()) // TODO: List<Person>?

// Entry ViewModel

class PersonEntryViewModel(private val personRepository: PersonRepo) : ViewModel() {
    var state by mutableStateOf(PersonEntryState())
        private set

    fun updateState(person: Person) {
        state = PersonEntryState(person = person, isValid = validateInput(person))
    }

    suspend fun createPerson() {
        if (validateInput(state.person)) {
            personRepository.insertPerson(state.person.toEntity())
            updateState(Person()) // TODO: move to UI?
        }
    }

    private fun validateInput(person: Person): Boolean {
        return with(person) {
            name.isNotBlank() && surname.isNotBlank() && language.isNotBlank()
        }
    }
}

data class PersonEntryState(val person: Person = Person(), val isValid: Boolean = false)

// Detail ViewModel

class PersonDetailViewModel(
    personId: Int,
    //savedStateHandle: SavedStateHandle,
    private val personRepository: PersonRepo,
) : ViewModel() {
    //private val personId: Int = checkNotNull(savedStateHandle["personId"])
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

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class PersonDetailState(val person: Person = Person())

fun PersonEntity.toDetailState(): PersonDetailState = PersonDetailState(person = this.toPerson())

// Edit ViewModel

class PersonEditViewModel(
    personId: Int,
    //savedStateHandle: SavedStateHandle,
    private val personRepository: PersonRepo
) : ViewModel() {
    var state by mutableStateOf(PersonEditState())
        private set

    //private val personId: Int = checkNotNull(savedStateHandle["personId"])

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
