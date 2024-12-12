# UniVerse
UniVerse is a disccusion app made simmilar to Brainly and Quora but it has more specific topic where more include in collage student problem, Unlike brainly, quora, and reddit where the app have a broad disccusion topic.

# Cloud Computing

### Architecture
This is the architecture we have for running the backend and machine learning model 

![image](https://github.com/user-attachments/assets/e8d95861-dc51-4f90-b744-67e5bdbc2dc9)

### Database
because we using firestore as the database, you need to make a service account for the acces to firestore database. Where you can do that by making a Firebase Account in the firebase console that can connect to your google cloud console
don't forget to put the key in the project folder and gitingnore the key because the key can't be push into github.

for the firestore Structure we made so it has 4 colletion topic, commnets, users, sessions
- topic for the topic information (topicId, title, description, etc)
- comments for the comment information (commentId, comment, etc) -> comment collection is located inside the topic document
- sessions for guest information
- users for the users information (username, password, etc)

### Model machine learning and Cloud storage
for the google storage we used bucket for the machine learning dataset. So the model can run with the dataset any where.
After that the model that has been done we deploy it on flask server using APP engine so it can connect to our backend code.

### API 
In the backend section we made so the stucture goes in like this 

Handlers -> middleware -> routes -> server

In the Handlers folder there is 3 main API that we make for the app

1. TopicHandler (for managing topic)
2. CommentHandler (for managing comment)
3. AunthHandler (for managing login)

##### The handler for Topic handler consist
- add Topic ( for adding a new topic )
  - method : 
    POST
  - endpoint : 
    http://< url >/api/topics
  - header :
    Authorization  Bearer < token-key >
  - body request :
    {"title": "What are the advantages of using Flutter for mobile app development?",
    "description": "I have heard a lot about Flutter. What makes it a good choice for mobile development?",
    "tags": ["mobile development", "Flutter", "cross-platform", "advantages"],"attachmentUrls":[]}
  - response :
    201 Created: Topic created successfully.
    400 Bad Request: Validation error or offensive content.

- edit Topic ( for editing a created topic )
  - method : 
    PUT
  - endpoint : 
    http://< url >/api/topics/:topicId
  - header :
    Authorization  Bearer < token-key >
  - body request :
  {"title": "What are the advantages of using Flutter for mobile app development?",
  "description": "I have heard a lot about Flutter. What makes it a good choice for mobile development?",
  "tags": ["mobile development", "Flutter", "cross-platform", "advantages"],"attachmentUrls":[]}
  - Response:
    200 OK: Topic updated successfully.
    403 Forbidden: Not the creator.
    404 Not Found: Topic not found.

- delete Topic ( for deleteing created topic )
  - method : 
    DELETE
  - endpoint : 
    http://< url >/api/topics/:topicId
  - header :
    Authorization  Bearer < token-key >
  - body request :
  {}
  - Response:
    200 OK: Topic deleted successfully.
    403 Forbidden: Not the creator.
    404 Not Found: Topic not found.

- get all Topic ( to show all topic thats has been created )
  - method : 
    GET
  - endpoint : 
    http://< url >/api/topics
  - header :
  - body request :
  {}
  - Response:
    200 OK: Topic deleted successfully.
    403 Forbidden: Not the creator.
    404 Not Found: Topic not found.
- get Topic by id ( to show a detailed topic )
  - method : 
    GET
  - endpoint : 
    http://< url >/api/topics
  - header :
  - body request :
  {}
  - Response:
    200 OK: Topic details.
    404 Not Found: Topic not found.
    
- recommend Topic ( to show recommended topic from the topic has been selected )
  - method : 
    GET
  - endpoint : 
    http://< url >/api/topics/:topicId/recommend
  - header :
    Authorization  Bearer < token-key >
  - body request :
  {}
  - Response:
    200 OK: Array of recommendations.
    404 Not Found: Topic not found.
    
- Toggle like ( to give like to a topic 
  - method : 
    POST
  - endpoint : 
    http://< url >/api/topics/:topicId/like
  - header :
    Authorization  Bearer < token-key >
  - body request :
  {}
  - Response:
    200 OK: Like/unlike toggled.
    404 Not Found: Topic not found.
##### The handler for comment handler consist

- Add a Comment
  - POST /topics/:topicId/comments
  - Headers : Authorization: Bearer <token> (Required)
  - Request Body : 
{
  "commentText": "Your comment here."
}
  - Responses :
201 Created
{
  "status": "success",
  "message": "Comment added successfully",
  "commentId": "<commentId>",
  "comment": {
    "username": "<username>",
    "commentText": "Your comment here.",
    "createdBy": "<username>",
    "upvotes": 0,
    "downvotes": 0,
    "userVotes": {},
    "createdAt": "<timestamp>",
    "updatedAt": "<timestamp>"
  }

- Get All Comments
  - GET /topics/:topicId/comments
  - Responses :
  200 OK
    [
      {
        "commentId": "<commentId>",
        "username": "<username>",
        "commentText": "Your comment here.",
        "createdBy": "<username>",
        "upvotes": 0,
        "downvotes": 0,
        "userVotes": {},
        "createdAt": "<timestamp>",
        "updatedAt": "<timestamp>"
      }
    ]


# Mobile Development
### Tools and Software
1. Integrated Development Environment (IDE):
  - Android Studio (latest stable version recommended).
  - Ensure Kotlin support is enabled during installation.
2. Version Control:
  - Git for managing the source code repository.
  - A GitHub account for repository hosting.
3. Build Tools:
  - Gradle for project builds.
4. Android SDK:
  - Install the necessary Android SDK versions.
  - Enable SDK tools like ADB (Android Debug Bridge).
5. Emulator or Physical Device:
  - An Android device with USB debugging enabled or an emulator configured in Android Studio.
### Dependencies and Libraries
1. Kotlin Standard Library
  - Kotlin Standard Library:
    implementation(libs.kotlin.stdlib)
2. Jetpack Components
  - These libraries provide essential components for Android development, like lifecycle management, navigation, and UI components:
    - AndroidX Core
      implementation(libs.androidx.core.ktx)
    - AndroidX AppCompat:
      implementation(libs.androidx.appcompat)
    - AndroidX Lifecycle:
        - LiveData:
          implementation(libs.androidx.lifecycle.livedata.ktx
        - ViewModel:
          implementation(libs.androidx.lifecycle.viewmodel.ktx)
    - AndroidX Navigation:
        - Fragment Navigation:
          implementation(libs.androidx.navigation.fragment.ktx)
        - UI Navigation:
          implementation(libs.androidx.navigation.ui.ktx)
    - AndroidX Preferences:
      implementation(libs.androidx.preference)
3. UI Libraries
  - Material Components for Android:
    implementation(libs.material)
  - ConstraintLayout:
    implementation(libs.androidx.constraintlayout)
  - CircleImageView (for circular images):
    implementation(libs.circleimageview)
  - Glide (image loading):
    implementation(libs.com.github.bumptech.glide.glide)
4. Testing Frameworks
  - JUnit (unit testing):
    testImplementation(libs.junit)
  - AndroidX JUnit (testing support for Android):
    androidTestImplementation(libs.androidx.junit)
  - Espresso (UI testing framework):
    androidTestImplementation(libs.androidx.espresso.core)
5. Third-party Libraries
  - Retrofit (networking):
    implementation(libs.retrofit)
  - Gson Converter for Retrofit:
    implementation(libs.converter.gson)
  - Logging Interceptor (for logging HTTP requests):
    implementation(libs.logging.interceptor)
  - Play Services Auth (for Google sign-in and authentication):
    implementation(libs.play.services.auth)
  - Firebase SDK (Firestore, Authentication, and Storage):
    - Firestore:
      implementation(libs.google.firebase.firestore)
    - Firebase Auth:
      implementation(libs.firebase.auth.ktx)
    - Firebase Storage:
      implementation(libs.firebase.storage.ktx)
    - Firebase BOM (Bill of Materials for Firebase libraries):
      implementation(platform(libs.firebase.bom))
  - Google Services (for integrating Firebase and other Google services):
    id("com.google.gms.google-services")
  - Cronet Embedded (networking library):
    implementation(libs.cronet.embedded)
  - Google ID (for Google APIs):
    implementation(libs.googleid)
6. Room (Database)
  - Room Core:
    implementation(libs.androidx.room.common)
  - Room KTX (extension functions):
    implementation(libs.androidx.room.ktx)
  - Room Compiler (for annotation processing in Room):
    kapt(libs.androidx.room.compiler)
7. Miscellaneous
  - AndroidX Activity (support for Android Activity classes):
    implementation(libs.androidx.activity)
  - AndroidX Credentials (for credential storage):
    implementation(libs.androidx.credentials)
  - AndroidX Ads Services (for ads services support):
    implementation(libs.androidx.ads.adservices)
### API Documentation
Key Components
- AddNewViewModel: Handles logic for creating new discussions.
- DashboardViewModel: Manages data displayed on the dashboard.
- EditTopicViewModel: Supports editing existing topics.
- HomeViewModel: Fetches and updates data for the home screen.
- ProfileViewModel: Manages user profile data and settings.
- RegisterViewModel: Handles user registration logic.
- TopicDetailViewModel: Provides details of a specific topic.
- SettingsViewModel: Manages application settings.
### User Guide
Key Features:
- Login and Registration
Access the app by signing in or registering.
- Profile Management
View and update profile settings.
- Discussion Topics
Create, edit, and view discussion topics.
- Settings
Customize app settings.
Navigation:
- Landing Screen: Guides users to login or register.
- Dashboard: Displays an overview of topics and activities.
- Profile: Access user information and saved posts.
- Settings: Configure application preferences.
### Development Guide
Project Structure
- Activities: Handles individual screens like LoginActivity, AddNewActivity.
- Fragments: Modular UI components like DashboardFragment and ProfileFragment.
- ViewModels: Encapsulate business logic for each screen.
- Adapters: Provide binding logic for lists and other UI components.
### How to Add a New Feature
1. Create a ViewModel:
  - Extend the ViewModel class.
  - Add necessary business logic and data fetching.
2. Design the UI:
  - Create an XML layout file.
  - Develop a corresponding Activity or Fragment.
3. Integrate ViewModel with UI:
  - Use ViewModelProvider to bind the ViewModel to your UI component.
4. Test Your Feature:
  - Use Android Studio’s built-in emulator or a physical device.
### Initial Release
- Features:
    - User login and registration.
    - Profile management.
    - Topic creation and editing.
    - Dashboard for topic overview.
    - Application settings.


# Machine Learning
