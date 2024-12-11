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
- edit Topic ( for editing a created topic )
- delete Topic ( for deleteing created topic )
- get all Topic ( to show all topic thats has been created )
- get Topic by id ( to show a detailed topic )
- recommend Topic ( to show recommended topic from the topic has been selected )
- Toggle like ( to give like to a topic )



# Mobile Development

# Machine Learning
