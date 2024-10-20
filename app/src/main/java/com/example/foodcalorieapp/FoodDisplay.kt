package com.example.foodcalorieapp

import android.graphics.Bitmap

data class FoodDisplay(val name: String, val servingSize:Double, val calories: Double, val fat: Double,
                       val protein: Double, val carbs: Double, val mealType: String, val id: Long)

data class MealImage(val image: String? = null, val id: Long? = null)