from flask import Flask, request, jsonify
import pickle
import pandas as pd
import firebase_admin
from firebase_admin import credentials, firestore
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity

app = Flask(__name__)

# Initialize Firebase
cred = credentials.Certificate("key.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# Load the model
with open('model.pkl', 'rb') as file:
    df, cv, similarity = pickle.load(file)

# Load data from Firestore
def load_data_from_db():
    forums_ref = db.collection('forums')
    docs = forums_ref.stream()
    data = []

    for doc in docs:
        forum = doc.to_dict()
        data.append({
            "id": doc.id,
            "title": forum['title'],
            "description": forum['description'],
            "tags": ', '.join(forum['tags'])  # Convert list to string
        })

    return pd.DataFrame(data)

# Update recommendation model
def update_model():
    global df, cv, similarity  # Update global variables
    df = load_data_from_db()

    # Preprocess tags
    df['tags'] = df['tags'].apply(lambda x: ', '.join(x) if isinstance(x, list) else x)

    # Update vectorizer and similarity matrix
    vector = cv.fit_transform(df['tags']).toarray()
    similarity = cosine_similarity(vector)

    # Save updated model
    with open('model.pkl', 'wb') as file:
        pickle.dump((df, cv, similarity), file)
    
    print("Model updated successfully!")

# Endpoint: Add forum and update model
@app.route('/add_forum', methods=['POST'])
def add_forum():
    data = request.get_json()
    title = data.get('title')
    description = data.get('description')
    tags = data.get('tags')

    # Save to Firestore
    db.collection('forums').add({
        'title': title,
        'description': description,
        'tags': tags
    })

    # Update the model
    update_model()

    return jsonify({"message": "Forum added and model updated!"}), 201

# Endpoint: Recommend forums
@app.route('/api', methods=['POST'])
def recommend():
    forum_title = request.json.get('forum_title')

    if not forum_title:
        return jsonify({"error": "Forum title is required."}), 400

    # Normalize input and dataset titles
    forum_title = forum_title.strip().lower()
    df['title'] = df['title'].str.strip().str.lower()

    if forum_title not in df['title'].values:
        from difflib import get_close_matches
        close_matches = get_close_matches(forum_title, df['title'].values, n=5, cutoff=0.1)
        return jsonify({"error": f"Title '{forum_title}' not found.", "suggestions": close_matches}), 404

    index = df[df['title'] == forum_title].index[0]
    distances = sorted(list(enumerate(similarity[index])), reverse=True, key=lambda x: x[1])
    recommendations = [{"title": df.iloc[i[0]].title, "similarity": i[1]} for i in distances[1:6]]

    return jsonify(recommendations)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
