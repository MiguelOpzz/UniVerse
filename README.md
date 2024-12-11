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

The handler for Topic handler consist
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



# Mobile Development

# Machine Learning
