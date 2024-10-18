package com.example.foodcalorieapp

data class FoodDisplay(val name: String, val servingSize:Double, val calories: Double, val fat: Double,
                       val protein: Double, val carbs: Double)

data class MealImage(val imageUri: ByteArray)