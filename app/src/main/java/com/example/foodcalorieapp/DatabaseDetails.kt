// Credits...
/* -------------------------------------------------------------------------------- */
// Below link helped with creating One-To-Many tables for Room databases.
// 1. https://www.youtube.com/watch?v=K8yKH5Ak84E&list=PLQkwcJG4YTCS3AD2C-yWtJUGTYMh5h3Zz&index=3

// Below link also helped with what 1. helped with.
// 2. https://developer.android.com/training/data-storage/room/relationships

// Below link helped with linking the two databases together.
// 3. https://www.youtube.com/watch?v=iTdzBM1zErA&list=PLQkwcJG4YTCS3AD2C-yWtJUGTYMh5h3Zz&index=5
/* -------------------------------------------------------------------------------- */

package com.example.foodcalorieapp

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update

@Entity(tableName = "Date")
data class Date(
    @PrimaryKey(autoGenerate = false)
    val dateString: String
)

@Entity(tableName = "Food")
data class Food(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Double,
    val fat: Double,
    val protein: Double,
    val carbs: Double,
    val dateString: String
)

data class DateWithFoods(
    @Embedded val date: Date,
    @Relation(
        parentColumn = "dateString",
        entityColumn = "dateString"
    )
    val foods: List<Food>
)

@Dao
interface DateWithFoodsDao {

    @Query("SELECT * FROM Food WHERE dateString = :dateString")
    suspend fun getFoodsWithDate(dateString: String): List<Food>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDate(date: Date)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food): Long

    @Query("SELECT * FROM Food WHERE id = :id AND dateString = :dateString")
    suspend fun getFoodByIdAndDate(id: Int, dateString: String): Food?

    @Update
    suspend fun updateFood(food: Food)

    @Delete
    suspend fun deleteFood(food: Food)

}

@Database(
    entities = [
        Date::class,
        Food::class
    ],
    version = 1
) abstract class AppDatabase : RoomDatabase() {
    abstract val dateWithFoodsDao: DateWithFoodsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this ) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "date-with-foods-database"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}


