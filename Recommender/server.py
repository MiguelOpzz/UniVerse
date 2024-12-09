from flask import Flask, request, jsonify
import pickle
import pandas as pd

app = Flask(__name__)

# Load the model
with open('model.pkl', 'rb') as file:
    df, cv, similarity = pickle.load(file)

@app.route('/api', methods=['POST'])
def recommend():
    forum_title = request.json.get('forum_title')
    
    # Validate input
    if not forum_title:
        return jsonify({"error": "Forum title is required."}), 400

    # Find recommendations
    if forum_title not in df['title'].values:
        return jsonify({"error": f"Title '{forum_title}' not found in the dataset."}), 404

    index = df[df['title'] == forum_title].index[0]
    distances = sorted(list(enumerate(similarity[index])), reverse=True, key=lambda x: x[1])
    recommendations = [{"title": df.iloc[i[0]].title, "similarity": i[1]} for i in distances[1:6]]

    return jsonify(recommendations)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
