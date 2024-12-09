import sys
import json
import pickle
import pandas as pd

def recommend(forum_title):
    # Load model
    with open('model.pkl', 'rb') as file:
        df, cv, similarity = pickle.load(file)

    # Find recommendations
    if forum_title not in df['title'].values:
        return {"error": f"Title '{forum_title}' not found in the dataset."}

    index = df[df['title'] == forum_title].index[0]
    distances = sorted(list(enumerate(similarity[index])), reverse=True, key=lambda x: x[1])
    recommendations = [{"title": df.iloc[i[0]].title, "similarity": i[1]} for i in distances[1:6]]

    return recommendations

if __name__ == '__main__':
    forum_title = sys.argv[1]  # Get input from Node.js
    try:
        result = recommend(forum_title)
        print(json.dumps(result))  # Output JSON to Node.js
    except Exception as e:
        print(json.dumps({"error": str(e)}), file=sys.stderr)
        sys.exit(1)
