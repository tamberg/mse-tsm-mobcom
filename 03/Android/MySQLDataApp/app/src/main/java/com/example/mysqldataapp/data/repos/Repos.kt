// (c) 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
// licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
// based on https://github.com/google-developer-training/basic-android-kotlin-compose-training-inventory-app

package com.example.mysqldataapp.data.repos

import kotlinx.coroutines.flow.Flow

import com.example.mysqldataapp.data.source.PersonEntity
import com.example.mysqldataapp.data.source.PersonDao

// Repository

interface PersonRepo {
    fun getPersonListFlow(): Flow<List<PersonEntity>>
    fun getPersonFlow(id: Int): Flow<PersonEntity?>
    suspend fun insertPerson(person: PersonEntity)
    suspend fun updatePerson(person: PersonEntity)
    suspend fun deletePerson(person: PersonEntity)
}

// Repository implementation

class LocalPersonRepo(private val personDao: PersonDao) : PersonRepo {
    override fun getPersonListFlow(): Flow<List<PersonEntity>> = personDao.getPersonList()
    override fun getPersonFlow(id: Int): Flow<PersonEntity?> = personDao.getPerson(id)
    override suspend fun insertPerson(person: PersonEntity) = personDao.insert(person)
    override suspend fun updatePerson(person: PersonEntity) = personDao.update(person)
    override suspend fun deletePerson(person: PersonEntity) = personDao.delete(person)
}
