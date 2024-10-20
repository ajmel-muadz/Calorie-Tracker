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

// Table for storing dates.
@Entity(tableName = "Date")
data class Date(
    @PrimaryKey(autoGenerate = false)
    val dateString: String
)

// Table for storing food items.
@Entity(tableName = "Food")
data class Food(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val calories: Double,
    val fat: Double,
    val protein: Double,
    val carbs: Double,
    val mealType: String,
    val dateString: String
)


// Table for storing user's goals.
@Entity(tableName = "user_goals")
data class UserGoals(
    @PrimaryKey val id: Int = 1,
    val caloriesGoal: Double,
    val fatGoal: Double,
    val proteinGoal: Double,
    val carbGoal: Double
)

// Data class to represent a date with its associated foods.
@Dao
interface DateWithFoodsDao {

    // Query to get all foods from a specific date
    @Query("SELECT * FROM Food WHERE dateString = :dateString")
    suspend fun getFoodsWithDate(dateString: String): List<Food>

    // Insert Query to insert a date into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDate(date: Date)

    // Insert Query to insert a food item into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food): Long

    // Query to get a specific food item by its ID and date
    @Query("SELECT * FROM Food WHERE id = :id AND dateString = :dateString")
    suspend fun getFoodByIdAndDate(id: Int, dateString: String): Food?

    // Update Query to update a specific food item
    @Update
    suspend fun updateFood(food: Food)


    // Query to reset the Ids of the Food table. after deletion of any food.
    @Query("DELETE FROM sqlite_sequence WHERE name='Food'")
    suspend fun resetFoodIdCounter()


    // Query to get all foods from the database
    @Query("SELECT * FROM Food")
    suspend fun getAllFoods(): List<Food>

    // Delete Query to delete a specific food item
    @Delete
    suspend fun deleteFood(food: Food)

    // Delete Query to delete a specfic date from the database, used after deletion of the last food item on a specific date
    @Delete
    suspend fun deleteDate(date: Date)

    // Insert Query to insert user's goals into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserGoals(userGoals: UserGoals)

    // Update Query to update user's goals in the database
    @Update
    suspend fun updateUserGoals(userGoals: UserGoals)

    // Query to retrieve user's goals from the database
    @Query("SELECT * FROM user_goals WHERE id = 1")
    suspend fun getUserGoals(): UserGoals?


}

// Database class for the application with its entities (tables)
@Database(
    entities = [
        Date::class,
        Food::class,
        UserGoals::class
    ],
    version = 2
) abstract class AppDatabase : RoomDatabase() { // Abstract class for the Room database
    abstract val dateWithFoodsDao: DateWithFoodsDao

    companion object {
        // Singleton instance of the database to ensure only one instance is created
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Function to get the database instance
        fun getInstance(context: Context): AppDatabase {
            synchronized(this ) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "date-with-foods-database"
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }
    }
}


