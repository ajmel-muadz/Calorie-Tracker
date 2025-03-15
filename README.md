## About The Project 

Food Calorie Management App

Objective

The Food Calorie Management App is an Android application designed to help users log their daily meals, track their calorie intake, search for food nutritional information using external APIs, and upload meal photos. The app ensures data persistence, allowing users to access their logs and images even after restarting the app. It also supports multiple screen sizes, handles screen rotations, and prevents crashes.

Features

1. Daily Food Log and Calorie Tracking

Users can add meals by entering details such as food name, portion size, and meal type (breakfast, lunch, dinner, snack).

The app calculates the total calories for each meal using food nutritional information from an external API or manually entered data.

If an API does not return data for a particular food item, users can manually enter nutritional information.

Displays a summary of the user’s total daily calorie intake compared to a predefined daily goal (e.g., 2000 kcal).

Meals are displayed in a scrollable list.

The food log persists, ensuring that users can view logged meals even after closing and reopening the app.

2. Search for Food Information using APIs

Integrates external APIs (e.g., CalorieNinjas API, Nutritionix API, Open Food Facts API) to retrieve food nutritional details.

Displays calorie content and key nutritional information (fats, proteins, carbohydrates) for each searched food item.

Allows users to add searched food items to their daily log with persistent storage.

3. Photo Upload for Meals

Users can upload meal photos from their camera or gallery.

Each photo is associated with a meal entry in the log to help users visually track their meals.

Meal photos persist in the app for future access.

4. Cloud Storage for Meal Photos

Uses a cloud storage solution (e.g., Firebase) for storing meal photos.

Provides a progress indicator while uploading photos.

Notifies users when uploads are complete and links cloud-stored photos to meal entries.

Technical Implementation

Developed in Java or Kotlin.

Utilizes third-party libraries and frameworks to enhance functionality.

Implements asynchronous operations for API calls and data uploads, ensuring smooth user experience.

Ensures responsive design across various screen sizes and orientations.

Conclusion

The Food Calorie Management App is a robust solution for tracking calorie intake, integrating external nutritional databases, and managing meal photos. Its user-friendly interface, data persistence, and cloud storage capabilities make it a valuable tool for health-conscious users.

## Build With
- Jetpack Compose
- Kotlin
- RESTful API
- Android Room
- CalorieNinjas
- FireBase

## Download 
Link to downloading the app: https://github.com/ajmel-muadz/Calorie-Tracker/releases/tag/Calorie-Tracker%2Fv1.0

## Authors
- [Ajmel Muadz](https://github.com/ajmel-muadz)
- [Mouktada Salman](https://github.com/MouktadaSalman)
- [Ahmed Youseif](https://github.com/Ahmedo-o)
- [M.Jauhar](https://github.com/MasterBam)

## License
This project is for educational purposes only.
