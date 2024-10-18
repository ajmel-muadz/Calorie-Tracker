# PLEASE READ THIS

***
## This README is used to relay any updates in the project. So far:

- When opening the app, you are shown the current date at the top. Users can add food with the button at the bottom.
- Foods are added via an API call to calorieninjas.com and displayed in a list at the front screen corresponding to the date.
- If a food does not exist, users can manually enter the food.
- All foods entered are stored in a Room database. Food lists are mapped to each date shown on the screen.
  The date to food mapping is a One-to-Many relationship.
***

***
## Stuff not done yet

### Major

- We need to make the photos stuff work. So users can take a photo of their food and attach it to a food in the list.
  - Mouktada: I made a icon button in the main page to allow the user to take a photo. It returns a specific Uri for the photo.
- Need to make this app work with Firebase.

## Minor
- Need to allow users to enter meal type (such as breakfast, lunch, dinner).
- Need to allow users to enter the amount of grams for their food (portion size). This is fairly trivial.
- Need to show for each food list a summary of the total calories, fat, protein and carbs for the day.
- Currently, users can only see the kcals (calories) for each food in the list. Ideally, if the user clicks on any food
  card it should show all macros, not just the calories.
***
 