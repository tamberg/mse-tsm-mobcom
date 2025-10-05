// (c) 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
// licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
// based on https://github.com/google-developer-training/basic-android-kotlin-compose-training-inventory-app

package com.example.mysqldataapp.data.source

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// https://stackoverflow.com/questions/338156/table-naming-dilemma-singular-vs-plural-names
@Entity(tableName = "person")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "surname") val surname: String?,
    @ColumnInfo(name = "language") val language: String?
)

@Dao
interface PersonDao { // see PersonDao_Impl.kt (generated)
    @Query("SELECT * from person WHERE id = :id") fun getPerson(id: Int): Flow<PersonEntity>
    @Query("SELECT * from person ORDER BY name ASC") fun getPersonList(): Flow<List<PersonEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(person: PersonEntity)
    @Update suspend fun update(person: PersonEntity)
    @Delete suspend fun delete(person: PersonEntity)
}

@Database(entities = [PersonEntity::class], version = 1, exportSchema = false)
abstract class PersonDatabase : RoomDatabase() { // see PersonDatabase_Impl.kt (generated)
    abstract fun personDao(): PersonDao
    companion object {
        @Volatile
        private var Instance: PersonDatabase? = null

        fun getInstance(context: Context): PersonDatabase { // TODO: rename to getInstance?
            return Instance ?: synchronized(this) { // singleton pattern
                Room.databaseBuilder(context, PersonDatabase::class.java, "person_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
